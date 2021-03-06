package me.relex.viewpagerheaderscroll;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import me.relex.viewpagerheaderscroll.base.BaseActivity;
import me.relex.viewpagerheaderscroll.fragment.ListViewFragment;
import me.relex.viewpagerheaderscroll.tools.ScrollableFragmentListener;
import me.relex.viewpagerheaderscroll.tools.ScrollableListener;
import me.relex.viewpagerheaderscroll.tools.ViewPagerHeaderHelper;
import me.relex.viewpagerheaderscroll.widget.TouchCallbackLayout;

public class MainActivity extends BaseActivity
    implements TouchCallbackLayout.TouchEventListener, ScrollableFragmentListener,
    ViewPagerHeaderHelper.OnViewPagerTouchListener {

    private static final long DEFAULT_DURATION = 300L;
    private static final float DEFAULT_DAMPING = 1.5f;

    private SparseArrayCompat<ScrollableListener> mScrollableListenerArrays =
        new SparseArrayCompat<>();
    private ViewPager mViewPager;
    private View mHeaderLayoutView;
    private ViewPagerHeaderHelper mViewPagerHeaderHelper;

    private int mTouchSlop;
    private int mTabHeight;
    private int mHeaderHeight;

    private Interpolator mInterpolator = new DecelerateInterpolator();
    private Toolbar mToolbar;
    private TabLayout mTabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
       // mTabHeight = getResources().getDimensionPixelSize(R.dimen.tabs_height);

        mHeaderHeight = getResources().getDimensionPixelSize(R.dimen.viewpager_header_height);

        mViewPagerHeaderHelper = new ViewPagerHeaderHelper(this, this);

        TouchCallbackLayout touchCallbackLayout = (TouchCallbackLayout) findViewById(R.id.layout);
        touchCallbackLayout.setTouchEventListener(this);

        mHeaderLayoutView = findViewById(R.id.header);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(2);//set cache pager
        mViewPager.setPageMargin(5);
        mViewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        if (mTabLayout != null) {
            mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        }
        mTabLayout.setupWithViewPager(mViewPager);

        mTabHeight =mTabLayout.getHeight();
        //   slidingTabLayout.setViewPager(mViewPager);
        ViewCompat.setTranslationY(mViewPager, mHeaderHeight);

    }


    @Override public boolean onLayoutInterceptTouchEvent(MotionEvent event) {

        return mViewPagerHeaderHelper.onLayoutInterceptTouchEvent(event,
            mTabHeight + mHeaderHeight);
    }


    @Override public boolean onLayoutTouchEvent(MotionEvent event) {
        return mViewPagerHeaderHelper.onLayoutTouchEvent(event);
    }


    @Override public boolean isViewBeingDragged(MotionEvent event) {
        return mScrollableListenerArrays.valueAt(mViewPager.getCurrentItem())
            .isViewBeingDragged(event);
    }


    @Override public boolean isLeftViewBeingDragged(MotionEvent event) {
        return false;
    }


    @Override public void onMoveStarted(float y) {

    }


    @Override public void onMove(float y, float yDx) {
        float headerTranslationY = ViewCompat.getTranslationY(mHeaderLayoutView) + yDx;
        if (headerTranslationY >= 0) { // pull end
            headerExpand(0L);
        } else if (headerTranslationY <= -mHeaderHeight) { // push end
            headerFold(0L);
        } else {
            ViewCompat.animate(mHeaderLayoutView)
                .translationY(headerTranslationY)
                .setDuration(0)
                .start();
            ViewCompat.animate(mViewPager)
                .translationY(headerTranslationY + mHeaderHeight)
                .setDuration(0)
                .start();
        }
    }


    @Override public void onMoveEnded(boolean isFling, float flingVelocityY) {

        float headerY = ViewCompat.getTranslationY(mHeaderLayoutView); // 0到负数
        if (headerY == 0 || headerY == -mHeaderHeight) {
            return;
        }

        if (mViewPagerHeaderHelper.getInitialMotionY() - mViewPagerHeaderHelper.getLastMotionY()
            < -mTouchSlop) {  // pull > mTouchSlop = expand
            headerExpand(headerMoveDuration(true, headerY, isFling, flingVelocityY));
        } else if (mViewPagerHeaderHelper.getInitialMotionY()
            - mViewPagerHeaderHelper.getLastMotionY()
            > mTouchSlop) { // push > mTouchSlop = fold
            headerFold(headerMoveDuration(false, headerY, isFling, flingVelocityY));
        } else {
            if (headerY > -mHeaderHeight / 2f) {  // headerY > header/2 = expand
                headerExpand(headerMoveDuration(true, headerY, isFling, flingVelocityY));
            } else { // headerY < header/2= fold
                headerFold(headerMoveDuration(false, headerY, isFling, flingVelocityY));
            }
        }
    }


    private long headerMoveDuration(boolean isExpand, float currentHeaderY, boolean isFling,
                                    float velocityY) {

        long defaultDuration = DEFAULT_DURATION;

        if (isFling) {

            float distance = isExpand ? Math.abs(mHeaderHeight) - Math.abs(currentHeaderY)
                                      : Math.abs(currentHeaderY);
            velocityY = Math.abs(velocityY) / 1000;

            defaultDuration = (long) (distance / velocityY * DEFAULT_DAMPING);

            defaultDuration =
                defaultDuration > DEFAULT_DURATION ? DEFAULT_DURATION : defaultDuration;
        }

        return defaultDuration;
    }


    private void headerFold(long duration) {
        ViewCompat.animate(mHeaderLayoutView)
            .translationY(-mHeaderHeight)
            .setDuration(duration)
            .setInterpolator(mInterpolator)
            .start();

        ViewCompat.animate(mViewPager).translationY(0).
            setDuration(duration).setInterpolator(mInterpolator).start();

        mViewPagerHeaderHelper.setHeaderExpand(false);
    }


    private void headerExpand(long duration) {
        ViewCompat.animate(mHeaderLayoutView)
            .translationY(0)
            .setDuration(duration)
            .setInterpolator(mInterpolator)
            .start();

        ViewCompat.animate(mViewPager)
            .translationY(mHeaderHeight)
            .setDuration(duration)
            .setInterpolator(mInterpolator)
            .start();
        mViewPagerHeaderHelper.setHeaderExpand(true);
    }


    @Override public void onFragmentAttached(ScrollableListener listener, int position) {
        mScrollableListenerArrays.put(position, listener);
    }


    @Override public void onFragmentDetached(ScrollableListener listener, int position) {
        mScrollableListenerArrays.remove(position);
    }


    private class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {

            if (position == 1) {
                return ListViewFragment.newInstance(position);
            }
            return ListViewFragment.newInstance(position);
        }


        @Override
        public int getCount() {
            return 2;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_country);
                case 1:
                    return getString(R.string.tab_continent);

            }

            return "";
        }
    }
}
