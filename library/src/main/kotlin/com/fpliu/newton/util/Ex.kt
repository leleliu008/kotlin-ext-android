package com.fpliu.newton.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.*
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.fpliu.kotlin.util.jdk.exec
import com.fpliu.kotlin.util.jdk.getUnicodeCharacterCount
import java.io.File
import java.util.*
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
fun makeSystemToast(message: CharSequence, @SystemToastDurationLevel durationLevel: Int = Toast.LENGTH_SHORT): Toast {
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

fun makeSystemToast(@StringRes stringRes: Int, @SystemToastDurationLevel duration: Int = Toast.LENGTH_SHORT): Toast {
    return makeSystemToast(getString(stringRes), duration)
}

fun showSystemToast(message: CharSequence, @SystemToastDurationLevel durationLevel: Int = Toast.LENGTH_SHORT) {
    makeSystemToast(message, durationLevel).show()
}

fun showSystemToast(@StringRes stringRes: Int, @SystemToastDurationLevel duration: Int = Toast.LENGTH_SHORT) {
    makeSystemToast(stringRes, duration).show()
}

fun getRoundRectShapeDrawable(@ColorInt color: Int, @FloatRange(from = 1.0) radius: Float = 10f): ShapeDrawable {
    val outerR = floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
    return ShapeDrawable(RoundRectShape(outerR, null, null)).apply {
        paint.color = color
    }
}

fun dp2px(@IntRange(from = 0) dp: Int): Int = (dp * appContext.resources.displayMetrics.density + 0.5).toInt()

fun dp2px(@FloatRange(from = 0.0) dp: Float): Int = (dp * appContext.resources.displayMetrics.density + 0.5).toInt()

fun getString(@StringRes stringRes: Int): String = appContext.getString(stringRes)

fun getString(@StringRes stringRes: Int, formatArgs: Array<String>): String = appContext.getString(stringRes, *formatArgs)

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
        e.printStackTrace()
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

    File(dir).run { if (!exists()) mkdirs() }

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

//获取状态栏高度
fun getStatusBarHeight(): Int {
    val resources = appContext.resources
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        return resources.getDimensionPixelSize(resourceId)
    }
    return 0
}

//读取/default.prop文件中的内容，必须借助命令行工具
fun getprop(name: String): String? {
    return exec("getprop $name")
}

fun TextView.underline() {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

//含有2个元素的一维数组，表示距离屏幕左上角的点，此处作为一个域变量是为了避免重复new
private val locationOfViewOnScreen = IntArray(2)

/**
 * 判断触摸点是否在给定的view上
 */
fun MotionEvent.isIn(view: View): Boolean {
    //如果此时view被隐藏掉了，触摸点肯定不会落在此view上
    if (view.visibility == View.GONE) {
        return false
    }

    //获取此view在屏幕上的位置（以屏幕左上角为参照点）
    view.getLocationOnScreen(locationOfViewOnScreen)

    //获取触摸点相对于屏幕左上角的偏移量
    val rawX = this.rawX
    val rawY = this.rawY

    //如果触摸点处于此view的矩形区域内
    return (rawX >= locationOfViewOnScreen[0]
        && rawX <= locationOfViewOnScreen[0] + view.width
        && rawY >= locationOfViewOnScreen[1]
        && rawY <= locationOfViewOnScreen[1] + view.height)
}

/**
 * 截图 截图会引用一个bitmap对象，有时候会很大，需要释放调用view.destroyDrawingCache();
 */
fun View.snapshot(): Bitmap? {
    //先销毁旧的
    destroyDrawingCache()
    //设置为可以截图
    isDrawingCacheEnabled = true
    //获取截图
    return drawingCache
}

/**
 * 显示或者隐藏状态栏
 * @param show   是否显示
 */
fun Window.showOrHideStatusBar(show: Boolean) {
    val lp = attributes
    if (show) {
        lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
        attributes = lp
        addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    } else {
        lp.flags = lp.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
        attributes = lp
        clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }
}

/**
 * 弹出软键盘
 * @param view    在哪个视图上显示
 */
fun showSoftInput(view: View?) {
    val imm = appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

/**
 * 弹出软键盘
 * @param view      在哪个视图上显示
 * @param delayTime 延迟时间（单位：ms）
 */
fun showSoftInputDelay(view: View?, delayTime: Int) {
    if (view == null || delayTime < 0) {
        return
    }

    if (delayTime == 0) {
        showSoftInput(view)
    } else {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                showSoftInput(view)
            }
        }, delayTime.toLong())
    }
}

/**
 * 隐藏软键盘
 */
fun View.hideSoftInput() {
    val imm = appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (imm.isActive(this)) {
        imm.hideSoftInputFromWindow(this.windowToken, 0)
    }
}

/**
 * 输入法是否已激活
 * @param packageName 输入法应用的包名
 * @return
 */
fun isInputMethodEnabled(packageName: String): Boolean = getEnabledInputMethodInfo(packageName) != null

