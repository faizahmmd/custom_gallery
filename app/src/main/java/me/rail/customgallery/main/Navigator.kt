package me.rail.customgallery.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import javax.inject.Inject

class Navigator @Inject constructor(activity: FragmentActivity) {
    private val fragmentManager = activity.supportFragmentManager

    fun replaceFragment(
        container: Int,
        fragment: Fragment,
        addToBackStack: Boolean = false
    ) {
        val transaction = fragmentManager.beginTransaction()

        if (addToBackStack) {
            transaction
                .addToBackStack(fragment::class.java.name)
                .add(container, fragment)
        } else {
            transaction.replace(container, fragment)
        }

        transaction.commit()
    }
}