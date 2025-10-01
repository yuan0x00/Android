package com.rapid.android.ui.feature.main.home.lastproject;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.core.ui.common.ToastUtils;
import com.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentLastProjectBinding;
import com.rapid.android.domain.model.ArticleListBean;
import com.rapid.android.ui.feature.main.home.FeedAdapter;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;


public class LastProjectFragment extends BaseFragment<LastProjectViewModel, FragmentLastProjectBinding> {

    private FeedAdapter feedAdapter;

    @Override
    protected LastProjectViewModel createViewModel() {
        return new ViewModelProvider(this).get(LastProjectViewModel.class);
    }

    @Override
    protected FragmentLastProjectBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentLastProjectBinding.inflate(inflater, container, false);
    }

    @Override
    protected void loadData() {
        super.loadData();
        viewModel.listProjectList();
    }

    @Override
    protected void setupObservers() {
        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                ToastUtils.showLongToast(msg);
            }
        });
        viewModel.getArticleListBean().observe(this, bean -> {
            if (bean != null) {
                feedAdapter.setData(bean);
            }
        });
    }

    @Override
    protected void initializeViews() {

        // 初始化 RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ArticleListBean feeds = new ArticleListBean();

        // 创建各模块 Adapter
        feedAdapter = new FeedAdapter(feeds);

        // 使用 ConcatAdapter 拼接
        ConcatAdapter concatAdapter = new ConcatAdapter(feedAdapter);
        binding.recyclerView.setAdapter(concatAdapter);

        //设置 Header / Footer / Listener
        binding.refreshLayout.setRefreshHeader(new ClassicsHeader(requireContext()));
        binding.refreshLayout.setRefreshFooter(new ClassicsFooter(requireContext()));
        binding.refreshLayout.setOnRefreshListener((refreshlayout) -> refreshlayout.finishRefresh(2000));
        binding.refreshLayout.setOnLoadMoreListener(refreshlayout -> refreshlayout.finishLoadMore(2000));

    }
}
