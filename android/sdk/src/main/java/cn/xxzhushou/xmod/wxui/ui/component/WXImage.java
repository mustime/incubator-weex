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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import cn.xxzhushou.xmod.wxui.WXEnvironment;
import cn.xxzhushou.xmod.wxui.WXSDKInstance;
import cn.xxzhushou.xmod.wxui.adapter.IWXImgLoaderAdapter;
import cn.xxzhushou.xmod.wxui.adapter.URIAdapter;
import cn.xxzhushou.xmod.wxui.annotation.Component;
import cn.xxzhushou.xmod.wxui.annotation.JSMethod;
import cn.xxzhushou.xmod.wxui.bridge.JSCallback;
import cn.xxzhushou.xmod.wxui.common.Constants;
import cn.xxzhushou.xmod.wxui.common.WXImageSharpen;
import cn.xxzhushou.xmod.wxui.common.WXImageStrategy;
import cn.xxzhushou.xmod.wxui.common.WXRuntimeException;
import cn.xxzhushou.xmod.wxui.ui.ComponentCreator;
import cn.xxzhushou.xmod.wxui.ui.action.BasicComponentData;
import cn.xxzhushou.xmod.wxui.ui.view.WXImageView;
import cn.xxzhushou.xmod.wxui.ui.view.border.BorderDrawable;
import cn.xxzhushou.xmod.wxui.utils.ImageDrawable;
import cn.xxzhushou.xmod.wxui.utils.ImgURIUtil;
import cn.xxzhushou.xmod.wxui.utils.SingleFunctionParser;
import cn.xxzhushou.xmod.wxui.utils.WXDomUtils;
import cn.xxzhushou.xmod.wxui.utils.WXLogUtils;
import cn.xxzhushou.xmod.wxui.utils.WXUtils;
import cn.xxzhushou.xmod.wxui.utils.WXViewToImageUtil;
import cn.xxzhushou.xmod.wxui.utils.WXViewUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Image component
 */
@Component(lazyload = false)
public class WXImage extends WXComponent<ImageView> {

  public static final String SUCCEED = "success";
  public static final String ERRORDESC = "errorDesc";
  private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 0x2;

  private String mSrc;
  private int mBlurRadius;
  private boolean mAutoRecycle = true;

  private static SingleFunctionParser.FlatMapper<Integer> BLUR_RADIUS_MAPPER = new SingleFunctionParser.FlatMapper<Integer>() {
    @Override
    public Integer map(String raw) {
      return WXUtils.getInteger(raw,0);
    }
  };

  public static class Creator implements ComponentCreator {
    @Override
    public WXComponent createInstance(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) throws IllegalAccessException, InvocationTargetException, InstantiationException {
      return new WXImage(instance, parent, basicComponentData);
    }
  }

  @Deprecated
  public WXImage(WXSDKInstance instance, WXVContainer parent, String instanceId, boolean isLazy, BasicComponentData basicComponentData) {
    this(instance, parent, basicComponentData);
  }

  public WXImage(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
    super(instance, parent, basicComponentData);
  }

