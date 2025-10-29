package com.rapid.android.feature.main.message;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.rapid.android.R;
import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.databinding.ActivityMessageCenterBinding;

/**
 * 消息中心入口 Activity，承载 {@link MessageFragment}。
 */
public class MessageCenterActivity extends BaseActivity<MessageCenterViewModel, ActivityMessageCenterBinding> {

    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, MessageCenterActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected MessageCenterViewModel createViewModel() {
        return new ViewModelProvider(this).get(MessageCenterViewModel.class);
    }

    @Override
    protected ActivityMessageCenterBinding createViewBinding(View rootView) {
        return ActivityMessageCenterBinding.bind(rootView);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_message_center;
    }

    @Override
    protected void initializeViews() {
        if (getSupportFragmentManager().findFragmentById(R.id.fragmentContainer) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new MessageFragment())
                    .commit();
        }
    }
}
