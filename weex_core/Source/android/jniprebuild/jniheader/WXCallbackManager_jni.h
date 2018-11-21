/*************************************
 * @Author  Irvin Pang @ XXTeam
 * @E-mail  halo.irvin@gmail.com
 *************************************/

#ifndef cn_xxzhushou_xmod_wxui_WXCallbackManager_JNI
#define cn_xxzhushou_xmod_wxui_WXCallbackManager_JNI

#include <android/base/jni/android_jni.h>

// Step 1: forward declarations.
namespace {
const char kWXCallbackManagerClassPath[] = "cn/xxzhushou/xmod/wxui/WXCallbackManager";
// Leaking this jclass as we cannot use LazyInstance from some threads.
jclass g_WXCallbackManager_clazz = NULL;
#define WXCallbackManager_clazz(env) g_WXCallbackManager_clazz

}  // namespace

static void onComponentCallback(JNIEnv* env, jclass jcaller, jstring, jstring, jstring);

static const JNINativeMethod kMethodsWXCallbackManager[] = {
    { "nativeOnComponentCallback",
"("
"Ljava/lang/String;"
"Ljava/lang/String;"
"Ljava/lang/String;"
")"
"V", reinterpret_cast<void*>(onComponentCallback) },
};

// Step 2: method stubs.

// Step 3: RegisterNatives.

static bool RegisterNativesImpl(JNIEnv* env) {

  g_WXCallbackManager_clazz = reinterpret_cast<jclass>(env->NewGlobalRef(
      base::android::GetClass(env, kWXCallbackManagerClassPath).Get()));

  const int kMethodsWXCallbackManagerSize =
      sizeof(kMethodsWXCallbackManager)/sizeof(kMethodsWXCallbackManager[0]);

  if (env->RegisterNatives(WXCallbackManager_clazz(env),
                           kMethodsWXCallbackManager,
                           kMethodsWXCallbackManagerSize) < 0) {
    //jni_generator::HandleRegistrationError(
    //    env, WXCallbackManager_clazz(env), __FILE__);
    return false;
  }

  return true;
}

#endif  // cn_xxzhushou_xmod_wxui_WXCallbackManager_JNI
