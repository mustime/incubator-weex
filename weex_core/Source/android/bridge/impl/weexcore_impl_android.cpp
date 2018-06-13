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
#include "weexcore_impl_android.h"
#include <android/base/jni/android_jni.h>
#include <android/jniprebuild/jniheader/WXBridge_jni.h>
#include <android/base/string/string_utils.h>
#include <core/render/manager/render_manager.h>
#include <core/render/page/render_page.h>
#include <core/render/node/render_object.h>
#include <core/config/core_environment.h>
#include <map>

using namespace WeexCore;

jclass jBridgeClazz;
jclass jWXLogUtils;
jclass jMapClazz;
jclass jSetClazz;
jmethodID jMapConstructorMethodId = nullptr;
jmethodID jMapPutMethodId = nullptr;
jmethodID jSetConstructorMethodId = nullptr;
jmethodID jSetAddMethodId = nullptr;

jmethodID jDoubleValueMethodId;
jobject jThis;
jobject jWMThis;
jlongArray jFirstScreenRenderTime = nullptr;
jlongArray jRenderFinishTime = nullptr;
std::map<std::string, jobject> componentTypeCache;

JStringCache refCache(2048);

static JavaVM *sVm = NULL;

JNIEnv *getJNIEnv() {
  JNIEnv *env = NULL;
  if ((sVm)->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
    return JNI_FALSE;
  }
  return env;
}

jstring getComponentTypeFromCache(const std::string type) {
  std::map<std::string, jobject>::const_iterator iter = componentTypeCache.find(type);
  if (iter != componentTypeCache.end()) {
    return (jstring)(iter->second);
  } else {
    return nullptr;
  }
}

jstring putComponentTypeToCache(const std::string type) {
  JNIEnv *env = getJNIEnv();
  jstring jType = env->NewStringUTF(type.c_str());
  jobject jGlobalType = env->NewGlobalRef(jType);
  componentTypeCache.insert(std::pair<std::string, jobject>(type, jGlobalType));
  env->DeleteLocalRef(jType);
  return (jstring) jGlobalType;
}

jstring getKeyFromCache(JNIEnv *env, const char *key) {
  return refCache.GetString(env, key);
}

jfloatArray c2jFloatArray(JNIEnv *env, const float c_array[]) {
  if (nullptr == c_array) {
    return nullptr;
  }
  if (0 == c_array[0]
      && 0 == c_array[1]
      && 0 == c_array[2]
      && 0 == c_array[3]) {
    // Default value;
    return nullptr;
  }
  jfloatArray jArray = env->NewFloatArray(4);
  env->SetFloatArrayRegion(jArray, 0, 4, c_array);
  return jArray;
}

static void BindMeasurementToRenderObject(JNIEnv* env, jobject jcaller,
                                          jlong ptr){
  RenderObject *render =  convert_long_to_render_object(ptr);
  if(render){
    render->BindMeasureFunc();
  }
}

static void OnInstanceClose(JNIEnv *env, jobject jcaller, jstring instanceId) {
  RenderManager::GetInstance()->ClosePage(jString2StrFast(env, instanceId));
}

static void SetDefaultHeightAndWidthIntoRootDom(JNIEnv *env, jobject jcaller,
                                                jstring instanceId, jfloat defaultWidth, jfloat defaultHeight,
                                                jboolean isWidthWrapContent, jboolean isHeightWrapContent) {
  RenderPage *page = RenderManager::GetInstance()->GetPage(jString2StrFast(env, instanceId));
  if (page == nullptr)
    return;

#if RENDER_LOG
  LOGD("[JNI] SetDefaultHeightAndWidthIntoRootDom >>>> pageId: %s, defaultWidth: %f, defaultHeight: %f",
       page->PageId().c_str(), defaultWidth,defaultHeight);
#endif

  page->SetDefaultHeightAndWidthIntoRootRender(defaultWidth, defaultHeight, isWidthWrapContent, isHeightWrapContent);
}

