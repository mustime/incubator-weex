/**
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

import android.content.Context;
import android.util.Log;

import com.taobao.weex.WXEnvironment;
import com.taobao.weex.WXSDKManager;
import com.taobao.weex.adapter.IWXUserTrackAdapter;
import com.taobao.weex.common.IWXBridge;
import com.taobao.weex.common.WXErrorCode;
import com.taobao.weex.dom.CSSShorthand;
import com.taobao.weex.layout.ContentBoxMeasurement;
import com.taobao.weex.utils.WXLogUtils;
import com.taobao.weex.utils.WXViewUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Communication interface for Java code and JavaScript code.
 */

public class WXBridge implements IWXBridge {

  private native void nativeInit(String width, String height);

  private native void nativeBindMeasurementToRenderObject(long ptr);

  private native void nativeSetRenderContainerWrapContent(boolean wrap, String instanceId);

  public native long[] nativeGetFirstScreenRenderTime(String instanceId);

  public native long[] nativeGetRenderFinishTime(String instanceId);

  private native void nativeSetDefaultHeightAndWidthIntoRootDom(String instanceId, float defaultWidth, float defaultHeight, boolean isWidthWrapContent, boolean isHeightWrapContent);

  private native void nativeOnInstanceClose(String instanceId);

  private native void nativeForceLayout(String instanceId);

  private native boolean nativeNotifyLayout(String instanceId);

  private native void nativeSetStyleWidth(String instanceId, String ref, float value);

  private native void nativeSetStyleHeight(String instanceId, String ref, float value);

  private native void nativeSetMargin(String instanceId, String ref, int edge, float value);

  private native void nativeSetPadding(String instanceId, String ref, int edge, float value);

  private native void nativeSetPosition(String instanceId, String ref, int edge, float value);

  private native void nativeMarkDirty(String instanceId, String ref, boolean dirty);

  private native void nativeRegisterCoreEnv(String key, String value);

  /**
   * update global config,
   * @param config params
   * */
  public native void nativeUpdateGlobalConfig(String config);

  public static final String TAG = "WXBridge";

  @Override
  public void init(Context context) {
    nativeInit(String.valueOf(WXViewUtils.getScreenWidth(context)), String.valueOf(WXViewUtils.getScreenHeight(context)));
  }

  @Override
  public int callCreateBody(String instanceId, String componentType, String ref,
                            HashMap<String, String> styles, HashMap<String, String> attributes, HashSet<String> events,
                            float[] margins, float[] paddings, float[] borders) {
    int errorCode = IWXBridge.INSTANCE_RENDERING;

    try {
      errorCode = WXBridgeManager.getInstance().callCreateBody(instanceId, componentType, ref,
              styles, attributes, events, margins, paddings, borders);
    } catch (Throwable e) {
      //catch everything during call native.
      if (WXEnvironment.isApkDebugable()) {
        WXLogUtils.e(TAG, "callCreateBody throw exception:" + e.getMessage());
      }
    }
    return errorCode;
  }

  @Override
  public int callAddElement(String instanceId, String componentType, String ref, int index, String parentRef,
                            HashMap<String, String> styles, HashMap<String, String> attributes, HashSet<String> events,
                            float[] margins, float[] paddings, float[] borders,  boolean willLayout) {
    int errorCode = IWXBridge.INSTANCE_RENDERING;

    try {
      errorCode = WXBridgeManager.getInstance().callAddElement(instanceId, componentType, ref, index, parentRef,
              styles, attributes, events, margins, paddings, borders, willLayout);
    } catch (Throwable e) {
      //catch everything during call native.
      if (WXEnvironment.isApkDebugable()) {
        e.printStackTrace();
        WXLogUtils.e(TAG, "callAddElement throw error:" + e.getMessage());
      }
    }
    return errorCode;
  }

  @Override
  public int callRemoveElement(String instanceId, String ref) {
    int errorCode = IWXBridge.INSTANCE_RENDERING;
    try {
      errorCode = WXBridgeManager.getInstance().callRemoveElement(instanceId, ref);
    } catch (Throwable e) {
      if (WXEnvironment.isApkDebugable()) {
        WXLogUtils.e(TAG, "callRemoveElement throw exception:" + e.getMessage());
      }
    }
    return errorCode;
  }