  @Override
  protected ImageView initComponentHostView(@NonNull Context context) {
    WXImageView view = new WXImageView(context);
    view.setScaleType(ScaleType.FIT_XY);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      view.setCropToPadding(true);
    }
    view.holdComponent(this);
    return view;
  }

  @Override
  protected boolean setProperty(String key, Object param) {
    switch (key) {
      case Constants.Name.RESIZE_MODE:
        String resizeMode = WXUtils.getString(param, null);
        if (resizeMode != null) { setResizeMode(resizeMode); }
        return true;
      case Constants.Name.RESIZE:
        String resize = WXUtils.getString(param, null);
        if (resize != null) { setResize(resize); }
        return true;
      case Constants.Name.SRC:
        String src = WXUtils.getString(param, null);
        if (src != null) { setSrc(src); }
        return true;
      case Constants.Name.IMAGE_QUALITY:
        return true;
      case Constants.Name.AUTO_RECYCLE:
        mAutoRecycle = WXUtils.getBoolean(param, mAutoRecycle);
        return true;
      case Constants.Name.FILTER:
        int blurRadius = 0;
        if (param != null && param instanceof String) {
          blurRadius = parseBlurRadius((String)param);
        }
        if (!TextUtils.isEmpty(this.mSrc)) {
          setBlurRadius(this.mSrc, blurRadius);
        }
        return true;
      default:
        return super.setProperty(key, param);
    }
  }

  @Override
  public void refreshData(WXComponent component) {
    super.refreshData(component);
    if (component instanceof WXImage) {
      setSrc(component.getAttrs().getImageSrc());
    }
  }

  public void setResizeMode(String resizeMode) {
    (getHostView()).setScaleType(getResizeMode(resizeMode));
  }

  private ScaleType getResizeMode(String resizeMode) {
    ScaleType scaleType = ScaleType.FIT_XY;
    if (TextUtils.isEmpty(resizeMode)) {
      return scaleType;
    }

    switch (resizeMode) {
      case "cover":
        scaleType = ScaleType.CENTER_CROP;
        break;
      case "contain":
        scaleType = ScaleType.FIT_CENTER;
        break;
      case "stretch":
        scaleType = ScaleType.FIT_XY;
        break;
      default:
        break;
    }
    return scaleType;
  }

  public void setResize(String resize) {
    (getHostView()).setScaleType(getResizeMode(resize));
  }

  /**
   * Process local scheme, load drawable.
   * @param rewrited
   */
  private void setLocalSrc(Uri rewrited) {
    ImageView imageView;
    Drawable localDrawable = ImgURIUtil.getDrawableFromLoaclSrc(getContext(), rewrited);
    if (localDrawable != null && (imageView = getHostView()) != null) {
      imageView.setImageDrawable(localDrawable);
    }
  }

  public void setSrc(String src) {
    if (src == null) {
      return;
    }

    ImageView image = getHostView();
    if("".equals(src) && image != null){
      image.setImageDrawable(null);
      return;
    }

    if(image != null){
      if(image.getDrawable() != null){
        image.setImageDrawable(null);
      }
    }


    this.mSrc = src;
    WXSDKInstance instance = getInstance();
    Uri rewrited = instance.rewriteUri(Uri.parse(src), URIAdapter.IMAGE);

    if (Constants.Scheme.LOCAL.equals(rewrited.getScheme())) {
      setLocalSrc(rewrited);
    } else {
      int blur = 0;
      String blurStr = getStyles().getBlur();
      blur = parseBlurRadius(blurStr);
      setRemoteSrc(rewrited, blur);
    }
  }

  private void setBlurRadius(@NonNull String src, int blurRadius) {
    if(getInstance() != null && blurRadius != mBlurRadius) {
      Uri parsedUri = getInstance().rewriteUri(Uri.parse(src), URIAdapter.IMAGE);
      if(!Constants.Scheme.LOCAL.equals(parsedUri.getScheme())) {
        setRemoteSrc(parsedUri,blurRadius);
      }
    }
  }

  private int parseBlurRadius(@Nullable String rawRadius) {
    if(rawRadius == null) {
      return 0;
    }
    SingleFunctionParser<Integer> parser = new SingleFunctionParser<Integer>(rawRadius,BLUR_RADIUS_MAPPER);
    List<Integer> list = null;
    try {
      list = parser.parse("blur");
    }catch (Exception e) {
      return 0;
    }
    if(list == null || list.isEmpty()) {
      return 0;
    }
    return list.get(0);
  }

  @Override
  public void recycled() {
    super.recycled();

    if (getInstance().getImgLoaderAdapter() != null) {
      getInstance().getImgLoaderAdapter().setImage(null, mHost,
              null, null);
    } else {
      if (WXEnvironment.isApkDebugable()) {
        throw new WXRuntimeException("getImgLoaderAdapter() == null");
      }
      WXLogUtils.e("Error getImgLoaderAdapter() == null");
    }
  }

  public void autoReleaseImage(){
    if(mAutoRecycle){
      if(getHostView() != null){
        if (getInstance().getImgLoaderAdapter() != null) {
          getInstance().getImgLoaderAdapter().setImage(null, mHost, null, null);
        }
      }
    }
  }

  public void autoRecoverImage() {
    if(mAutoRecycle) {
      setSrc(mSrc);
    }
  }

  private void setRemoteSrc(Uri rewrited,int blurRadius) {

    WXImageStrategy imageStrategy = new WXImageStrategy();
    imageStrategy.isClipping = true;

    WXImageSharpen imageSharpen = getAttrs().getImageSharpen();
    imageStrategy.isSharpen = imageSharpen == WXImageSharpen.SHARPEN;

    imageStrategy.blurRadius = Math.max(0, blurRadius);
    this.mBlurRadius = blurRadius;

    imageStrategy.setImageListener(new WXImageStrategy.ImageListener() {
      @Override
      public void onImageFinish(String url, ImageView imageView, boolean result, Map extra) {
        if (getEvents().contains(Constants.Event.ONLOAD)) {
          Map<String, Object> params = new HashMap<String, Object>();
          Map<String, Object> size = new HashMap<>(2);
          if (imageView != null && imageView instanceof Measurable) {
            size.put("naturalWidth", ((Measurable) imageView).getNaturalWidth());
            size.put("naturalHeight", ((Measurable) imageView).getNaturalHeight());
          } else {
            size.put("naturalWidth", 0);
            size.put("naturalHeight", 0);
          }

          if (containsEvent(Constants.Event.ONLOAD)) {
            params.put("success", result);
            params.put("size", size);
            fireEvent(Constants.Event.ONLOAD, params);
          }
        }
        monitorImgSize(imageView);
      }
    });

    String placeholder=null;
    if(getAttrs().containsKey(Constants.Name.PLACEHOLDER)){
      placeholder= (String) getAttrs().get(Constants.Name.PLACEHOLDER);
    }else if(getAttrs().containsKey(Constants.Name.PLACE_HOLDER)){
      placeholder=(String)getAttrs().get(Constants.Name.PLACE_HOLDER);
    }
    if(placeholder!=null){
      imageStrategy.placeHolder = getInstance().rewriteUri(Uri.parse(placeholder),URIAdapter.IMAGE).toString();
    }

    imageStrategy.instanceId = getInstanceId();
    IWXImgLoaderAdapter imgLoaderAdapter = getInstance().getImgLoaderAdapter();
    if (imgLoaderAdapter != null) {
      imgLoaderAdapter.setImage(rewrited.toString(), getHostView(),
          getAttrs().getImageQuality(), imageStrategy);
    }
  }

  @Override
  protected void onFinishLayout() {
    super.onFinishLayout();
    updateBorderRadius();
  }

  @Override
  public void updateProperties(Map<String, Object> props) {
    super.updateProperties(props);
    updateBorderRadius();
  }

  private void updateBorderRadius() {
    if (getHostView() instanceof WXImageView) {
      final WXImageView imageView = (WXImageView)getHostView();
      BorderDrawable borderDrawable = WXViewUtils.getBorderDrawable(getHostView());
      float[] borderRadius;
      if (borderDrawable != null) {
        RectF borderBox = new RectF(0, 0, WXDomUtils.getContentWidth(this), WXDomUtils.getContentHeight(this));
        borderRadius = borderDrawable.getBorderInnerRadius(borderBox);
      } else {
        borderRadius = new float[] {0, 0, 0, 0, 0, 0, 0, 0};
      }
      imageView.setBorderRadius(borderRadius);

      if (imageView.getDrawable() instanceof ImageDrawable) {
        ImageDrawable imageDrawable = (ImageDrawable)imageView.getDrawable();
        float[] previousRadius = imageDrawable.getCornerRadii();
        if (!Arrays.equals(previousRadius, borderRadius)) {
          imageDrawable.setCornerRadii(borderRadius);
        }
      }
    }
  }

  /**
   * Need permission {android.permission.WRITE_EXTERNAL_STORAGE}
   */
  @JSMethod(uiThread = false)
  public void save(final JSCallback saveStatuCallback) {

    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      if (getContext() instanceof Activity) {
        ActivityCompat.requestPermissions((Activity) getContext(),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
      }
    }

    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      if (saveStatuCallback != null) {
        Map<String, Object> result = new HashMap<>();
        result.put(SUCCEED, false);
        result.put(ERRORDESC,"Permission denied: android.permission.WRITE_EXTERNAL_STORAGE");
        saveStatuCallback.invoke(result);
      }
      return;
    }

    if (mHost == null) {
      if (saveStatuCallback != null) {
        Map<String, Object> result = new HashMap<>();
        result.put(SUCCEED, false);
        result.put(ERRORDESC,"Image component not initialized");
        saveStatuCallback.invoke(result);
      }
      return;
    }

    if (mSrc == null || mSrc.equals("")) {
      if (saveStatuCallback != null) {
        Map<String, Object> result = new HashMap<>();
        result.put(SUCCEED, false);
        result.put(ERRORDESC,"Image does not have the correct src");
        saveStatuCallback.invoke(result);
      }
      return;
    }

    WXViewToImageUtil.generateImage(mHost, 0, 0xfff8f8f8, new WXViewToImageUtil.OnImageSavedCallback() {
      @Override
      public void onSaveSucceed(String path) {
        if (saveStatuCallback != null) {
          Map<String, Object> result = new HashMap<>();
          result.put(SUCCEED, true);
          saveStatuCallback.invoke(result);
        }
      }

      @Override
      public void onSaveFailed(String errorDesc) {
        if (saveStatuCallback != null) {
          Map<String, Object> result = new HashMap<>();
          result.put(SUCCEED, false);
          result.put(ERRORDESC,errorDesc);
          saveStatuCallback.invoke(result);
        }
      }
    });
  }

  private void monitorImgSize(ImageView imageView){
    if (null == imageView){
      return;
    }
    WXSDKInstance instance = getInstance();
    if (null == instance){
      return;
    }
    ViewGroup.LayoutParams params =imageView.getLayoutParams();
    Drawable img = imageView.getDrawable();
    if (null == params || null ==img){
      return;
    }

    if (img.getIntrinsicHeight() * img.getIntrinsicWidth() > imageView.getMeasuredHeight() *
            imageView.getMeasuredWidth()){
      instance.getWXPerformance().wrongImgSizeCount++;
    }
  }

  @Override
  public void destroy() {
    if(getHostView() instanceof WXImageView){
      if (getInstance().getImgLoaderAdapter() != null) {
        getInstance().getImgLoaderAdapter().setImage(null, mHost, null, null);
      }
    }
    super.destroy();
  }

  public interface Measurable {
    int getNaturalWidth();
    int getNaturalHeight();
  }
}
