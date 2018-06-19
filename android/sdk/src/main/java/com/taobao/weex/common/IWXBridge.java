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
package com.taobao.weex.common;

import android.content.Context;

import com.taobao.weex.dom.CSSShorthand;
import com.taobao.weex.layout.ContentBoxMeasurement;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Bridge interface, native bridge and debug bridge both need to implement this interface
 */
public interface IWXBridge extends IWXObject {

  int DESTROY_INSTANCE = -1;
  int INSTANCE_RENDERING = 1;
  int INSTANCE_RENDERING_ERROR = 0;

  void init(Context context);

  int callCreateBody(String instanceId, String componentType, String ref,
                            HashMap<String, String> styles, HashMap<String, String> attributes, HashSet<String> events,
                            float[] margins, float[] paddings, float[] borders);

  int callAddElement(String instanceId, String componentType, String ref, int index, String parentRef,
                            HashMap<String, String> styles, HashMap<String, String> attributes, HashSet<String> events,
                            float[] margins, float[] paddings, float[] borders, boolean willLayout);

  int callRemoveElement(String instanceId, String ref);

  int callMoveElement(String instanceId, String ref, String parentref, int index);

  int callUpdateStyle(String instanceId, String ref,
                             HashMap<String, Object> styles,
                             HashMap<String, String> paddings,
                             HashMap<String, String> margins,
                             HashMap<String, String> borders);

  int callUpdateAttrs(String instanceId, String ref,
                      HashMap<String, String> attrs);

  int callLayout(String instanceId, String ref, int top, int bottom, int left, int right, int height, int width, int index);

  int callCreateFinish(String instanceId);

  int callAppendTreeCreateFinish(String instanceId, String ref);

  int callHasTransitionPros(String instanceId, String ref, HashMap<String, String> styles);

  ContentBoxMeasurement getMeasurementFunc(String instanceId, long renderObjectPtr);

  void bindMeasurementToRenderObject(long ptr);

  void setRenderContainerWrapContent(boolean wrap, String instanceId);

  long[] getFirstScreenRenderTime(String instanceId);

  long[] getRenderFinishTime(String instanceId);

  void setDefaultHeightAndWidthIntoRootDom(String instanceId, float defaultWidth, float defaultHeight, boolean isWidthWrapContent, boolean isHeightWrapContent);

  void onInstanceClose(String instanceId);

  void forceLayout(String instanceId);

  boolean notifyLayout(String instanceId);

  void setStyleWidth(String instanceId, String ref, float value);

  void setStyleHeight(String instanceId, String ref, float value);

  void setMargin(String instanceId, String ref, CSSShorthand.EDGE edge, float value);

  void setPadding(String instanceId, String ref, CSSShorthand.EDGE edge, float value);

  void setPosition(String instanceId, String ref, CSSShorthand.EDGE edge, float value);

  void markDirty(String instanceId, String ref, boolean dirty);

  void registerCoreEnv(String key, String value);

  void setViewPortWidth(String instanceId, float value);

  void reportNativeInitStatus(String statusCode, String errorMsg);
}
