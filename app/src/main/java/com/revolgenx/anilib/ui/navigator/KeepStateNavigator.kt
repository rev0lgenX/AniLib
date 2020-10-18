package com.revolgenx.anilib.fragment.navigator

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.annotation.CallSuper
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.ClassType
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.NavigatorProvider
import androidx.navigation.fragment.FragmentNavigator
import com.revolgenx.anilib.R
import timber.log.Timber
import java.util.*

/*todo:// backstate keeps adding i think handle properly*/
@Navigator.Name("keep_state_fragment")
class KeepStateNavigator(
    @NonNull private val mContext: Context
    , @NonNull private val mFragmentManager: FragmentManager
    , private val mContainerId: Int
) : Navigator<KeepStateNavigator.Destination>() {

    companion object{
        private const val KEY_BACK_STACK_IDS = "androidx-nav-fragment:navigator:backStackIds"
    }

    private val mBackStack = ArrayDeque<Int>()


    override fun navigate(
        destination: Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Extras?
    ): NavDestination? {

        if (mFragmentManager.isStateSaved) {
            Timber.i("Ignoring navigate() call: FragmentManager has already saved its state")
            return null
        }

        val destId = destination.id
        val tag = destId.toString()
        val transaction = mFragmentManager.beginTransaction()

        val currentFragment = mFragmentManager.primaryNavigationFragment

        if (currentFragment != null) {
            transaction.detach(currentFragment)
        }

        var fragment = mFragmentManager.findFragmentByTag(tag)
        if (fragment == null) {
            val className = destination.className
            fragment = mFragmentManager.fragmentFactory.instantiate(mContext.classLoader, className)
            fragment.arguments = args
            transaction.add(mContainerId, fragment, tag)
        } else {
            transaction.attach(fragment)
        }

        transaction.setPrimaryNavigationFragment(fragment)
        transaction.setReorderingAllowed(true)
        transaction.commit()
        mBackStack.add(destId)
        return destination
    }


    override fun createDestination(): Destination {
        return Destination(this)
    }

    override fun popBackStack(): Boolean {
        if (mBackStack.isEmpty()) {
            return false
        }
        if (mFragmentManager.isStateSaved) {
            Timber.i("Ignoring popBackStack() call: FragmentManager has already saved its state")
            return false
        }
        mFragmentManager.popBackStack(
            generateBackStackName(mBackStack.size, mBackStack.peekLast()),
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        mBackStack.removeLast()
        return true
    }

    private fun generateBackStackName(backStackIndex: Int, destId: Int): String {
        return "$backStackIndex-$destId"
    }


    override fun onSaveState(): Bundle? {
        val b = Bundle()
        val backStack = IntArray(mBackStack.size)
        var index = 0
        for (id in mBackStack) {
            backStack[index++] = id
        }
        b.putIntArray(KEY_BACK_STACK_IDS, backStack)
        return b
    }

    override fun onRestoreState(savedState: Bundle) {
        val backStack =
            savedState.getIntArray(KEY_BACK_STACK_IDS)
        if (backStack != null) {
            mBackStack.clear()
            for (destId in backStack) {
                mBackStack.add(destId)
            }
        }
    }


    /**
     * NavDestination specific to [FragmentNavigator]
     */
    @ClassType(Fragment::class)
    class Destination(fragmentNavigator: Navigator<*>) : NavDestination(fragmentNavigator) {

        private var mClassName: String? = null

        /**
         * Construct a new fragment destination. This destination is not valid until you set the
         * Fragment via [.setClassName].
         *
         * @param navigatorProvider The [NavController] which this destination
         * will be associated with.
         */
        constructor(navigatorProvider: NavigatorProvider) : this(
            navigatorProvider.getNavigator(
                FragmentNavigator::class.java
            )
        ) {
        }

        @CallSuper
        override fun onInflate(context: Context, attrs: AttributeSet) {
            super.onInflate(context, attrs)
            val a = context.resources.obtainAttributes(
                attrs,
                R.styleable.FragmentNavigator
            )
            val className = a.getString(R.styleable.FragmentNavigator_android_name)
            className?.let { setClassName(it) }
            a.recycle()
        }

        /**
         * Set the Fragment class name associated with this destination
         * @param className The class name of the Fragment to show when you navigate to this
         * destination
         * @return this [Destination]
         */
        fun setClassName(className: String): Destination {
            mClassName = className
            return this
        }

        /**
         * Gets the Fragment's class name associated with this destination
         *
         * @throws IllegalStateException when no Fragment class was set.
         */
        val className: String
            get() {
                checkNotNull(mClassName) { "Fragment class was not set" }
                return mClassName!!
            }

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append(super.toString())
            sb.append(" class=")
            if (mClassName == null) {
                sb.append("null")
            } else {
                sb.append(mClassName)
            }
            return sb.toString()
        }
    }
}