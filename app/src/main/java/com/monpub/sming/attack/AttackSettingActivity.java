package com.monpub.sming.attack;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.monpub.sming.R;
import com.monpub.sming.sming.SmingManager;
import com.monpub.sming.sming.SmingSettingFragment;

public class AttackSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        int headTextColor;
        if (Build.VERSION.SDK_INT >= 23) {
            headTextColor = getResources().getColor(R.color.setup_item_head_text, null);
        } else {
            headTextColor = getResources().getColor(R.color.setup_item_head_text);
        }

        toolbar.setTitleTextColor(headTextColor);
        setSupportActionBar(toolbar);

        int headColor;
        if (Build.VERSION.SDK_INT >= 23) {
            headColor = getResources().getColor(R.color.setup_item_head, null);
        } else {
            headColor = getResources().getColor(R.color.setup_item_head);
        }

        getSupportActionBar().setTitle("총공 설정");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(headColor));
        getFragmentManager().beginTransaction()
                .replace(R.id.frame, new AttackSettingFragment())
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
