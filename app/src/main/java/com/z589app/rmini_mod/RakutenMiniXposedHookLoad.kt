package com.z589app.rmini_mod

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.children
import de.robv.android.xposed.*
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File
import java.lang.Exception

const val TARGET_PACKAGE = "com.android.systemui"
const val LOG_TAG = "RMini "
const val KEY_CHANGE_WALLPAPER = "change_wallpaper"
const val KEY_REMOVE_CARRIR_STATUSBAR = "remove_carrier_statusbar"
const val KEY_REMOVE_CARRIER_KEYGUARD = "remove_carrier_keyguard"
const val KEY_MOVE_CLOCK_RIGHT = "move_clock_right"
const val KEY_ICON_DISABLE_LIST = "icon_disable_list"


const val SHARED_PREF_DIR = "/data/user_de/0/com.z589app.rmini_mod/shared_prefs/"
const val SHARED_PREF_FILE = "com.z589app.rmini_mod_preferences.xml"

class RakutenMiniXposedHookLoad : IXposedHookLoadPackage, IXposedHookZygoteInit {
   var mXSPrefences: XSharedPreferences? = null

    fun loadPrefBoolean(key: String) = mXSPrefences?.getBoolean(key, false)?: false
    fun loadPrefList(key: String) = mXSPrefences?.getStringSet(key, null)?: null

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

        // テスト
        if (loadPrefList(KEY_ICON_DISABLE_LIST)?.isEmpty()==false) {
            findAndHookMethod(
                "com.android.systemui.statusbar.phone.StatusIconContainer",
                lpparam.classLoader,
                "applyIconStates",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                    }

                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log(LOG_TAG + "applyIconStates: After Hooked")

                        val ll = param.thisObject as LinearLayout
                        val disable_list = loadPrefList(KEY_ICON_DISABLE_LIST)

                        for(v in ll.children){
                            // XposedBridge.log(LOG_TAG + "applyIconStates: child=" + v)
                            val mSlot = getMember(lpparam.classLoader, v as Object, "mSlot") as String
                            // XposedBridge.log(LOG_TAG + "applyIconStates: slot=" + slot)

                            if(disable_list?.contains(mSlot) == true){
                                try {
                                    val mIcon = getMember(
                                        lpparam.classLoader,
                                        v as Object,
                                        "mIcon"
                                    ) as Object
                                    XposedHelpers.setBooleanField(mIcon, "visible", false)
                                }catch (ex: Exception){
                                    XposedBridge.log(LOG_TAG + "applyIconStates: ex=" + ex)
                                }
                                // v.visibility = View.GONE
                                // XposedHelpers.callMethod(v, "setVisibleState", 1, false)
                            }
                        }

                    }
                }
            )
        }


        // キーガード（画面ロック時）のキャリアラベル。
        if (loadPrefBoolean(KEY_REMOVE_CARRIER_KEYGUARD)) {
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
        if (loadPrefBoolean(KEY_REMOVE_CARRIR_STATUSBAR)) {
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
                        var mCarrierText = getMember(lpparam, param, "mCarrierText") as TextView?
                        if(mCarrierText==null){
                            return
                        }

                        mCarrierText.text = ""
                        mCarrierText.visibility = View.GONE
                        val parentView = mCarrierText.parent as ViewGroup
                        parentView.removeView(mCarrierText)
                        XposedBridge.log(LOG_TAG + "updateIsKeygurad: Done")
                    }
                }
            )
        }


        if (loadPrefBoolean(KEY_CHANGE_WALLPAPER)) {
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
        if (loadPrefBoolean(KEY_MOVE_CLOCK_RIGHT)) {
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

    }

    fun getMember(
        lpparam: LoadPackageParam,
        mhparam: XC_MethodHook.MethodHookParam,
        field_name: String
    ): Any? {
        return getMember(
            lpparam.classLoader,
            mhparam.thisObject as Object,
            field_name
        )
    }

    fun getMember(
        classLoader: ClassLoader,
        obj: Object,
        field_name: String
    ): Any? {
        val clazz = findClass(
            obj.javaClass.name,
            classLoader
        )?: return null
        val field = findFieldIfExists(clazz, field_name)?: return null
        return field.get(obj)
    }
}

