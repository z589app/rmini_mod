package com.z589app.xposedtest

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
import java.lang.Exception


public class Tutorial : IXposedHookLoadPackage{
    private var mContext: Context? = null

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        // XposedBridge.log("Loaded app: " + lpparam.packageName)

        if(!lpparam.packageName.equals("com.android.systemui"))
            return;

        XposedBridge.log("Hello XPosed")

//        // クロックの色変えるチュートリアル
//        findAndHookMethod(
//            "com.android.systemui.statusbar.policy.Clock",
//            lpparam.classLoader,
//            "updateClock",
//            object : XC_MethodHook() {
//                @Throws(Throwable::class)
//                override fun beforeHookedMethod(param: MethodHookParam) {
//                    // this will be called before the clock was updated by the original method
//                    // XposedBridge.log("updateClock Before Hooked")
//                }
//
//                @Throws(Throwable::class)
//                override fun afterHookedMethod(param: MethodHookParam) {
//                    // XposedBridge.log("updateClock After Hooked")
//                    val tv = param.thisObject as TextView
//                    val text = tv.text.toString()
//                    tv.text = "${text}" // "${text}:)"
//                    tv.setTextColor(Color.CYAN)
//                }
//            })


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


//        // FeatureUtil dame
//        val cls = findClassIfExists(
//            "com.android.systemui.util.FeatureUtils",
//            lpparam.classLoader
//        )
//        XposedBridge.log("FeatureUtils found: " + cls)
//
//        if(cls != null) {
//            setStaticBooleanField(
//                cls,
//                "SHOW_CARRIER_ON_STATUSBAR",
//                false
//            )
//        }


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

                    val c = findClass("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader)
                    XposedBridge.log("updateIsKeyguard 0: " + c)
                    val b = findFieldIfExists(c, "mCarrierText")
                    XposedBridge.log("updateIsKeyguard 1: " + b)
                    if(b!=null){
                        XposedBridge.log("updateIsKeyguard 3: ")
                        try {
                            var d = b.get(param.thisObject) as TextView?
                            XposedBridge.log("updateIsKeyguard 4: " + d)
                            if(d!=null) {
                                XposedBridge.log("updateIsKeyguard 6: " + d.text + d.visibility)
                                d.text = ""
                                d.visibility = View.GONE
                                XposedBridge.log("updateIsKeyguard 7: " + d.text + d.visibility)
                                val p = d.parent as ViewGroup
                                p.removeView(d)
                            }
                        }catch (ex:Exception){
                            XposedBridge.log(ex)
                        }
                    }
                }
            }
        )

//        // kore
//        // 消えるけど何かのタイミングで戻る。
//        findAndHookMethod(
//            "com.android.keyguard.CarrierText",
//            lpparam.classLoader,
//            "updateCarrierText",
//            object : XC_MethodHook() {
//                @Throws(Throwable::class)
//                override fun beforeHookedMethod(param: MethodHookParam) {
//                    // this will be called before the clock was updated by the original method
//                }
//
//                @Throws(Throwable::class)
//                override fun afterHookedMethod(param: MethodHookParam) {
//                    XposedBridge.log("uCT After Hooked")
//                    var tv = param.thisObject as TextView
//                    XposedBridge.log("uCT 0: " + tv.text + tv.visibility)
//                    tv.visibility = View.GONE
//                    tv.text = "Dokuten"
//                    XposedBridge.log("uCT 1: " + tv.text + tv.visibility)
//                }
//            }
//        )

