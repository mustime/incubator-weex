/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package cn.xxzhushou.xmod.wxui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.WorkerThread;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.alibaba.fastjson.JSONObject;
import cn.xxzhushou.xmod.wxui.adapter.IDrawableLoader;
import cn.xxzhushou.xmod.wxui.adapter.IWXHttpAdapter;
import cn.xxzhushou.xmod.wxui.adapter.IWXImgLoaderAdapter;
import cn.xxzhushou.xmod.wxui.adapter.IWXUserTrackAdapter;
import cn.xxzhushou.xmod.wxui.adapter.URIAdapter;
import cn.xxzhushou.xmod.wxui.bridge.EventResult;
import cn.xxzhushou.xmod.wxui.bridge.WXBridgeManager;
import cn.xxzhushou.xmod.wxui.common.Constants;
import cn.xxzhushou.xmod.wxui.common.Destroyable;
import cn.xxzhushou.xmod.wxui.common.OnWXScrollListener;
import cn.xxzhushou.xmod.wxui.common.WXErrorCode;
import cn.xxzhushou.xmod.wxui.common.WXPerformance;
import cn.xxzhushou.xmod.wxui.common.WXRefreshData;
import cn.xxzhushou.xmod.wxui.common.WXRenderStrategy;
import cn.xxzhushou.xmod.wxui.dom.WXEvent;
import cn.xxzhushou.xmod.wxui.layout.ContentBoxMeasurement;
import cn.xxzhushou.xmod.wxui.performance.WXAnalyzerDataTransfer;
import cn.xxzhushou.xmod.wxui.ui.action.GraphicActionAddElement;
import cn.xxzhushou.xmod.wxui.ui.component.NestedContainer;
import cn.xxzhushou.xmod.wxui.ui.component.WXBasicComponentType;
import cn.xxzhushou.xmod.wxui.ui.component.WXComponent;
import cn.xxzhushou.xmod.wxui.ui.component.WXComponentFactory;
import cn.xxzhushou.xmod.wxui.ui.flat.FlatGUIContext;
import cn.xxzhushou.xmod.wxui.ui.view.WXScrollView;
import cn.xxzhushou.xmod.wxui.utils.Trace;
import cn.xxzhushou.xmod.wxui.utils.WXFileUtils;
import cn.xxzhushou.xmod.wxui.utils.WXJsonUtils;
import cn.xxzhushou.xmod.wxui.utils.WXLogUtils;
import cn.xxzhushou.xmod.wxui.utils.WXReflectionUtils;
import cn.xxzhushou.xmod.wxui.utils.WXUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Each instance of WXSDKInstance represents an running weex instance.
 * It can be a pure weex view, or mixed with native view
 */
public class WXSDKInstance implements IWXActivityStateListener,View.OnLayoutChangeListener {

  private static  final  String SOURCE_TEMPLATE_BASE64_MD5 = "templateSourceBase64MD5";

  //Performance
  public boolean mEnd = false;
  public boolean isJSCreateFinish =false;
  public static final String BUNDLE_URL = "bundleUrl";
  private IWXUserTrackAdapter mUserTrackAdapter;
  private IWXRenderListener mRenderListener;
  private IWXStatisticsListener mStatisticsListener;
  /** package **/ Context mContext;
  private final String mInstanceId;
  private RenderContainer mRenderContainer;
  private WXComponent mRootComp;
  public boolean mRendered;
  private WXRefreshData mLastRefreshData;
  private NestedInstanceInterceptor mNestedInstanceInterceptor;
  private String mBundleUrl = "";
  private boolean isDestroy=false;
  private Map<String,Serializable> mUserTrackParams;
  private boolean isCommit=false;
  private WXGlobalEventReceiver mGlobalEventReceiver=null;
  private boolean trackComponent;
  private boolean enableLayerType = true;
  private boolean mNeedValidate = false;
  private boolean mNeedReLoad = false;
  private int mInstanceViewPortWidth = 750;
  private @NonNull
  FlatGUIContext mFlatGUIContext =new FlatGUIContext();

  /**
   *for network tracker
   */
  public String mwxDims[] = new String [5];
  public long measureTimes[] = new long [5];

  public WeakReference<String> templateRef;
  public Map<String,List<String>> responseHeaders = new HashMap<>();

  /**
   * Render strategy.
   */
  private WXRenderStrategy mRenderStrategy = WXRenderStrategy.APPEND_ONCE;

  /**
   * Render start time
   */
  public long mRenderStartTime;
  /**
   * Refresh start time
   */
  private long mRefreshStartTime;
  private WXPerformance mWXPerformance;
  private ScrollView mScrollView;
  private WXScrollView.WXScrollViewListener mWXScrollViewListener;

  private List<OnWXScrollListener> mWXScrollListeners;

  private List<String> mLayerOverFlowListeners;

  public List<String> getLayerOverFlowListeners() {
    return mLayerOverFlowListeners;
  }

  public void addLayerOverFlowListener(String ref) {
    if (mLayerOverFlowListeners == null)
      mLayerOverFlowListeners = new ArrayList<>();
    mLayerOverFlowListeners.add(ref);
  }

  public void removeLayerOverFlowListener(String ref) {
    if (mLayerOverFlowListeners != null)
      mLayerOverFlowListeners.remove(ref);
  }

  /**
   * whether we are in preRender mode
   * */
  private volatile boolean isPreRenderMode;

