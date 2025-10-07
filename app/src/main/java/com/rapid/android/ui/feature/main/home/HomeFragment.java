package com.rapid.android.ui.feature.main.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayoutMediator;
import com.rapid.android.R;
import com.rapid.android.core.common.utils.ToastUtils;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentHomeBinding;
import com.rapid.android.ui.feature.login.LoginActivity;
import com.rapid.android.ui.feature.main.home.search.SearchActivity;
import com.rapid.android.ui.feature.share.ShareArticleActivity;

public class HomeFragment extends BaseFragment<HomeViewModel, FragmentHomeBinding> {

    @Override
    protected HomeViewModel createViewModel() {
        return new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Override
    protected FragmentHomeBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void loadData() {
        super.loadData();
    }

    @Override
    protected void initializeViews() {
        setupViewPager();
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_search) {
                startActivity(new Intent(requireContext(), SearchActivity.class));
                return true;
            } else if (itemId == R.id.action_share_article) {
                if (!SessionManager.getInstance().isLoggedIn()) {
                    ToastUtils.showShortToast(getString(R.string.mine_toast_require_login));
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                    return true;
                }
                ShareArticleActivity.start(requireContext());
                return true;
            }
            return false;
        });
    }

    private void setupViewPager() {
        HomeViewPagerAdapter adapter = new HomeViewPagerAdapter(requireActivity());
        adapter.addFragments();
        binding.viewPager.setAdapter(adapter);

        // 将 TabLayout 与 ViewPager2 绑定
        new TabLayoutMediator(binding.topTab, binding.viewPager,
                (tab, position) -> tab.setText(adapter.getPageTitle(position))).attach();
    }

}
