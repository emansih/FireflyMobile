package xyz.hisname.fireflyiii.ui

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
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
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.ui.about.AboutFragment
import xyz.hisname.fireflyiii.ui.account.ListAccountFragment
import xyz.hisname.fireflyiii.ui.base.BaseActivity
import xyz.hisname.fireflyiii.ui.bills.ListBillFragment
import xyz.hisname.fireflyiii.ui.dashboard.DashboardFragment
import xyz.hisname.fireflyiii.ui.transaction.TransactionFragment
import xyz.hisname.fireflyiii.ui.piggybank.ListPiggyFragment
import xyz.hisname.fireflyiii.ui.rules.RulesFragment
import xyz.hisname.fireflyiii.ui.settings.SettingsFragment
import xyz.hisname.fireflyiii.ui.transaction.AddTransactionFragment
import xyz.hisname.fireflyiii.util.DeviceUtil


class HomeActivity: BaseActivity(){

    private var result: Drawer? = null
    private lateinit var headerResult: AccountHeader
    private var profile: IProfile<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        animateToolbar()
        setUpHeader(savedInstanceState)
        setSupportActionBar(activity_toolbar)
        setUpDrawer(savedInstanceState)
        supportActionBar?.title = ""
        setNavIcon()
        if(intent.getStringExtra("transaction") != null) {
            val transaction = intent.getStringExtra("transaction")
            when (transaction) {
                "Withdrawal" -> {
                    val bundle = bundleOf("transactionType" to "Withdrawal")
                    changeFragment(AddTransactionFragment().apply { arguments = bundle }, "addTrans")
                }
                "Deposit" -> {
                    val bundle = bundleOf("transactionType" to "Deposit")
                    changeFragment(AddTransactionFragment().apply { arguments = bundle }, "addTrans")

                }
                "Transfer" -> {
                    val bundle = bundleOf("transactionType" to "Transfer")
                    changeFragment(AddTransactionFragment().apply { arguments = bundle }, "addTrans")
                }
            }
        } else {
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, DashboardFragment(), "dash")
                        .commit()
            }
        }
    }

    private fun setUpHeader(savedInstanceState: Bundle?){
        profile = ProfileDrawerItem()
                .withName(AppPref(this).userEmail)
                .withEmail(AppPref(this).userRole)
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
                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_blue_A400))
                .withIconTintingEnabled(true)
                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_credit_card).sizeDp(24))
                .withSelectable(false)
                .withSubItems(
                        SecondaryDrawerItem().withName("Asset Accounts")
                                .withLevel(3)
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_cyan_A400))
                                .withIconTintingEnabled(true)
                                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_money_bill).sizeDp(24))
                                .withIdentifier(3),
                        SecondaryDrawerItem().withName("Expense Accounts")
                                .withLevel(3)
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_yellow_400))
                                .withIconTintingEnabled(true)
                                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_shopping_cart).sizeDp(24))
                                .withIdentifier(4),
                        SecondaryDrawerItem().withName("Revenue Accounts")
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_black_1000))
                                .withIconTintingEnabled(true)
                                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_download).sizeDp(24))
                                .withLevel(3)
                                .withIdentifier(5),
                        SecondaryDrawerItem().withName("Liability Accounts")
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_deep_purple_500))
                                .withIconTintingEnabled(true)
                                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_ticket_alt).sizeDp(24))
                                .withLevel(3)
                                .withIdentifier(21)
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
                        SecondaryDrawerItem().withName("Withdrawals")
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
                                .withIdentifier(16),
                        SecondaryDrawerItem().withName("Rules")
                                .withLevel(4)
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_brown_500))
                                .withIconTintingEnabled(true)
                                // Rules -> ruler icon, get it?
                                .withIcon(R.drawable.ic_ruler)
                                .withIdentifier(17)/*,
                        SecondaryDrawerItem().withName("Recurring Transactions")
                                .withLevel(4)
                                .withIdentifier(18)*/

                )
        val settings = PrimaryDrawerItem()
                .withIdentifier(19)
                .withName("Settings")
                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_teal_500))
                .withIconTintingEnabled(true)
                .withIcon(R.drawable.ic_settings)
        val about = PrimaryDrawerItem()
                .withIdentifier(20)
                .withName("About")
                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_pink_500))
                .withIconTintingEnabled(true)
                .withIcon(R.drawable.ic_perm_identity_black_24dp)

        result = DrawerBuilder()
                .withActivity(this)
                .withFullscreen(true)
                .withToolbar(activity_toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(dashboard, transactions,account,/* budgets, categories, tags, reports,
                        ,*/ moneyManagement,settings, about)
                .withOnDrawerItemClickListener{ _, _, drawerItem ->
                    when {
                        drawerItem.identifier == 1L -> {
                            supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container,
                                            DashboardFragment(), "dash")
                                    .commit()
                        }
                        drawerItem.identifier == 3L -> {
                            val bundle = bundleOf("accountType" to "asset")
                            changeFragment(ListAccountFragment().apply { arguments = bundle })
                        }
                        drawerItem.identifier == 4L -> {
                            val bundle = bundleOf("accountType" to "expense")
                            changeFragment(ListAccountFragment().apply { arguments = bundle })
                        }
                        drawerItem.identifier == 5L -> {
                            val bundle = bundleOf("accountType" to "revenue")
                            changeFragment(ListAccountFragment().apply { arguments = bundle })
                        }
                        drawerItem.identifier == 11L -> {
                            val bundle = bundleOf("transactionType" to "Withdrawal")
                            changeFragment(TransactionFragment().apply { arguments = bundle })
                        }
                        drawerItem.identifier == 12L -> {
                            val bundle = bundleOf("transactionType" to "Deposit")
                            changeFragment(TransactionFragment().apply { arguments = bundle })
                        }
                        drawerItem.identifier == 13L -> {
                            val bundle = bundleOf("transactionType" to "Transfer")
                            changeFragment(TransactionFragment().apply { arguments = bundle })
                        }
                        drawerItem.identifier == 15L -> {
                            changeFragment(ListPiggyFragment())
                        }
                        drawerItem.identifier == 16L -> {
                            changeFragment(ListBillFragment())
                        }
                        drawerItem.identifier == 17L -> {
                            changeFragment(RulesFragment())
                        }
                        drawerItem.identifier == 19L -> {
                            changeFragment(SettingsFragment())
                        }
                        drawerItem.identifier == 20L -> {
                            changeFragment(AboutFragment())
                        }
                        drawerItem.identifier == 21L -> {
                            val bundle = bundleOf("accountType" to "liability")
                            changeFragment(ListAccountFragment().apply { arguments = bundle })
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
                .commit()
    }

    private fun changeFragment(fragment: Fragment, tag: String){
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .addToBackStack(null)
                .commit()
    }

    override fun onBackPressed() {
        when {
            result?.isDrawerOpen!! -> result?.closeDrawer()
            supportFragmentManager.backStackEntryCount > 1 -> supportFragmentManager.popBackStack()
            supportFragmentManager.backStackEntryCount == 0 -> {
                if(supportFragmentManager.findFragmentByTag("dash") is DashboardFragment){
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
                when {
                    supportFragmentManager.findFragmentByTag("addTrans") is AddTransactionFragment -> {
                        val drawerLayout = result?.drawerLayout
                        drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                        result?.actionBarDrawerToggle?.isDrawerIndicatorEnabled = false
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    }
                    else -> {
                        // show back icon and lock nav drawer
                        val drawerLayout = result?.drawerLayout
                        drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                        result?.actionBarDrawerToggle?.isDrawerIndicatorEnabled = false
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    }
                }
            } else {
                val drawerLayout = result?.drawerLayout
                drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                result?.actionBarDrawerToggle?.isDrawerIndicatorEnabled = true
            }
        }
    }
}