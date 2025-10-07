package com.rapid.android.ui.feature.main.message;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayoutMediator;
import com.rapid.android.R;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentMessageBinding;
import com.rapid.android.ui.common.UiFeedback;

import java.util.EnumMap;
import java.util.Map;

public class MessageFragment extends BaseFragment<MessageViewModel, FragmentMessageBinding>
        implements MessageListFragment.Host {

    private final Map<MessageCategory, MessageListFragment> fragmentCache = new EnumMap<>(MessageCategory.class);
    private TabLayoutMediator tabMediator;

    @Override
    protected MessageViewModel createViewModel() {
        return new ViewModelProvider(this).get(MessageViewModel.class);
    }

    @Override
    protected FragmentMessageBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentMessageBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        MessagePagerAdapter adapter = new MessagePagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        tabMediator = new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(adapter.getPageTitle(requireContext(), position)));
        tabMediator.attach();

        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_refresh) {
                refreshAll();
                return true;
            }
            return false;
        });

        binding.unreadSummary.setText(getString(R.string.message_unread_count_value, 0));
    }

    @Override
    protected void setupObservers() {
        viewModel.getUnreadCount().observe(this, count -> {
            if (count == null) {
                count = 0;
            }
            binding.unreadSummary.setText(getString(R.string.message_unread_count_value, count));
        });

        viewModel.getLoading().observe(this, loading ->
                binding.unreadCard.setAlpha(Boolean.TRUE.equals(loading) ? 0.7f : 1f));

        UiFeedback.observeError(this, viewModel.getErrorMessage());
    }

    @Override
    protected void loadData() {
        viewModel.refreshUnreadCount();
    }

    @Override
    public void onListFragmentAttached(@NonNull MessageCategory category, @NonNull MessageListFragment fragment) {
        fragmentCache.put(category, fragment);
    }

    @Override
    public void onListFragmentDetached(@NonNull MessageCategory category) {
        fragmentCache.remove(category);
    }

    @Override
    public void onDestroyView() {
        if (tabMediator != null) {
            tabMediator.detach();
            tabMediator = null;
        }
        fragmentCache.clear();
        super.onDestroyView();
    }

    private void refreshAll() {
        viewModel.refreshUnreadCount();
        for (MessageListFragment fragment : fragmentCache.values()) {
            fragment.refreshList();
        }
    }
}
