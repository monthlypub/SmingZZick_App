package com.monpub.sming.sming;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.monpub.sming.R;

public class SmingSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        int headTextColor;
        if (Build.VERSION.SDK_INT >= 23) {
            headTextColor = getResources().getColor(R.color.setup_item_head_text, null);
        } else {
            headTextColor = getResources().getColor(R.color.setup_item_head_text);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(headTextColor);
        setSupportActionBar(toolbar);

        int headColor;
        if (Build.VERSION.SDK_INT >= 23) {
            headColor = getResources().getColor(R.color.setup_item_head, null);
        } else {
            headColor = getResources().getColor(R.color.setup_item_head);
        }

        getSupportActionBar().setTitle("스밍 설정");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(headColor));
        getFragmentManager().beginTransaction()
                .replace(R.id.frame, new SmingSettingFragment())
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SmingManager.getInstance().refreshVibrate();
    }
}
