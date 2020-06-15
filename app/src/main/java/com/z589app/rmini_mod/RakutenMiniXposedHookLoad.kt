package com.z589app.rmini_mod

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import de.robv.android.xposed.*
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File
import java.lang.Exception


const val TARGET_PACKAGE = "com.android.systemui"
//TODO ?
// const val SHARED_PREF = "/storage/emulated/0/com.z589app.rmini_mod_preferences.xml"
// const val SHARED_PREF = "/storage/emulated/0/Android/data/com.z589app.rmini_mod/shared_prefs/com.z589app.rmini_mod_preferences.xml"
// const val SHARED_PREF = "/sdcard/_rmini/com.z589app.rmini_mod_preferences.xml"

class RakutenMiniXposedHookLoad : IXposedHookLoadPackage {
    private var mContext: Context? = null

    private var doing = false

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        // XposedBridge.log("Loaded app: " + lpparam.packageName)

        if (!lpparam.packageName.equals(TARGET_PACKAGE))
            return

        XposedBridge.log("Hello XPosed")

        var changeWallpaperEnable = false
        var removeCarrierStatusBarEnable = false
        var removeCarrierKeyguardEnable = false
        var moveClockRight = false
        var removeNFCIcon = true

        val file = File(SHARED_PREF_DIR, SHARED_PREF_FILE)
        if (file.exists()) {
            val xsp = XSharedPreferences(File(SHARED_PREF_DIR, SHARED_PREF_FILE))
            // val xsp = XSharedPreferences(BuildConfig.APPLICATION_ID)
            xsp.makeWorldReadable()
            xsp.reload()
            XposedBridge.log("Pref: " + xsp.file)
            XposedBridge.log("Pref: " + xsp.file.canRead())
            XposedBridge.log("Pref: " + xsp.all)

            changeWallpaperEnable = xsp.getBoolean("change_wallpaper", false)
            removeCarrierStatusBarEnable = xsp.getBoolean("remove_carrier_statusbar", false)
            removeCarrierKeyguardEnable = xsp.getBoolean("remove_carrier_keyguard", false)
            // moveClockRight = xsp.getBoolean("move_clock_right", false)
            removeNFCIcon = xsp.getBoolean("reove_nfc_icon", false)
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

        if (moveClockRight) {
            /// 時計右寄せ → テスト中
            findAndHookMethod(
                "com.android.systemui.statusbar.policy.Clock",
                lpparam.classLoader,
                // "updateClockVisibility",
                "getSmallTime",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                    }

                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if(doing==false) {
                            doing = true
                            XposedBridge.log("updateClockVisibility After Hooked")

                            val clockTv = param.thisObject as TextView?
                            val parent = clockTv?.parent as LinearLayout
                            XposedBridge.log("updateClockVisibility Parent: " + parent.javaClass.`package`)
//                            if(parent.parent!=null){
//                                XposedBridge.log("updateClockVisibility Parent Parent: " + parent.parent)
//                                val pp = parent.parent as FrameLayout
//                            }

                            clockTv?.gravity = Gravity.END or Gravity.CENTER_VERTICAL;
                            clockTv.setTextColor(Color.BLUE)
                            val clockTvIndex = parent.indexOfChild(clockTv)
                            val childcount = parent.childCount

                            val lp = clockTv?.layoutParams as LinearLayout.LayoutParams
                            XposedBridge.log("updateClockVisibility Gravity: " + lp.gravity)
                            lp.gravity = Gravity.END or Gravity.CENTER_VERTICAL

                            if(clockTvIndex!=childcount-1) {
                                XposedBridge.log("updateClockVisibility Child Count: " + clockTvIndex + " of " + childcount)
                                val children = arrayOfNulls<View>(childcount - 1)
                                var k = 0
                                for (i in 0 until childcount) {
                                    if (i != clockTvIndex) {
                                        children[k] = parent.getChildAt(i)
                                        XposedBridge.log("updateClockVisibility removeView: " + i)
                                        parent.removeView(children[k])
                                        k += 1
                                    }
                                }
                                XposedBridge.log("updateClockVisibility addView start")
                                for (i in 0 until childcount - 1) {
                                    try {
                                        XposedBridge.log("updateClockVisibility addView: " + i)
                                        if (children[i] != null) {
                                            parent.addView(children[i], i)
                                        }
                                    } catch (ex: Exception) {
                                        XposedBridge.log(ex)
                                    }
                                }
                            }
                            XposedBridge.log("updateClockVisibility Done")
                            doing = false
                        }else {
                            XposedBridge.log("doing = true")
                        }

                    }
                }
            )
        }

//        /// 時計右寄せ → エラー
//        if (moveClockRight) {
//            findAndHookMethod(
//                "com.android.systemui.qs",
//                lpparam.classLoader,
//                "onFinishInflate",
//                object : XC_MethodHook() {
//                    @Throws(Throwable::class)
//                    override fun beforeHookedMethod(param: MethodHookParam) {
//                    }
//
//                    @Throws(Throwable::class)
//                    override fun afterHookedMethod(param: MethodHookParam) {
//                        XposedBridge.log("qs.onFinishInflate After Hooked")
//                        val rl = param.thisObject as RelativeLayout
//                        val mClockView = XposedHelpers.getObjectField(rl, "mClockView") as TextView
//                        mClockView.setTextColor(Color.GREEN)
//                    }
//                }
//            )
//        }
        if (removeNFCIcon) {
            /// NFC Icon Remove
            findAndHookMethod(
                "com.android.systemui.statusbar.phone.PhoneStatusBarPolicy",
                lpparam.classLoader,
                "updateNFC",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("TEST: " + param.args)
                        XposedBridge.log("TEST: " + param.args.size)
                    }
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("TEST: " + param.args)
                        // mIconController.setIconVisibility(mNFC, false);
                        val mIconController = getMember(lpparam, param,
                            "com.android.systemui.statusbar.phone.PhoneStatusBarPolicy", "mIconController")
                        val mNFC = getMember(lpparam, param,
                            "java.lang.String", "mNFC")
                        callMethod(mIconController, "setIconVisibility", mNFC, false)
                    }
                }
            )
        }
    }

    fun getMember(lpparam: LoadPackageParam, mhparam: XC_MethodHook.MethodHookParam, pkg_class_name: String, field_name: String) : Any?{
        val clazz = findClass(
            pkg_class_name,
            lpparam.classLoader
        )
        val field = findFieldIfExists(clazz, field_name)
        if (field == null) {
            return null
        }
        return field.get(mhparam.thisObject)

    }
}