/**
 * 获取已激活输入法的详细信息
 * @param packageName 输入法应用的包名
 * @return
 */
fun getEnabledInputMethodInfo(packageName: String): InputMethodInfo? {
    val imm = appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val imeInfoList = imm.enabledInputMethodList
    if (imeInfoList != null) {
        for (imeInfo in imeInfoList) {
            if (packageName == imeInfo.packageName) {
                return imeInfo
            }
        }
    }
    return null
}

/**
 * 输入法是否已启用
 * @param packageName 输入法应用的包名
 * @return
 */
fun isInputMethodInUse(packageName: String): Boolean {
    val imeInfo = getEnabledInputMethodInfo(packageName)
    if (imeInfo != null) {
        val ourId = imeInfo.id
        // 当前输入法id
        val curId = Settings.Secure.getString(appContext.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)

        return ourId != null && ourId == curId
    }
    return false
}

/**
 * 设置在系统中改变字体大小不会影响我们的自体大小，如要考虑到美观
 * 此方法只在第一个Activity中调用就可以了，不需要在所有的Activity中调用
 */
fun Activity.remainFont() {
    val configuration = resources.configuration
    configuration.fontScale = 1.0f
    resources.updateConfiguration(configuration, null)
}

/**
 * 设置屏幕方向
 * @param portrait 是否是竖屏
 */
fun Activity.changeScreenOrientation(portrait: Boolean) {
    requestedOrientation = if (portrait) {
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    } else {
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
}

fun ListView.getTotalHeight(): Int {
    val adapter = adapter ?: return 0
    var totalHeight = 0
    for (i in 0 until adapter.count) {
        val convertView = adapter.getView(i, null, this)
        convertView.measure(0, 0)
        totalHeight += convertView.measuredHeight
    }
    totalHeight += dividerHeight * (adapter.count - 1)
    return totalHeight
}

fun GridView.getTotalHeight(numColumns: Int): Int {
    val adapter = adapter ?: return 0
    var totalHeight = 0
    val count = adapter.count
    var i = 0
    while (i < count) {
        val convertView = adapter.getView(i, null, this)
        convertView.measure(0, 0)
        totalHeight += convertView.measuredHeight
        i += numColumns
    }
    return totalHeight
}

/**
 * 获得焦点
 */
fun View.obtainFocus() {
    isFocusable = true
    isFocusableInTouchMode = true
    requestFocus()

    if (this is EditText) {
        val text = text.toString()
        val length = text.length
        setSelection(length, length)
    }
}

fun View.lostFocus() {
    isFocusable = false
    isFocusableInTouchMode = false
}

fun isAppOnForeground(): Boolean {
    val activityManager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    // Returns a list of application processes that are running on the device
    val appProcesses = activityManager.runningAppProcesses ?: return false

    for (appProcess in appProcesses) {
        // importance:
        // The relative importance level that the system places
        // on this process.
        // May be one of IMPORTANCE_FOREGROUND, IMPORTANCE_VISIBLE,
        // IMPORTANCE_SERVICE, IMPORTANCE_BACKGROUND, or IMPORTANCE_EMPTY.
        // These constants are numbered so that "more important" values are
        // always smaller than "less important" values.
        // processName:
        // The name of the process that this object is associated with.
        if (appProcess.processName == appContext.packageName && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
            return true
        }
    }
    return false
}

fun isActivityOnForeground(activityClazz: Class<out Activity>): Boolean {
    val activityManager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningTaskInfos = activityManager.getRunningTasks(1)
    if (runningTaskInfos != null) {
        val topActivity = runningTaskInfos[0].topActivity
        val activityPackageName = topActivity.packageName
        val currentPackageName = appContext.packageName
        return activityPackageName == currentPackageName && topActivity.className == activityClazz.name
    }
    return false
}

/**
 * 打开网络设置
 *
 * @param context 上下文
 */
fun startNetworkSettingActivity(context: Context?): Boolean {
    if (context == null) {
        return false
    }

    val intent = Intent(Settings.ACTION_SETTINGS).apply {
        flags = (Intent.FLAG_ACTIVITY_NEW_TASK
            or Intent.FLAG_ACTIVITY_SINGLE_TOP
            or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    return try {
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun newColorStateList(normal: Int, pressed: Int, focused: Int, unable: Int): ColorStateList {
    val colors = intArrayOf(pressed, focused, normal, focused, unable, normal)
    val states = arrayOfNulls<IntArray>(6)
    states[0] = intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled)
    states[1] = intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused)
    states[2] = intArrayOf(android.R.attr.state_enabled)
    states[3] = intArrayOf(android.R.attr.state_focused)
    states[4] = intArrayOf(android.R.attr.state_window_focused)
    states[5] = intArrayOf()
    return ColorStateList(states, colors)
}

@SuppressLint("MissingPermission")
fun isNetworkAvailable(): Boolean {
    // 获取系统的连接服务
    val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetInfo = connectivityManager.activeNetworkInfo
    return activeNetInfo != null && activeNetInfo.isConnected
}
