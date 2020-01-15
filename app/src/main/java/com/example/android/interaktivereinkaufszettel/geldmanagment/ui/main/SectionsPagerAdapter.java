package com.example.android.interaktivereinkaufszettel.geldmanagment.ui.main;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.android.interaktivereinkaufszettel.geldmanagment.Category;
import java.util.ArrayList;
import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

    private List<Category> categories;

    public SectionsPagerAdapter(Context context, List<Category> categories, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.categories = new ArrayList<>(categories);
    }

    @Override
    public Fragment getItem(int position) {
        return PlaceholderFragment.newInstance(categories.get(position), this);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return categories.get(position).gibName();
    }

    @Override
    public int getCount() {
        return categories.size();
    }
}