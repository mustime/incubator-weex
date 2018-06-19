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
package com.taobao.weex.bridge;

import android.app.Application;
import android.os.Looper;
import android.support.annotation.UiThread;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.taobao.weex.WXEnvironment;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.WXSDKManager;
import com.taobao.weex.common.IWXBridge;
import com.taobao.weex.common.WXConfig;
import com.taobao.weex.common.WXRuntimeException;
import com.taobao.weex.dom.CSSShorthand;
import com.taobao.weex.layout.ContentBoxMeasurement;
import com.taobao.weex.ui.action.BasicGraphicAction;
import com.taobao.weex.ui.action.GraphicActionAddElement;
import com.taobao.weex.ui.action.GraphicActionAppendTreeCreateFinish;
import com.taobao.weex.ui.action.GraphicActionCreateBody;
import com.taobao.weex.ui.action.GraphicActionCreateFinish;
import com.taobao.weex.ui.action.GraphicActionLayout;
import com.taobao.weex.ui.action.GraphicActionMoveElement;
import com.taobao.weex.ui.action.GraphicActionRemoveElement;
import com.taobao.weex.ui.action.GraphicActionUpdateAttr;
import com.taobao.weex.ui.action.GraphicActionUpdateStyle;
import com.taobao.weex.ui.action.GraphicPosition;
import com.taobao.weex.ui.action.GraphicSize;
import com.taobao.weex.ui.component.WXComponent;
import com.taobao.weex.utils.WXLogUtils;
import com.taobao.weex.utils.WXViewUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class WXBridgeManager {

  static volatile WXBridgeManager mBridgeManager;

  private static final boolean BRIDGE_LOG_SWITCH = false;

  private IWXBridge mWXBridge = new WXBridge();
  private List<String> mDestroyedInstanceId = new ArrayList<>();
  private StringBuilder mLodBuilder = new StringBuilder(50);
  private WXParams mInitParams;

  private WXBridgeManager() {
  }

  public static WXBridgeManager getInstance() {
    if (mBridgeManager == null) {
      synchronized (WXBridgeManager.class) {
        if (mBridgeManager == null) {
          mBridgeManager = new WXBridgeManager();
        }
      }
    }
    return mBridgeManager;
  }

  public void initBridge(Application app) {
    mWXBridge.init(app.getApplicationContext());
  }

  @Deprecated
  public void fireEvent(final String instanceId, final String ref,
                        final String type, final Map<String, Object> data) {
    this.fireEvent(instanceId, ref, type, data, null);
  }

  /**
   * Do not direct invoke this method in Components, use {@link WXSDKInstance#fireEvent(String, String, Map, Map)} instead.
   *
   * @param instanceId
   * @param ref
   * @param type
   * @param data
   * @param domChanges
   */
  @Deprecated
  public void fireEvent(final String instanceId, final String ref,
                        final String type, final Map<String, Object> data, final Map<String, Object> domChanges) {
    fireEventOnNode(instanceId, ref, type, data, domChanges);
  }

  /**
   * Notify the JavaScript about the event happened on Android
   */
  public void fireEventOnNode(final String instanceId, final String ref,
                              final String type, final Map<String, Object> data, final Map<String, Object> domChanges) {
    fireEventOnNode(instanceId, ref, type, data, domChanges, null, null);
  }

  /**
   * Notify the JavaScript about the event happened on Android
   */
  public void fireEventOnNode(final String instanceId, final String ref,
                              final String type, final Map<String, Object> data,
                              final Map<String, Object> domChanges, List<Object> params){
    fireEventOnNode(instanceId, ref, type, data, domChanges, params, null);
  }

  public void fireEventOnNode(final String instanceId, final String ref,
                              final String type, final Map<String, Object> data,
                              final Map<String, Object> domChanges, List<Object> params,  EventResult callback) {
    if (TextUtils.isEmpty(instanceId) || TextUtils.isEmpty(ref) || TextUtils.isEmpty(type)) {
      return;
    }
    if (!checkMainThread()) {
      throw new WXRuntimeException(
              "fireEvent must be called by main thread");
    }
    WXLogUtils.e("weex", "fireEventOnNode ref = " + ref + ", type = " + type);
    if(callback == null) {
      // XXTODO
    }else{
      // XXTODO
    }
  }

  private boolean checkMainThread() {
    return Looper.myLooper() == Looper.getMainLooper();
  }

  private WXParams assembleDefaultOptions() {
    Map<String, String> config = WXEnvironment.getConfig();
    WXParams wxParams = new WXParams();
    wxParams.setPlatform(config.get(WXConfig.os));
    wxParams.setCacheDir(config.get(WXConfig.cacheDir));
    wxParams.setOsVersion(config.get(WXConfig.sysVersion));
    wxParams.setAppVersion(config.get(WXConfig.appVersion));
    wxParams.setWeexVersion(config.get(WXConfig.weexVersion));
    wxParams.setDeviceModel(config.get(WXConfig.sysModel));
    wxParams.setShouldInfoCollect(config.get("infoCollect"));
    wxParams.setLogLevel(config.get(WXConfig.logLevel));
    String appName = config.get(WXConfig.appName);
    if (!TextUtils.isEmpty(appName)) {
      wxParams.setAppName(appName);
    }
    wxParams.setDeviceWidth(TextUtils.isEmpty(config.get("deviceWidth")) ? String.valueOf(WXViewUtils.getScreenWidth(WXEnvironment.sApplication)) : config.get("deviceWidth"));
    wxParams.setDeviceHeight(TextUtils.isEmpty(config.get("deviceHeight")) ? String.valueOf(WXViewUtils.getScreenHeight(WXEnvironment.sApplication)) : config.get("deviceHeight"));
    wxParams.setOptions(WXEnvironment.getCustomOptions());
    mInitParams = wxParams;
    return wxParams;
  }

  public WXParams getInitParams() {
    return mInitParams;
  }

  private boolean checkEmptyScreen(WXSDKInstance instance){
    if (null == instance || instance.isDestroy()){
      return false;
    }
    WXComponent rootComponent = instance.getRootComponent();
    if (null == rootComponent) {
      return true;
    }

    View rootView = rootComponent.getRealView();
    if (null == rootView){
      return true;
    }

    if (rootView instanceof ViewGroup) {
      return ((ViewGroup) rootView).getChildCount() > 0;
    }else {
      return false;
    }
  }

  //This method is deprecated because of performance issue.
  @Deprecated
  public void notifyTrimMemory() {

  }

  public static class TimerInfo {

    public String callbackId;
    public long time;
    public String instanceId;
  }

  public int callCreateBody(String pageId, String componentType, String ref,
                            HashMap<String, String> styles, HashMap<String, String> attributes, HashSet<String> events,
                            float[] margins, float[] paddings, float[] borders) {
    long start = System.currentTimeMillis();
    if (TextUtils.isEmpty(pageId) || TextUtils.isEmpty(componentType) || TextUtils.isEmpty(ref)) {
      // if (WXEnvironment.isApkDebugable()) {
      WXLogUtils.d("[WXBridgeManager] callCreateBody: call CreateBody tasks is null");
      // }
      return IWXBridge.INSTANCE_RENDERING_ERROR;
    }

    if (WXEnvironment.isApkDebugable() && BRIDGE_LOG_SWITCH) {
      mLodBuilder.append("[WXBridgeManager] callCreateBody >>>> pageId:").append(pageId)
              .append(", componentType:").append(componentType).append(", ref:").append(ref)
              .append(", styles:").append(styles)
              .append(", attributes:").append(attributes)
              .append(", events:").append(events);
      WXLogUtils.d(mLodBuilder.substring(0));
      mLodBuilder.setLength(0);
    }

    if (mDestroyedInstanceId != null && mDestroyedInstanceId.contains(pageId)) {
      return IWXBridge.DESTROY_INSTANCE;
    }

    try {
      if (WXSDKManager.getInstance().getSDKInstance(pageId) != null) {
        final BasicGraphicAction action = new GraphicActionCreateBody(pageId, ref, componentType,
                styles, attributes, events, margins, paddings, borders);
        WXSDKManager.getInstance().getWXRenderManager().postGraphicAction(action.getPageId(), action);
      }
    } catch (Exception e) {
      WXLogUtils.e("[WXBridgeManager] callCreateBody exception: ", e);
      e.printStackTrace();
    }

    return IWXBridge.INSTANCE_RENDERING;
  }

  public int callAddElement(String pageId, String componentType, String ref, int index, String parentRef,
                            HashMap<String, String> styles, HashMap<String, String> attributes, HashSet<String> events,
                            float[] margins, float[] paddings, float[] borders,boolean willLayout) {
    long start = System.currentTimeMillis();
    if (TextUtils.isEmpty(pageId) || TextUtils.isEmpty(componentType) || TextUtils.isEmpty(ref)) {
      WXLogUtils.d("[WXBridgeManager] callAddElement: call CreateBody tasks is null");

      return IWXBridge.INSTANCE_RENDERING_ERROR;
    }

    if (WXEnvironment.isApkDebugable() && BRIDGE_LOG_SWITCH) {
      mLodBuilder.append("[WXBridgeManager] callAddElement >>>> pageId:").append(pageId)
              .append(", componentType:").append(componentType).append(", ref:").append(ref).append(", index:").append(index)
              .append(", parentRef:").append(parentRef)
              .append(", styles:").append(styles)
              .append(", attributes:").append(attributes)
              .append(", events:").append(events);
      WXLogUtils.d(mLodBuilder.substring(0));
      mLodBuilder.setLength(0);
    }

    if (mDestroyedInstanceId != null && mDestroyedInstanceId.contains(pageId)) {
      return IWXBridge.DESTROY_INSTANCE;
    }

    try {
      if (WXSDKManager.getInstance().getSDKInstance(pageId) != null) {
        final GraphicActionAddElement action = new GraphicActionAddElement(pageId, ref, componentType, parentRef, index,
            styles, attributes, events, margins, paddings, borders);
        if(willLayout) {
          WXSDKManager.getInstance().getSDKInstance(pageId).addInActiveAddElementAction(ref, action);
        }else{
          WXSDKManager.getInstance().getWXRenderManager().postGraphicAction(pageId, action);
        }
      }
    } catch (Exception e) {
      WXLogUtils.e("[WXBridgeManager] callAddElement exception: ", e);
      e.printStackTrace();
    }

    return IWXBridge.INSTANCE_RENDERING;
  }

  public int callRemoveElement(String instanceId, String ref) {

    if (WXEnvironment.isApkDebugable() && BRIDGE_LOG_SWITCH) {
      mLodBuilder.append("[WXBridgeManager] callRemoveElement >>>> instanceId:").append(instanceId)
              .append(", ref:").append(ref);
      WXLogUtils.d(mLodBuilder.substring(0));
      mLodBuilder.setLength(0);
    }

    if (mDestroyedInstanceId != null && mDestroyedInstanceId.contains(instanceId)) {
      return IWXBridge.DESTROY_INSTANCE;
    }

    try {
      WXSDKInstance instance = WXSDKManager.getInstance().getSDKInstance(instanceId);
      if (instance != null) {
        final BasicGraphicAction action = new GraphicActionRemoveElement(instanceId, ref);
        if(instance.getInActiveAddElementAction(ref)!=null){
          instance.removeInActiveAddElmentAction(ref);
        }
        else {
          WXSDKManager.getInstance().getWXRenderManager()
              .postGraphicAction(action.getPageId(), action);
        }
      }
    } catch (Exception e) {
      WXLogUtils.e("[WXBridgeManager] callRemoveElement exception: ", e);
      e.printStackTrace();
    }

    return IWXBridge.INSTANCE_RENDERING;
  }

  public int callMoveElement(String instanceId, String ref, String parentref, int index) {

    if (WXEnvironment.isApkDebugable() && BRIDGE_LOG_SWITCH) {
      mLodBuilder.append("[WXBridgeManager] callMoveElement >>>> instanceId:").append(instanceId)
              .append(", parentref:").append(parentref)
              .append(", index:").append(index)
              .append(", ref:").append(ref);
      WXLogUtils.d(mLodBuilder.substring(0));
      mLodBuilder.setLength(0);
    }

    if (mDestroyedInstanceId != null && mDestroyedInstanceId.contains(instanceId)) {
      return IWXBridge.DESTROY_INSTANCE;
    }

    try {
      if (WXSDKManager.getInstance().getSDKInstance(instanceId) != null) {
        final BasicGraphicAction action = new GraphicActionMoveElement(instanceId, ref, parentref, index);
        WXSDKManager.getInstance().getWXRenderManager().postGraphicAction(action.getPageId(), action);
      }
    } catch (Exception e) {
      WXLogUtils.e("[WXBridgeManager] callMoveElement exception: ", e);
      e.printStackTrace();
    }

    return IWXBridge.INSTANCE_RENDERING;
  }

  public int callUpdateStyle(String instanceId, String ref, HashMap<String, Object> styles,
                             HashMap<String, String> paddings,
                             HashMap<String, String> margins,
                             HashMap<String, String> borders) {
    if (WXEnvironment.isApkDebugable() && BRIDGE_LOG_SWITCH) {
      mLodBuilder.append("[WXBridgeManager] callUpdateStyle >>>> instanceId:").append(instanceId)
              .append(", ref:").append(ref).append(", styles:").append(styles.toString())
              .append(", paddings:").append(paddings.toString())
                      .append(", margins:").append(margins.toString())
                              .append(", borders:").append(borders.toString());
      WXLogUtils.d(mLodBuilder.substring(0));
      mLodBuilder.setLength(0);
    }

    if (mDestroyedInstanceId != null && mDestroyedInstanceId.contains(instanceId)) {
      return IWXBridge.DESTROY_INSTANCE;
    }

    try {
      if (WXSDKManager.getInstance().getSDKInstance(instanceId) != null) {
        final BasicGraphicAction action = new GraphicActionUpdateStyle(instanceId, ref, styles, paddings, margins, borders);
        WXSDKManager.getInstance().getWXRenderManager().postGraphicAction(action.getPageId(), action);
      }
    } catch (Exception e) {
      WXLogUtils.e("[WXBridgeManager] callUpdateStyle exception: ", e);
      e.printStackTrace();
    }

    return IWXBridge.INSTANCE_RENDERING;
  }

  public int callUpdateAttrs(String instanceId, String ref, HashMap<String, String> attrs) {
    if (WXEnvironment.isApkDebugable() && BRIDGE_LOG_SWITCH) {
      mLodBuilder.append("[WXBridgeManager] callUpdateAttrs >>>> instanceId:").append(instanceId)
              .append(", ref:").append(ref).append(", attrs:").append(attrs.toString());
      WXLogUtils.d(mLodBuilder.substring(0));
      mLodBuilder.setLength(0);
    }

    if (mDestroyedInstanceId != null && mDestroyedInstanceId.contains(instanceId)) {
      return IWXBridge.DESTROY_INSTANCE;
    }

    try {
      if (WXSDKManager.getInstance().getSDKInstance(instanceId) != null) {
        final BasicGraphicAction action = new GraphicActionUpdateAttr(instanceId, ref, attrs);
        WXSDKManager.getInstance().getWXRenderManager().postGraphicAction(action.getPageId(), action);
      }
    } catch (Exception e) {
      WXLogUtils.e("[WXBridgeManager] callUpdateAttrs exception: ", e);
      e.printStackTrace();
    }

    return IWXBridge.INSTANCE_RENDERING;
  }

  public int callLayout(String pageId, String ref, int top, int bottom, int left, int right, int height, int width, int index) {
    long start = System.currentTimeMillis();
    if (TextUtils.isEmpty(pageId) || TextUtils.isEmpty(ref)) {
      WXLogUtils.d("[WXBridgeManager] callLayout: call callLayout arguments is null");
      return IWXBridge.INSTANCE_RENDERING_ERROR;
    }

    if (WXEnvironment.isApkDebugable() && BRIDGE_LOG_SWITCH) {
      mLodBuilder.append("[WXBridgeManager] callLayout >>>> instanceId:").append(pageId)
            .append(", ref:").append(ref).append(", height:").append(height).append(", width:").append(width)
              .append(", top:").append(top)
              .append(", bottom:").append(bottom)
              .append(", left:").append(left)
              .append(", right:").append(right);
      WXLogUtils.d(mLodBuilder.substring(0));
      mLodBuilder.setLength(0);
    }

    if (mDestroyedInstanceId != null && mDestroyedInstanceId.contains(pageId)) {
      return IWXBridge.DESTROY_INSTANCE;
    }

    try {
      if (WXSDKManager.getInstance().getSDKInstance(pageId) != null) {
        GraphicSize size = new GraphicSize(width, height);
        GraphicPosition position = new GraphicPosition(left, top, right, bottom);
        GraphicActionAddElement addAction = WXSDKManager.getInstance().getSDKInstance(pageId).getInActiveAddElementAction(ref);
        if(addAction!=null) {
          addAction.setSize(size);
          addAction.setPosition(position);
          if(!TextUtils.equals(ref, WXComponent.ROOT)) {
            addAction.setIndex(index);
          }
          WXSDKManager.getInstance().getWXRenderManager().postGraphicAction(pageId, addAction);
          WXSDKInstance instance = WXSDKManager.getInstance().getSDKInstance(pageId);
          if(instance != null){
            instance.removeInActiveAddElmentAction(ref);
          }
        }
        else {
          final BasicGraphicAction action = new GraphicActionLayout(pageId, ref, position, size);
          WXSDKManager.getInstance().getWXRenderManager().postGraphicAction(action.getPageId(), action);
        }
      }
    } catch (Exception e) {
      WXLogUtils.e("[WXBridgeManager] callLayout exception: ", e);
      e.printStackTrace();
    }

    return IWXBridge.INSTANCE_RENDERING;
  }

  public int callAppendTreeCreateFinish(String instanceId, String ref) {
    if (WXEnvironment.isApkDebugable() && BRIDGE_LOG_SWITCH) {
      mLodBuilder.append("[WXBridgeManager] callAppendTreeCreateFinish >>>> instanceId:").append(instanceId)
              .append(", ref:").append(ref);
      WXLogUtils.d(mLodBuilder.substring(0));
      mLodBuilder.setLength(0);
    }

    if (mDestroyedInstanceId != null && mDestroyedInstanceId.contains(instanceId)) {
      return IWXBridge.DESTROY_INSTANCE;
    }

    try {
      GraphicActionAppendTreeCreateFinish action = new GraphicActionAppendTreeCreateFinish(instanceId, ref);
      WXSDKManager.getInstance().getWXRenderManager().postGraphicAction(instanceId, action);
    } catch (Exception e) {
      WXLogUtils.e("[WXBridgeManager] callAppendTreeCreateFinish exception: ", e);
      e.printStackTrace();
    }

    return IWXBridge.INSTANCE_RENDERING;
  }

  public int callCreateFinish(String instanceId) {
    if (WXEnvironment.isApkDebugable() && BRIDGE_LOG_SWITCH) {
      mLodBuilder.append("[WXBridgeManager] callCreateFinish >>>> instanceId:").append(instanceId);
      WXLogUtils.d(mLodBuilder.substring(0));
      mLodBuilder.setLength(0);
    }

    if (mDestroyedInstanceId != null && mDestroyedInstanceId.contains(instanceId)) {
      return IWXBridge.DESTROY_INSTANCE;
    }

    try {
      long start = System.currentTimeMillis();
      WXSDKInstance instance = WXSDKManager.getInstance().getSDKInstance(instanceId);
      if (instance != null) {
        instance.firstScreenCreateInstanceTime(start);
        GraphicActionCreateFinish action = new GraphicActionCreateFinish(instanceId);
        WXSDKManager.getInstance().getWXRenderManager().postGraphicAction(instanceId, action);
      }
    } catch (Exception e) {
      WXLogUtils.e("[WXBridgeManager] callCreateFinish exception: ", e);
      e.printStackTrace();
    }

    return IWXBridge.INSTANCE_RENDERING;
  }

  public ContentBoxMeasurement getMeasurementFunc(String instanceId, long renderObjectPtr) {
    ContentBoxMeasurement contentBoxMeasurement = null;
    WXSDKInstance instance = WXSDKManager.getInstance().getSDKInstance(instanceId);
    if (instance != null) {
      contentBoxMeasurement = instance.getContentBoxMeasurement(renderObjectPtr);
    }
    return contentBoxMeasurement;
  }
  
  public void bindMeasurementToRenderObject(long ptr){
    mWXBridge.bindMeasurementToRenderObject(ptr);
  }


  /**
   * Native: Layout
   * @param instanceId
   * @return
   */
  @UiThread
  public boolean notifyLayout(String instanceId) {
    return mWXBridge.notifyLayout(instanceId);
  }

  @UiThread
  public void forceLayout(String instanceId) {
    mWXBridge.forceLayout(instanceId);
  }

  /**
   * native: OnInstanceClose
   * @param instanceId
   */
  public void onInstanceClose(String instanceId) {
    mWXBridge.onInstanceClose(instanceId);
  }

  /**
   * native: SetDefaultHeightAndWidthIntoRootDom
   * @param instanceId
   * @param defaultWidth
   * @param defaultHeight
   */
  public void setDefaultRootSize(final String instanceId, final float defaultWidth, final float defaultHeight, final boolean isWidthWrapContent, final boolean isHeightWrapContent) {
    mWXBridge.setDefaultHeightAndWidthIntoRootDom(instanceId, defaultWidth, defaultHeight, isWidthWrapContent, isHeightWrapContent);
  }

  public void setRenderContentWrapContentToCore(boolean wrap, final String instanceId) {
    mWXBridge.setRenderContainerWrapContent(wrap, instanceId);
  }

  public void setStyleWidth(String instanceId, String ref, float value) {
    mWXBridge.setStyleWidth(instanceId, ref, value);
  }

  public void setStyleHeight(String instanceId, String ref, float value) {
    mWXBridge.setStyleHeight(instanceId, ref, value);
  }

  public long[] getFirstScreenRenderTime(String instanceId) {
    return mWXBridge.getFirstScreenRenderTime(instanceId);
  }

  public long[] getRenderFinishTime(String instanceId) {
    return mWXBridge.getRenderFinishTime(instanceId);
  }

  public void setViewPortWidth(String instanceId, float value) {
    mWXBridge.setViewPortWidth(instanceId, value);
  }

  public void setMargin(String instanceId, String ref, CSSShorthand.EDGE edge, float value) {
    mWXBridge.setMargin(instanceId, ref, edge, value);
  }

  public void setPadding(String instanceId, String ref, CSSShorthand.EDGE edge, float value) {
    mWXBridge.setPadding(instanceId, ref, edge, value);
  }

  public void setPosition(String instanceId, String ref, CSSShorthand.EDGE edge, float value) {
    mWXBridge.setPosition(instanceId, ref, edge, value);
  }

  public void markDirty(String instanceId, String ref, boolean dirty) {
    mWXBridge.markDirty(instanceId, ref, dirty);
  }

  public int callHasTransitionPros(String instanceId, String ref, HashMap<String, String> styles) {
    WXComponent component = WXSDKManager.getInstance().getWXRenderManager().getWXComponent(instanceId, ref);
    if (null == component || null == component.getTransition() || null == component.getTransition().getProperties()) {
      return IWXBridge.DESTROY_INSTANCE;
    }

    for(String property : component.getTransition().getProperties()){
      if(styles.containsKey(property)){
        return IWXBridge.INSTANCE_RENDERING;
      }
    }
    return IWXBridge.INSTANCE_RENDERING_ERROR;
  }

  public void registerCoreEnv(String key, String value) {
    mWXBridge.registerCoreEnv(key, value);
  }

}
