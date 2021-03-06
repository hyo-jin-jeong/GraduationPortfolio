package com.kakao.smartmemo.Adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.kakao.smartmemo.Contract.MainAdapterContract
import com.kakao.smartmemo.R
import com.kakao.smartmemo.View.ManagementFragment
import com.kakao.smartmemo.View.MapFragment
import com.kakao.smartmemo.View.MemoListFragment
import com.kakao.smartmemo.View.TodoListFragment

private val TAB_TITLES = arrayOf(
    R.string.tab_text_1,
    R.string.tab_text_2,
    R.string.tab_text_3,
    R.string.tab_text_5
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm), MainAdapterContract.View, MainAdapterContract.Model {

    override fun getItem(position: Int): Fragment {
        when(position){
            0 -> {return MapFragment()
            }
            1 ->  {return MemoListFragment()
            }
            2 -> {return TodoListFragment()
            }
            3 -> {return  ManagementFragment()
            }

        }
        return MapFragment()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 4
    }

    override fun notifyAdapter() {
        notifyDataSetChanged()
    }

    override fun getUser() {

    }

    override fun getGroup() {

    }
}