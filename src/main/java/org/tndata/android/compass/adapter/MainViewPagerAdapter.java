package org.tndata.android.compass.adapter;

import java.util.ArrayList;

import org.tndata.android.compass.R;
import org.tndata.android.compass.fragment.CategoryFragment;
import org.tndata.android.compass.fragment.MyGoalsFragment;
import org.tndata.android.compass.model.Category;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class MainViewPagerAdapter extends FragmentStatePagerAdapter {
    private Context mContext;
    private ArrayList<Category> mCategories = new ArrayList<Category>();

    public MainViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    public void setCategories(ArrayList<Category> categories) {
        mCategories = categories;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0) {
            fragment = new MyGoalsFragment();
        } else {
            fragment = CategoryFragment.newInstance(mCategories
                    .get(position - 1));
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return mCategories.size() + 1;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return mContext.getResources().getString(R.string.my_goals_title)
                    .toUpperCase();
        } else {
            return mCategories.get(position - 1).getTitle().toUpperCase();
        }
    }

    public String getPositionImageUrl(int position) {
        if (position == 0) {
            return null;
        } else {
            return mCategories.get(position - 1).getImageUrl();
        }
    }

}