  private boolean mCurrentGround = false;
  private ComponentObserver mComponentObserver;
  private Map<String, GraphicActionAddElement> inactiveAddElementAction = new ArrayMap<>();

  private Map<Long, ContentBoxMeasurement> mContentBoxMeasurements = new ArrayMap<>();

  private int maxHiddenEmbedsNum = -1; //max hidden embed num, -1 standard for ulimit

  public int getMaxHiddenEmbedsNum() {
    return maxHiddenEmbedsNum;
  }

  public void setMaxHiddenEmbedsNum(int maxHiddenEmbedsNum) {
    this.maxHiddenEmbedsNum = maxHiddenEmbedsNum;
  }

  @WorkerThread
  @RestrictTo(Scope.LIBRARY)
  public void addInActiveAddElementAction(String ref, GraphicActionAddElement action){
    inactiveAddElementAction.put(ref, action);
  }

  @WorkerThread
  @RestrictTo(Scope.LIBRARY)
  public void removeInActiveAddElmentAction(String ref){
    inactiveAddElementAction.remove(ref);
  }

  @WorkerThread
  @RestrictTo(Scope.LIBRARY)
  public GraphicActionAddElement getInActiveAddElementAction(String ref){
    return inactiveAddElementAction.get(ref);
  }

  /**
   * If anchor is created manually(etc. define a layout xml resource ),
   * be aware do not add it to twice when {@link IWXRenderListener#onViewCreated(WXSDKInstance, View)}.
   * @param a
   */
  public void setRenderContainer(RenderContainer a){
    if(a != null) {
      a.setSDKInstance(this);
      a.addOnLayoutChangeListener(this);
    }

    mRenderContainer = a;
    if (mRenderContainer != null && mRenderContainer.getLayoutParams() != null
            && mRenderContainer.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
      WXBridgeManager.getInstance().setRenderContentWrapContentToCore(true, getInstanceId());
    } else {
      WXBridgeManager.getInstance().setRenderContentWrapContentToCore(false, getInstanceId());
    }
  }

  public boolean isTrackComponent() {
    return trackComponent;
  }

  public void setTrackComponent(boolean trackComponent) {
    this.trackComponent = trackComponent;
  }

  /**
   * Tell whether it is enabled to change the layerType
   * {@link android.view.View#setLayerType(int, Paint)}
   * @return True for enable to change the layerType of component, false otherwise. The default
   * is True
   */
  public boolean isLayerTypeEnabled() {
    return enableLayerType;
  }

