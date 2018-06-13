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
package com.taobao.weex;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.taobao.weex.adapter.ClassLoaderAdapter;
import com.taobao.weex.adapter.DefaultUriAdapter;
import com.taobao.weex.adapter.DefaultWXHttpAdapter;
import com.taobao.weex.adapter.ICrashInfoReporter;
import com.taobao.weex.adapter.IDrawableLoader;
import com.taobao.weex.adapter.ITracingAdapter;
import com.taobao.weex.adapter.IWXAccessibilityRoleAdapter;
import com.taobao.weex.adapter.IWXHttpAdapter;
import com.taobao.weex.adapter.IWXImgLoaderAdapter;
import com.taobao.weex.adapter.IWXSoLoaderAdapter;
import com.taobao.weex.adapter.IWXUserTrackAdapter;
import com.taobao.weex.adapter.URIAdapter;
import com.taobao.weex.appfram.navigator.IActivityNavBarSetter;
import com.taobao.weex.appfram.storage.DefaultWXStorage;
import com.taobao.weex.appfram.storage.IWXStorageAdapter;
import com.taobao.weex.bridge.WXBridgeManager;
import com.taobao.weex.bridge.WXModuleManager;
import com.taobao.weex.bridge.WXValidateProcessor;
import com.taobao.weex.common.WXRefreshData;
import com.taobao.weex.common.WXRuntimeException;
import com.taobao.weex.common.WXThread;
import com.taobao.weex.common.WXWorkThreadManager;
import com.taobao.weex.performance.IWXAnalyzer;
import com.taobao.weex.ui.WXRenderManager;
import com.taobao.weex.utils.WXLogUtils;
import com.taobao.weex.utils.WXUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manger class for weex context.
 */
public class WXSDKManager {

  private static volatile WXSDKManager sManager;
  private static AtomicInteger sInstanceId = new AtomicInteger(0);
  private final WXWorkThreadManager mWXWorkThreadManager;
  private WXBridgeManager mBridgeManager;
  /** package **/ WXRenderManager mWXRenderManager;

  private IWXUserTrackAdapter mIWXUserTrackAdapter;
  private IWXImgLoaderAdapter mIWXImgLoaderAdapter;
  private IWXSoLoaderAdapter mIWXSoLoaderAdapter;
  private IDrawableLoader mDrawableLoader;
  private IWXHttpAdapter mIWXHttpAdapter;
  private IActivityNavBarSetter mActivityNavBarSetter;
  private IWXAccessibilityRoleAdapter mRoleAdapter;
  private List<IWXAnalyzer> mWXAnalyzerList;

  private ICrashInfoReporter mCrashInfo;

  private IWXStorageAdapter mIWXStorageAdapter;
  private IWXStatisticsListener mStatisticsListener;
  private URIAdapter mURIAdapter;
  private ClassLoaderAdapter mClassLoaderAdapter;
  private ITracingAdapter mTracingAdapter;
  private WXValidateProcessor mWXValidateProcessor;

  private List<InstanceLifeCycleCallbacks> mLifeCycleCallbacks;

  private static final int DEFAULT_VIEWPORT_WIDTH = 750;

  private WXSDKManager() {
    this(new WXRenderManager());
  }

  private WXSDKManager(WXRenderManager renderManager) {
    mWXRenderManager = renderManager;
    mBridgeManager = WXBridgeManager.getInstance();
    mWXWorkThreadManager = new WXWorkThreadManager();
    mWXAnalyzerList = new ArrayList<>();
  }

  /**
   * Used in junit test
   */
  static void initInstance(WXRenderManager renderManager){
    sManager = new WXSDKManager(renderManager);
  }

  public void registerStatisticsListener(IWXStatisticsListener listener) {
    mStatisticsListener = listener;
  }

  public IWXStatisticsListener getWXStatisticsListener() {
    return mStatisticsListener;
  }

  public void onSDKEngineInitialize() {
    if (mStatisticsListener != null) {
      mStatisticsListener.onSDKEngineInitialize();
    }
  }

