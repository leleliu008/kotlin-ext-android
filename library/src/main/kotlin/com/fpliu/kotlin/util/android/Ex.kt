package com.fpliu.kotlin.util.android

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.*
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.fpliu.kotlin.util.jdk.getUnicodeCharacterCount
import java.io.File
import kotlin.reflect.KClass

var appContext =
    try {
        val activityThread = Class.forName("android.app.ActivityThread")
        val app = activityThread.getMethod("currentApplication")
        app.isAccessible = true
        app.invoke(null as Any?) as Application
    } catch (throwable: Throwable) {
        throw RuntimeException("Failed to get current application!", throwable)
    }

const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT
const val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT

fun Activity.startActivity(targetActivityOfKClass: KClass<*>) {
    startActivity(Intent(this, targetActivityOfKClass.java))
}

fun Fragment.startActivity(targetActivityOfKClass: KClass<*>) {
    startActivity(Intent(activity, targetActivityOfKClass.java))
}

fun View.getActivity(): Activity? {
    var context = context
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

fun Dialog.getActivity(): Activity? {
    var context = context
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

fun View.setBackgroundColorRes(@ColorRes colorRes: Int) {
    setBackgroundColor(getColorInt(colorRes))
}

fun TextView.setTextColorRes(@ColorRes colorRes: Int) {
    setTextColor(getColorInt(colorRes))
}

@IntDef(value = [Toast.LENGTH_SHORT, Toast.LENGTH_LONG])
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class SystemToastDurationLevel

/**
 * @param message      要显示的文本
 * @param durationLevel 显示时长，单位：毫秒
 */
fun showSystemToast(message: CharSequence, @SystemToastDurationLevel durationLevel: Int = Toast.LENGTH_SHORT): Toast {
    return Toast(appContext).apply {
        duration = durationLevel
        setGravity(Gravity.CENTER, 0, 0)
        view = TextView(appContext).apply {
            val width = dp2px(20.0f)
            setPadding(width, width, width, width)
            setBackgroundDrawable(getRoundRectShapeDrawable(Color.parseColor("#BA000000")))
            text = message
            setTextColor(Color.WHITE)
            textSize = 16f
        }
    }
}

fun showSystemToast(@StringRes stringRes: Int, @SystemToastDurationLevel duration: Int = Toast.LENGTH_SHORT): Toast {
    return showSystemToast(appContext.resources.getText(stringRes), duration)
}

fun getRoundRectShapeDrawable(color: Int, radius: Float = 10f): ShapeDrawable {
    val outerR = floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
    return ShapeDrawable(RoundRectShape(outerR, null, null)).apply {
        paint.color = color
    }
}

fun dp2px(@IntRange(from = 0) dp: Int): Int = (dp * appContext.resources.displayMetrics.density + 0.5).toInt()

fun dp2px(@FloatRange(from = 0.0) dp: Float): Int = (dp * appContext.resources.displayMetrics.density + 0.5).toInt()

@ColorInt
fun getColorInt(@ColorRes colorRes: Int) = ContextCompat.getColor(appContext, colorRes)

fun getDrawable(@DrawableRes drawableRes: Int) = ContextCompat.getDrawable(appContext, drawableRes)

fun getDimension(@DimenRes dimenRes: Int): Float = appContext.resources.getDimension(dimenRes)

fun getDimensionPixelSize(@DimenRes dimenRes: Int): Int = appContext.resources.getDimensionPixelSize(dimenRes)

@ColorInt
fun String.toColorInt(@ColorInt defaultColorInt: Int = Color.WHITE) =
    try {
        Color.parseColor(this)
    } catch (e: Exception) {
        defaultColorInt
    }

fun ImageView.displayImage(bitmap: Bitmap) {
    setImageBitmap(bitmap)
}

fun isWeiBoInstalled() = isAppInstalled("com.sina.weibo")

fun isAppInstalled(targetPackageName: String) = appContext.packageManager.getInstalledPackages(0)?.any { targetPackageName == it.packageName }
    ?: false

fun udid() = getIMEI().takeUnless { it == "" } ?: androidId() ?: ""

fun androidId() = Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
    ?: ""

/**
 * 获取系统唯一标志
 */
val phoneModel by lazy { Build.MODEL }

/**
 * 获取手机制造厂商
 */
val manufacturer by lazy { Build.MANUFACTURER }

/**
 * 获取系统版本号
 */
val osVersionCode by lazy { Build.VERSION.SDK_INT }

/**
 * 获取系统代号
 */
val osVersionName by lazy { Build.VERSION.RELEASE }

/***
 * 获取应用的名称
 */
val myAppName by lazy {
    try {
        val pm = appContext.packageManager
        val applicationInfo = pm.getApplicationInfo(appContext.packageName, 0)
        pm.getApplicationLabel(applicationInfo) as String
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

/**
 * 获取软件的版本号
 */
val myVersionCode by lazy {
    try {
        val pm = appContext.packageManager
        val packageInfo = pm.getPackageInfo(appContext.packageName, 0)
        packageInfo.versionCode
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}

/**
 * 获取软件的版本代号
 */
val myVersionName by lazy {
    try {
        val pm = appContext.packageManager
        val packageInfo = pm.getPackageInfo(appContext.packageName, 0)
        packageInfo.versionName
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

/**
 * 获取包名
 */
val myPackageName by lazy { appContext.packageName }

/**
 * 获取IMEI号
 * 需要申请权限 android.permission.READ_PHONE_STATE
 */
@SuppressLint("MissingPermission")
fun getIMEI(): String {
    return try {
        val tm = appContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tm.deviceId
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

/**
 * 判断外部存储器是否可用
 */
fun isExternalStorageAvailable() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

/**
 * 获取外部存储器的路径
 */
fun getExternalStorageDirectory(): String? = Environment.getExternalStorageDirectory().absolutePath

/**
 * 获取SD卡的路径
 */
val mySDPath by lazy { if (isExternalStorageAvailable()) getExternalStorageDirectory() + "/" + appContext.packageName else "" }

/**
 * 获取ROM上的私有目录
 */
val internalDir by lazy { appContext.filesDir.absolutePath }

/**
 * 获取我们的私有的目录，优先在SD卡上
 */
val myDir by lazy {
    var dir = mySDPath
    if (TextUtils.isEmpty(dir)) {
        dir = internalDir
    }

    val file = File(dir)
    if (!file.exists()) {
        file.mkdirs()
    }

    dir
}

val screenWidth by lazy {
    val metric = DisplayMetrics()
    val windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    windowManager.defaultDisplay.getMetrics(metric)
    metric.widthPixels
}

val screenHeight by lazy {
    val metric = DisplayMetrics()
    val windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    windowManager.defaultDisplay.getMetrics(metric)
    metric.heightPixels
}

val screenDensity by lazy {
    val metric = DisplayMetrics()
    val windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    windowManager.defaultDisplay.getMetrics(metric)
    metric.density
}

/**
 * 获取当前进程的进程名
 * @return         进程名
 */
fun getCurrentProcessName(): String {
    val pid = android.os.Process.myPid()
    val activityManager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (processInfo in activityManager.runningAppProcesses) {
        if (processInfo.pid == pid) {
            return processInfo.processName
        }
    }
    return ""
}

fun Context.uriFromRes(@StringRes stringRes: Int): Uri? {
    val r = resources ?: return null
    return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
        + r.getResourcePackageName(stringRes) + "/"
        + r.getResourceTypeName(stringRes) + "/"
        + r.getResourceEntryName(stringRes))
}

fun Context.getMipmapRes(name: String) = resources.getIdentifier(name, "mipmap", packageName)

class UnicodeCharacterCountFilter(val max: Int) : InputFilter {

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        val srcStr = source.toString()

        val srcLength = srcStr.getUnicodeCharacterCount()

        val destStr = dest.toString()
        val destLength = destStr.getUnicodeCharacterCount()

        return if (srcLength + destLength > max) {
            ""
        } else {
            null // keep original
        }
    }
}

//全局替换字体，必须明确声明你使用的字体是monospace
//<style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar">
//    ....
//    <item name="android:typeface">monospace</item>
//</style>
@Throws(Exception::class)
fun globalReplaceFont(appContext: Context, fontFileRelativeToAssetsDir: String) {
    Typeface::class.java.getDeclaredField("MONOSPACE").run {
        isAccessible = true
        set(null, Typeface.createFromAsset(appContext.assets, fontFileRelativeToAssetsDir))
    }
}