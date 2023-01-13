package xyz.hisname.fireflyiii.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Checks if the given permission has been granted.
 * @author Arnau Mora
 * @since 20221124
 */
fun Context.isPermissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/**
 * Alias for [Fragment.requireContext].[Context.isPermissionGranted].
 * Checks if the given permission has been granted.
 * @author Arnau Mora
 * @since 20221124
 * @throws IllegalStateException If not currently associated with a context.
 */
@Throws(IllegalStateException::class)
fun Fragment.isPermissionGranted(permission: String) =
    requireContext().isPermissionGranted(permission)

/**
 * Runs [ActivityResultLauncher.launch] with the [Manifest.permission.ACCESS_COARSE_LOCATION] and
 * [Manifest.permission.ACCESS_FINE_LOCATION] permissions.
 * @author Arnau Mora
 * @since 20221124
 */
fun ActivityResultLauncher<Array<String>>.launchLocationPermissionsRequest() = launch(
    arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
)
