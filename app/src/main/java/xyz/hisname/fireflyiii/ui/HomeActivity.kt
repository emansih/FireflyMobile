package xyz.hisname.fireflyiii.ui

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.mikepenz.iconics.IconicsColor
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.utils.backgroundColorRes
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerUIUtils
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import kotlinx.android.synthetic.main.activity_base.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.repository.GlobalViewModel
import xyz.hisname.fireflyiii.ui.about.AboutFragment
import xyz.hisname.fireflyiii.ui.account.ListAccountFragment
import xyz.hisname.fireflyiii.ui.base.BaseActivity
import xyz.hisname.fireflyiii.ui.bills.ListBillFragment
import xyz.hisname.fireflyiii.ui.categories.CategoriesFragment
import xyz.hisname.fireflyiii.ui.currency.CurrencyListFragment
import xyz.hisname.fireflyiii.ui.dashboard.DashboardFragment
import xyz.hisname.fireflyiii.ui.onboarding.OnboardingActivity
import xyz.hisname.fireflyiii.ui.transaction.TransactionFragmentV2
import xyz.hisname.fireflyiii.ui.piggybank.ListPiggyFragment
import xyz.hisname.fireflyiii.ui.rules.RulesFragment
import xyz.hisname.fireflyiii.ui.settings.SettingsFragment
import xyz.hisname.fireflyiii.ui.tags.ListTagsFragment
import xyz.hisname.fireflyiii.ui.transaction.TransactionFragmentV1
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionActivity
import xyz.hisname.fireflyiii.util.KeyguardUtil
import xyz.hisname.fireflyiii.util.extension.dpToPx
import xyz.hisname.fireflyiii.util.extension.getCompatColor
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastError


class HomeActivity: BaseActivity(){

