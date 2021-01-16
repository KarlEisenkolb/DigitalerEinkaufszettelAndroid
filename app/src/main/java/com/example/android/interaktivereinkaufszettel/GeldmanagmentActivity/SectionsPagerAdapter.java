package com.example.android.interaktivereinkaufszettel.GeldmanagmentActivity;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Category;
import java.util.ArrayList;
import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

    private List<Category> categories;
    private PlaceholderFragment[] fragments;

    public SectionsPagerAdapter(List<Category> categories, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.categories = new ArrayList<>(categories);
        this.fragments = new PlaceholderFragment[categories.size()];
    }

    @Override
    public PlaceholderFragment getItem(int position) {
        if(fragments[position] == null)
            fragments[position] = PlaceholderFragment.newInstance(categories.get(position), position);
        return fragments[position];
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