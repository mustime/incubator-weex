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
package cn.xxzhushou.xmod.wxui.ui.config;

import android.content.res.AssetManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cn.xxzhushou.xmod.wxui.WXEnvironment;
import cn.xxzhushou.xmod.wxui.WXSDKEngine;
import cn.xxzhushou.xmod.wxui.bridge.JavascriptInvokable;
import cn.xxzhushou.xmod.wxui.utils.WXFileUtils;
import cn.xxzhushou.xmod.wxui.utils.WXLogUtils;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by furture on 2018/2/7.
 */

public class AutoScanConfigRegister {


    public static final  String TAG  = "WeexScanConfigRegister";

    private static ConcurrentLinkedQueue<JavascriptInvokable> autoLoadClass  = new ConcurrentLinkedQueue<>();

    /**
     * pre load module class and methods when so&jsf init
     **/
    public static void preLoad(JavascriptInvokable invokable){
        if(invokable instanceof ConfigModuleFactory){
            return;
        }
        if(invokable instanceof ConfigComponentHolder){
            return;
        }
        autoLoadClass.add(invokable);
    }

    /**
     * auto scan config files and do auto config from files, none need center register
     * */
    public static void doScanConfig(){
       Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                doScanConfigSync();
                JavascriptInvokable invokable = autoLoadClass.poll();
                int count = 0;
                while (invokable != null){
                    invokable.getMethods();
                    invokable = autoLoadClass.poll();
                    count++;
                }
                if(WXEnvironment.isApkDebugable()){
                    WXLogUtils.d(TAG, "auto preload class's methods count " + count);
                }
            }
        });
       thread.setName("AutoScanConfigRegister");
       thread.start();
    }

    private static void doScanConfigSync(){
        if(WXEnvironment.sApplication == null){
            return;
        }
        try{
            AssetManager assetManager = WXEnvironment.sApplication.getApplicationContext().getAssets();

            String[] configFiles = new String[0];
            try {
                configFiles = assetManager.list("");
            } catch (IOException e) {
                WXLogUtils.e(TAG, e);
            }
            if(configFiles == null || configFiles.length == 0){
                return;
            }
            for(String configFile : configFiles){
                if(TextUtils.isEmpty(configFile)){
                    continue;
                }
                if(configFile.startsWith("weex_config_") && configFile.endsWith(".json")){
                    String name = configFile;
                    if(TextUtils.isEmpty(name)){
                        return;
                    }
                    try {
                        String config = WXFileUtils.loadAsset(name, WXEnvironment.getApplication());
                        if (TextUtils.isEmpty(config)) {
                            continue;
                        }
                        if (WXEnvironment.isApkDebugable()) {
                            WXLogUtils.d(TAG, configFile + " find config " + config);
                        }
                        JSONObject object = JSON.parseObject(config);
                        if (object.containsKey("modules")) {
                            JSONArray array = object.getJSONArray("modules");
                            for (int i = 0; i < array.size(); i++) {
                                ConfigModuleFactory configModuleFactory = ConfigModuleFactory.fromConfig(array.getJSONObject(i));
                                if (configModuleFactory == null) {
                                    continue;
                                }
                                WXSDKEngine.registerModule(configModuleFactory.getName(), configModuleFactory, false);
                            }
                        }

                        if (object.containsKey("components")) {
                            JSONArray array = object.getJSONArray("components");
                            for (int i = 0; i < array.size(); i++) {
                                ConfigComponentHolder configComponentHolder = ConfigComponentHolder.fromConfig(array.getJSONObject(i));
                                if (configComponentHolder == null) {
                                    return;
                                }
                                WXSDKEngine.registerComponent(configComponentHolder, configComponentHolder.isAppendTree(), configComponentHolder.getType());
                            }
                        }
                    }catch (Exception e){
                        WXLogUtils.e(TAG, e);
                    }
                }
            }
        }catch (Exception eout){
            WXLogUtils.e(TAG, eout);
        }
    }

}
