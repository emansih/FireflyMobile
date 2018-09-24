package xyz.hisname.fireflyiii.ui

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import kotlinx.android.synthetic.main.activity_base.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.base.BaseActivity
import xyz.hisname.fireflyiii.ui.bills.ListBillFragment
import xyz.hisname.fireflyiii.ui.dashboard.DashboardFragment
import xyz.hisname.fireflyiii.ui.transaction.TransactionFragment
import xyz.hisname.fireflyiii.ui.piggybank.ListPiggyFragment
import xyz.hisname.fireflyiii.util.DeviceUtil


class HomeActivity: BaseActivity(){

    private var result: Drawer? = null
    private lateinit var headerResult: AccountHeader
    private var profile: IProfile<*>? = null
    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        animateToolbar()
        setUpHeader(savedInstanceState)
        setSupportActionBar(activity_toolbar)
        setUpDrawer(savedInstanceState)
        supportActionBar?.title = ""
        setNavIcon()
        if(savedInstanceState == null){
            val bundle = bundleOf("fireflyUrl" to baseUrl, "access_token" to accessToken)
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DashboardFragment().apply { arguments = bundle }, "dash")
                    .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                    .commit()
        }
    }

    private fun setUpHeader(savedInstanceState: Bundle?){
        val role = sharedPref.getString("userRole", "")
        val email = sharedPref.getString("userEmail","")
        profile = ProfileDrawerItem()
                .withName(email)
                .withEmail(role)
                .withIcon(R.drawable.ic_piggy_bank)
        headerResult = AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withSelectionListEnabledForSingleProfile(false)
                .withCompactStyle(true)
                .addProfiles(profile)
                .withSavedInstance(savedInstanceState)
                .build()
    }

    private fun setUpDrawer(savedInstanceState: Bundle?){
        val dashboard = PrimaryDrawerItem()
                .withIdentifier(1)
                .withName("Dashboard")
                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_deep_orange_500))
                .withIconTintingEnabled(true)
                .withIcon(R.drawable.ic_view_dashboard)
        val account = ExpandableDrawerItem().withName("Accounts")
                .withIdentifier(2)
                .withSelectable(false)
                .withSubItems(
                        SecondaryDrawerItem().withName("Asset Accounts")
                                .withLevel(3)
                                .withIdentifier(3),
                        SecondaryDrawerItem().withName("Expanse Accounts")
                                .withLevel(3)
                                .withIdentifier(4),
                        SecondaryDrawerItem().withName("Revenue Accounts")
                                .withLevel(3)
                                .withIdentifier(5)
                )
        val budgets = PrimaryDrawerItem()
                .withIdentifier(6)
                .withName("Budgets")
        val categories = PrimaryDrawerItem()
                .withIdentifier(7)
                .withName("Categories")
        val tags = PrimaryDrawerItem()
                .withIdentifier(8)
                .withName("Tags")
        val reports = PrimaryDrawerItem()
                .withIdentifier(9)
                .withName("Reports")
        val transactions = ExpandableDrawerItem().withName("Transactions")
                .withIcon(R.drawable.ic_refresh)
                .withIdentifier(10)
                .withSelectable(false)
                .withSubItems(
                        SecondaryDrawerItem().withName("Expenses")
                                .withLevel(3)
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_blue_grey_500))
                                .withIconTintingEnabled(true)
                                .withIcon(R.drawable.ic_arrow_left)
                                .withIdentifier(11),
                        SecondaryDrawerItem().withName("Revenue / Income")
                                .withLevel(3)
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_grey_500))
                                .withIconTintingEnabled(true)
                                .withIcon(R.drawable.ic_arrow_right)
                                .withIdentifier(12),
                        SecondaryDrawerItem().withName("Transfers")
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_green_500))
                                .withIconTintingEnabled(true)
                                .withIcon(R.drawable.ic_bank_transfer)
                                .withLevel(3)
                                .withIdentifier(13)
                )
        val moneyManagement = ExpandableDrawerItem().withName("Money Management")
                .withIdentifier(14)
                .withIcon(R.drawable.ic_euro_sign)
                .withSelectable(false)
                .withSubItems(
                        SecondaryDrawerItem().withName("Piggy Banks")
                                .withLevel(4)
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_red_500))
                                .withIconTintingEnabled(true)
                                .withIcon(R.drawable.ic_sort_descending)
                                .withIdentifier(15),
                        SecondaryDrawerItem().withName("Bills")
                                .withLevel(4)
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_amber_500))
                                .withIconTintingEnabled(true)
                                .withIcon(R.drawable.ic_calendar_blank)
                                .withIdentifier(16)/*,
                        SecondaryDrawerItem().withName("Rules")
                                .withLevel(4)
                                .withIdentifier(17),
                        SecondaryDrawerItem().withName("Recurring Transactions")
                                .withLevel(4)
                                .withIdentifier(18)*/

                )
        result = DrawerBuilder()
                .withActivity(this)
                .withFullscreen(true)
                .withToolbar(activity_toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(dashboard, transactions,/*account, budgets, categories, tags, reports,
                        ,*/ moneyManagement)
                .withOnDrawerItemClickListener{ _, _, drawerItem ->
                    when {
                        drawerItem.identifier == 1L -> {
                            val bundle = bundleOf("fireflyUrl" to baseUrl, "access_token" to accessToken)
                            supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container,
                                            DashboardFragment().apply { arguments = bundle }, "dash")
                                    .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                                    .commit()
                        }
                        drawerItem.identifier == 11L -> {
                            val bundle = bundleOf("fireflyUrl" to baseUrl,
                                    "access_token" to accessToken, "transactionType" to "expenses")
                            changeFragment(TransactionFragment().apply { arguments = bundle })
                        }
                        drawerItem.identifier == 12L -> {
                            val bundle = bundleOf("fireflyUrl" to baseUrl,
                                    "access_token" to accessToken, "transactionType" to "income")
                            changeFragment(TransactionFragment().apply { arguments = bundle })
                        }
                        drawerItem.identifier == 13L -> {
                            val bundle = bundleOf("fireflyUrl" to baseUrl,
                                    "access_token" to accessToken, "transactionType" to "transfers")
                            changeFragment(TransactionFragment().apply { arguments = bundle })
                        }
                        drawerItem.identifier == 15L -> {
                            val bundle = bundleOf("fireflyUrl" to baseUrl, "access_token" to accessToken)
                            changeFragment(ListPiggyFragment().apply { arguments = bundle })
                        }
                        drawerItem.identifier == 16L -> {
                            val bundle = bundleOf("fireflyUrl" to baseUrl, "access_token" to accessToken)
                            changeFragment(ListBillFragment().apply { arguments = bundle })
                        }
                        else -> {

                        }
                    }
                    false
                }
                .withOnDrawerNavigationListener {
                    onBackPressed()
                    true
                }
                .withSavedInstance(savedInstanceState)
                .build()
        supportActionBar?.setHomeButtonEnabled(true)
        result?.actionBarDrawerToggle?.isDrawerIndicatorEnabled = true
    }

    // sick animation stolen from here: http://frogermcs.github.io/Instagram-with-Material-Design-concept-is-getting-real/
    private fun animateToolbar(){
        val toolbarSize = DeviceUtil.dpToPx(56)
        activity_appbar.translationY = -toolbarSize.toFloat()
        activity_appbar.animate().translationY(0.toFloat()).setDuration(300).startDelay = 300
    }

    private fun changeFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                .commit()
    }

    override fun onBackPressed() {
        when {
            result?.isDrawerOpen!! -> result?.closeDrawer()
            supportFragmentManager.backStackEntryCount > 1 -> supportFragmentManager.popBackStack()
            supportFragmentManager.backStackEntryCount == 0 -> {
                val fragment = supportFragmentManager.findFragmentByTag("dash")
                if(fragment is DashboardFragment){
                    finish()
                } else {
                    result?.setSelection(1)
                }
            }
            else -> super.onBackPressed()
        }
    }

    private fun setNavIcon(){
        supportFragmentManager.addOnBackStackChangedListener {
            if(supportFragmentManager.backStackEntryCount >= 1){
                // show back icon and lock nav drawer
                val drawerLayout = result?.drawerLayout
                drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                result?.actionBarDrawerToggle?.isDrawerIndicatorEnabled = false
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            } else {
                val drawerLayout = result?.drawerLayout
                drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                result?.actionBarDrawerToggle?.isDrawerIndicatorEnabled = true
            }
        }
    }
}