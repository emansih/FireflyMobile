package xyz.hisname.fireflyiii.util

import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter


/*
   Extension of FragmentStatePagerAdapter which intelligently caches
   all active fragments and manages the fragment lifecycles.
   Usage involves extending from SmartFragmentStatePagerAdapter as you would any other PagerAdapter.
   Code taken from: https://gist.github.com/nesquena/c715c9b22fb873b1d259
*/
abstract class SmartFragmentStatePagerAdapter<T: Fragment>(fragmentManager: FragmentManager): FragmentStatePagerAdapter(fragmentManager) {

    private val registeredFragments = SparseArray<T>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as T
        registeredFragments.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        registeredFragments.remove(position)
        super.destroyItem(container, position, `object`)
    }


}