//        // kari 呼ばれてない。
//        findAndHookMethod(
//            "com.android.systemui.statusbar.phone.CollapsedStatusBarFragment",
//            lpparam.classLoader,
//            "showClock",
//            object : XC_MethodHook() {
//                @Throws(Throwable::class)
//                override fun beforeHookedMethod(param: MethodHookParam) {
//                    // this will be called before the clock was updated by the original method
//                }
//
//                @Throws(Throwable::class)
//                override fun afterHookedMethod(param: MethodHookParam) {
//                    XposedBridge.log("sC After Hooked")
//                    // var tv = param.thisObject as TextView
//                    // XposedBridge.log("oSSC 0: " + tv.text + tv.visibility)
//                    // tv.visibility = View.GONE
//                    // // tv.text = "Dokuten"
//                    // XposedBridge.log("oSSC 1: " + tv.text + tv.visibility)
//                }
//            }
//        )

//        // kari これも呼ばれてない??
//        findAndHookMethod(
//            "com.android.systemui.statusbar.phone.HeadsUpAppearanceController",
//            lpparam.classLoader,
//            "setShown",
//            object : XC_MethodHook() {
//                @Throws(Throwable::class)
//                override fun beforeHookedMethod(param: MethodHookParam) {
//                    // this will be called before the clock was updated by the original method
//                }
//
//                @Throws(Throwable::class)
//                override fun afterHookedMethod(param: MethodHookParam) {
//                    XposedBridge.log("setShown After Hooked")
//                    // var tv = param.thisObject as TextView
//                    // XposedBridge.log("oSSC 0: " + tv.text + tv.visibility)
//                    // tv.visibility = View.GONE
//                    // // tv.text = "Dokuten"
//                    // XposedBridge.log("oSSC 1: " + tv.text + tv.visibility)
//                }
//            }
//        )

//        // kari1
//        findAndHookMethod(
//            "com.android.keyguard.CarrierText",
//            lpparam.classLoader,
//            "onVisibilityChanged",
//            object : XC_MethodHook() {
//                @Throws(Throwable::class)
//                override fun beforeHookedMethod(param: MethodHookParam) {
//                }
//
//                @Throws(Throwable::class)
//                override fun afterHookedMethod(param: MethodHookParam) {
//                    XposedBridge.log("oVC After Hooked")
//                }
//            }
//        )
//        // kari2
//        findAndHookMethod(
//            "com.android.keyguard.CarrierText",
//            lpparam.classLoader,
//            "updateCarrierTextWithSimIoError",
//            object : XC_MethodHook() {
//                @Throws(Throwable::class)
//                override fun beforeHookedMethod(param: MethodHookParam) {
//                }
//
//                @Throws(Throwable::class)
//                override fun afterHookedMethod(param: MethodHookParam) {
//                    XposedBridge.log("uCTWSIE After Hooked")
//                }
//            }
//        )
//        // kari3
//        findAndHookMethod(
//            "com.android.keyguard.CarrierText",
//            lpparam.classLoader,
//            "setDark",
//            object : XC_MethodHook() {
//                @Throws(Throwable::class)
//                override fun beforeHookedMethod(param: MethodHookParam) {
//                }
//
//                @Throws(Throwable::class)
//                override fun afterHookedMethod(param: MethodHookParam) {
//                    XposedBridge.log("setDark After Hooked")
//                }
//            }
//        )
//        // NG
//        findAndHookMethod(
//            "com.android.keyguard.CarrierText",
//            lpparam.classLoader,
//            "setVisibility",
//            object : XC_MethodHook() {
//                @Throws(Throwable::class)
//                override fun beforeHookedMethod(param: MethodHookParam) {
//                    // this will be called before the clock was updated by the original method
//                    XposedBridge.log("sV Before Hooked")
//                }
//
//                @Throws(Throwable::class)
//                override fun afterHookedMethod(param: MethodHookParam) {
//                    XposedBridge.log("sV After Hooked")
//                    var tv = param.thisObject as TextView
//                    XposedBridge.log("sV 0: " + tv.text + tv.visibility)
//                    tv.text = "Lakuten"
//                    tv.visibility = View.GONE
//                    XposedBridge.log("sV 1: " + tv.text + tv.visibility)
//                }
//            }
//        )




        ///--- 壁紙変更
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

