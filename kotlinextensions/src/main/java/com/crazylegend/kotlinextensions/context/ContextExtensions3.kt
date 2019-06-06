package com.crazylegend.kotlinextensions.context

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Typeface
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils.isEmpty
import android.util.TypedValue
import android.view.View
import androidx.annotation.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.crazylegend.kotlinextensions.enums.ContentColumns
import com.crazylegend.kotlinextensions.enums.ContentOrder
import com.crazylegend.kotlinextensions.toFile
import java.io.File
import java.io.InputStream
import java.util.*


/**
 * Created by hristijan on 2/27/19 to long live and prosper !
 */


/**
 * check if you can resolve the intent
 */
fun Context.isIntentResolvable(intent: Intent) =
    packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()


/**
 * start third party App
 *
 * *If App Installed ;)
 */
fun Context.startApp(packageName: String) =
    if (isAppInstalled(packageName)) startActivity(packageManager.getLaunchIntentForPackage(packageName)) else {
    }


/**
 * Check if an App is Installed on the user device.
 */
fun Context.isAppInstalled(packageName: String): Boolean {
    return try {
        packageManager.getApplicationInfo(packageName, 0)
        true
    } catch (ignore: Exception) {
        false
    }
}



/**
 * Checks if App is in Background
 */
fun Context.isBackground(pName: String = packageName): Boolean {
    activityManager.runningAppProcesses.forEach {
        @Suppress("DEPRECATION")
        if (it.processName == pName)
            return it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND
    }
    return false
}


/**
 * get Application Name,
 *
 * @property pName the Package Name of the Target Application, Default is Current.
 *
 * Provide Package or will provide the current App Detail
 */
@Throws(PackageManager.NameNotFoundException::class)
fun Context.getAppName(pName: String = packageName): String {
    return packageManager.getApplicationLabel(packageManager.getApplicationInfo(pName, 0)).toString()
}

/**
 * get Application Icon,
 *
 * @property pName the Package Name of the Target Application, Default is Current.
 *
 * Provide Package or will provide the current App Detail
 */
@Throws(PackageManager.NameNotFoundException::class)
fun Context.getAppIcon(pName: String = packageName): Drawable {
    return packageManager.getApplicationInfo(pName, 0).loadIcon(packageManager)
}

/**
 * get Application Size in Bytes
 *
 * @property pName the Package Name of the Target Application, Default is Current.
 *
 * Provide Package or will provide the current App Detail
 */
@Throws(PackageManager.NameNotFoundException::class)
fun Context.getAppSize(pName: String = packageName): Long {
    return packageManager.getApplicationInfo(pName, 0).sourceDir.toFile().length()
}

/**
 * get Application Apk File
 *
 * @property pName the Package Name of the Target Application, Default is Current.
 *
 * Provide Package or will provide the current App Detail
 */
@Throws(PackageManager.NameNotFoundException::class)
fun Context.getAppApk(pName: String = packageName): File {
    return packageManager.getApplicationInfo(pName, 0).sourceDir.toFile()
}


/**
 * get Application Version Name
 *
 * @property pName the Package Name of the Target Application, Default is Current.
 *
 * Provide Package or will provide the current App Detail
 */
@Throws(PackageManager.NameNotFoundException::class)
fun Context.getAppVersionName(pName: String = packageName): String {
    return packageManager.getPackageInfo(pName, 0).versionName
}

/**
 * get Application Version Code
 *
 * @property pName the Package Name of the Target Application, Default is Current.
 *
 * Provide Package or will provide the current App Detail
 */
@RequiresApi(Build.VERSION_CODES.P)
@Throws(PackageManager.NameNotFoundException::class)
fun Context.getAppVersionCode(pName: String = packageName): Long {
    return packageManager.getPackageInfo(pName, 0).longVersionCode
}


/**
 * Show Date Picker and Get the Picked Date Easily
 */
