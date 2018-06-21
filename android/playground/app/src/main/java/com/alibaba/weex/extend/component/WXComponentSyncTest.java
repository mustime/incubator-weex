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
package com.alibaba.weex.extend.component;

import cn.xxzhushou.xmod.wxui.WXSDKInstance;
import cn.xxzhushou.xmod.wxui.annotation.JSMethod;
import cn.xxzhushou.xmod.wxui.ui.action.BasicComponentData;
import cn.xxzhushou.xmod.wxui.ui.component.WXDiv;
import cn.xxzhushou.xmod.wxui.ui.component.WXVContainer;
import cn.xxzhushou.xmod.wxui.utils.WXLogUtils;

/**
 * Created by zhengshihan on 2016/12/30.
 */

public class WXComponentSyncTest extends WXDiv {

    public WXComponentSyncTest(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
    }

    @JSMethod (uiThread = false)
    public void testSyncCall(){
        WXLogUtils.d("11WXComponentSyncTest :"+ Thread.currentThread().getName());
    }

    @JSMethod (uiThread = true)
    public void testAsyncCall(){
        WXLogUtils.e("22WXComponentSynTest :"+ Thread.currentThread().getName() );
    }
}