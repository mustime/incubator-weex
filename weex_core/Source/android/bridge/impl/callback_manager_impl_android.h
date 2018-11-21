/*************************************
 * @Author  Irvin Pang @ XXTeam
 * @E-mail  halo.irvin@gmail.com
 *************************************/

#ifndef WEEX_PROJECT_CALLBACK_MANAGER_IMPL_ANDROID_H
#define WEEX_PROJECT_CALLBACK_MANAGER_IMPL_ANDROID_H

#include <jni.h>

namespace WeexCore{
    bool RegisterJNICallbackManager(JNIEnv *env);
}

#endif //WEEX_PROJECT_CALLBACK_MANAGER_IMPL_ANDROID_H