  public static WXSDKManager getInstance() {
    if (sManager == null) {
      synchronized (WXSDKManager.class) {
        if(sManager == null) {
          sManager = new WXSDKManager();
        }
      }
    }
    return sManager;
  }

  public static int getInstanceViewPortWidth(String instanceId){
    WXSDKInstance instance = getInstance().getSDKInstance(instanceId);
    if (instance == null) {
      return DEFAULT_VIEWPORT_WIDTH;
    }
    return instance.getInstanceViewPortWidth();
  }

  static void setInstance(WXSDKManager manager){
    sManager = manager;
  }

  public IActivityNavBarSetter getActivityNavBarSetter() {
    return mActivityNavBarSetter;
  }

  public void setActivityNavBarSetter(IActivityNavBarSetter mActivityNavBarSetter) {
    this.mActivityNavBarSetter = mActivityNavBarSetter;
  }

  public WXRenderManager getWXRenderManager() {
    return mWXRenderManager;
  }

  public WXWorkThreadManager getWXWorkThreadManager() {
    return mWXWorkThreadManager;
  }

  public @Nullable WXSDKInstance getSDKInstance(String instanceId) {
    return instanceId == null? null : mWXRenderManager.getWXSDKInstance(instanceId);
  }

  public void postOnUiThread(Runnable runnable, long delayMillis) {
    mWXRenderManager.postOnUiThread(WXThread.secure(runnable), delayMillis);
  }

  public void destroy() {
    if (mWXWorkThreadManager != null) {
      mWXWorkThreadManager.destroy();
    }
  }

  /**
   * Do not direct invoke this method in Components, use {@link WXSDKInstance#fireEvent(String, String, Map, Map)} instead.
   */
  @Deprecated
  public void fireEvent(final String instanceId, String ref, String type) {
    fireEvent(instanceId, ref, type, new HashMap<String, Object>());
  }

  /**
   * FireEvent back to JS
   * Do not direct invoke this method in Components, use {@link WXSDKInstance#fireEvent(String, String, Map, Map)} instead.
   */
  @Deprecated
  public void fireEvent(final String instanceId, String ref, String type, Map<String, Object> params){
    fireEvent(instanceId,ref,type,params,null);
  }

  /**
   * Do not direct invoke this method in Components, use {@link WXSDKInstance#fireEvent(String, String, Map, Map)} instead.
   **/
  @Deprecated
  public void fireEvent(final String instanceId, String ref, String type, Map<String, Object> params,Map<String,Object> domChanges) {
    if (WXEnvironment.isApkDebugable() && Looper.getMainLooper().getThread().getId() != Thread.currentThread().getId()) {
      throw new WXRuntimeException("[WXSDKManager]  fireEvent error");
    }
//    mBridgeManager.fireEventOnNode(instanceId, ref, type, params,domChanges);
    // XXTODO
  }

  void refreshInstance(String instanceId, WXRefreshData jsonData) {
    // XXTODO
  }

  void destroyInstance(String instanceId) {
    setCrashInfo(WXEnvironment.WEEX_CURRENT_KEY,"");
    if (TextUtils.isEmpty(instanceId)) {
      return;
    }
    if (!WXUtils.isUiThread()) {
      throw new WXRuntimeException("[WXSDKManager] destroyInstance error");
    }
    if (mLifeCycleCallbacks != null) {
      for (InstanceLifeCycleCallbacks callbacks : mLifeCycleCallbacks) {
        callbacks.onInstanceDestroyed(instanceId);
      }
    }
    mWXRenderManager.removeRenderStatement(instanceId);
    WXModuleManager.destroyInstanceModules(instanceId);
  }

  String generateInstanceId() {
    return String.valueOf(sInstanceId.incrementAndGet());
  }

  public IWXUserTrackAdapter getIWXUserTrackAdapter() {
    return mIWXUserTrackAdapter;
  }

  public IWXImgLoaderAdapter getIWXImgLoaderAdapter() {
    return mIWXImgLoaderAdapter;
  }

