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

package xyz.hisname.fireflyiii.ui.about

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.danielstone.materialaboutlibrary.MaterialAboutFragment
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.util.extension.getCompatDrawable
import xyz.hisname.fireflyiii.util.extension.getImprovedViewModel

class AboutFragment: MaterialAboutFragment() {

    private val aboutViewModel by lazy { getImprovedViewModel(AboutViewModel::class.java) }

    override fun getMaterialAboutList(context: Context): MaterialAboutList{
        return createMaterialAboutList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrieveData()
    }

    private fun retrieveData(){
        aboutViewModel.userSystem().observe(viewLifecycleOwner){ isSuccessful ->
            if(isSuccessful){
                refreshMaterialAboutList()
            }
        }
    }

    private fun createMaterialAboutList(): MaterialAboutList{
        val appCardBuilder = MaterialAboutCard.Builder()
        appCardBuilder.addItem(ConvenienceBuilder.createAppTitleItem(getString(R.string.app_name),
                getCompatDrawable(R.drawable.ic_piggy_bank)))
                .addItem(ConvenienceBuilder.createVersionActionItem(requireContext(),
                        IconicsDrawable(requireContext()).apply {
                            icon = GoogleMaterial.Icon.gmd_phone
                            sizeDp = 24
                        },
                resources.getString(R.string.mobile_version),false)).addItem(MaterialAboutActionItem.Builder()
                        .text(resources.getString(R.string.server_version))
                        .subText(aboutViewModel.serverVersion)
                        .icon(IconicsDrawable(requireContext()).apply{
                            icon = FontAwesome.Icon.faw_server
                            sizeDp = 24
                        })
                        .build())
                .addItem(MaterialAboutActionItem.Builder()
                        .text(resources.getString(R.string.api_version))
                        .subText(aboutViewModel.apiVersion)
                        .icon(IconicsDrawable(requireContext()).apply {
                            icon = FontAwesome.Icon.faw_globe
                            sizeDp = 24
                        })
                        .build())
                .addItem(MaterialAboutActionItem.Builder()
                        .text(resources.getString(R.string.operating_system))
                        .subText(aboutViewModel.userOs)
                        .icon(setUserOsIcon())
                        .build())
                .build()
        val authorCardBuilder = MaterialAboutCard.Builder()
        authorCardBuilder.title(resources.getString(R.string.author))
        authorCardBuilder.addItem(MaterialAboutActionItem.Builder()
                .text("Daniel Quah")
                .subText("emansih")
                .icon(IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_perm_identity
                    sizeDp = 24
                })
                .setOnClickAction {
                    requireContext().startActivity(Intent(Intent.ACTION_VIEW, "https://github.com/emansih".toUri()))
                }.build())
                .addItem(MaterialAboutActionItem.Builder()
                        .text(resources.getString(R.string.view_on_github))
                        .icon(IconicsDrawable(requireContext()).apply {
                            icon = GoogleMaterial.Icon.gmd_link
                            sizeDp = 24
                        })
                        .setOnClickAction {
                            requireContext().startActivity(Intent(Intent.ACTION_VIEW, "https://github.com/emansih/FireflyMobile".toUri()))
                        }.build())
        return MaterialAboutList(appCardBuilder.build(), authorCardBuilder.build())
    }

    // Because why not?
    private fun setUserOsIcon(): Drawable{
        return when {
            aboutViewModel.userOs.toLowerCase().contains("windows") -> IconicsDrawable(requireContext(),FontAwesome.Icon.faw_windows)
            aboutViewModel.userOs.toLowerCase().contains("linux") -> IconicsDrawable(requireContext(), FontAwesome.Icon.faw_linux)
            aboutViewModel.userOs.toLowerCase().contains("bsd") -> // yea... this is freebsd icon. sorry other BSDs
                IconicsDrawable(requireContext(), FontAwesome.Icon.faw_freebsd)
            else -> IconicsDrawable(requireContext(), FontAwesome.Icon.faw_server)
        }
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = resources.getString(R.string.about)
    }
}