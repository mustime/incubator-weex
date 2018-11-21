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
package cn.xxzhushou.xmod.wxui.ui.action;

import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.WorkerThread;

import cn.xxzhushou.xmod.wxui.WXSDKInstance;
import cn.xxzhushou.xmod.wxui.WXSDKManager;
import cn.xxzhushou.xmod.wxui.dom.transition.WXTransition;
import cn.xxzhushou.xmod.wxui.ui.component.WXComponent;
import cn.xxzhushou.xmod.wxui.ui.component.WXVContainer;
import cn.xxzhushou.xmod.wxui.utils.WXLogUtils;

import java.util.Map;
import java.util.Set;

public class GraphicActionAddElement extends GraphicActionAbstractAddElement {

  private WXVContainer parent;
  private WXComponent child;
  private GraphicPosition layoutPosition;
  private GraphicSize layoutSize;
  private boolean isJSCreateFinish = false;

  public GraphicActionAddElement(String pageId, String ref,
                                 String componentType, String parentRef,
                                 int index,
                                 Map<String, String> style,
                                 Map<String, String> attributes,
                                 Set<String> events,
                                 float[] margins,
                                 float[] paddings,
                                 float[] borders) {
    super(pageId, ref);
    this.mComponentType = componentType;
    this.mParentRef = parentRef;
    this.mIndex = index;
    this.mStyle = style;
    this.mAttributes = attributes;
    this.mEvents = events;
    this.mPaddings = paddings;
    this.mMargins = margins;
    this.mBorders = borders;

    WXSDKInstance instance = WXSDKManager.getInstance().getWXRenderManager().getWXSDKInstance(getPageId());
    if (instance == null || instance.getContext() == null) {
      return;
    }

    try {
      parent = (WXVContainer) WXSDKManager.getInstance().getWXRenderManager()
          .getWXComponent(getPageId(), mParentRef);
      BasicComponentData basicComponentData = new BasicComponentData(ref, mComponentType,
          mParentRef);
      child = createComponent(instance, parent, basicComponentData);
      child.setTransition(WXTransition.fromMap(child.getStyles(), child));
      isJSCreateFinish = instance.isJSCreateFinish;

      if (child == null || parent == null) {
        return;
      }
    }catch (ClassCastException e){
      e.printStackTrace();
    }

  }

  @RestrictTo(Scope.LIBRARY)
  @WorkerThread
  public void setSize(GraphicSize graphicSize){
    this.layoutSize = graphicSize;
  }

  @RestrictTo(Scope.LIBRARY)
  @WorkerThread
  public void setPosition(GraphicPosition position){
    this.layoutPosition = position;
  }

  @RestrictTo(Scope.LIBRARY)
  @WorkerThread
  public void setIndex(int index){
    mIndex = index;
  }

  @Override
  public void executeAction() {
    super.executeAction();
    try {
      parent.addChild(child, mIndex);
      parent.createChildViewAt(mIndex);

      if(layoutPosition !=null && layoutSize != null) {
        child.setDemission(layoutSize, layoutPosition);
      }
      child.applyLayoutAndEvent(child);

      child.bindData(child);
      WXSDKInstance instance = WXSDKManager.getInstance().getWXRenderManager().getWXSDKInstance(getPageId());
      if (null!=instance){
        instance.onElementChange(isJSCreateFinish);
       // instance.setma
      }
    } catch (Exception e) {
      WXLogUtils.e("add component failed.", e);
    }
  }
}