fun Context.showDatePicker(year: Int, month: Int, day: Int, onDatePicked: (year: Int, month: Int, day: Int) -> Unit) {
    DatePickerDialog(this, { _, pyear, pmonth, pdayOfMonth ->
        onDatePicked(pyear, pmonth, pdayOfMonth)
    }, year, month, day).show()
}

/**
 * Show the Time Picker and Get the Picked Time Easily
 */
fun Context.showTimePicker(
    currentDate: Date = com.crazylegend.kotlinextensions.dateAndTime.currentDate,
    is24Hour: Boolean = false,
    onDatePicked: (hour: Int, minute: Int) -> Unit
) {
    @Suppress("DEPRECATION")
    TimePickerDialog(this, { _, hourOfDay, minute ->
        onDatePicked(hourOfDay, minute)

    }, currentDate.hours, currentDate.minutes, is24Hour).show()
}

/**
 * get Android ID
 */
val Context.getAndroidID: String? @SuppressLint("HardwareIds")
get() {
    return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
}


/**
 * get Device IMEI
 *
 * Requires READ_PHONE_STATE Permission
 */
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("HardwareIds")
@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
fun Context.getIMEI() = telephonyManager?.imei


/**
 * Creates shortcut launcher for pre/post oreo devices
 */
@Suppress("DEPRECATION")
inline fun <reified T> Activity.createShortcut(title: String, @DrawableRes icon: Int) {
    val shortcutIntent = Intent(this, T::class.java)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { // code for adding shortcut on pre oreo device
        val intent = Intent("com.android.launcher.action.INSTALL_SHORTCUT")
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title)
        intent.putExtra("duplicate", false)
        val parcelable = Intent.ShortcutIconResource.fromContext(this, icon)
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, parcelable)
        this.sendBroadcast(intent)
       // println("added_to_homescreen")
    } else {
        val shortcutManager = this.getSystemService(ShortcutManager::class.java)
        if (shortcutManager.isRequestPinShortcutSupported) {
            val pinShortcutInfo = ShortcutInfo.Builder(this, "some-shortcut-")
                .setIntent(shortcutIntent)
                .setIcon(Icon.createWithResource(this, icon))
                .setShortLabel(title)
                .build()

            shortcutManager.requestPinShortcut(pinShortcutInfo, null)
           // println("added_to_homescreen")
        } else {
           // println("failed_to_add")
        }
    }
}


/**
 * Reboot the application
 *
 * @param[restartIntent] optional, desired activity to show after the reboot
 */
fun Context.reboot(restartIntent: Intent? = this.packageManager.getLaunchIntentForPackage(this.packageName)) {
    restartIntent?.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    if (this is Activity) {
        this.startActivity(restartIntent)
        finishAffinity(this)
    } else {
        restartIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(restartIntent)
    }
}

/* ********************************************
 *               Private methods              *
 ******************************************** */

 fun finishAffinity(activity: Activity) {
    activity.setResult(Activity.RESULT_CANCELED)
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> activity.finishAffinity()
        else -> ActivityCompat.finishAffinity(activity)
    }
}


inline fun Context.color(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)
inline fun Context.boolean(@BoolRes id: Int): Boolean = resources.getBoolean(id)
inline fun Context.integer(@IntegerRes id: Int): Int = resources.getInteger(id)
inline fun Context.dimen(@DimenRes id: Int): Float = resources.getDimension(id)
inline fun Context.dimenPixelSize(@DimenRes id: Int): Int = resources.getDimensionPixelSize(id)
inline fun Context.drawable(@DrawableRes id: Int): Drawable? = ContextCompat.getDrawable(this, id)

//Attr retrievers
fun Context.resolveColor(@AttrRes attr: Int, @ColorInt fallback: Int = 0): Int {
    val a = theme.obtainStyledAttributes(intArrayOf(attr))
    try {
        return a.getColor(0, fallback)
    } finally {
        a.recycle()
    }
}

fun Context.resolveDrawable(@AttrRes attr: Int): Drawable? {
    val a = theme.obtainStyledAttributes(intArrayOf(attr))
    try {
        return a.getDrawable(0)
    } finally {
        a.recycle()
    }
}

