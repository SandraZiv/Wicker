package hr.fer.android.wicker.adapters;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import hr.fer.android.wicker.R;
import hr.fer.android.wicker.activities.MainActivity;
import hr.fer.android.wicker.activities.MainFragment;
import hr.fer.android.wicker.entity.Counter;

/**
 * Implementation of PagerAdapter that represents each page as a Fragment
 * that is persistently kept in the fragment manager as long as the user can return to the page.
 * <p>
 * This implementation uses {@link Counter} to share data from {@link MainActivity}
 */
public class SwipePageAdapter extends FragmentPagerAdapter {
    private Context context;

    public SwipePageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return MainFragment.createFragment(position + 1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return position == 0 ? context.getString(R.string.counter) : context.getString(R.string.info);
    }

    @Override
    public int getCount() {
        return 2; //number of pages
    }
}