  /**
   * Enable the ability of changing layerType. e.g. {@link android.view.View#setLayerType(int, Paint)}
   * Disable the ability of changing layerType will have tremendous <strong>performance
   * punishment</strong>.
   *
   * <strong>Do not</strong> set this to false unless you know exactly what you are doing.
   * @param enable True for enable to change the layerType of component, false otherwise. The default
   * is True
   */
  public void enableLayerType(boolean enable) {
    enableLayerType = enable;
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public @NonNull
  FlatGUIContext getFlatUIContext(){
    return mFlatGUIContext;
  }

  public boolean isNeedValidate() {
    return mNeedValidate;
  }

  public boolean isNeedReLoad() {
    return mNeedReLoad;
  }

  public void setNeedLoad(boolean load) {
    mNeedReLoad = load;
  }

  public void setInstanceViewPortWidth(int instanceViewPortWidth) {
    this.mInstanceViewPortWidth = instanceViewPortWidth;
  }

  public int getInstanceViewPortWidth(){
    return mInstanceViewPortWidth;
  }

  public interface OnInstanceVisibleListener{
    void onAppear();
    void onDisappear();
  }
  private List<OnInstanceVisibleListener> mVisibleListeners = new ArrayList<>();

  public WXSDKInstance(Context context) {
    mInstanceId = WXSDKManager.getInstance().generateInstanceId();
    init(context);
  }

  /**
   * For unittest only.
   */
  @RestrictTo(Scope.TESTS)
  WXSDKInstance(Context context,String id) {
    mInstanceId = id;
    init(context);
  }

  public WXComponent getRootComponent() {
    return mRootComp;
  }

  public void setNestedInstanceInterceptor(NestedInstanceInterceptor interceptor){
    mNestedInstanceInterceptor = interceptor;
  }

  public final WXSDKInstance createNestedInstance(NestedContainer container){
    WXSDKInstance sdkInstance = newNestedInstance();
    if(mNestedInstanceInterceptor != null){
      mNestedInstanceInterceptor.onCreateNestInstance(sdkInstance,container);
    }
    if(sdkInstance != null){
        sdkInstance.setComponentObserver(this.getComponentObserver());
    }
    return sdkInstance;
  }

  protected WXSDKInstance newNestedInstance() {
    return new WXSDKInstance(mContext);
  }

  public void addOnInstanceVisibleListener(OnInstanceVisibleListener l){
    mVisibleListeners.add(l);
  }

  public void removeOnInstanceVisibleListener(OnInstanceVisibleListener l){
    mVisibleListeners.remove(l);
  }

  public void init(Context context) {
    mContext = context;


    mWXPerformance = new WXPerformance();
    mWXPerformance.WXSDKVersion = WXEnvironment.WXSDK_VERSION;
    mWXPerformance.JSLibInitTime = WXEnvironment.sJSLibInitTime;

    mUserTrackAdapter=WXSDKManager.getInstance().getIWXUserTrackAdapter();
  }

  /**
   * Set a Observer for component.
   * This observer will be called in each component, should not doing
   * anything will impact render performance.
   *
   * @param observer
   */
  public void setComponentObserver(ComponentObserver observer){
    mComponentObserver = observer;
  }

  public ComponentObserver getComponentObserver(){
    return mComponentObserver;
  }

  @Deprecated
  public void setBizType(String bizType) {
    if (!TextUtils.isEmpty(bizType)) {
      mWXPerformance.bizType = bizType;
    }
  }

  public ScrollView getScrollView() {
    return mScrollView;
  }

  public void setRootScrollView(ScrollView scrollView) {
    mScrollView = scrollView;
    if (mWXScrollViewListener != null) {
      ((WXScrollView) mScrollView).addScrollViewListener(mWXScrollViewListener);
    }
  }

  @Deprecated
  public void registerScrollViewListener(WXScrollView.WXScrollViewListener scrollViewListener) {
    mWXScrollViewListener = scrollViewListener;
  }

  @Deprecated
  public WXScrollView.WXScrollViewListener getScrollViewListener() {
    return mWXScrollViewListener;
  }

  @Deprecated
  public void setIWXUserTrackAdapter(IWXUserTrackAdapter adapter) {
  }

  public void ensureRenderArchor(){
    if(mRenderContainer == null){
      if (getContext() != null) {
        setRenderContainer(new RenderContainer(getContext()));
        mRenderContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRenderContainer.setBackgroundColor(Color.TRANSPARENT);
        mRenderContainer.setSDKInstance(this);
        mRenderContainer.addOnLayoutChangeListener(this);
      }
    }
  }

  private String wrapPageName(String pageName, String url) {
    if(TextUtils.equals(pageName, WXPerformance.DEFAULT)){
      pageName = url;
      try {
        Uri uri=Uri.parse(url);
        if(uri!=null){
          Uri.Builder builder=new Uri.Builder();
          builder.scheme(uri.getScheme());
          builder.authority(uri.getAuthority());
          builder.path(uri.getPath());
          pageName=builder.toString();
        }
      } catch (Exception e) {
      }
    }
    return pageName;
  }

  private String assembleFilePath(Uri uri) {
    if(uri!=null && uri.getPath()!=null){
      return uri.getPath().replaceFirst("/","");
    }
    return "";
  }

  /**
   * Refresh instance asynchronously.
   * @param data the new data
   */
  public void refreshInstance(Map<String, Object> data) {
    if (data == null) {
      return;
    }
    refreshInstance(WXJsonUtils.fromObjectToJSONString(data));
  }

  /**
   * Refresh instance asynchronously.
   * @param jsonData the new data
   */
  public void refreshInstance(String jsonData) {
    if (jsonData == null) {
      return;
    }
    mRefreshStartTime = System.currentTimeMillis();
    //cancel last refresh message
    if (mLastRefreshData != null) {
      mLastRefreshData.isDirty = true;
    }

    mLastRefreshData = new WXRefreshData(jsonData, false);

    WXSDKManager.getInstance().refreshInstance(mInstanceId, mLastRefreshData);
  }

  public WXRenderStrategy getRenderStrategy() {
    return mRenderStrategy;
  }

  public Context getUIContext() {
    return mContext;
  }

  public String getInstanceId() {
    return mInstanceId;
  }

  public Context getContext() {
    if(mContext == null){
      WXLogUtils.e("WXSdkInstance mContext == null");
    }
    return mContext;
  }

  public int getWeexHeight() {
    return mRenderContainer == null ? 0: mRenderContainer.getHeight();
  }

  public int getWeexWidth() {
    return mRenderContainer == null ? 0: mRenderContainer.getWidth();
  }


  public IWXImgLoaderAdapter getImgLoaderAdapter() {
    return WXSDKManager.getInstance().getIWXImgLoaderAdapter();
  }

  public IDrawableLoader getDrawableLoader() {
    return WXSDKManager.getInstance().getDrawableLoader();
  }

  public URIAdapter getURIAdapter(){
    return WXSDKManager.getInstance().getURIAdapter();
  }

  public Uri rewriteUri(Uri uri,String type){
    return getURIAdapter().rewrite(this,type,uri);
  }

  public IWXHttpAdapter getWXHttpAdapter() {
    return WXSDKManager.getInstance().getIWXHttpAdapter();
  }

  public IWXStatisticsListener getWXStatisticsListener() {
    return mStatisticsListener;
  }

  @Deprecated
  public void reloadImages() {
    if (mScrollView == null) {
      return;
    }
  }


  public boolean isPreRenderMode() {
    return this.isPreRenderMode;
  }

  public void setPreRenderMode(final boolean isPreRenderMode) {
    WXSDKManager.getInstance().getWXRenderManager().postOnUiThread(new Runnable() {
      @Override
      public void run() {
        WXSDKInstance.this.isPreRenderMode = isPreRenderMode;
      }
    },0);
  }

  public void setContext(@NonNull Context context) {
    this.mContext = context;
  }

  /********************************
   * begin register listener
   ********************************************************/
  public void registerRenderListener(IWXRenderListener listener) {
    mRenderListener = listener;
  }

  @Deprecated
  public void registerActivityStateListener(IWXActivityStateListener listener) {

  }

  public void registerStatisticsListener(IWXStatisticsListener listener) {
    mStatisticsListener = listener;
  }

  /**set render start time*/
  public void setRenderStartTime(long renderStartTime) {
    this.mRenderStartTime = renderStartTime;
  }

  /********************************
   * end register listener
   ********************************************************/


  /********************************
   *  begin hook Activity life cycle callback
   ********************************************************/

  @Override
  public void onActivityCreate() {
    if(mRootComp != null) {
      mRootComp.onActivityCreate();
    }else{
      WXLogUtils.w("Warning :Component tree has not build completely,onActivityCreate can not be call!");
    }

    mGlobalEventReceiver=new WXGlobalEventReceiver(this);
    getContext().registerReceiver(mGlobalEventReceiver,new IntentFilter(WXGlobalEventReceiver.EVENT_ACTION));
  }

  @Override
  public void onActivityStart() {
    if(mRootComp != null) {
      mRootComp.onActivityStart();
    }else{
      WXLogUtils.w("Warning :Component tree has not build completely,onActivityStart can not be call!");
    }

  }

  public boolean onCreateOptionsMenu(Menu menu) {
    if(mRootComp != null) {
      mRootComp.onCreateOptionsMenu(menu);
    }else{
      WXLogUtils.w("Warning :Component tree has not build completely,onActivityStart can not be call!");
    }
    return true;
  }

  @Override
  public void onActivityPause() {
    onViewDisappear();
    if(!isCommit){
      Set<String> componentTypes= WXComponentFactory.getComponentTypesByInstanceId(getInstanceId());
      if(componentTypes!=null && componentTypes.contains(WXBasicComponentType.SCROLLER)){
        mWXPerformance.useScroller=1;
      }
      mWXPerformance.wxDims = mwxDims;
      mWXPerformance.measureTimes = measureTimes;
      if (mUserTrackAdapter != null) {
        mUserTrackAdapter.commit(mContext, null, IWXUserTrackAdapter.LOAD, mWXPerformance, getUserTrackParams());
      }
      WXAnalyzerDataTransfer.transferPerformance(mWXPerformance, getInstanceId());
      isCommit=true;
    }
    if(mRootComp != null) {
      mRootComp.onActivityPause();
    }else{
      WXLogUtils.w("Warning :Component tree has not build completely,onActivityPause can not be call!");
    }

    WXLogUtils.i("Application onActivityPause()");
    if (!mCurrentGround) {
      WXLogUtils.i("Application to be in the backround");
      Intent intent = new Intent(WXGlobalEventReceiver.EVENT_ACTION);
      intent.putExtra(WXGlobalEventReceiver.EVENT_NAME, Constants.Event.PAUSE_EVENT);
      intent.putExtra(WXGlobalEventReceiver.EVENT_WX_INSTANCEID, getInstanceId());
      mContext.sendBroadcast(intent);
      this.mCurrentGround = true;
    }
  }


  @Override
  public void onActivityResume() {
    if(mRootComp != null) {
      mRootComp.onActivityResume();
    }else{
      WXLogUtils.w("Warning :Component tree has not build completely, onActivityResume can not be call!");
    }

    if (mCurrentGround) {
      WXLogUtils.i("Application  to be in the foreground");
      Intent intent = new Intent(WXGlobalEventReceiver.EVENT_ACTION);
      intent.putExtra(WXGlobalEventReceiver.EVENT_NAME, Constants.Event.RESUME_EVENT);
      intent.putExtra(WXGlobalEventReceiver.EVENT_WX_INSTANCEID, getInstanceId());
      //todo tmp solution for gray version
      if (null != mContext){
        mContext.sendBroadcast(intent);
      }else {
        WXEnvironment.getApplication().sendBroadcast(intent);
      }
      this.mCurrentGround = false;
    }

    onViewAppear();
  }

  @Override
  public void onActivityStop() {
    if(mRootComp != null) {
      mRootComp.onActivityStop();
    }else{
      WXLogUtils.w("Warning :Component tree has not build completely, onActivityStop can not be call!");
    }


  }

  @Override
  public void onActivityDestroy() {
    if(mRootComp != null) {
      mRootComp.onActivityDestroy();
    }else{
      WXLogUtils.w("Warning :Component tree has not build completely, onActivityDestroy can not be call!");
    }

    destroy();
  }

  @Override
  public boolean onActivityBack() {
    if(mRootComp != null) {
      return mRootComp.onActivityBack();
    }else{
      WXLogUtils.w("Warning :Component tree has not build completely, onActivityBack can not be call!");
    }

    return false;
  }

  public boolean onBackPressed() {
    WXComponent comp = getRootComponent();
    if(comp != null) {
      WXEvent events= comp.getEvents();
      boolean hasNativeBackHook = events.contains(Constants.Event.NATIVE_BACK);
      if (hasNativeBackHook) {
        EventResult result = comp.fireEventWait(Constants.Event.NATIVE_BACK, null);
        if (WXUtils.getBoolean(result.getResult(), false)) {
          return true;
        }
      }

      boolean hasBackPressed = events.contains(Constants.Event.CLICKBACKITEM);
      if (hasBackPressed) {
        fireEvent(comp.getRef(), Constants.Event.CLICKBACKITEM,null, null);
      }
      return hasBackPressed;
    }
    return false;
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data){
    if(mRootComp != null) {
      mRootComp.onActivityResult(requestCode,resultCode,data);
    }else{
      WXLogUtils.w("Warning :Component tree has not build completely, onActivityResult can not be call!");
    }
  }


  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if(mRootComp != null) {
       mRootComp.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }else{
      WXLogUtils.w("Warning :Component tree has not build completely, onRequestPermissionsResult can not be call!");
    }
  }

