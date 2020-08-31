package xyz.hisname.fireflyiii.receiver

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.twofortyfouram.log.Lumberjack
import com.twofortyfouram.spackle.AndroidSdkVersion
import com.twofortyfouram.spackle.bundle.BundleScrubber
import xyz.hisname.fireflyiii.util.TaskerPlugin

// Code adapted from: https://git.oupsman.fr/oupson/photo-tasker-plugin/-/blob/master/app/src/main/java/oupson/phototaskerplugin/receiver/AbstractPluginSettingReceiver.kt
/**
 *
 * Abstract superclass for a plug-in setting BroadcastReceiver implementation.
 *
 * The plug-in receiver lifecycle is as follows:
 *
 *  1. [.onReceive] is called by the Android
 * frameworks.
 * onReceive() will verify that the Intent is valid.  If the Intent is invalid, the receiver
 * returns
 * immediately.  If the Intent appears to be valid, then the lifecycle continues.
 *  1. [.isBundleValid] is called to determine whether [ ][com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE] is valid. If the Bundle is
 * invalid, then the
 * receiver returns immediately.  If the bundle is valid, then the lifecycle continues.
 *  1. [.isAsync] is called to determine whether the remaining work should be performed on
 * a
 * background thread.
 *  1. [.firePluginSetting] is called to trigger
 * the plug-in setting's action.
 *
 *
 *
 * Implementations of this BroadcastReceiver must be registered in the Android
 * Manifest with an Intent filter for
 * [ACTION_FIRE_SETTING][com.twofortyfouram.locale.api.Intent.ACTION_FIRE_SETTING]. The
 * BroadcastReceiver must be exported, enabled, and cannot have permissions
 * enforced on it.
 *
 */
abstract class AbstractPluginSettingReceiver : AbstractAsyncReceiver() {
    /*
     * The multiple return statements in this method are a little gross, but the
     * alternative of nested if statements is even worse :/
     */
    override fun onReceive(context: Context, intent: Intent) {
        println("1: "  + isOrderedBroadcast)
        if (BundleScrubber.scrub(intent)) {
            return
        }
        Lumberjack.v("Received %s", intent) //$NON-NLS-1$
        /*
         * Note: It is OK if a host sends an ordered broadcast for plug-in
         * settings. Such a behavior would allow the host to optionally block until the
         * plug-in setting finishes.
         */if (com.twofortyfouram.locale.api.Intent.ACTION_FIRE_SETTING != intent.action) {
            Lumberjack
                    .e(
                            "Intent action is not %s",
                            com.twofortyfouram.locale.api.Intent.ACTION_FIRE_SETTING
                    ) //$NON-NLS-1$
            return
        }
        /*
         * Ignore implicit intents, because they are not valid. It would be
         * meaningless if ALL plug-in setting BroadcastReceivers installed were
         * asked to handle queries not intended for them. Ideally this
         * implementation here would also explicitly assert the class name as
         * well, but then the unit tests would have trouble. In the end,
         * asserting the package is probably good enough.
         */if (context.packageName != intent.getPackage()
                && ComponentName(context, this.javaClass.name) != intent
                        .component
        ) {
            Lumberjack.e("Intent is not explicit") //$NON-NLS-1$
            return
        }
        val bundle = intent
                .getBundleExtra(com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE)
        if (BundleScrubber.scrub(intent)) {
            return
        }
        if (null == bundle) {
            Lumberjack.e(
                    "%s is missing",
                    com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE
            ) //$NON-NLS-1$
            return
        }
        if (!isBundleValid(bundle)) {
            Lumberjack.e(
                    "%s is invalid",
                    com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE
            ) //$NON-NLS-1$
            return
        }
        if (isAsync) {
            val callback: AsyncCallback = object : AsyncCallback {
                override fun runAsync(): Int {
                    firePluginSetting(context, intent, bundle)
                    return Activity.RESULT_OK
                }
            }
            goAsyncWithCallback(callback, isOrderedBroadcast)
        } else {
            firePluginSetting(context, intent, bundle)
            if (isOrderedBroadcast){
                resultCode = TaskerPlugin.Setting.RESULT_CODE_PENDING
            }
        }

    }

    /**
     *
     * Gives the plug-in receiver an opportunity to validate the Bundle, to
     * ensure that a malicious application isn't attempting to pass
     * an invalid Bundle.
     *
     *
     * This method will be called on the BroadcastReceiver's Looper (normatively the main thread)
     *
     *
     * @param bundle The plug-in's Bundle previously returned by the edit
     * Activity.  `bundle` should not be mutated by this method.
     * @return true if `bundle` appears to be valid.  false if `bundle` appears to be
     * invalid.
     */
    protected abstract fun isBundleValid(bundle: Bundle): Boolean

    /**
     * Configures the receiver whether it should process the Intent in a
     * background thread. Plug-ins should return true if their
     * [.firePluginSetting] method performs any
     * sort of disk IO (ContentProvider query, reading SharedPreferences, etc.).
     * or other work that may be slow.
     *
     *
     * Asynchronous BroadcastReceivers are not supported prior to Honeycomb, so
     * with older platforms broadcasts will always be processed on the BroadcastReceiver's Looper
     * (which for Manifest registered receivers will be the main thread).
     *
     * @return True if the receiver should process the Intent in a background
     * thread. False if the plug-in should process the Intent on the
     * BroadcastReceiver's Looper (normatively the main thread).
     */
    protected abstract val isAsync: Boolean

    /**
     * If [.isAsync] returns true, this method will be called on a
     * background thread. If [.isAsync] returns false, this method will
     * be called on the main thread. Regardless of which thread this method is
     * called on, this method MUST return within 10 seconds per the requirements
     * for BroadcastReceivers.
     *
     * @param context BroadcastReceiver context.
     * @param bundle  The plug-in's Bundle previously returned by the edit
     * Activity.
     */
    protected abstract fun firePluginSetting(
            context: Context,
            intent: Intent,
            bundle: Bundle
    )
}