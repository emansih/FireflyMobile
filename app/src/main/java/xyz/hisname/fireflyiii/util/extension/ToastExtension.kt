package xyz.hisname.fireflyiii.util.extension

import android.content.Context
import android.widget.Toast
import es.dmoral.toasty.Toasty
import androidx.fragment.app.Fragment as SupportFragment

fun Context.toastInfo(message: String, duration: Int = Toast.LENGTH_SHORT) =
        Toasty.info(this, message, duration).show()

fun Context.toastError(message: String, duration: Int = Toast.LENGTH_SHORT) =
        Toasty.error(this, message, duration).show()

fun Context.toastSuccess(message: String, duration: Int = Toast.LENGTH_SHORT) =
        Toasty.success(this, message, duration).show()

fun SupportFragment.toastInfo(message: String, duration: Int = Toast.LENGTH_SHORT) =
        requireActivity().toastInfo(message, duration)

fun SupportFragment.toastError(message: String, duration: Int = Toast.LENGTH_SHORT) =
        requireActivity().toastError(message, duration)

fun SupportFragment.toastSuccess(message: String, duration: Int = Toast.LENGTH_SHORT) =
        requireActivity().toastSuccess(message, duration)
