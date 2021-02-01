/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.util.extension

import android.content.Context
import android.widget.Toast
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import es.dmoral.toasty.Toasty
import xyz.hisname.fireflyiii.R
import androidx.fragment.app.Fragment as SupportFragment

fun Context.toastInfo(message: String, duration: Int = Toast.LENGTH_SHORT) =
        Toasty.info(this, message, duration).show()

fun Context.toastError(message: String?, duration: Int = Toast.LENGTH_SHORT) =
        Toasty.error(this, message.toString(), duration).show()

fun Context.toastSuccess(message: String, duration: Int = Toast.LENGTH_SHORT) =
        Toasty.success(this, message, duration).show()

fun SupportFragment.toastInfo(message: String, duration: Int = Toast.LENGTH_SHORT) =
        requireActivity().toastInfo(message, duration)

fun SupportFragment.toastError(message: String?, duration: Int = Toast.LENGTH_SHORT) =
        requireActivity().toastError(message, duration)

fun SupportFragment.toastSuccess(message: String, duration: Int = Toast.LENGTH_SHORT) =
        requireActivity().toastSuccess(message, duration)

fun SupportFragment.toastOffline(message: String, duration: Int = Toast.LENGTH_SHORT) =
       Toasty.custom(requireActivity(), message, IconicsDrawable(requireContext()).apply {
           icon = GoogleMaterial.Icon.gmd_cloud_off
           sizeDp = 24
       }, getCompatColor(R.color.darkBlue), getCompatColor(R.color.md_white_1000),
               duration, true, true).show()



