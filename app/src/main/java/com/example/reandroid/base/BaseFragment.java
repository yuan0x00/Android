package com.example.reandroid.base;

import android.content.Intent;
import android.net.Uri;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class BaseFragment extends Fragment {

    protected NavController nav() {
        return NavHostFragment.findNavController(this);
    }

    protected void openUrlInBrowser(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

}
