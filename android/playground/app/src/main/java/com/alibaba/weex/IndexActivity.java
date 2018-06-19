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
package com.alibaba.weex;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.client.android.CaptureActivity;
import com.taobao.weex.IWXRenderListener;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.WXSDKManager;
import com.taobao.weex.ui.WXRenderManager;
import com.taobao.weex.utils.WXLogUtils;

public class IndexActivity extends WXBaseActivity implements IWXRenderListener {

  private static final String TAG = "IndexActivity";
  private static final int CAMERA_PERMISSION_REQUEST_CODE = 0x1;
  private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 0x2;
  private static final String DEFAULT_IP = "your_current_IP";
  private static String sCurrentIp = DEFAULT_IP; // your_current_IP

  private ProgressBar mProgressBar;
  private TextView mTipView;

  private BroadcastReceiver mReloadReceiver;

  ViewGroup mContainer = null;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_index);
    mContainer = (ViewGroup)findViewById(R.id.index_container);

//    mProgressBar = (ProgressBar) findViewById(R.id.index_progressBar);
//    mTipView = (TextView) findViewById(R.id.index_tip);
//    mProgressBar.setVisibility(View.VISIBLE);
//    mTipView.setVisibility(View.VISIBLE);
//
//
//    if (!WXSoInstallMgrSdk.isCPUSupport()) {
//      mProgressBar.setVisibility(View.INVISIBLE);
//      mTipView.setText(R.string.cpu_not_support_tip);
//      return;
//    }
//
//    if (TextUtils.equals(sCurrentIp, DEFAULT_IP)) {
//      renderPage(WXFileUtils.loadAsset("landing.weex.js", this), getIndexUrl());
//    } else {
//      renderPageByURL(getIndexUrl());
//    }
//
//
//    mReloadReceiver = new BroadcastReceiver() {
//      @Override
//      public void onReceive(Context context, Intent intent) {
//        createWeexInstance();
//        if (TextUtils.equals(sCurrentIp, DEFAULT_IP)) {
//          renderPage(WXFileUtils.loadAsset("landing.weex.js", getApplicationContext()), getIndexUrl());
//        } else {
//          renderPageByURL(getIndexUrl());
//        }
//        mProgressBar.setVisibility(View.VISIBLE);
//      }
//    };
//
//    LocalBroadcastManager.getInstance(this).registerReceiver(mReloadReceiver, new IntentFilter(WXSDKEngine.JS_FRAMEWORK_RELOAD));
//
//    CheckForUpdateUtil.checkForUpdate(this);

    WXSDKInstance mInstance = new WXSDKInstance(this);
    mInstance.setInstanceViewPortWidth(750);
    mInstance.registerRenderListener(this);

    WXRenderManager manager = WXSDKManager.getInstance().getWXRenderManager();
    manager.registerInstance(mInstance);
//    RenderContainer renderContainer = new RenderContainer(this);
//    mInstance.setRenderContainer(renderContainer);
    mInstance.ensureRenderArchor();
//    mContainer.addView(renderContainer);
    mInstance.mRendered = true;

    boolean ret = nativeCreateRoot(mInstance.getInstanceId(), "/sdcard/input.json", "/sdcard/input_style.json");
    WXLogUtils.e(TAG, "FUCK ret = " + ret);
  }

  private native boolean nativeCreateRoot(String instanceId, String layoutPath, String stylePath);

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (TextUtils.equals(sCurrentIp, DEFAULT_IP)) {
      getMenuInflater().inflate(R.menu.main_scan, menu);
    } else {
      getMenuInflater().inflate(R.menu.main, menu);
    }
    return true;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      startActivity(new Intent(this, CaptureActivity.class));
    } else if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
    } else {
      Toast.makeText(this, "request camara permission fail!", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    LocalBroadcastManager.getInstance(this).unregisterReceiver(mReloadReceiver);
  }

  @Override
  public void onPause() {
    super.onPause();
//    WXSDKManager.getInstance().takeJSHeapSnapshot("/sdcard/weex/");
  }

  private static String getIndexUrl() {
    return "http://" + sCurrentIp + ":12580/examples/build/index.js";
  }

  @Override
  public void onViewCreated(WXSDKInstance instance, View view) {
    WXLogUtils.d(TAG, "onViewCreated");
    if(view.getParent() == null) {
      mContainer.addView(view);
    }
    mContainer.requestLayout();
  }

  @Override
  public void onRenderSuccess(WXSDKInstance instance, int width, int height) {

  }

  @Override
  public void onRefreshSuccess(WXSDKInstance instance, int width, int height) {

  }

  @Override
  public void onException(WXSDKInstance instance, String errCode, String msg) {

  }
}

