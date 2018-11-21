/*************************************
 * @Author  Irvin Pang @ XXTeam
 * @E-mail  halo.irvin@gmail.com
 *************************************/

#include "callback_manager_impl_android.h"
#include <android/jniprebuild/jniheader/WXCallbackManager_jni.h>
#include <android/base/string/scoped_jstring_utf8.h>
#include <core/manager/weex_callback_manager.h>

static void onComponentCallback(JNIEnv* env, jclass jcaller, jstring jinstanceID, jstring jref, jstring jtype) {
  WeexCore::WeexCallbackManager::getInstance()->postToComponent(
    WeexCore::ScopedJStringUTF8(env, jinstanceID).getChars(),
    WeexCore::ScopedJStringUTF8(env, jref).getChars(),
    WeexCore::ScopedJStringUTF8(env, jtype).getChars());
}

namespace WeexCore {
  bool RegisterJNICallbackManager(JNIEnv *env) {
      return RegisterNativesImpl(env);
  }
}
