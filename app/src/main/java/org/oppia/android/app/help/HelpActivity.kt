package org.oppia.android.app.help

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.help.faq.FAQListActivity
import org.oppia.android.app.help.faq.RouteToFAQSingleListener
import org.oppia.android.app.help.faq.faqsingle.FAQSingleActivity
import org.oppia.android.app.help.thirdparty.ThirdPartyDependencyListActivity
import org.oppia.android.app.model.HelpActivityParams
import org.oppia.android.app.model.HelpActivityStateBundle
import org.oppia.android.app.model.PolicyPage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.HELP_ACTIVITY
import org.oppia.android.app.policies.PoliciesActivity
import org.oppia.android.app.policies.RouteToPoliciesListener
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

const val FAQ_LIST_FRAGMENT_TAG = "FAQListFragment.tag"
const val POLICIES_FRAGMENT_TAG = "PoliciesFragment.tag"
const val THIRD_PARTY_DEPENDENCY_LIST_FRAGMENT_TAG = "ThirdPartyDependencyListFragment.tag"
const val LICENSE_LIST_FRAGMENT_TAG = "LicenseListFragment.tag"
const val LICENSE_TEXT_FRAGMENT_TAG = "LicenseTextFragment.tag"

/** The help page activity for FAQs, third-party dependencies and policies page. */
class HelpActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  RouteToFAQListListener,
  RouteToFAQSingleListener,
  RouteToPoliciesListener,
  RouteToThirdPartyDependencyListListener,
  LoadFaqListFragmentListener,
  LoadPoliciesFragmentListener,
  LoadThirdPartyDependencyListFragmentListener,
  LoadLicenseListFragmentListener,
  LoadLicenseTextViewerFragmentListener {

  @Inject
  lateinit var helpActivityPresenter: HelpActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  private lateinit var selectedFragment: String
  private lateinit var selectedHelpOptionsTitle: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val args =
      intent.getProtoExtra(HELP_ACTIVITY_PARAMS_KEY, HelpActivityParams.getDefaultInstance())
    val isFromNavigationDrawer = args?.isFromNavigationDrawer ?: false

    val stateArgs = savedInstanceState?.getProto(
      HELP_ACTIVITY_STATE_KEY,
      HelpActivityStateBundle.getDefaultInstance()
    )

    selectedFragment =
      stateArgs?.selectedFragmentTag ?: FAQ_LIST_FRAGMENT_TAG
    val selectedDependencyIndex =
      stateArgs?.selectedDependencyIndex ?: 0
    val selectedLicenseIndex = stateArgs?.selectedLicenseIndex ?: 0

    selectedHelpOptionsTitle = stateArgs?.helpOptionsTitle
      ?: resourceHandler.getStringInLocale(R.string.faq_activity_title)

    val policiesActivityParams = stateArgs?.policiesActivityParams

    helpActivityPresenter.handleOnCreate(
      selectedHelpOptionsTitle,
      isFromNavigationDrawer,
      selectedFragment,
      selectedDependencyIndex,
      selectedLicenseIndex,
      policiesActivityParams
    )
    title = resourceHandler.getStringInLocale(R.string.menu_help)
  }

  companion object {
    /** Params key for HelpActivity. */
    const val HELP_ACTIVITY_PARAMS_KEY =
      "HelpActivity.params"

    /** Arguments key for HelpActivity saved state. */
    const val HELP_ACTIVITY_STATE_KEY =
      "HelpActivity.state"

    fun createHelpActivityIntent(
      context: Context,
      profileId: ProfileId?,
      isFromNavigationDrawer: Boolean
    ): Intent {
      val args = HelpActivityParams.newBuilder().apply {
        this.isFromNavigationDrawer = isFromNavigationDrawer
      }.build()
      val intent = Intent(context, HelpActivity::class.java)
      intent.putProtoExtra(HELP_ACTIVITY_PARAMS_KEY, args)
      intent.decorateWithScreenName(HELP_ACTIVITY)
      if (profileId != null) {
        intent.decorateWithUserProfileId(profileId)
      }
      return intent
    }
  }

  override fun onRouteToFAQList() {
    val intent = FAQListActivity.createFAQListActivityIntent(this)
    startActivity(intent)
  }

  override fun onRouteToThirdPartyDependencyList() {
    val intent = ThirdPartyDependencyListActivity.createThirdPartyDependencyListActivityIntent(this)
    startActivity(intent)
  }

  override fun loadFaqListFragment() {
    helpActivityPresenter.handleLoadFAQListFragment()
  }

  override fun loadThirdPartyDependencyListFragment() {
    helpActivityPresenter.handleLoadThirdPartyDependencyListFragment()
  }

  override fun loadLicenseListFragment(dependencyIndex: Int) {
    helpActivityPresenter.handleLoadLicenseListFragment(dependencyIndex)
  }

  override fun loadLicenseTextViewerFragment(dependencyIndex: Int, licenseIndex: Int) {
    helpActivityPresenter.handleLoadLicenseTextViewerFragment(dependencyIndex, licenseIndex)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    helpActivityPresenter.handleOnSavedInstanceState(outState)
  }

  // TODO(#3681): Add support to display Single FAQ in split mode on tablet devices.
  override fun onRouteToFAQSingle(question: String, answer: String) {
    startActivity(FAQSingleActivity.createFAQSingleActivityIntent(this, question, answer))
  }

  override fun onRouteToPolicies(policyPage: PolicyPage) {
    startActivity(PoliciesActivity.createPoliciesActivityIntent(this, policyPage))
  }

  override fun loadPoliciesFragment(policyPage: PolicyPage) {
    helpActivityPresenter.handleLoadPoliciesFragment(policyPage)
  }
}
