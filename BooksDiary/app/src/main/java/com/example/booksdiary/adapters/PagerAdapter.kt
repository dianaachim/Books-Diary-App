package com.example.booksdiary.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.booksdiary.fragments.BooksFragment
import com.example.booksdiary.fragments.FavoritesFragment
import com.example.booksdiary.fragments.WishlistFragment

class PagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> WishlistFragment()
            2 -> FavoritesFragment()
            else -> { // Note the block
                BooksFragment()
            }
        }
    }
}