package cn.xxzhushou.xmod.wxui;

/**
 * Author: Irvin Pang
 * E-mail: halo.irvin@gmail.com
 */
public class WXCallbackManager {

	public static native void nativeOnComponentCallback(String instanceID, String ref, String type);

}
