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
package cn.xxzhushou.xmod.wxui.ui.component;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import cn.xxzhushou.xmod.wxui.WXSDKInstance;
import cn.xxzhushou.xmod.wxui.annotation.Component;
import cn.xxzhushou.xmod.wxui.common.Constants;
import cn.xxzhushou.xmod.wxui.ui.action.BasicComponentData;
import cn.xxzhushou.xmod.wxui.ui.component.list.WXListComponent;
import cn.xxzhushou.xmod.wxui.ui.view.WXFrameLayout;
import cn.xxzhushou.xmod.wxui.ui.view.WXRefreshLayout;
import cn.xxzhushou.xmod.wxui.ui.view.refresh.core.WXSwipeLayout;
import cn.xxzhushou.xmod.wxui.ui.view.refresh.wrapper.BaseBounceView;
import cn.xxzhushou.xmod.wxui.utils.WXUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * div component
 */
@Component(lazyload = false)
public class WXRefresh extends WXBaseRefresh implements WXSwipeLayout.WXOnRefreshListener{

  public static final String HIDE = "hide";

  @Deprecated
  public WXRefresh(WXSDKInstance instance, WXVContainer parent, String instanceId, boolean isLazy, BasicComponentData basicComponentData) {
    this(instance, parent, isLazy, basicComponentData);
  }

  public WXRefresh(WXSDKInstance instance, WXVContainer parent, boolean lazy, BasicComponentData basicComponentData) {
    super(instance, parent, lazy, basicComponentData);
  }

  @Override
  protected WXFrameLayout initComponentHostView(@NonNull Context context) {
    return new WXRefreshLayout(context);
  }

  @Override
  public boolean canRecycled() {
    return false;
  }

  @Override
  public void onRefresh() {
    if(isDestoryed()){
      return;
    }
    
    if (getEvents().contains(Constants.Event.ONREFRESH)) {
      fireEvent(Constants.Event.ONREFRESH);
    }
  }

  @Override
  public int getLayoutTopOffsetForSibling() {
    //offset siblings
    return getParent() instanceof Scrollable ? -Math.round(getLayoutHeight()) : 0;
  }

  @Override
  public void onPullingDown(float dy, int pullOutDistance, float viewHeight) {
    if (getEvents() != null && getEvents().contains(Constants.Event.ONPULLING_DOWN)) {
      Map<String, Object> data = new HashMap<>();
      data.put(Constants.Name.DISTANCE_Y, dy);
      data.put(Constants.Name.PULLING_DISTANCE, pullOutDistance);
      data.put(Constants.Name.VIEW_HEIGHT, viewHeight);
      fireEvent(Constants.Event.ONPULLING_DOWN, data);
    }
  }

  @Override
  protected boolean setProperty(String key, Object param) {
    switch (key) {
      case Constants.Name.DISPLAY:
        String display = WXUtils.getString(param,null);
        if (display != null)
          setDisplay(display);
        return true;
    }
    return super.setProperty(key,param);
  }

  public void setDisplay(String display) {
    if (!TextUtils.isEmpty(display)) {
      if (display.equals(HIDE)) {
        if (getParent() instanceof WXListComponent || getParent() instanceof WXScroller) {
          if (((BaseBounceView)getParent().getHostView()).getSwipeLayout().isRefreshing()) {
            ((BaseBounceView) getParent().getHostView()).finishPullRefresh();
            ((BaseBounceView) getParent().getHostView()).onRefreshingComplete();
          }
        }
      }
    }
  }
}
