package com.example.jads;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;

    private Fragment accountFragment;
    private Fragment searchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Setup ViewPager with Fragments
        setupViewPager();

        // Set Default Tab (Home)
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // Set Bottom Navigation Listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                showHome();
                return true;
            } else if (item.getItemId() == R.id.nav_account) {
                showAccountFragment();
                return true;
            } else if (item.getItemId() == R.id.nav_search) {
                showSearchFragment();
                return true;
            }
            return false;
        });
    }

    private void setupViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new LookingFragment()); // Looking
        fragments.add(new SellingFragment()); // Selling

        ViewPagerAdapter adapter = new ViewPagerAdapter(this, fragments);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Looking");
            } else {
                tab.setText("Selling");
            }
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    getWindow().setStatusBarColor(getColor(R.color.black));
                } else {
                    getWindow().setStatusBarColor(getColor(R.color.dark_blue));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // No action needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // No action needed
            }
        });
    }

    private void showHome() {
        // Ensure only ViewPager is visible
        tabLayout.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);

        // Remove other fragments
        clearFragment("AccountFragment");
        clearFragment("SearchFragment");
    }

    private void showAccountFragment() {
        // Hide ViewPager
        tabLayout.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);

        // Show Account Fragment
        loadFragment(new AccountFragment(), "AccountFragment");
    }

    private void showSearchFragment() {
        // Hide ViewPager
        tabLayout.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);

        // Show Search Fragment
        loadFragment(new SearchFragment(), "SearchFragment");
    }

    private void loadFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Replace existing fragment
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);
        if (existingFragment == null) {
            transaction.replace(R.id.content_frame, fragment, tag);
        } else {
            transaction.show(existingFragment);
        }

        transaction.commitNowAllowingStateLoss();
    }

    private void clearFragment(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);

        if (fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commitNowAllowingStateLoss();
        }
    }
}
