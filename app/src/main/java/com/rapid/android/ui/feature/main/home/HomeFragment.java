package com.rapid.android.ui.feature.main.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.core.ui.components.dialog.DialogController;
import com.core.ui.components.dialog.DialogEffect;
import com.core.ui.presentation.BaseFragment;
import com.google.android.material.tabs.TabLayoutMediator;
import com.rapid.android.databinding.FragmentHomeBinding;
import com.rapid.android.ui.common.dialog.TipDialogView;
import com.rapid.android.ui.feature.main.home.search.SearchActivity;

public class HomeFragment extends BaseFragment<HomeViewModel, FragmentHomeBinding> {

    private DialogController dialogController;

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
        dialogController = DialogController.from(this, binding.getRoot());
        setupViewPager();
        binding.topSearch.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), SearchActivity.class));
        });
        binding.avatar.setOnClickListener(v -> {

            dialogController.show(new DialogEffect.Confirm(
                    "", "", "", "", () -> {
            }, () -> {
            }));

            dialogController.show(new DialogEffect.Loading(true));

            dialogController.show(new DialogEffect.Custom(
                    new TipDialogView(requireContext())));
//
            dialogController.show(new DialogEffect.Toast(
                    "Toast"
            ));

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
