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

package xyz.hisname.fireflyiii.ui.base

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.extension.consume

abstract class BaseDetailFragment: BaseFragment(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // TODO: Remove this...
        if(this.javaClass != TransactionDetailsFragment::class.java){
            inflater.inflate(R.menu.detail_menu, menu)
            super.onCreateOptionsMenu(menu, inflater)
        }
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar)
            .menu.findItem(R.id.appWideSearch)
            .isVisible = false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = resources.getString(R.string.details)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = resources.getString(R.string.details)
    }

    abstract fun deleteItem()
    abstract fun editItem()

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        android.R.id.home -> consume {
            parentFragmentManager.popBackStack()
        }
        R.id.menu_item_delete -> consume {
            deleteItem()
        }
        R.id.menu_item_edit -> consume {
            editItem()
        }
        else -> super.onOptionsItemSelected(item)
    }
}