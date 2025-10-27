package com.rapid.android.feature.container;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.rapid.android.R;
import com.rapid.android.core.common.utils.WindowInsetsUtils;

public class FragmentContainerActivity extends AppCompatActivity {

    private static final String EXTRA_FRAGMENT_CLASS = "fragment_class";
    private static final String EXTRA_TITLE = "title";

    public static void start(Context context, Class<? extends Fragment> fragmentClass, String title) {
        Intent intent = new Intent(context, FragmentContainerActivity.class);
        intent.putExtra(EXTRA_FRAGMENT_CLASS, fragmentClass);
        intent.putExtra(EXTRA_TITLE, title);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        WindowInsetsUtils.applySystemWindowInsets(findViewById(android.R.id.content));

        String title = getIntent().getStringExtra(EXTRA_TITLE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // 添加 Fragment
        if (savedInstanceState == null) {
            Class<? extends Fragment> fragmentClass =
                    (Class<? extends Fragment>) getIntent().getSerializableExtra(EXTRA_FRAGMENT_CLASS);

            try {
                Fragment fragment = fragmentClass.newInstance();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "无法打开页面", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}