static void SetRenderContainerWrapContent(JNIEnv* env, jobject jcaller, jboolean wrap, jstring instanceId) {
  RenderPage *page = RenderManager::GetInstance()->GetPage(jString2StrFast(env, instanceId));
  if (page == nullptr)
    return;

  page->SetRenderContainerWidthWrapContent(wrap);
}

static jlongArray GetFirstScreenRenderTime(JNIEnv *env, jobject jcaller, jstring instanceId) {
  jlongArray jTemp = env->NewLongArray(3);

  RenderPage *page = RenderManager::GetInstance()->GetPage(jString2StrFast(env, instanceId));
  if (page == nullptr) {
    if (jFirstScreenRenderTime != nullptr) {
      env->DeleteGlobalRef(jFirstScreenRenderTime);
      jFirstScreenRenderTime = nullptr;
    }
    jFirstScreenRenderTime = static_cast<jlongArray>(env->NewGlobalRef(jTemp));
    return jFirstScreenRenderTime;
  }

  std::vector<long> temp = page->PrintFirstScreenLog();

  jlong ret[3];

  ret[0] = temp[0];
  ret[1] = temp[1];
  ret[2] = temp[2];
  env->SetLongArrayRegion(jTemp, 0, 3, ret);

  if (jFirstScreenRenderTime != nullptr) {
    env->DeleteGlobalRef(jFirstScreenRenderTime);
    jFirstScreenRenderTime = nullptr;
  }
  jFirstScreenRenderTime = static_cast<jlongArray>(env->NewGlobalRef(jTemp));

  env->DeleteLocalRef(jTemp);
  return jFirstScreenRenderTime;
}

static jlongArray GetRenderFinishTime(JNIEnv *env, jobject jcaller, jstring instanceId) {
  jlongArray jTemp = env->NewLongArray(3);

  RenderPage *page = RenderManager::GetInstance()->GetPage(jString2StrFast(env, instanceId));
  if (page == nullptr) {
    if (jRenderFinishTime != nullptr) {
      env->DeleteGlobalRef(jRenderFinishTime);
      jRenderFinishTime = nullptr;
    }
    jRenderFinishTime = static_cast<jlongArray>(env->NewGlobalRef(jTemp));
    return jRenderFinishTime;
  }

  std::vector<long> temp = page->PrintRenderSuccessLog();

  jlong ret[3];

  ret[0] = temp[0];
  ret[1] = temp[1];
  ret[2] = temp[2];
  env->SetLongArrayRegion(jTemp, 0, 3, ret);

  if (jRenderFinishTime != nullptr) {
    env->DeleteGlobalRef(jRenderFinishTime);
    jRenderFinishTime = nullptr;
  }
  jRenderFinishTime = static_cast<jlongArray>(env->NewGlobalRef(jTemp));

  env->DeleteLocalRef(jTemp);
  return jRenderFinishTime;
}

//Notice that this method is invoked from main thread.
static jboolean NotifyLayout(JNIEnv* env, jobject jcaller, jstring instanceId) {
  RenderPage *page = RenderManager::GetInstance()->GetPage(jString2StrFast(env, instanceId));
  if (page != nullptr) {

    if (!page->needLayout.load()) {
      page->needLayout.store(true);
    }

    bool ret = !page->hasForeLayoutAction.load() && page->isDirty();
    if (ret) {
      page->hasForeLayoutAction.store(true);
    }
    return ret ? JNI_TRUE : JNI_FALSE;
  }
}

//Notice that this method is invoked from JS thread.
static void ForceLayout(JNIEnv *env, jobject jcaller, jstring instanceId) {
  RenderPage *page = RenderManager::GetInstance()->GetPage(jString2StrFast(env, instanceId));
  if (page != nullptr) {

#if RENDER_LOG
    LOGD("[JNI] ForceLayout >>>> pageId: %s, needForceLayout: %s", jString2StrFast(env, instanceId).c_str(), page->hasForeLayoutAction.load()?"true":"false");
#endif

    page->LayoutImmediately();
    page->hasForeLayoutAction.store(false);
  }
}

