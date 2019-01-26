package xyz.hisname.fireflyiii.ui

import android.accounts.AccountManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.google_material_typeface_library.GoogleMaterial
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
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerUIUtils
import kotlinx.android.synthetic.main.activity_base.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.ui.about.AboutFragment
import xyz.hisname.fireflyiii.ui.account.ListAccountFragment
import xyz.hisname.fireflyiii.ui.base.BaseActivity
import xyz.hisname.fireflyiii.ui.bills.ListBillFragment
import xyz.hisname.fireflyiii.ui.categories.CategoriesFragment
import xyz.hisname.fireflyiii.ui.currency.CurrencyListFragment
import xyz.hisname.fireflyiii.ui.dashboard.DashboardFragment
import xyz.hisname.fireflyiii.ui.transaction.TransactionFragment
import xyz.hisname.fireflyiii.ui.piggybank.ListPiggyFragment
import xyz.hisname.fireflyiii.ui.rules.RulesFragment
import xyz.hisname.fireflyiii.ui.settings.SettingsFragment
import xyz.hisname.fireflyiii.ui.tags.ListTagsFragment
import xyz.hisname.fireflyiii.ui.transaction.AddTransactionFragment
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
        setProfileImage()
        setUpHeader(savedInstanceState)
        setSupportActionBar(activity_toolbar)
        setUpDrawer(savedInstanceState)
        supportActionBar?.title = ""
        setNavIcon()
        setBottomNav()
        if(intent.getStringExtra("transaction") != null) {
            val transaction = intent.getStringExtra("transaction")
            val addTransactionFragment = AddTransactionFragment()
            when (transaction) {
                "Withdrawal" -> {
                    supportFragmentManager.commitNow(true) {
                        addTransactionFragment.arguments = bundleOf("transactionType" to "Withdrawal")
                        replace(R.id.bigger_fragment_container, addTransactionFragment, "fragmentTransaction")
                    }
                }
                "Deposit" -> {
                    supportFragmentManager.commitNow(true) {
                        addTransactionFragment.arguments = bundleOf("transactionType" to "Deposit")
                        replace(R.id.bigger_fragment_container, addTransactionFragment, "fragmentTransaction")
                    }
                }
                "Transfer" -> {
                    supportFragmentManager.commitNow(true) {
                        addTransactionFragment.arguments = bundleOf("transactionType" to "Transfer")
                        replace(R.id.bigger_fragment_container, addTransactionFragment, "fragmentTransaction")
                    }
                }
                // Home screen shortcut
                "transactionFragment" -> {
                    supportFragmentManager.commit {
                        addTransactionFragment.arguments = bundleOf("revealX" to globalFAB.width / 2, "revealY" to globalFAB.height / 2, "transactionType" to "Withdrawal")
                        replace(R.id.bigger_fragment_container, addTransactionFragment, "fragmentTransaction")
                        addToBackStack(null)
                    }
                }
            }
        }
        supportFragmentManager.commit{
            replace(R.id.fragment_container, DashboardFragment(), "dash")
        }
        globalFAB.setImageDrawable(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_add).sizeDp(24))
    }

    private fun setUpHeader(savedInstanceState: Bundle?){
        profile = ProfileDrawerItem()
                .withName(AuthenticatorManager(AccountManager.get(this)).userEmail)
                .withEmail(AppPref(sharedPref).userRole)
                .withIcon(Constants.PROFILE_URL)
        headerResult = AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withSelectionListEnabledForSingleProfile(false)
                .withCompactStyle(true)
                .withHeightDp(100)
                .addProfiles(profile)
                .withSavedInstance(savedInstanceState)
                .build()
        val headerView = headerResult.headerBackgroundView
        headerView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryLight))
    }

    private fun setProfileImage(){
        DrawerImageLoader.init(object : AbstractDrawerImageLoader(){
            override fun set(imageView: ImageView, uri: Uri?, placeholder: Drawable?, tag: String?) {
                super.set(imageView, uri, placeholder, tag)
                Glide.with(imageView.context)
                        .load(Constants.PROFILE_URL)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop())
                        .into(imageView)
            }
            override fun cancel(imageView: ImageView) {
                Glide.with(imageView.context).clear(imageView)
                super.cancel(imageView)
            }
            override fun placeholder(ctx: Context?, tag: String): Drawable {
                return when (tag) {
                    DrawerImageLoader.Tags.PROFILE.name -> DrawerUIUtils.getPlaceHolder(ctx)
                    DrawerImageLoader.Tags.ACCOUNT_HEADER.name -> IconicsDrawable(ctx).iconText(" ")
                            .backgroundColorRes(R.color.md_orange_500)
                            .sizeDp(56)
                    "customUrlItem" -> IconicsDrawable(ctx).iconText(" ")
                            .backgroundColorRes(R.color.md_orange_500)
                            .sizeDp(56)
                    else -> super.placeholder(ctx)
                }
            }
        })
    }

    private fun setUpDrawer(savedInstanceState: Bundle?){
        val dashboard = PrimaryDrawerItem()
                .withIdentifier(1)
                .withName(R.string.dashboard)
                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_deep_orange_500))
                .withIconTintingEnabled(true)
                .withIcon(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_dashboard).sizeDp(24))
        val account = ExpandableDrawerItem().withName(R.string.account)
                .withIdentifier(2)
                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_blue_A400))
                .withIconTintingEnabled(true)
                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_credit_card).sizeDp(24))
                .withSelectable(false)
                .withSubItems(
                        SecondaryDrawerItem().withName(R.string.asset_account)
                                .withLevel(3)
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_cyan_A400))
                                .withIconTintingEnabled(true)
                                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_money_bill).sizeDp(24))
                                .withIdentifier(3),
                        SecondaryDrawerItem().withName(R.string.expense_account)
                                .withLevel(3)
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_yellow_400))
                                .withIconTintingEnabled(true)
                                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_shopping_cart).sizeDp(24))
                                .withIdentifier(4),
                        SecondaryDrawerItem().withName(R.string.revenue_account)
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_black_1000))
                                .withIconTintingEnabled(true)
                                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_download).sizeDp(24))
                                .withLevel(3)
                                .withIdentifier(5),
                        SecondaryDrawerItem().withName(R.string.liability_account)
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
                .withName(R.string.categories)
                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                .withSelectedIconColor(ContextCompat.getColor(this,R.color.material_blue_grey_800))
                .withIconTintingEnabled(true)
                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_chart_bar).sizeDp(24))
        val tags = PrimaryDrawerItem()
                .withIdentifier(8)
                .withName(R.string.tags)
                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_green_400))
                .withIconTintingEnabled(true)
                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_tag).sizeDp(24))
        val reports = PrimaryDrawerItem()
                .withIdentifier(9)
                .withName("Reports")
        val transactions = ExpandableDrawerItem().withName(R.string.transaction)
                .withIcon(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_refresh).sizeDp(24))
                .withIdentifier(10)
                .withSelectable(false)
                .withSubItems(
                        SecondaryDrawerItem().withName(R.string.withdrawal)
                                .withLevel(3)
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_blue_grey_500))
                                .withIconTintingEnabled(true)
                                .withIcon(R.drawable.ic_arrow_left)
                                .withIdentifier(11),
                        SecondaryDrawerItem().withName(R.string.revenue_income_menu)
                                .withLevel(3)
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_grey_500))
                                .withIconTintingEnabled(true)
                                .withIcon(R.drawable.ic_arrow_right)
                                .withIdentifier(12),
                        SecondaryDrawerItem().withName(R.string.transfer)
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_green_500))
                                .withIconTintingEnabled(true)
                                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_exchange_alt).sizeDp(24))
                                .withLevel(3)
                                .withIdentifier(13)
                )
        val moneyManagement = ExpandableDrawerItem().withName(R.string.money_management)
                .withIdentifier(14)
                .withIcon(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_euro_symbol).sizeDp(24))
                .withSelectable(false)
                .withSubItems(
                        SecondaryDrawerItem().withName(R.string.piggy_bank)
                                .withLevel(4)
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_red_500))
                                .withIconTintingEnabled(true)
                                .withIcon(R.drawable.ic_sort_descending)
                                .withIdentifier(15),
                        SecondaryDrawerItem().withName(R.string.bill)
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
                                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_random).sizeDp(24))
                                .withIdentifier(17)/*,
                        SecondaryDrawerItem().withName("Recurring Transactions")
                                .withLevel(4)
                                .withIdentifier(18)*/

                )
        val options = ExpandableDrawerItem().withName(R.string.options)
                .withIdentifier(14)
                .withIcon(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_settings).sizeDp(24))
                .withSelectable(false)
                .withSubItems(
                        SecondaryDrawerItem().withName(R.string.settings)
                                .withLevel(4)
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_teal_500))
                                .withIconTintingEnabled(true)
                                .withIdentifier(19)
                                .withIcon(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_settings).sizeDp(24)),
                        SecondaryDrawerItem().withName(R.string.currency)
                                .withLevel(4)
                                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_pink_800))
                                .withIconTintingEnabled(true)
                                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_money_bill).sizeDp(24))
                                .withIdentifier(22)
                )
        val about = PrimaryDrawerItem()
                .withIdentifier(20)
                .withName("About")
                .withSelectedTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                .withSelectedIconColor(ContextCompat.getColor(this,R.color.md_pink_500))
                .withIconTintingEnabled(true)
                .withIcon(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_perm_identity).sizeDp(24))
        result = DrawerBuilder()
                .withActivity(this)
                .withFullscreen(true)
                .withToolbar(activity_toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(dashboard, transactions,account, tags, categories, /* budgets, tags, reports,
                        */ moneyManagement, options, about)
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
                        drawerItem.identifier == 7L ->{
                            changeFragment(CategoriesFragment())
                        }
                        drawerItem.identifier == 8L -> {
                            changeFragment(ListTagsFragment())
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
                        drawerItem.identifier == 22L -> {
                            changeFragment(CurrencyListFragment())
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
        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
        }
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
            supportFragmentManager.backStackEntryCount == 1 -> {
                if(supportFragmentManager.findFragmentByTag("fragmentTransaction") is AddTransactionFragment){
                    supportFragmentManager.commit {
                        remove(AddTransactionFragment())
                    }
                    findViewById<CoordinatorLayout>(R.id.fragment_add_transaction_root).isVisible = false
                    findViewById<FloatingActionButton>(R.id.globalFAB).isVisible = true
                    findViewById<BottomNavigationView>(R.id.transactionBottomView).isGone = true
                    findViewById<FrameLayout>(R.id.fragment_container).isVisible = true
                    val drawerLayout = result?.drawerLayout
                    drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    result?.actionBarDrawerToggle?.isDrawerIndicatorEnabled = true
                } else {
                    supportFragmentManager.popBackStack()
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

    private fun setBottomNav(){
        transactionBottomView.apply {
            menu.add(Menu.NONE, 0, Menu.NONE, R.string.withdrawal).setIcon(R.drawable.ic_arrow_left)
            menu.add(Menu.NONE, 1, Menu.NONE, R.string.income).setIcon(R.drawable.ic_arrow_right)
            menu.add(Menu.NONE, 2, Menu.NONE, R.string.transfer).icon = IconicsDrawable(this@HomeActivity)
                    .icon(FontAwesome.Icon.faw_exchange_alt)
                    .sizeDp(24)
        }
        transactionBottomView.setOnNavigationItemSelectedListener {item: MenuItem ->
            when(item.itemId){
                0 -> {
                    supportFragmentManager.commitNow(true) {
                        val addTransactionFragment = AddTransactionFragment()
                        addTransactionFragment.arguments = bundleOf("transactionType" to "Withdrawal")
                        replace(R.id.bigger_fragment_container, addTransactionFragment, "fragmentTransaction")
                    }
                    true
                }
                1 -> {
                    supportFragmentManager.commitNow(true) {
                        val addTransactionFragment = AddTransactionFragment()
                        addTransactionFragment.arguments = bundleOf("transactionType" to "Deposit")
                        replace(R.id.bigger_fragment_container, addTransactionFragment, "fragmentTransaction")

                    }
                    true
                }
                2 -> {
                    supportFragmentManager.commitNow(true) {
                        val addTransactionFragment = AddTransactionFragment()
                        addTransactionFragment.arguments = bundleOf("transactionType" to "Transfer")
                        replace(R.id.bigger_fragment_container, addTransactionFragment, "fragmentTransaction")
                    }
                    true
                }
                else -> {
                    false
                }
            }
        }
    }
}