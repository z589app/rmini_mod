package com.z589app.rmini_mod

import android.content.Context
import android.content.res.Resources
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

const val TARGET_PACKAGE = "com.android.systemui"
const val LOG_TAG = "RMini "
const val KEY_CHANGE_WALLPAPER = "change_wallpaper"
const val KEY_REMOVE_CARRIR_STATUSBAR = "remove_carrier_statusbar"
const val KEY_REMOVE_CARRIER_KEYGUARD = "remove_carrier_keyguard"
const val KEY_MOVE_CLOCK_RIGHT = "move_clock_right"
const val KEY_REMOVE_NFC_ICON = "remove_nfc_icon"

const val SHARED_PREF_DIR = "/data/user_de/0/com.z589app.rmini_mod/shared_prefs/"
const val SHARED_PREF_FILE = "com.z589app.rmini_mod_preferences.xml"

class RakutenMiniXposedHookLoad : IXposedHookLoadPackage, IXposedHookZygoteInit {
   var mXSPrefences: XSharedPreferences? = null

    fun prefLoad(key: String) = mXSPrefences?.getBoolean(key, false)?: false

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam?) {
        val file = File(SHARED_PREF_DIR, SHARED_PREF_FILE)
        mXSPrefences = XSharedPreferences(file)
        XposedBridge.log(LOG_TAG + "Pref: " + mXSPrefences?.file)
        XposedBridge.log(LOG_TAG + "Pref: " + mXSPrefences?.file?.canRead())
        XposedBridge.log(LOG_TAG + "Pref: " + mXSPrefences?.all)
    }

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        // XposedBridge.log(LOG_TAG + "Loaded app: " + lpparam.packageName)

        if (!lpparam.packageName.equals(TARGET_PACKAGE))
            return

        XposedBridge.log(LOG_TAG + "Hello XPosed")

        // キーガード（画面ロック時）のキャリアラベル。
        if (prefLoad(KEY_REMOVE_CARRIER_KEYGUARD)) {
            findAndHookMethod(
                "com.android.systemui.statusbar.phone.KeyguardStatusBarView",
                lpparam.classLoader,
                "onFinishInflate",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                    }

                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log(LOG_TAG + "onFinishInflate: After Hooked")

                        val rl = param.thisObject as RelativeLayout
                        val mContext = XposedHelpers.getObjectField(rl, "mContext") as Context ?: return
                        val res: Resources = mContext.resources
                        val id =
                            res.getIdentifier(
                                "keyguard_carrier_text",
                                "id",
                                "com.android.systemui"
                            )

                        val cl_tv = rl.findViewById<TextView>(id) ?: return
                        cl_tv.visibility = View.GONE
                        XposedBridge.log(LOG_TAG + "onFinishInflate: Done")
                    }
                }
            )
        }


        // 無理やりView削除版
        if (prefLoad(KEY_REMOVE_CARRIR_STATUSBAR)) {
            findAndHookMethod(
                "com.android.systemui.statusbar.phone.StatusBar",
                lpparam.classLoader,
                "updateIsKeyguard",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                    }

                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log(LOG_TAG + "updateIsKeygurad: After Hooked")
//                        val statusbar_clazz = findClass(
//                            "com.android.systemui.statusbar.phone.StatusBar",
//                            lpparam.classLoader
//                        )
//                        val mCarrierTextField =
//                            findFieldIfExists(statusbar_clazz, "mCarrierText")
//                                ?: return
//
//                        var mCarrierText = mCarrierTextField.get(param.thisObject) as TextView ?: return
                        var mCarrierText = getMember(lpparam, param, "mCarrierText") as TextView ?: return
                        mCarrierText.text = ""
                        mCarrierText.visibility = View.GONE
                        val parentView = mCarrierText.parent as ViewGroup
                        parentView.removeView(mCarrierText)
                        XposedBridge.log(LOG_TAG + "updateIsKeygurad: Done")
                    }
                }
            )
        }


        if (prefLoad(KEY_CHANGE_WALLPAPER)) {
            /// 壁紙変更
            findAndHookMethod(
                "com.android.systemui.ImageWallpaper",
                lpparam.classLoader,
                "getWhiteWallpaper",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                    }

                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log(LOG_TAG + "getWhiteWallpaper: After Hooked")
                        param.result = null
                        XposedBridge.log(LOG_TAG + "getWhiteWallpaper: Done")
                    }
                }
            )
        }

        /// 時計右寄せ
        if (prefLoad(KEY_MOVE_CLOCK_RIGHT)) {
            findAndHookMethod(
                "com.android.systemui.statusbar.phone.PhoneStatusBarView",
                lpparam.classLoader,
                "onFinishInflate",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                    }

                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log(LOG_TAG + "PhoneStatusBarView: After Hooked " + param.thisObject)
                        val psbView = param.thisObject as FrameLayout

//                        // ステータスバーのレイアウト構造。
//                        // 有益なデバッグ情報
//                        for(child in psbView.children) {
//                            XposedBridge.log("PhoneStatusBarView Child: " + child)
//                            if(child is ViewGroup){
//                                for(gChild in child.children) {
//                                    XposedBridge.log("PhoneStatusBarView gChild: " + gChild)
//                                    if(gChild is ViewGroup){
//                                        for(ggChild in gChild.children) {
//                                            XposedBridge.log("PhoneStatusBarView ggChild: " + ggChild)
//                                            if(ggChild is ViewGroup) {
//                                                for(gggChild in ggChild.children) {
//                                                    XposedBridge.log("PhoneStatusBarView gggChild: " + gggChild)
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }

                        val res = psbView.context.resources
                        val leftSide = psbView.findViewById<LinearLayout>(
                            res.getIdentifier(
                                "status_bar_left_side",
                                "id",
                                "com.android.systemui"
                            )
                        ) ?: return

                        val clockTv = leftSide.findViewById<TextView>(
                            res.getIdentifier(
                                "clock",
                                "id",
                                "com.android.systemui"
                            )
                        ) ?: return

                        val systemIcons = psbView.findViewById<LinearLayout>(
                            res.getIdentifier(
                                "system_icons",
                                "id",
                                "com.android.systemui"
                            )
                        ) ?: return

                        leftSide.removeView(clockTv)
                        systemIcons.addView(clockTv)
                        XposedBridge.log(LOG_TAG + "PhoneStatusBarView: Done")
                    }
                }
            )
        }

        /// NFC Icon Remove
        if (prefLoad(KEY_REMOVE_NFC_ICON)) {
            findAndHookMethod(
                "com.android.systemui.statusbar.phone.PhoneStatusBarPolicy",
                lpparam.classLoader,
                "updateNFC",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                    }

                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log(LOG_TAG + "updateNFC: After Hooked " + param.args)
                        val mIconController = getMember(
                            lpparam,
                            param,
                            "mIconController"
                        )?: return
                        val mNFC = getMember(
                            lpparam, param,
                            "mNFC"
                        )?: return
                        callMethod(mIconController, "setIconVisibility", mNFC, false)
                        XposedBridge.log(LOG_TAG + "updateNFC: Done")
                    }
                }
            )
        }

    }

    fun getMember(
        lpparam: LoadPackageParam,
        mhparam: XC_MethodHook.MethodHookParam,
        field_name: String
    ): Any? {
        val clazz = findClass(
            mhparam.thisObject.javaClass.name,
            lpparam.classLoader
        )?: return null
        val field = findFieldIfExists(clazz, field_name)?: return null
        return field.get(mhparam.thisObject)

    }

}