  /********************************
   *  end hook Activity life cycle callback
   ********************************************************/

  public void onViewDisappear(){
    WXComponent comp = getRootComponent();
    if(comp != null) {
      fireEvent(comp.getRef(), Constants.Event.VIEWDISAPPEAR, null, null);
      //call disappear of nested instances
      for(OnInstanceVisibleListener instance:mVisibleListeners){
        instance.onDisappear();
      }
    }
  }

  public void onViewAppear(){
    WXComponent comp = getRootComponent();
    if(comp != null) {
      fireEvent( comp.getRef(), Constants.Event.VIEWAPPEAR,null, null);
      for(OnInstanceVisibleListener instance:mVisibleListeners){
        instance.onAppear();
      }
    }
  }


  public void onCreateFinish() {
    if (null != mWXPerformance){
      mWXPerformance.callCreateFinishTime=System.currentTimeMillis()-mWXPerformance
              .renderTimeOrigin;
    }
    if (mContext != null) {
      runOnUiThread(new Runnable() {

        @Override
        public void run() {
          if ( mContext != null) {
            onViewAppear();
            View wxView= mRenderContainer;
            if(mRenderListener != null) {
              mRenderListener.onViewCreated(WXSDKInstance.this, wxView);
            }
            if (mStatisticsListener != null) {
              mStatisticsListener.onFirstView();
            }
          }
        }
      });
    }
  }

