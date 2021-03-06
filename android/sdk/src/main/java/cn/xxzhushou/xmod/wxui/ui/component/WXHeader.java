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

import cn.xxzhushou.xmod.wxui.WXSDKInstance;
import cn.xxzhushou.xmod.wxui.annotation.Component;
import cn.xxzhushou.xmod.wxui.common.Constants;
import cn.xxzhushou.xmod.wxui.ui.action.BasicComponentData;
import cn.xxzhushou.xmod.wxui.ui.component.list.WXCell;

/**
 * The same as sticky cell
 */
@Component(lazyload = false)
public class WXHeader extends WXCell {

  @Deprecated
  public WXHeader(WXSDKInstance instance, WXVContainer parent, String instanceId, boolean isLazy, BasicComponentData basicComponentData) {
    this(instance, parent, isLazy, basicComponentData);
  }

  public WXHeader(WXSDKInstance instance, WXVContainer parent, boolean lazy, BasicComponentData basicComponentData) {
    super(instance, parent, lazy, basicComponentData);
    String parantType = parent.getComponentType();
    if(WXBasicComponentType.LIST.equals(parantType)
            || WXBasicComponentType.RECYCLE_LIST.equals(parantType)){
      setSticky(Constants.Value.STICKY);
    }
  }

  @Override
  public boolean isLazy() {
    return false;
  }

  @Override
  public boolean isSticky() {
    return true;
  }

  @Override
  public boolean canRecycled() {
    return false;
  }
}
