package com.z589app.rmini_mod

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import de.robv.android.xposed.*
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File


const val TARGET_PACKAGE = "com.android.systemui"
//TODO ?
// const val SHARED_PREF = "/storage/emulated/0/com.z589app.rmini_mod_preferences.xml"
// const val SHARED_PREF = "/storage/emulated/0/Android/data/com.z589app.rmini_mod/shared_prefs/com.z589app.rmini_mod_preferences.xml"
// const val SHARED_PREF = "/sdcard/_rmini/com.z589app.rmini_mod_preferences.xml"

class RakutenMiniXposedHookLoad : IXposedHookLoadPackage {
    private var mContext: Context? = null

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        // XposedBridge.log("Loaded app: " + lpparam.packageName)

        if (!lpparam.packageName.equals(TARGET_PACKAGE))
            return

        XposedBridge.log("Hello XPosed")

        var changeWallpaperEnable = false
        var removeCarrierStatusBarEnable = false
        var removeCarrierKeyguardEnable = false

        val file = File(SHARED_PREF_DIR, SHARED_PREF_FILE)
        if (file.exists()) {
            val xsp = XSharedPreferences(File(SHARED_PREF_DIR, SHARED_PREF_FILE))
            // val xsp = XSharedPreferences(BuildConfig.APPLICATION_ID)
            xsp.makeWorldReadable()
            xsp.reload()
            XposedBridge.log("TEST: " + xsp.file)
            XposedBridge.log("TEST: " + xsp.file.canRead())
            XposedBridge.log("TEST: " + xsp.all)

            changeWallpaperEnable = xsp.getBoolean("change_wallpaper", false)
            removeCarrierStatusBarEnable = xsp.getBoolean("remove_carrier_statusbar", false)
            removeCarrierKeyguardEnable = xsp.getBoolean("remove_carrier_keyguard", false)
        }

        if (removeCarrierKeyguardEnable) {
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
                        val res: Resources = mContext.resources
                        val id =
                            res.getIdentifier("keyguard_carrier_text", "id", "com.android.systemui")

                        val cl_tv = rl.findViewById<TextView>(id)
                        if (cl_tv != null) {
                            XposedBridge.log("onFinishInflate 1:")
                            cl_tv.visibility = View.GONE
                        }
                    }
                }
            )
        }


        // 無理やりView削除版
        if (removeCarrierStatusBarEnable) {
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
        }


        if (changeWallpaperEnable) {
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

        if (true) {
            /// 時計右寄せ
            findAndHookMethod(
                "com.android.systemui.statusbar.policy.Clock",
                lpparam.classLoader,
                "getSmallTime",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                    }

                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("getSmallTime After Hooked")

                        val clockTv = param.thisObject as TextView?
                        val parent = clockTv?.parent as LinearLayout
                        XposedBridge.log("PARENT: " + parent.childCount)

                        clockTv?.gravity = Gravity.END or Gravity.CENTER_VERTICAL;
                        clockTv.setTextColor(Color.BLUE)
                        val lp = clockTv?.layoutParams as LinearLayout.LayoutParams
                        XposedBridge.log("LP: " + lp.gravity)
                        lp.gravity = Gravity.END or Gravity.CENTER_VERTICAL

                        //bootloop parent.removeView(clockTv)
                        //bootloop parent.addView(clockTv, lp)

//                        new ClockPositionInfo(parentRight, -1,
//                        Gravity.END | Gravity.CENTER_VERTICAL,
//                        mClock.getPaddingEnd(), mClock.getPaddingStart()));
                    }
                }
            )
        }

    }

}