    private val drawerToggle by lazy { ActionBarDrawerToggle(this,
            activity_base_root, activity_toolbar,
            com.mikepenz.materialdrawer.R.string.material_drawer_open,
            com.mikepenz.materialdrawer.R.string.material_drawer_close) }
    private lateinit var headerResult: AccountHeaderView
    private val globalViewModel by lazy { getViewModel(GlobalViewModel::class.java) }
    private val accountManager by lazy { AuthenticatorManager(AccountManager.get(this))  }
    private val keyguardUtil by lazy { KeyguardUtil(this) }
    private var instanceState: Bundle? = null

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(accountManager.authMethod.isBlank()|| sharedPref(this).baseUrl.isBlank()){
            AuthenticatorManager(AccountManager.get(this)).destroyAccount()
            val onboardingActivity = Intent(this, OnboardingActivity::class.java)
            startActivity(onboardingActivity)
            finish()
        } else {
            instanceState = savedInstanceState
            setContentView(R.layout.activity_base)
            if(keyguardUtil.isAppKeyguardEnabled()){
                keyguardUtil.initKeyguard()
            } else {
                setup(savedInstanceState)
                supportActionBar?.setHomeButtonEnabled(true)
            }
        }
    }

    private fun setup(savedInstanceState: Bundle?){
        animateToolbar()
        setProfileImage()
        setUpHeader(savedInstanceState)
        setSupportActionBar(activity_toolbar)
        setUpDrawer(savedInstanceState)
        supportActionBar?.title = ""
        setNavIcon()
        if (intent.getStringExtra("transaction") != null) {
            val transaction = intent.getStringExtra("transaction")
            when (transaction) {
                "Withdrawal" -> {
                    startActivity(Intent(this, AddTransactionActivity::class.java.apply {
                        bundleOf("transactionType" to "Withdrawal")
                    }))
                }
                "Deposit" -> {
                    startActivity(Intent(this, AddTransactionActivity::class.java.apply {
                        bundleOf("transactionType" to "Deposit")
                    }))
                }
                "Transfer" -> {
                    startActivity(Intent(this, AddTransactionActivity::class.java.apply {
                        bundleOf("transactionType" to "Transfer")
                    }))
                }
                // Home screen shortcut
                "transactionFragment" -> {
                    startActivity(Intent(this, AddTransactionActivity::class.java.apply {
                        bundleOf("transactionType" to "Withdrawal")
                    }))
                }
            }
        }
        supportFragmentManager.commit {
            replace(R.id.fragment_container, DashboardFragment(), "dash")
        }
        globalFAB.setImageDrawable(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_add).sizeDp(24))
    }

    private fun setUpHeader(savedInstanceState: Bundle?){
        val profile = ProfileDrawerItem()
                .withName(AuthenticatorManager(AccountManager.get(this)).userEmail)
                .withNameShown(true)
                .withEmail(sharedPref(this).userRole)
                .withIcon(Constants.PROFILE_URL)
        headerResult = AccountHeaderView(this).apply {
            addProfile(profile,0)
            withSavedInstance(savedInstanceState)
        }
        headerResult.accountHeaderBackground.setBackgroundColor(getCompatColor(R.color.colorPrimaryLight))
    }


    private fun setProfileImage(){
        DrawerImageLoader.init(object : DrawerImageLoader.IDrawerImageLoader{
            override fun cancel(imageView: ImageView) {
                Glide.with(imageView.context).clear(imageView)
            }

            override fun placeholder(ctx: Context): Drawable {
                return DrawerUIUtils.getPlaceHolder(ctx)
            }

            override fun placeholder(ctx: Context, tag: String?): Drawable {
                return when (tag) {
                    DrawerImageLoader.Tags.PROFILE.name -> DrawerUIUtils.getPlaceHolder(ctx)
                    DrawerImageLoader.Tags.ACCOUNT_HEADER.name -> IconicsDrawable(ctx).iconText(" ")
                            .backgroundColorRes(R.color.md_orange_500)
                            .sizeDp(56)
                    "customUrlItem" -> IconicsDrawable(ctx).iconText(" ")
                            .backgroundColorRes(R.color.md_orange_500)
                            .sizeDp(56)
                    else -> placeholder(ctx)
                }
            }

            @Deprecated("Remove this")
            override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable) {
                Glide.with(imageView.context)
                        .load(Constants.PROFILE_URL)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop())
                        .into(imageView)
            }

            override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String?) {
                Glide.with(imageView.context)
                        .load(Constants.PROFILE_URL)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop())
                        .into(imageView)
            }

        })
    }

    private fun setUpDrawer(savedInstanceState: Bundle?){
        val dashboard = PrimaryDrawerItem()
                .withIdentifier(1)
                .withName(R.string.dashboard)
                .withSelectedTextColor(getCompatColor(R.color.colorAccent))
                .withSelectedIcon(IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_dashboard)
                        .sizeDp(24)
                        .colorRes(R.color.md_deep_orange_500))
                .withIconTintingEnabled(true)
                .withIcon(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_dashboard).sizeDp(24))
        val account = ExpandableDrawerItem().withName(R.string.account)
                .withIdentifier(2)
                .withSelectedTextColor(getCompatColor(R.color.colorAccent))
                .withSelectedIcon(IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_credit_card)
                        .sizeDp(24)
                        .colorRes(R.color.md_blue_A400))
                .withIconTintingEnabled(true)
                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_credit_card).sizeDp(24))
                .withSelectable(false)
                .withSubItems(
                        SecondaryDrawerItem().withName(R.string.asset_account)
                                .withLevel(3)
                                .withSelectedTextColor(getCompatColor(R.color.colorAccent))
                                .withSelectedIcon(IconicsDrawable(this)
                                        .icon(FontAwesome.Icon.faw_money_bill)
                                        .sizeDp(24)
                                        .colorRes(R.color.md_cyan_A400))
                                .withIconTintingEnabled(true)
                                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_money_bill).sizeDp(24))
                                .withIdentifier(3),
                        SecondaryDrawerItem().withName(R.string.expense_account)
                                .withLevel(3)
                                .withSelectedTextColor(getCompatColor(R.color.colorAccent))
                                .withSelectedIcon(IconicsDrawable(this)
                                        .icon(FontAwesome.Icon.faw_shopping_cart)
                                        .sizeDp(24)
                                        .colorRes(R.color.md_yellow_400))
                                .withIconTintingEnabled(true)
                                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_shopping_cart).sizeDp(24))
                                .withIdentifier(4),
                        SecondaryDrawerItem().withName(R.string.revenue_account)
                                .withSelectedTextColor(getCompatColor(R.color.colorAccent))
                                .withSelectedIcon(IconicsDrawable(this)
                                        .icon(FontAwesome.Icon.faw_download)
                                        .sizeDp(24)
                                        .colorRes(R.color.md_black_1000))
                                .withIconTintingEnabled(true)
                                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_download).sizeDp(24))
                                .withLevel(3)
                                .withIdentifier(5),
                        SecondaryDrawerItem().withName(R.string.liability_account)
                                .withSelectedTextColor(getCompatColor(R.color.colorAccent))
                                .withSelectedIcon(IconicsDrawable(this)
                                        .icon(FontAwesome.Icon.faw_ticket_alt)
                                        .sizeDp(24)
                                        .colorRes(R.color.md_deep_purple_500))
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
                .withSelectedTextColor(getCompatColor(R.color.colorAccent))
                .withSelectedIcon(IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_chart_bar)
                        .sizeDp(24)
                        .colorRes(R.color.material_blue_grey_800))
                .withIconTintingEnabled(true)
                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_chart_bar).sizeDp(24))
        val tags = PrimaryDrawerItem()
                .withIdentifier(8)
                .withName(R.string.tags)
                .withSelectedTextColor(getCompatColor(R.color.colorAccent))
                .withSelectedIcon(IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_tag)
                        .sizeDp(24)
                        .colorRes(R.color.md_green_400))
                .withIconTintingEnabled(true)
                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_tag).sizeDp(24))
        val reports = PrimaryDrawerItem()
                .withIdentifier(9)
                .withName("Reports")
        val transactions = ExpandableDrawerItem().withName(R.string.transaction)
                .withIcon(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_refresh).sizeDp(24))
                .withIconTintingEnabled(true)
                .withIdentifier(10)
                .withSelectable(false)
                .withSubItems(
                        SecondaryDrawerItem().withName(R.string.withdrawal)
                                .withLevel(3)
                                .withSelectedTextColor(getCompatColor(R.color.colorAccent))
                                .withSelectedIcon(IconicsDrawable(this)
                                        .icon(FontAwesome.Icon.faw_arrow_left)
                                        .sizeDp(24)
                                        .colorRes(R.color.md_blue_grey_500))
                                .withIconTintingEnabled(true)
                                .withIcon(R.drawable.ic_arrow_left)
                                .withIdentifier(11),
                        SecondaryDrawerItem().withName(R.string.revenue_income_menu)
                                .withLevel(3)
                                .withSelectedTextColor(getCompatColor(R.color.colorAccent))
                                .withSelectedIcon(IconicsDrawable(this)
                                        .icon(FontAwesome.Icon.faw_arrow_right)
                                        .sizeDp(24)
                                        .colorRes(R.color.md_grey_500))
                                .withIconTintingEnabled(true)
                                .withIcon(R.drawable.ic_arrow_right)
                                .withIdentifier(12),
                        SecondaryDrawerItem().withName(R.string.transfer)
                                .withSelectedTextColor(getCompatColor(R.color.colorAccent))
                                .withSelectedIcon(IconicsDrawable(this)
                                        .icon(FontAwesome.Icon.faw_exchange_alt)
                                        .sizeDp(24)
                                        .colorRes(R.color.md_green_500))
                                .withIconTintingEnabled(true)
                                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_exchange_alt).sizeDp(24))
                                .withLevel(3)
                                .withIdentifier(13)
                )
        val moneyManagement = ExpandableDrawerItem().withName(R.string.money_management)
                .withIdentifier(14)
                .withIcon(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_euro_symbol).sizeDp(24))
                .withIconTintingEnabled(true)
                .withSelectable(false)
                .withSubItems(
                        SecondaryDrawerItem().withName(R.string.piggy_bank)
                                .withLevel(4)
                                .withSelectedTextColor(getCompatColor(R.color.colorAccent))
                                .withSelectedIcon(IconicsDrawable(this)
                                        .icon(FontAwesome.Icon.faw_sort_down)
                                        .sizeDp(24)
                                        .colorRes(R.color.md_red_500))
                                .withIconTintingEnabled(true)
                                .withIcon(R.drawable.ic_sort_descending)
                                .withIdentifier(15),
                        SecondaryDrawerItem().withName(R.string.bill)
                                .withLevel(4)
                                .withSelectedTextColor(getCompatColor(R.color.colorAccent))
                                .withSelectedIcon(IconicsDrawable(this)
                                        .icon(FontAwesome.Icon.faw_calendar)
                                        .sizeDp(24)
                                        .colorRes(R.color.md_amber_500))
                                .withIconTintingEnabled(true)
                                .withIcon(R.drawable.ic_calendar_blank)
                                .withIdentifier(16),
                        SecondaryDrawerItem().withName("Rules")
                                .withLevel(4)
                                .withSelectedTextColor(getCompatColor(R.color.colorAccent))
                                .withSelectedIcon(IconicsDrawable(this)
                                        .icon(FontAwesome.Icon.faw_random)
                                        .sizeDp(24)
                                        .colorRes(R.color.md_brown_500))
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
                .withIconTintingEnabled(true)
                .withSubItems(
                        SecondaryDrawerItem().withName(R.string.settings)
                                .withLevel(4)
                                .withSelectedTextColor(getCompatColor(R.color.colorAccent))
                                .withSelectedIcon(IconicsDrawable(this)
                                        .icon(GoogleMaterial.Icon.gmd_settings)
                                        .sizeDp(24)
                                        .colorRes(R.color.md_teal_500))
                                .withIconTintingEnabled(true)
                                .withIdentifier(19)
                                .withIcon(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_settings).sizeDp(24)),
                        SecondaryDrawerItem().withName(R.string.currency)
                                .withLevel(4)
                                .withSelectedTextColor(getCompatColor(R.color.colorAccent))
                                .withSelectedIcon(IconicsDrawable(this)
                                        .icon(FontAwesome.Icon.faw_money_bill)
                                        .sizeDp(24)
                                        .colorRes(R.color.md_pink_800))
                                .withIconTintingEnabled(true)
                                .withIcon(IconicsDrawable(this).icon(FontAwesome.Icon.faw_money_bill).sizeDp(24))
                                .withIdentifier(22)
                )
        val about = PrimaryDrawerItem()
                .withIdentifier(20)
                .withName("About")
                .withSelectedTextColor(getCompatColor(R.color.colorAccent))
                .withSelectedIcon(IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_perm_identity)
                        .sizeDp(24)
                        .colorRes(R.color.md_pink_500))
                .withIconTintingEnabled(true)
                .withIcon(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_perm_identity).sizeDp(24))
        slider.apply {
            itemAdapter.add(dashboard, transactions, account, tags, categories, /* budgets, tags, reports,
                        */ moneyManagement, options, about)
            accountHeader = headerResult
            onDrawerItemClickListener = { v: View?, drawerItem: IDrawerItem<*>, position: Int ->
                when (drawerItem.identifier) {
                    1L -> {
                        supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container,
                                        DashboardFragment(), "dash")
                                .commit()
                    }
                    3L -> {
                        val bundle = bundleOf("accountType" to "asset")
                        changeFragment(ListAccountFragment().apply { arguments = bundle })
                    }
                    4L -> {
                        val bundle = bundleOf("accountType" to "expense")
                        changeFragment(ListAccountFragment().apply { arguments = bundle })
                    }
                    5L -> {
                        val bundle = bundleOf("accountType" to "revenue")
                        changeFragment(ListAccountFragment().apply { arguments = bundle })
                    }
                    7L -> {
                        changeFragment(CategoriesFragment())
                    }
                    8L -> {
                        changeFragment(ListTagsFragment())
                    }
                    11L -> {
                        val bundle = bundleOf("transactionType" to "Withdrawal")
                        if (sharedPref(this@HomeActivity).transactionListType) {
                            changeFragment(TransactionFragmentV1().apply { arguments = bundle })

                        } else {
                            changeFragment(TransactionFragmentV2().apply { arguments = bundle })
                        }
                    }
                    12L -> {
                        val bundle = bundleOf("transactionType" to "Deposit")
                        if (sharedPref(this@HomeActivity).transactionListType) {
                            changeFragment(TransactionFragmentV1().apply { arguments = bundle })
                        } else {
                            changeFragment(TransactionFragmentV2().apply { arguments = bundle })
                        }
                    }
                    13L -> {
                        val bundle = bundleOf("transactionType" to "Transfer")
                        if (sharedPref(this@HomeActivity).transactionListType) {
                            changeFragment(TransactionFragmentV1().apply { arguments = bundle })
                        } else {
                            changeFragment(TransactionFragmentV2().apply { arguments = bundle })
                        }
                    }
                    15L -> {
                        changeFragment(ListPiggyFragment())
                    }
                    16L -> {
                        changeFragment(ListBillFragment())
                    }
                    17L -> {
                        changeFragment(RulesFragment())
                    }
                    19L -> {
                        changeFragment(SettingsFragment())
                    }
                    20L -> {
                        changeFragment(AboutFragment())
                    }
                    21L -> {
                        val bundle = bundleOf("accountType" to "liability")
                        changeFragment(ListAccountFragment().apply { arguments = bundle })
                    }
                    22L -> {
                        changeFragment(CurrencyListFragment())
                    }
                    else -> {

                    }
                }
                false
            }
            withSavedInstance(savedInstanceState)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerToggle.isDrawerIndicatorEnabled = true
        activity_base_root.addDrawerListener(drawerToggle)
    }

    // sick animation stolen from here: http://frogermcs.github.io/Instagram-with-Material-Design-concept-is-getting-real/
    private fun animateToolbar(){
        val toolbarSize = dpToPx(56)
        activity_appbar.translationY = -toolbarSize.toFloat()
        activity_appbar.animate().translationY(0f).setDuration(300).startDelay = 300
    }

    private fun changeFragment(fragment: Fragment){
        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
        }
    }

    override fun onBackPressed() {
        if(activity_base_root.isDrawerOpen(slider)) {
            activity_base_root.closeDrawer(slider)
        } else {
            when {
                //supportFragmentManager.backStackEntryCount > 1 -> supportFragmentManager.popBackStack()
                supportFragmentManager.backStackEntryCount == 0 -> {
                    if (supportFragmentManager.findFragmentByTag("dash") is DashboardFragment) {
                        finish()
                    } else {
                        slider.setSelection(1)
                    }
                }
                else -> {
                    globalViewModel.handleBackPress(true)
                }
            }
        }
    }

    private fun setNavIcon(){
        supportFragmentManager.addOnBackStackChangedListener {
            if(supportFragmentManager.backStackEntryCount >= 1){
                // show back icon and lock nav drawer
                activity_base_root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                drawerToggle.isDrawerIndicatorEnabled = false
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            } else {
                activity_base_root.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                drawerToggle.isDrawerIndicatorEnabled = true
            }
        }
        drawerToggle.setToolbarNavigationClickListener {
            onBackPressed()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(slider?.saveInstanceState(outState) ?: outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(2804 == requestCode){
            if(resultCode == RESULT_OK){
                setup(instanceState)
            } else {
                toastError("Authentication fail")
            }
        }
    }
}