fun Context.resolveBoolean(@AttrRes attr: Int, fallback: Boolean = false): Boolean {
    val a = theme.obtainStyledAttributes(intArrayOf(attr))
    try {
        return a.getBoolean(0, fallback)
    } finally {
        a.recycle()
    }
}

fun Context.resolveString(@AttrRes attr: Int, fallback: String = ""): String {
    val v = TypedValue()
    return if (theme.resolveAttribute(attr, v, true)) v.string.toString() else fallback
}


fun Context.cancelNotification(notificationID: Int) = NotificationManagerCompat.from(this).cancel(notificationID)


fun Context.string(@StringRes str: Int): String {
    return getString(str)
}


fun Context.dimenInt(@DimenRes dmn: Int): Int {
    return resources.getDimensionPixelSize(dmn)
}

fun Context.int(@IntegerRes int: Int): Int {
    return resources.getInteger(int)
}

fun Context.font(@FontRes font: Int): Typeface? {
    return ResourcesCompat.getFont(this, font)
}

fun Context.stringArray(array: Int): Array<String> {
    return resources.getStringArray(array)
}

fun Context.intArray(array: Int): IntArray {
    return resources.getIntArray(array)
}

/**
 * Checks if a Broadcast can be resolved
 */
fun Context.canResolveBroadcast(intent: Intent) = packageManager.queryBroadcastReceivers(intent, 0).isNotEmpty()

/**
 * Checks if a Provider exists with given name
 */
fun Context.providerExists(providerName: String) = packageManager.resolveContentProvider(providerName, 0) != null


fun Context.watchYoutubeVideo(id: String) {
    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id"))
    val webIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("http://www.youtube.com/watch?v=$id")
    )
    try {
        this.startActivity(appIntent)
    } catch (ex: ActivityNotFoundException) {
        this.startActivity(webIntent)
    }

}

inline fun <reified T> Context.getAppWidgetsIdsFor(): IntArray {
    return AppWidgetManager.getInstance(this).getAppWidgetIds(
        ComponentName(this, T::class.java)
    )
}

fun Context?.openGoogleMaps(address: String?) {
    if (isEmpty(address))
        return

    val gmmIntentUri = Uri.parse("geo:0,0?q=${address?.trim()}")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.`package` = "com.google.android.apps.maps"
    this?.startActivity(mapIntent)
}

/**
 * Hides all the views passed in the arguments
 */
fun Context.hideViews(vararg views: View) = views.forEach { it.visibility = View.GONE }

/**
 * Shows all the views passed in the arguments
 */
fun Context.showViews(vararg views: View) = views.forEach { it.visibility = View.VISIBLE }

fun Context.unRegisterReceiverSafe(broadcastReceiver: BroadcastReceiver) {
    // needs to be in try catch in order to avoid crashing on Samsung Lollipop devices https://issuetracker.google.com/issues/37001269#c3
    try {
        this.unregisterReceiver(broadcastReceiver)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
    }
}

fun Context.getFontCompat(fontRes: Int): Typeface? {
    return ResourcesCompat.getFont(this, fontRes)
}

fun Context.registerReceiverSafe(broadcastReceiver: BroadcastReceiver, intentFilter: IntentFilter) {
    // needs to be in try catch in order to avoid crashing on Samsung Lollipop devices https://issuetracker.google.com/issues/37001269#c3
    try {
        this.registerReceiver(broadcastReceiver, intentFilter)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
    }
}

fun Context.getProductionApplicationId(): String {
    val applicationId = packageName
    return when {
        applicationId.contains(".stage") -> applicationId.dropLast(6)
        applicationId.contains(".debug") -> applicationId.dropLast(6)
        else -> applicationId
    }
}



fun Context.areNotificationsEnabled(): Boolean {
    return NotificationManagerCompat.from(this).areNotificationsEnabled()
}

fun Context.createInputStreamFromUri(uri: Uri): InputStream? {
    return contentResolver.openInputStream(uri)
}