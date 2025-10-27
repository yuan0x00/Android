package com.rapid.android.feature.main.home.recommend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.R;
import com.rapid.android.databinding.ItemModuleEntryBinding;
import com.rapid.android.feature.container.FragmentContainerActivity;
import com.rapid.android.feature.main.discover.harmony.HarmonyFragment;
import com.rapid.android.feature.main.discover.navigation.NavigationFragment;
import com.rapid.android.feature.main.discover.project.ProjectFragment;
import com.rapid.android.feature.main.discover.routes.RoutesFragment;
import com.rapid.android.feature.main.discover.system.SystemFragment;
import com.rapid.android.feature.main.discover.tool.ToolsFragment;
import com.rapid.android.feature.main.discover.tutorial.TutorialFragment;
import com.rapid.android.feature.main.discover.wechat.WechatFragment;
import com.rapid.android.feature.main.discover.wenda.WendaFragment;

import java.util.ArrayList;
import java.util.List;

public class ModuleEntryAdapter extends RecyclerView.Adapter<ModuleEntryAdapter.ModuleEntryViewHolder> {

    private final List<Fragment> fragments = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();
    private final List<Integer> images = new ArrayList<>();
    private final FragmentActivity host;

    public ModuleEntryAdapter(@NonNull Context context) {
        super();
        this.host = (FragmentActivity) context;
    }

    @NonNull
    @Override
    public ModuleEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemModuleEntryBinding binding = ItemModuleEntryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ModuleEntryAdapter.ModuleEntryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleEntryAdapter.ModuleEntryViewHolder holder, int position) {
        holder.bind(fragments, titles, images);
    }

    @Override
    public int getItemCount() {
        return fragments.isEmpty() ? 0 : 1;
    }

    public void addFragment(Fragment fragment, String title, Integer image) {
        fragments.add(fragment);
        titles.add(title);
        images.add(image);
    }

    public void addFragments() {
        addFragment(new HarmonyFragment(), host.getString(R.string.discover_tab_harmony), R.drawable.harmonyos_space);
        addFragment(new RoutesFragment(), host.getString(R.string.discover_tab_routes), R.drawable.route_24px);
        addFragment(new TutorialFragment(), host.getString(R.string.discover_tab_tutorial), R.drawable.book_2_24px);
        addFragment(new WendaFragment(), host.getString(R.string.discover_tab_wenda), R.drawable.forum_24px);
        addFragment(new ProjectFragment(), host.getString(R.string.discover_tab_project), R.drawable.folder_code_24px);
        addFragment(new WechatFragment(), host.getString(R.string.discover_tab_wechat), R.drawable.wechar_space);
        addFragment(new ToolsFragment(), host.getString(R.string.discover_tab_tools), R.drawable.service_toolbox_24px);
        addFragment(new SystemFragment(), host.getString(R.string.discover_tab_system), R.drawable.family_history_24px);
        addFragment(new NavigationFragment(), host.getString(R.string.discover_tab_navigation), R.drawable.signpost_24px);
    }

    public static class ModuleEntryViewHolder extends RecyclerView.ViewHolder {

        private ItemModuleEntryBinding binding;

        public ModuleEntryViewHolder(@NonNull ItemModuleEntryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(List<Fragment> fragments, List<String> titles, List<Integer> drawableIds) {
            ModuleEntryItemAdapter moduleEntryItemAdapter = new ModuleEntryItemAdapter();
            moduleEntryItemAdapter.setData(titles, drawableIds, fragments);
            moduleEntryItemAdapter.setOnItemClickListener((position, fragment, title) -> {
                FragmentContainerActivity.start(binding.getRoot().getContext(), fragment.getClass(), title);
            });
            binding.recyclerView.setAdapter(moduleEntryItemAdapter);
            binding.recyclerView.setLayoutManager(new GridLayoutManager(binding.getRoot().getContext(), 5));
        }
    }
}
