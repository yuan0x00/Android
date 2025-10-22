package com.rapid.android.ui.feature.main.home.recommend;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.rapid.android.R;
import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.databinding.FragmentPopularSectionBinding;
import com.rapid.android.ui.feature.web.ArticleWebViewActivity;

import java.util.List;

public class PopularSectionFragment extends Fragment {

    private static final String ARG_SECTION = "section";
    private static final int MAX_CHIPS = 20;

    private FragmentPopularSectionBinding binding;
    private HomePopularSection section;

    public static PopularSectionFragment newInstance(HomePopularSection section) {
        PopularSectionFragment fragment = new PopularSectionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SECTION, new SerializableSection(section));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            SerializableSection serializable = (SerializableSection) getArguments().getSerializable(ARG_SECTION);
            if (serializable != null) {
                section = serializable.toSection();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPopularSectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (section != null) {
            bindSection(section);
        }
    }

    private void bindSection(HomePopularSection section) {
        binding.chipGroup.removeAllViews();

        List<CategoryNodeBean> chapters = section.getChapters();
        if (chapters == null || chapters.isEmpty()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int count = Math.min(chapters.size(), MAX_CHIPS);
        for (int i = 0; i < count; i++) {
            CategoryNodeBean item = chapters.get(i);
            if (item == null || TextUtils.isEmpty(item.getName())) {
                continue;
            }
            Chip chip = (Chip) inflater.inflate(R.layout.item_home_popular_chip, binding.chipGroup, false);
            chip.setText(item.getName());
            String link = resolveLink(item);
            if (!TextUtils.isEmpty(link)) {
                chip.setOnClickListener(v -> ArticleWebViewActivity.start(v.getContext(), link, item.getName()));
            } else {
                chip.setOnClickListener(null);
            }
            binding.chipGroup.addView(chip);
        }
    }

    private String resolveLink(CategoryNodeBean bean) {
        if (bean == null) {
            return "";
        }
        if (!TextUtils.isEmpty(bean.getLink())) {
            return bean.getLink();
        }
        List<CategoryNodeBean> children = bean.getChildren();
        if (children == null) {
            return "";
        }
        for (CategoryNodeBean child : children) {
            if (child != null && !TextUtils.isEmpty(child.getLink())) {
                return child.getLink();
            }
        }
        return "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class SerializableSection implements java.io.Serializable {
        private final String title;
        private final List<CategoryNodeBean> chapters;

        SerializableSection(HomePopularSection section) {
            this.title = section.getTitle();
            this.chapters = section.getChapters();
        }

        HomePopularSection toSection() {
            return new HomePopularSection(title, chapters);
        }
    }
}
