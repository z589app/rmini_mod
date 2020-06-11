package com.z589app.rmini_mod

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam


const val TARGET_PACKAGE = "com.android.systemui"

public class Tutorial : IXposedHookLoadPackage{
    private var mContext: Context? = null

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        // XposedBridge.log("Loaded app: " + lpparam.packageName)

        if(!lpparam.packageName.equals(TARGET_PACKAGE))
            return;

        XposedBridge.log("Hello XPosed")

        // キーガード（画面ロック時）のキャリアラベル。
        findAndHookMethod(
            "com.android.systemui.statusbar.phone.KeyguardStatusBarView",
            lpparam.classLoader,
            "onFinishInflate",
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    // this will be called before the clock was updated by the original method
                }

                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    XposedBridge.log("onFinishInflate After Hooked")

                    val rl = param.thisObject as RelativeLayout
                    val mContext = XposedHelpers.getObjectField(rl, "mContext") as Context
                    val res: Resources = mContext.getResources()
                    val id = res.getIdentifier("keyguard_carrier_text", "id", "com.android.systemui")

                    val cl_tv = rl.findViewById<TextView>(id)
                    if(cl_tv != null) {
                        XposedBridge.log("onFinishInflate 1:")
                        cl_tv.visibility = View.GONE
                    }
                }
            }
        )


        // 無理やりView削除版
        findAndHookMethod(
            "com.android.systemui.statusbar.phone.StatusBar",
            lpparam.classLoader,
            "updateIsKeyguard",
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    // this will be called before the clock was updated by the original method
                }

                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    XposedBridge.log("updateIsKeygurad After Hooked")
                    val statusbar_clazz = findClass(
                        "com.android.systemui.statusbar.phone.StatusBar",
                        lpparam.classLoader
                    )
                    XposedBridge.log("updateIsKeyguard 0: " + statusbar_clazz)
                    val mCarrierTextField = findFieldIfExists(statusbar_clazz, "mCarrierText")
                    XposedBridge.log("updateIsKeyguard 1: " + mCarrierTextField)
                    if (mCarrierTextField == null) {
                        return
                    }
                    XposedBridge.log("updateIsKeyguard 3: ")
                    var mCarrierText = mCarrierTextField.get(param.thisObject) as TextView?
                    XposedBridge.log("updateIsKeyguard 4: " + mCarrierText)
                    if (mCarrierText == null) {
                        return
                    }
                    XposedBridge.log("updateIsKeyguard 6: " + mCarrierText.text + mCarrierText.visibility)
                    mCarrierText.text = ""
                    mCarrierText.visibility = View.GONE
                    XposedBridge.log("updateIsKeyguard 7: " + mCarrierText.text + mCarrierText.visibility)
                    val parentView = mCarrierText.parent as ViewGroup
                    parentView.removeView(mCarrierText)
                    XposedBridge.log("updateIsKeyguard 8: ")
                }
            }
        )


        /// 壁紙変更
        findAndHookMethod(
            "com.android.systemui.ImageWallpaper",
            lpparam.classLoader,
            "getWhiteWallpaper",
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    // this will be called before the clock was updated by the original method
                }

                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    XposedBridge.log("getWhiteWallpaper After Hooked")
                    param.result = null
                }
            }
        )
    }

}