  /**
   * call back when update finish
   */
  public void onUpdateFinish() {
    WXLogUtils.d("Instance onUpdateSuccess");
  }


  public void runOnUiThread(Runnable action) {
    WXSDKManager.getInstance().postOnUiThread(action, 0);
  }

  public void onRenderSuccess(final int width, final int height) {
    isJSCreateFinish = true;
    firstScreenRenderFinished();

    long time = System.currentTimeMillis() - mRenderStartTime;
    long[] renderFinishTime = WXBridgeManager.getInstance().getRenderFinishTime(getInstanceId());
    WXLogUtils.renderPerformanceLog("onRenderSuccess", time);
    WXLogUtils.renderPerformanceLog("   invokeCreateInstance",mWXPerformance.communicateTime);
    WXLogUtils.renderPerformanceLog("   onRenderSuccessCallBridgeTime", renderFinishTime[0]);
    WXLogUtils.renderPerformanceLog("   onRenderSuccessCssLayoutTime", renderFinishTime[1]);
    WXLogUtils.renderPerformanceLog("   onRenderSuccessParseJsonTime", renderFinishTime[2]);

    mWXPerformance.callBridgeTime = renderFinishTime[0];
    mWXPerformance.cssLayoutTime = renderFinishTime[1];
    mWXPerformance.parseJsonTime = renderFinishTime[2];

    mWXPerformance.totalTime = time;
    if(mWXPerformance.screenRenderTime<0.001){
      mWXPerformance.screenRenderTime =  time;
    }
    WXLogUtils.d(WXLogUtils.WEEX_PERF_TAG, "mComponentNum:" + mWXPerformance.componentCount);

    if (mRenderListener != null && mContext != null) {
      runOnUiThread(new Runnable() {

        @Override
        public void run() {
          if (mRenderListener != null && mContext != null) {
            mRenderListener.onRenderSuccess(WXSDKInstance.this, width, height);
            if (mUserTrackAdapter != null) {
              WXPerformance performance=new WXPerformance();
              performance.errCode=WXErrorCode.WX_SUCCESS.getErrorCode();
              performance.args=getBundleUrl();
              mUserTrackAdapter.commit(mContext,null,IWXUserTrackAdapter.JS_BRIDGE,performance,getUserTrackParams());
            }

            WXLogUtils.d(WXLogUtils.WEEX_PERF_TAG, mWXPerformance.toString());
          }
        }
      });
    }
    if(!WXEnvironment.isApkDebugable()){
      WXLogUtils.e("weex_perf",mWXPerformance.getPerfData());
    }
  }

  public void onRefreshSuccess(final int width, final int height) {
    WXLogUtils.renderPerformanceLog("onRefreshSuccess", (System.currentTimeMillis() - mRefreshStartTime));
    if (mRenderListener != null && mContext != null) {
      runOnUiThread(new Runnable() {

        @Override
        public void run() {
          if (mRenderListener != null && mContext != null) {
            mRenderListener.onRefreshSuccess(WXSDKInstance.this, width, height);
          }
        }
      });
    }
  }

  /**
   * when add/rm element
   */
  public void onElementChange(boolean afterJSCreateFinish){
    if (isDestroy() || !afterJSCreateFinish ||null == mRenderContainer || mRenderContainer.isPageHasEvent() ||
            mWXPerformance == null){
      return;
    }
    long lazyLoadTime = System.currentTimeMillis()- mWXPerformance.renderTimeOrigin - mWXPerformance
            .callCreateFinishTime;
    if (lazyLoadTime > 8000){
      //bad case
      return;
    }
    getWXPerformance().interactionTime = mWXPerformance.callCreateFinishTime + lazyLoadTime;
  }