  @Override
  public int callMoveElement(String instanceId, String ref, String parentref, int index) {
    int errorCode = IWXBridge.INSTANCE_RENDERING;
    try {
      errorCode = WXBridgeManager.getInstance().callMoveElement(instanceId, ref, parentref, index);
    } catch (Throwable e) {
      if (WXEnvironment.isApkDebugable()) {
        WXLogUtils.e(TAG, "callMoveElement throw exception:" + e.getMessage());
      }
    }
    return errorCode;
  }

  @Override
  public int callUpdateStyle(String instanceId, String ref,
                             HashMap<String, Object> styles,
                             HashMap<String, String> paddings,
                             HashMap<String, String> margins,
                             HashMap<String, String> borders) {
    int errorCode = IWXBridge.INSTANCE_RENDERING;
    try {
      errorCode = WXBridgeManager.getInstance().callUpdateStyle(instanceId, ref, styles, paddings, margins, borders);
    } catch (Throwable e) {
      //catch everything during call native.
      if (WXEnvironment.isApkDebugable()) {
        WXLogUtils.e(TAG, "callUpdateStyle throw exception:" + e.getMessage());
      }
    }
    return errorCode;
  }

  @Override
  public int callUpdateAttrs(String instanceId, String ref, HashMap<String, String> attrs) {
    int errorCode = IWXBridge.INSTANCE_RENDERING;
    try {
      errorCode = WXBridgeManager.getInstance().callUpdateAttrs(instanceId, ref, attrs);
    } catch (Throwable e) {
      //catch everything during call native.
      if (WXEnvironment.isApkDebugable()) {
        WXLogUtils.e(TAG, "callUpdateAttr throw exception:" + e.getMessage());
      }
    }
    return errorCode;
  }

  @Override
  public int callLayout(String instanceId, String ref, int top, int bottom, int left, int right, int height, int width, int index) {
    int errorCode = IWXBridge.INSTANCE_RENDERING;
    try {
      errorCode = WXBridgeManager.getInstance().callLayout(instanceId, ref, top, bottom, left, right, height, width, index);
    } catch (Throwable e) {
      //catch everything during call native.
      if (WXEnvironment.isApkDebugable()) {
        WXLogUtils.e(TAG, "callLayout throw exception:" + e.getMessage());
      }
    }
    return errorCode;
  }

  @Override
  public int callCreateFinish(String instanceId) {
    int errorCode = IWXBridge.INSTANCE_RENDERING;
    try {
      errorCode = WXBridgeManager.getInstance().callCreateFinish(instanceId);
    } catch (Throwable e) {
      WXLogUtils.e(TAG, "callCreateFinish throw exception:" + e.getMessage());
    }
    return errorCode;
  }

  @Override
  public int callAppendTreeCreateFinish(String instanceId, String ref) {
    int errorCode = IWXBridge.INSTANCE_RENDERING;
    try {
      errorCode = WXBridgeManager.getInstance().callAppendTreeCreateFinish(instanceId, ref);
    } catch (Throwable e) {
      WXLogUtils.e(TAG, "callAppendTreeCreateFinish throw exception:" + e.getMessage());
    }
    return errorCode;
  }

  @Override
  public int callHasTransitionPros(String instanceId, String ref, HashMap<String, String> styles) {
    int errorCode = IWXBridge.INSTANCE_RENDERING;
    try {
      errorCode = WXBridgeManager.getInstance().callHasTransitionPros(instanceId, ref, styles);
    } catch (Throwable e) {
      if (WXEnvironment.isApkDebugable()) {
        WXLogUtils.e(TAG, "callHasTransitionPros throw exception:" + e.getMessage());
      }
    }
    return errorCode;
  }

  @Override
  public ContentBoxMeasurement getMeasurementFunc(String instanceId, long renderObjectPtr) {
    ContentBoxMeasurement obj = null;
    try {
      obj = WXBridgeManager.getInstance().getMeasurementFunc(instanceId, renderObjectPtr);
    } catch (Throwable e) {
      if (WXEnvironment.isApkDebugable()) {
        WXLogUtils.e(TAG, "getMeasurementFunc throw exception:" + e.getMessage());
      }
    }
    return obj;
  }