  public IDrawableLoader getDrawableLoader() {
    return mDrawableLoader;
  }

  public @NonNull IWXHttpAdapter getIWXHttpAdapter() {
    if (mIWXHttpAdapter == null) {
      mIWXHttpAdapter = new DefaultWXHttpAdapter();
    }
    return mIWXHttpAdapter;
  }

  public @NonNull URIAdapter getURIAdapter() {
    if(mURIAdapter == null){
      mURIAdapter = new DefaultUriAdapter();
    }
    return mURIAdapter;
  }

  public ClassLoaderAdapter getClassLoaderAdapter() {
    if(mClassLoaderAdapter == null){
      mClassLoaderAdapter = new ClassLoaderAdapter();
    }
    return mClassLoaderAdapter;
  }

  public IWXSoLoaderAdapter getIWXSoLoaderAdapter() {
    return mIWXSoLoaderAdapter;
  }

  public List<IWXAnalyzer> getWXAnalyzerList(){
    return mWXAnalyzerList;
  }

  public void addWXAnalyzer(IWXAnalyzer analyzer){
    if (!mWXAnalyzerList.contains(analyzer)) {
      mWXAnalyzerList.add(analyzer);
    }
  }

  public void rmWXAnalyzer(IWXAnalyzer analyzer){
    mWXAnalyzerList.remove(analyzer);
  }

  void setInitConfig(InitConfig config){
    this.mIWXHttpAdapter = config.getHttpAdapter();
    this.mIWXImgLoaderAdapter = config.getImgAdapter();
    this.mDrawableLoader = config.getDrawableLoader();
    this.mIWXStorageAdapter = config.getStorageAdapter();
    this.mIWXUserTrackAdapter = config.getUtAdapter();
    this.mURIAdapter = config.getURIAdapter();
    this.mIWXSoLoaderAdapter = config.getIWXSoLoaderAdapter();
    this.mClassLoaderAdapter = config.getClassLoaderAdapter();
  }

  public IWXStorageAdapter getIWXStorageAdapter(){
    if(mIWXStorageAdapter == null){
      if(WXEnvironment.sApplication != null){
        mIWXStorageAdapter = new DefaultWXStorage(WXEnvironment.sApplication);
      }else{
        WXLogUtils.e("WXStorageModule", "No Application context found,you should call WXSDKEngine#initialize() method in your application");
      }
    }
    return mIWXStorageAdapter;
  }

  public void registerValidateProcessor(WXValidateProcessor processor){
    this.mWXValidateProcessor = processor;
  }

  public WXValidateProcessor getValidateProcessor(){
    return mWXValidateProcessor;
  }


  public void setCrashInfoReporter(ICrashInfoReporter mCrashInfo) {
    this.mCrashInfo = mCrashInfo;
  }

  public void setCrashInfo(String key, String value) {
    if(mCrashInfo!=null){
      mCrashInfo.addCrashInfo(key,value);
    }
  }

  public void setTracingAdapter(ITracingAdapter adapter) {
    this.mTracingAdapter = adapter;
  }

  public ITracingAdapter getTracingAdapter() {
    return mTracingAdapter;
  }

  public void registerInstanceLifeCycleCallbacks(InstanceLifeCycleCallbacks callbacks) {
    if (mLifeCycleCallbacks == null) {
      mLifeCycleCallbacks = new ArrayList<>();
    }
    mLifeCycleCallbacks.add(callbacks);
  }

  public void setAccessibilityRoleAdapter(IWXAccessibilityRoleAdapter adapter) {
    this.mRoleAdapter = adapter;
  }

  public IWXAccessibilityRoleAdapter getAccessibilityRoleAdapter() {
    return mRoleAdapter;
  }

  public WXBridgeManager getWXBridgeManager() {
    return mBridgeManager;
  }

  public interface InstanceLifeCycleCallbacks {
    void onInstanceDestroyed(String instanceId);
    void onInstanceCreated(String instanceId);
  }
}
