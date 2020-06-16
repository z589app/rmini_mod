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
const val KEY_CHANGE_WALLPAPER = "change_wallpaper"
const val KEY_REMOVE_CARRIR_STATUSBAR = "remove_carrier_statusbar"
const val KEY_REMOVE_CARRIER_KEYGUARD = "remove_carrier_keyguard"
const val KEY_MOVE_CLOCK_RIGHT = "move_clock_right"
const val KEY_REMOVE_NFC_ICON = "remove_nfc_icon"

class RakutenMiniXposedHookLoad : IXposedHookLoadPackage {
    private var mContext: Context? = null

    private var doing = false

    var xsp: XSharedPreferences? = null

   fun prefLoad(key: String): Boolean{
        if(xsp==null){
            val file = File(SHARED_PREF_DIR, SHARED_PREF_FILE)
            if (file.exists()) {
                xsp = XSharedPreferences(file)
                // val xsp = XSharedPreferences(BuildConfig.APPLICATION_ID)
                xsp!!.makeWorldReadable()
                xsp!!.reload()

                XposedBridge.log("Pref: " + xsp?.file)
                XposedBridge.log("Pref: " + xsp?.file?.canRead())
                XposedBridge.log("Pref: " + xsp?.all)

                return xsp!!.getBoolean(key, true)
            }else {
                XposedBridge.log("Pref: FileNotFound")
                return true
            }
        }else{
            XposedBridge.log("Pref Loaded: " + xsp?.all)
            return xsp!!.getBoolean(key,true)
        }
    }

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        // XposedBridge.log("Loaded app: " + lpparam.packageName)

        if (!lpparam.packageName.equals(TARGET_PACKAGE))
            return

        XposedBridge.log("Hello XPosed")

        // キーガード（画面ロック時）のキャリアラベル。
        if (prefLoad(KEY_REMOVE_CARRIER_KEYGUARD)) {
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
                            res.getIdentifier(
                                "keyguard_carrier_text",
                                "id",
                                "com.android.systemui"
                            )

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
        if (prefLoad(KEY_REMOVE_CARRIR_STATUSBAR)) {
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
                        val mCarrierTextField =
                            findFieldIfExists(statusbar_clazz, "mCarrierText")
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


        if (prefLoad(KEY_CHANGE_WALLPAPER)) {
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
                        XposedBridge.log("PhoneStatusBarView: " + param.thisObject)
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
                        )
                        XposedBridge.log("PhoneStatusBarView leftSide: " + leftSide)

                        if (leftSide != null) {
                            val clockTv = leftSide.findViewById<TextView>(
                                res.getIdentifier(
                                    "clock",
                                    "id",
                                    "com.android.systemui"
                                )
                            )
                            XposedBridge.log("PhoneStatusBarView clockTv: " + clockTv)

                            if (clockTv != null) {
                                val systemIcons = psbView.findViewById<LinearLayout>(
                                    res.getIdentifier(
                                        "system_icons",
                                        "id",
                                        "com.android.systemui"
                                    )
                                )
                                XposedBridge.log("PhoneStatusBarView systemIcons: " + systemIcons)

                                if (systemIcons != null) {
                                    leftSide.removeView(clockTv)
                                    systemIcons.addView(clockTv)
                                }
                            }
                        }
                    }
                }
            )
        }

//        if (removeNFCIcon) {
//            /// NFC Icon Remove
//            findAndHookMethod(
//                "com.android.systemui.statusbar.phone.StatusIconContainer",
//                lpparam.classLoader,
//                "applyIconStates",
//                object : XC_MethodHook() {
//                    @Throws(Throwable::class)
//                    override fun beforeHookedMethod(param: MethodHookParam) {
//                        XposedBridge.log("onViewAdded: " + param.args)
//                        XposedBridge.log("onViewAdded: " + param.args.size)
//                    }
//
//                    @Throws(Throwable::class)
//                    override fun afterHookedMethod(param: MethodHookParam) {
//                    }
//                }
//            )
//        }

        /// NFC Icon Remove
        if (prefLoad(KEY_REMOVE_NFC_ICON)) {
            findAndHookMethod(
                "com.android.systemui.statusbar.phone.PhoneStatusBarPolicy",
                lpparam.classLoader,
                "updateNFC",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("updateNFC: " + param.args)
                        XposedBridge.log("updateNFC: " + param.args.size)
                    }

                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("updateNFC: " + param.args)
                        // mIconController.setIconVisibility(mNFC, false);
                        val mIconController = getMember(
                            lpparam,
                            param,
                            "com.android.systemui.statusbar.phone.PhoneStatusBarPolicy",
                            "mIconController"
                        )
                        XposedBridge.log("updateNFC mIconCotroller: " + mIconController)
                        val mNFC = getMember(
                            lpparam, param,
                            "com.android.systemui.statusbar.phone.PhoneStatusBarPolicy",
                            "mNFC"
                        )
                        XposedBridge.log("updateNFC mNFC: " + mNFC)
                        if (mIconController != null && mNFC != null) {
                            callMethod(mIconController, "setIconVisibility", mNFC, false)
                        }
                    }
                }
            )
        }

    }

    fun getMember(
        lpparam: LoadPackageParam,
        mhparam: XC_MethodHook.MethodHookParam,
        pkg_class_name: String,
        field_name: String
    ): Any? {
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