static void SetStyleWidth(JNIEnv *env, jobject jcaller,
                          jstring instanceId, jstring ref, jfloat value) {

  RenderPage *page = RenderManager::GetInstance()->GetPage(jString2StrFast(env, instanceId));
  if (page == nullptr)
    return;

  RenderObject *render = page->GetRenderObject(jString2StrFast(env, ref));
  if (render == nullptr)
    return;

  render->setStyleWidthLevel(CSS_STYLE);
  render->setStyleWidth(value, true);
  page->updateDirty(true);
}

static void SetStyleHeight(JNIEnv *env, jobject jcaller,
                           jstring instanceId, jstring ref, jfloat value) {

  RenderPage *page = RenderManager::GetInstance()->GetPage(jString2StrFast(env, instanceId));
  if (page == nullptr)
    return;

  RenderObject *render = page->GetRenderObject(jString2StrFast(env, ref));
  if (render == nullptr)
    return;

  render->setStyleHeightLevel(CSS_STYLE);
  render->setStyleHeight(value);
  page->updateDirty(true);
}

static void SetMargin(JNIEnv *env, jobject jcaller,
                      jstring instanceId, jstring ref, jint edge, jfloat value) {
  RenderPage *page = RenderManager::GetInstance()->GetPage(jString2StrFast(env, instanceId));
  if (page == nullptr)
    return;

  RenderObject *render = page->GetRenderObject(jString2StrFast(env, ref));
  if (render == nullptr)
    return;

  if (edge == 0) {
    render->setMargin(kMarginTop, value);
  } else if (edge == 1) {
    render->setMargin(kMarginBottom, value);
  } else if (edge == 2) {
    render->setMargin(kMarginLeft, value);
  } else if (edge == 3) {
    render->setMargin(kMarginRight, value);
  } else if (edge == 4) {
    render->setMargin(kMarginALL, value);
  }
  page->updateDirty(true);
}

static void SetPadding(JNIEnv *env, jobject jcaller,
                       jstring instanceId, jstring ref, jint edge, jfloat value) {

  RenderPage *page = RenderManager::GetInstance()->GetPage(jString2StrFast(env, instanceId));
  if (page == nullptr)
    return;

  RenderObject *render = page->GetRenderObject(jString2StrFast(env, ref));
  if (render == nullptr)
    return;

  if (edge == 0) {
    render->setPadding(kPaddingTop, value);
  } else if (edge == 1) {
    render->setPadding(kPaddingBottom, value);
  } else if (edge == 2) {
    render->setPadding(kPaddingLeft, value);
  } else if (edge == 3) {
    render->setPadding(kPaddingRight, value);
  } else if (edge == 4) {
    render->setPadding(kPaddingALL, value);
  }
  page->updateDirty(true);
}


static void SetPosition(JNIEnv *env, jobject jcaller,
                        jstring instanceId, jstring ref, jint edge, jfloat value) {

  RenderPage *page = RenderManager::GetInstance()->GetPage(jString2StrFast(env, instanceId));
  if (page == nullptr)
    return;

  RenderObject *render = page->GetRenderObject(jString2StrFast(env, ref));
  if (render == nullptr)
    return;

  if (edge == 0) {
    render->setStylePosition(kPositionEdgeTop, value);
  } else if (edge == 1) {
    render->setStylePosition(kPositionEdgeBottom, value);
  } else if (edge == 2) {
    render->setStylePosition(kPositionEdgeLeft, value);
  } else if (edge == 3) {
    render->setStylePosition(kPositionEdgeRight, value);
  }
  page->updateDirty(true);
}

static void MarkDirty(JNIEnv *env, jobject jcaller,
                      jstring instanceId, jstring ref, jboolean dirty) {
  RenderPage *page = RenderManager::GetInstance()->GetPage(jString2StrFast(env, instanceId));
  if (page == nullptr)
    return;

  if (dirty) {

    RenderObject *render = page->GetRenderObject(jString2StrFast(env, ref));
    if (render == nullptr)
      return;
    render->markDirty();
  }
}