  @Override
  public void bindMeasurementToRenderObject(long ptr){
    nativeBindMeasurementToRenderObject(ptr);
  }

  @Override
  public void setRenderContainerWrapContent(boolean wrap, String instanceId) {
    nativeSetRenderContainerWrapContent(wrap, instanceId);
  }

  @Override
  public long[] getFirstScreenRenderTime(String instanceId) {
    return nativeGetFirstScreenRenderTime(instanceId);
  }

  @Override
  public long[] getRenderFinishTime(String instanceId) {
    return nativeGetRenderFinishTime(instanceId);
  }

  @Override
  public void setDefaultHeightAndWidthIntoRootDom(String instanceId, float defaultWidth, float defaultHeight, boolean isWidthWrapContent, boolean isHeightWrapContent) {
    nativeSetDefaultHeightAndWidthIntoRootDom(instanceId, defaultWidth, defaultHeight, isWidthWrapContent, isHeightWrapContent);
  }

  @Override
  public void onInstanceClose(String instanceId) {
    nativeOnInstanceClose(instanceId);
  }

  @Override
  public void forceLayout(String instanceId) {
    nativeForceLayout(instanceId);
  }

  @Override
  public boolean notifyLayout(String instanceId) {
    return nativeNotifyLayout(instanceId);
  }

  @Override
  public void setStyleWidth(String instanceId, String ref, float value) {
    nativeSetStyleWidth(instanceId, ref, value);
  }

  @Override
  public void setMargin(String instanceId, String ref, CSSShorthand.EDGE edge, float value) {
    nativeSetMargin(instanceId, ref, edge.ordinal(), value);
  }

  @Override
  public void setPadding(String instanceId, String ref, CSSShorthand.EDGE edge, float value) {
    nativeSetPadding(instanceId, ref, edge.ordinal(), value);
  }

  @Override
  public void setPosition(String instanceId, String ref, CSSShorthand.EDGE edge, float value) {
    nativeSetPosition(instanceId, ref, edge.ordinal(), value);
  }

  @Override
  public void markDirty(String instanceId, String ref, boolean dirty) {
    nativeMarkDirty(instanceId, ref, dirty);
  }

  @Override
  public void setStyleHeight(String instanceId, String ref, float value) {
    nativeSetStyleHeight(instanceId, ref, value);
  }

  @Override
  public void registerCoreEnv(String key, String value) {
    nativeRegisterCoreEnv(key, value);
  }

  public void reportNativeInitStatus(String statusCode, String errorMsg) {
    if (WXErrorCode.WX_JS_FRAMEWORK_INIT_SINGLE_PROCESS_SUCCESS.getErrorCode().equals(statusCode)
            || WXErrorCode.WX_JS_FRAMEWORK_INIT_FAILED.getErrorCode().equals(statusCode)) {
      IWXUserTrackAdapter userTrackAdapter = WXSDKManager.getInstance().getIWXUserTrackAdapter();
      if (userTrackAdapter != null) {
        Map<String, Serializable> params = new HashMap<>(3);
        params.put(IWXUserTrackAdapter.MONITOR_ERROR_CODE, statusCode);
        params.put(IWXUserTrackAdapter.MONITOR_ARG, "InitFrameworkNativeError");
        params.put(IWXUserTrackAdapter.MONITOR_ERROR_MSG, errorMsg);
        Log.e("Dyy", "reportNativeInitStatus is running and errorCode is " + statusCode + " And errorMsg is " + errorMsg);
        userTrackAdapter.commit(null, null, IWXUserTrackAdapter.INIT_FRAMEWORK, null, params);
      }

      return;
    }

    for (WXErrorCode e : WXErrorCode.values()) {
      if (e.getErrorType().equals(WXErrorCode.ErrorType.NATIVE_ERROR)
              && e.getErrorCode().equals(statusCode)) {
        WXLogUtils.e(TAG, "initFramework " + errorMsg);
        break;
      }
    }
  }
}
