package com.rapid.android.feature.main.discover;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayoutMediator;
import com.rapid.android.R;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentDiscoverBinding;
import com.rapid.android.feature.search.SearchActivity;

public class DiscoverFragment extends BaseFragment<DiscoverViewModel, FragmentDiscoverBinding> {

    private TabLayoutMediator tabMediator;

    @Override
    protected DiscoverViewModel createViewModel() {
        return new ViewModelProvider(this).get(DiscoverViewModel.class);
    }

    @Override
    protected FragmentDiscoverBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentDiscoverBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        DiscoverPagerAdapter adapter = new DiscoverPagerAdapter(this, requireContext());
        binding.viewPager.setAdapter(adapter);

        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_search) {
                startActivity(new Intent(requireContext(), SearchActivity.class));
                return true;
            }
            return false;
        });

        tabMediator = new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(adapter.getPageTitle(position)));
        tabMediator.attach();
    }

    @Override
    public void onDestroyView() {
        if (tabMediator != null) {
            tabMediator.detach();
            tabMediator = null;
        }
        super.onDestroyView();
    }
}