static void RegisterCoreEnv(JNIEnv *env, jobject jcaller, jstring key, jstring value) {
  LOGE("RegisterCoreEnv，key: %s, value: %s", jString2StrFast(env, key).c_str(),
       jString2StrFast(env, value).c_str());
  WXCoreEnvironment::getInstance()->AddOption(jString2StrFast(env, key), jString2StrFast(env, value));
}

static void SetViewPortWidth(JNIEnv *env, jobject jcaller, jstring instanceId, jfloat value) {
  RenderPage *page = RenderManager::GetInstance()->GetPage(jString2StrFast(env, instanceId));
  if (page == nullptr)
    return;

  page->SetViewPortWidth(value);
}

namespace WeexCore {
  bool RegisterJNIGlobal(JNIEnv *env) {
      return RegisterNativesImpl(env);
  }

  jint OnLoad(JavaVM *vm, void *reserved) {
    LOGD("begin JNI_OnLoad");
    JNIEnv *env;
    /* Get environment */
    if ((vm)->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
      return JNI_FALSE;
    }
    try {
      throw 1;
    } catch (int) {
    }
    sVm = vm;
    jclass tempClass = env->FindClass(
        "com/taobao/weex/bridge/WXBridge");
    jBridgeClazz = (jclass) env->NewGlobalRef(tempClass);

    tempClass = env->FindClass("com/taobao/weex/utils/WXLogUtils");
    jWXLogUtils = (jclass) env->NewGlobalRef(tempClass);

    tempClass = env->FindClass("com/taobao/weex/utils/WXMap");
    jMapClazz = (jclass) env->NewGlobalRef(tempClass);

    tempClass = env->FindClass("java/util/HashSet");
    jSetClazz = (jclass) env->NewGlobalRef(tempClass);

    jMapConstructorMethodId = env->GetMethodID(jMapClazz, "<init>", "()V");
    jMapPutMethodId = env->GetMethodID(jMapClazz, "put", "(Ljava/lang/String;[B)Ljava/lang/String;");
    jSetConstructorMethodId = env->GetMethodID(jSetClazz, "<init>", "()V");
    jSetAddMethodId = env->GetMethodID(jSetClazz, "add", "(Ljava/lang/Object;)Z");

    LOGD("end JNI_OnLoad");

    return JNI_VERSION_1_4;
  }

  void Unload(JavaVM *vm, void *reserved) {
    LOGD("beigin JNI_OnUnload");
    JNIEnv *env;

    /* Get environment */
    if ((vm)->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
      return;
    }
    env->DeleteGlobalRef(jBridgeClazz);
    env->DeleteGlobalRef(jWXLogUtils);
    env->DeleteGlobalRef(jMapClazz);

    jMapConstructorMethodId = nullptr;
    jMapPutMethodId = nullptr;
    jSetConstructorMethodId = nullptr;
    jSetAddMethodId = nullptr;

    if (jFirstScreenRenderTime != nullptr) {
      env->DeleteLocalRef(jFirstScreenRenderTime);
      jFirstScreenRenderTime = nullptr;
    }

    if (jRenderFinishTime != nullptr) {
      env->DeleteLocalRef(jRenderFinishTime);
      jRenderFinishTime = nullptr;
    }

    for (auto iter = componentTypeCache.begin(); iter != componentTypeCache.end(); iter++) {
      if (iter->second != nullptr) {
        env->DeleteGlobalRef(iter->second);
        iter->second = nullptr;
      }
    }
    componentTypeCache.clear();

    refCache.clearRefCache(env);

    if (jThis)
      env->DeleteGlobalRef(jThis);
    if (jWMThis)
      env->DeleteLocalRef(jWMThis);
    LOGD(" end JNI_OnUnload");
  }
}