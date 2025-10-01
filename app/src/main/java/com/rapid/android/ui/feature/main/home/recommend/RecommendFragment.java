package com.rapid.android.ui.feature.main.home.recommend;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.core.common.utils.ToastUtils;
import com.core.ui.presentation.BaseFragment;
import com.lib.domain.model.ArticleListBean;
import com.lib.domain.model.BannerItemBean;
import com.rapid.android.databinding.FragmentRecommandBinding;
import com.rapid.android.ui.feature.main.home.BannerAdapter;
import com.rapid.android.ui.feature.main.home.FeedAdapter;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;

import java.util.Arrays;
import java.util.List;


public class RecommendFragment extends BaseFragment<RecommendViewModel, FragmentRecommandBinding> {

    private BannerAdapter bannerAdapter;
    private FeedAdapter feedAdapter;

    @Override
    protected RecommendViewModel createViewModel() {
        return new ViewModelProvider(this).get(RecommendViewModel.class);
    }

    @Override
    protected FragmentRecommandBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentRecommandBinding.inflate(inflater, container, false);
    }

    @Override
    protected void loadData() {
        super.loadData();
        viewModel.banner();
        viewModel.articleList();
    }

    @Override
    protected void setupObservers() {
        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                ToastUtils.showLongToast(msg);
            }
        });
        viewModel.getBannerList().observe(this, bannerList -> {
            if (bannerList != null && !bannerList.isEmpty()) {
                bannerAdapter.setData(bannerList);
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

        // 模拟数据
        List<BannerItemBean> bannerUrls = Arrays.asList(
                new BannerItemBean("https://public.ysjf.com/content/title-image/Y7IoA2mGtho.jpg"),
                new BannerItemBean("https://public.ysjf.com/product/preview/Y9s2dgnGrU.jpg"),
                new BannerItemBean("https://public.ysjf.com/product/preview/cT9mqPVWS6.jpg")
        );

        ArticleListBean feeds = new ArticleListBean();

        // 创建各模块 Adapter
        bannerAdapter = new BannerAdapter(bannerUrls);
        feedAdapter = new FeedAdapter(feeds);

        // 使用 ConcatAdapter 拼接
        ConcatAdapter concatAdapter = new ConcatAdapter(bannerAdapter, feedAdapter);
        binding.recyclerView.setAdapter(concatAdapter);

        //设置 Header / Footer / Listener
        binding.refreshLayout.setRefreshHeader(new ClassicsHeader(requireContext()));
        binding.refreshLayout.setRefreshFooter(new ClassicsFooter(requireContext()));
        binding.refreshLayout.setOnRefreshListener((refreshlayout) -> refreshlayout.finishRefresh(2000));
        binding.refreshLayout.setOnLoadMoreListener(refreshlayout -> refreshlayout.finishLoadMore(2000));

    }
}