  public void onRenderError(final String errCode, final String msg) {
    if (mRenderListener != null && mContext != null) {
      runOnUiThread(new Runnable() {

        @Override
        public void run() {
          if (mRenderListener != null && mContext != null) {
            mRenderListener.onException(WXSDKInstance.this, errCode, msg);
          }
        }
      });
    }
  }

  @Override
  public final void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int
          oldTop, int oldRight, int oldBottom) {
    if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
      onLayoutChange(v);
    }
  }

  /**
   * Subclass should override this method to get notifications of layout change of GodView.
   * @param godView the godView.
   */
  public void onLayoutChange(View godView) {

  }

  private boolean mCreateInstance =true;
  public void firstScreenCreateInstanceTime(long time) {
    if(mCreateInstance) {
      mWXPerformance.firstScreenJSFExecuteTime = time -mRenderStartTime;
      mCreateInstance =false;
    }
  }

  public void callJsTime(final long time){
    if (!mEnd){
      mWXPerformance.fsCallJsTotalTime+=time;
      mWXPerformance.fsCallJsTotalNum++;
    }
  }

  public void onComponentCreate(WXComponent component,long createTime) {
      mWXPerformance.mActionAddElementCount++;
      mWXPerformance.mActionAddElementSumTime += createTime;
      if (!mEnd){
        mWXPerformance.fsComponentCreateTime+=createTime;
        mWXPerformance.fsComponentCount++;
      }
      mWXPerformance.componentCount++;
      mWXPerformance.componentCreateTime+=createTime;
  }

  public void callActionAddElementTime(long time) {
      mWXPerformance.mActionAddElementSumTime += time;
  }

  public void firstScreenRenderFinished() {
      if (mEnd)
          return;

      mEnd = true;

      if (mStatisticsListener != null && mContext != null) {
          runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  if (mStatisticsListener != null && mContext != null) {
                      Trace.beginSection("onFirstScreen");
                      mStatisticsListener.onFirstScreen();
                      Trace.endSection();
                  }
              }
          });
      }

      mWXPerformance.screenRenderTime = System.currentTimeMillis() - mRenderStartTime;
      mWXPerformance.fsRenderTime = System.currentTimeMillis();
      long[] fitstScreenPerformance = WXBridgeManager.getInstance().getFirstScreenRenderTime(getInstanceId());
      WXLogUtils.renderPerformanceLog("firstScreenRenderFinished", mWXPerformance.screenRenderTime);
      WXLogUtils.renderPerformanceLog("    firstScreenJSFExecuteTime", mWXPerformance.firstScreenJSFExecuteTime);
      WXLogUtils.renderPerformanceLog("    firstScreenCallBridgeTime", fitstScreenPerformance[0]);
      WXLogUtils.renderPerformanceLog("    firstScreenCssLayoutTime", fitstScreenPerformance[1]);
      WXLogUtils.renderPerformanceLog("    firstScreenParseJsonTime", fitstScreenPerformance[2]);
  }

  public void createInstanceFinished(long time) {

  }

  private void destroyView(View rootView) {
    try {
      if (rootView instanceof ViewGroup) {
        ViewGroup cViewGroup = ((ViewGroup) rootView);
        for (int index = 0; index < cViewGroup.getChildCount(); index++) {
          destroyView(cViewGroup.getChildAt(index));
        }

        cViewGroup.removeViews(0, ((ViewGroup) rootView).getChildCount());
        // Ensure that the viewgroup's status to be normal
        WXReflectionUtils.setValue(rootView, "mChildrenCount", 0);

      }
      if(rootView instanceof Destroyable){
        ((Destroyable)rootView).destroy();
      }
    } catch (Exception e) {
      WXLogUtils.e("WXSDKInstance destroyView Exception: ", e);
    }
  }

  public synchronized void destroy() {
    if(!isDestroy()) {
      if(mRendered) {
        WXSDKManager.getInstance().destroyInstance(mInstanceId);
      }
      WXComponentFactory.removeComponentTypesByInstanceId(getInstanceId());

      if (mGlobalEventReceiver != null) {
        getContext().unregisterReceiver(mGlobalEventReceiver);
        mGlobalEventReceiver = null;
      }
      if (mRootComp != null) {
        mRootComp.destroy();
        destroyView(mRenderContainer);
        mRootComp = null;
      }

      if (mGlobalEvents != null) {
        mGlobalEvents.clear();
      }

      if (mComponentObserver != null) {
        mComponentObserver = null;
      }

      if (mLayerOverFlowListeners != null) {
        mLayerOverFlowListeners.clear();
      }

      getFlatUIContext().destroy();
      mFlatGUIContext = null;

      mWXScrollListeners = null;
      mRenderContainer = null;
      mNestedInstanceInterceptor = null;
      mUserTrackAdapter = null;
      mScrollView = null;
      mContext = null;
      mRenderListener = null;
      isDestroy = true;
      mStatisticsListener = null;
      if(responseHeaders != null){
        responseHeaders.clear();
      }
      if(templateRef != null){
        templateRef = null;
      }
      if (null != mContentBoxMeasurements) {
        mContentBoxMeasurements.clear();
      }
      mWXPerformance.afterInstanceDestroy(mInstanceId);

      new Handler(Looper.getMainLooper()).post(new Runnable() {
        @Override
        public void run() {
          WXBridgeManager.getInstance().onInstanceClose(getInstanceId());
        }
      });
    }
  }

  public boolean isDestroy(){
    return isDestroy;
  }

  /**
   * @return If you use render () the return value may be empty
   */
  public @Nullable String getBundleUrl() {
    return mBundleUrl;
  }

  public View getRootView() {
    if (mRootComp == null)
      return null;
    return mRootComp.getRealView();
  }

  public View getContainerView() {
    return mRenderContainer;
  }

  @Deprecated
  public void setBundleUrl(String url){
    mBundleUrl = url;
    if(WXSDKManager.getInstance().getValidateProcessor()!=null) {
      mNeedValidate = WXSDKManager.getInstance().getValidateProcessor().needValidate(mBundleUrl);
    }
  }

  public void onRootCreated(WXComponent root) {
    this.mRootComp = root;
    this.mRootComp.deepInComponentTree=1;
    mRenderContainer.addView(root.getHostView());
    setSize(mRenderContainer.getWidth(),mRenderContainer.getHeight());
  }

  /**
   * Move fixed view to container ,except it's already moved.
   * @param fixedChild
   */
  public void moveFixedView(View fixedChild){
    if(mRenderContainer != null) {
      ViewGroup parent;
      if((parent = (ViewGroup) fixedChild.getParent()) != null){
        if (parent != mRenderContainer) {
          parent.removeView(fixedChild);
          mRenderContainer.addView(fixedChild);
        }
      }else{
        mRenderContainer.addView(fixedChild);
      }
    }
  }

  public void removeFixedView(View fixedChild){
    if(mRenderContainer != null) {
      mRenderContainer.removeView(fixedChild);
    }
  }

  public int getRenderContainerPaddingLeft() {
    if(mRenderContainer != null) {
      return mRenderContainer.getPaddingLeft();
    }
    return 0;
  }

  public int getRenderContainerPaddingTop() {
    if(mRenderContainer != null) {
      return mRenderContainer.getPaddingTop();
    }
    return 0;
  }

  public synchronized List<OnWXScrollListener> getWXScrollListeners() {
    return mWXScrollListeners;
  }

  public synchronized void registerOnWXScrollListener(OnWXScrollListener wxScrollListener) {
    if(mWXScrollListeners==null){
      mWXScrollListeners=new ArrayList<>();
    }
    mWXScrollListeners.add(wxScrollListener);
  }

  public void setSize(int width, int height) {
    if (width > 0 && height > 0 & !isDestroy && mRendered && mRenderContainer != null) {
      ViewGroup.LayoutParams layoutParams = mRenderContainer.getLayoutParams();
      if (layoutParams != null) {
        final float realWidth = width;
        final float realHeight = height;
        if (mRenderContainer.getWidth() != width || mRenderContainer.getHeight() != height) {
          layoutParams.width = width;
          layoutParams.height = height;
          mRenderContainer.setLayoutParams(layoutParams);
        }

        if (mRootComp != null && layoutParams != null) {
          final boolean isWidthWrapContent = layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT;
          final boolean isHeightWrapContent = layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT;

          new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
              WXBridgeManager.getInstance().setDefaultRootSize(getInstanceId(), realWidth, realHeight, isWidthWrapContent,
                      isHeightWrapContent);
            }
          });
        }
      }
    }
  }

  /*Global Event*/
  private HashMap<String, List<String>> mGlobalEvents = new HashMap<>();

  public void fireGlobalEventCallback(String eventName, Map<String,Object> params){
    List<String> callbacks=mGlobalEvents.get(eventName);
    if(callbacks!=null){
      for(String callback:callbacks){
//        WXSDKManager.getInstance().callback(mInstanceId,callback,params,true);
        // XXTODO
        WXLogUtils.e("weex", "fireGlobalEventCallback eventName = " + eventName);
      }
    }
  }

  /**
   * Fire event callback on a element.
   * @param elementRef
   * @param type
   * @param data
   * @param domChanges
   */
  public void fireEvent(String elementRef,final String type, final Map<String, Object> data,final Map<String, Object> domChanges, List<Object> eventArgs){
    fireEvent(elementRef, type, data, domChanges, eventArgs, null);
  }

  public void fireEvent(String elementRef,final String type, final Map<String, Object> data,final Map<String, Object> domChanges, List<Object> eventArgs, EventResult callback) {
    if (null != mWXPerformance && mWXPerformance.fsCallEventTotalNum<Integer.MAX_VALUE){
      mWXPerformance.fsCallEventTotalNum++;
    }
    WXBridgeManager.getInstance().fireEventOnNode(getInstanceId(),elementRef,type,data,domChanges, eventArgs, callback);
  }
  /**
   * Fire event callback on a element.
   * @param elementRef
   * @param type
   * @param data
   * @param domChanges
   */
  public void fireEvent(String elementRef,final String type, final Map<String, Object> data,final Map<String, Object> domChanges){
    fireEvent(elementRef, type, data, domChanges, null);
  }

  public void fireEvent(String elementRef,final String type, final Map<String, Object> data){
    fireEvent(elementRef,type,data,null);
  }

  public void fireEvent(String ref, String type){
    fireEvent(ref,type,new HashMap<String, Object>());
  }

  protected void addEventListener(String eventName, String callback) {
    if (TextUtils.isEmpty(eventName) || TextUtils.isEmpty(callback)) {
      return;
    }
    List<String> callbacks = mGlobalEvents.get(eventName);
    if (callbacks == null) {
      callbacks = new ArrayList<>();
      mGlobalEvents.put(eventName, callbacks);
    }
    callbacks.add(callback);
  }
  protected void removeEventListener(String eventName, String callback) {
    if (TextUtils.isEmpty(eventName) || TextUtils.isEmpty(callback)) {
      return;
    }
    List<String> callbacks = mGlobalEvents.get(eventName);
    if (callbacks != null) {
      callbacks.remove(callback);
    }
  }

  protected void removeEventListener(String eventName) {
    if (TextUtils.isEmpty(eventName)) {
      return;
    }
    mGlobalEvents.remove(eventName);
  }

  public WXPerformance getWXPerformance(){
    return mWXPerformance;
  }

  public Map<String, Serializable> getUserTrackParams() {
    return mUserTrackParams;
  }

  public void addUserTrackParameter(String key,Serializable value){
    if(this.mUserTrackParams == null){
      this.mUserTrackParams = new ConcurrentHashMap<>();
    }
    mUserTrackParams.put(key,value);
  }

  public void clearUserTrackParameters(){
    if(this.mUserTrackParams != null){
      this.mUserTrackParams.clear();
    }
  }

  public void removeUserTrackParameter(String key){
    if(this.mUserTrackParams != null){
      this.mUserTrackParams.remove(key);
    }
  }

  public void setMaxDomDeep(int maxDomDeep){
    if (null == mWXPerformance){
      return;
    }
    if (mWXPerformance.maxDeepVDomLayer <= maxDomDeep){
      mWXPerformance.maxDeepVDomLayer = maxDomDeep;
    }
  }

  public void onHttpStart(){
    if (!mEnd){
      mWXPerformance.fsRequestNum++;
    }
  }

  private boolean isNet(String requestType){

    return "network".equals(requestType) || "2g".equals(requestType) || "3g".equals(requestType)
            || "4g".equals(requestType) || "wifi".equals(requestType) || "other".equals(requestType)
            || "unknown".equals(requestType);
  }

  /**
   * return md5, and bytes length
   * */
  public String getTemplateInfo() {
    String template = getTemplate();
    if(template == null){
      return " template md5 null " + JSONObject.toJSONString(responseHeaders);
    }
    if(TextUtils.isEmpty(template)){
      return " template md5  length 0 " + JSONObject.toJSONString(responseHeaders);
    }
    try {
      byte[] bts = template.getBytes("UTF-8");
      String sourceMD5 = WXFileUtils.md5(bts);
      String sourceBase64MD5 = WXFileUtils.base64Md5(bts);
      ArrayList<String> sourceMD5List = new ArrayList<>();
      ArrayList<String> sourceBase64MD5List = new ArrayList<>();
      sourceMD5List.add(sourceMD5);
      sourceBase64MD5List.add(sourceBase64MD5);
      responseHeaders.put("templateSourceMD5", sourceMD5List);
      responseHeaders.put(SOURCE_TEMPLATE_BASE64_MD5, sourceBase64MD5List);
      return " template md5 " + sourceMD5 + " length " +   bts.length
              + " base64 md5 " + sourceBase64MD5
              + " response header " + JSONObject.toJSONString(responseHeaders);
    } catch (UnsupportedEncodingException e) {
      return "template md5 getBytes error";
    }

  }

  /**
   * check template header md5 match with header  content-md5
   * */
  public boolean isContentMd5Match(){
    if(responseHeaders == null){
      return true;
    }
    List<String> contentMD5s = responseHeaders.get("Content-Md5");
    if(contentMD5s == null){
      contentMD5s  = responseHeaders.get("content-md5");
    }
    if(contentMD5s == null || contentMD5s.size() <= 0){
      return true;
    }
    String md5 = contentMD5s.get(0);

    List<String> sourceBase64Md5 = responseHeaders.get(SOURCE_TEMPLATE_BASE64_MD5);
    if(sourceBase64Md5 == null){
      getTemplateInfo();
      sourceBase64Md5 = responseHeaders.get(SOURCE_TEMPLATE_BASE64_MD5);
    }
    if(sourceBase64Md5 == null || sourceBase64Md5.size() == 0){
      return  true;
    }
    return  md5.equals(sourceBase64Md5.get(0));
  }

  public String getTemplate() {
    if(templateRef == null){
      return  null;
    }
    return templateRef.get();
  }

  public void setTemplate(String template) {
    this.templateRef = new WeakReference<String>(template);
  }

  public interface NestedInstanceInterceptor {
    void onCreateNestInstance(WXSDKInstance instance, NestedContainer container);
  }

  public void OnVSync() {
    boolean forceLayout = WXBridgeManager.getInstance().notifyLayout(getInstanceId());
    if(forceLayout) {
      new Handler(Looper.getMainLooper()).post(new Runnable() {
        @Override
        public void run() {
          WXBridgeManager.getInstance().forceLayout(getInstanceId());
        }
      });
    }
  }

  public void addContentBoxMeasurement(long renderObjectPtr, ContentBoxMeasurement contentBoxMeasurement) {
    mContentBoxMeasurements.put(renderObjectPtr, contentBoxMeasurement);
  }

  public ContentBoxMeasurement getContentBoxMeasurement(long renderObjectPtr) {
    return mContentBoxMeasurements.get(renderObjectPtr);
  }
}
