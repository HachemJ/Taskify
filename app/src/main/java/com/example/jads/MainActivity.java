package com.example.jads;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        // Setup ViewPager with Fragments
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new LookingFragment()); // Fragment for Looking
        fragments.add(new SellingFragment()); // Fragment for Selling
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, fragments);
        viewPager.setAdapter(adapter);

        // Bind TabLayout and ViewPager2 with dynamic colors for tabs and status bar
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Looking");
            } else {
                tab.setText("Selling");
            }
        }).attach();

        // Dynamic Indicator and Status Bar Color Change
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Blue for "Looking"
                    tabLayout.setSelectedTabIndicatorColor(getColor(R.color.black));
                    getWindow().setStatusBarColor(getColor(R.color.black));
                } else if (tab.getPosition() == 1) {
                    // Dark blue for "Selling"
                    tabLayout.setSelectedTabIndicatorColor(getColor(R.color.dark_blue));
                    getWindow().setStatusBarColor(getColor(R.color.dark_blue));

                    // Modify "Add Post" button in SellingFragment
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    SellingFragment sellingFragment = (SellingFragment) fragmentManager.findFragmentByTag("f1"); // Ensure this matches the SellingFragment tag

                    if (sellingFragment != null && sellingFragment.getView() != null) {
                        Button addPostButton = sellingFragment.getView().findViewById(R.id.openAddPostDialogButton);
                        if (addPostButton != null) {
                            addPostButton.setBackgroundColor(getColor(R.color.dark_blue)); // Change background to dark blue
                            addPostButton.setTextColor(getColor(R.color.black)); // Change text to black
                        }
                    }
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

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Clear all fragments before replacing
            clearAllFragments();

            if (item.getItemId() == R.id.nav_home) {
                // Show Home with TabLayout
                tabLayout.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.VISIBLE);
            } else {
                // Hide Home-specific views
                tabLayout.setVisibility(View.GONE);
                viewPager.setVisibility(View.GONE);

                // Load respective fragment
                Fragment selectedFragment = null;
                if (item.getItemId() == R.id.nav_account) {
                    selectedFragment = new AccountFragment();
                } else if (item.getItemId() == R.id.nav_search) {
                    selectedFragment = new SearchFragment();
                }

                if (selectedFragment != null) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame, selectedFragment);
                    transaction.commit();
                }
            }
            return true;
        });

        // Set Default Tab (Home)
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // Handle Window Insets for Edge-to-Edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            WindowInsetsCompat.Type.systemBars();
            return insets;
        });
    }

    // Helper method to clear all fragments
    private void clearAllFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        for (Fragment fragment : fragmentManager.getFragments()) {
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
    }
}
