package com.monpub.sming.sticker;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.monpub.sming.Constant;
import com.monpub.sming.R;
import com.monpub.sming.sming.SmingData;
import com.monpub.textmaker.TextMakeFragment;
import com.monpub.textmaker.TextMakingInfo;

import java.io.File;
import java.io.PrintWriter;

public class TextMakeActivity extends AppCompatActivity {
    public final static String EXTRA_MAKING_INFO = "extra_making_info";
    public final static String EXTRA_FILE_NAME = "extra_file_name";

    private TextMakeFragment fragment;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_make);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextMakingInfo textMakingInfo = null;

        Intent intent = getIntent();
        if (intent != null) {
            textMakingInfo = intent.getParcelableExtra(EXTRA_MAKING_INFO);
            fileName = intent.getStringExtra(EXTRA_FILE_NAME);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame, fragment = TextMakeFragment.newInstance(Constant.getFontDirectory().getPath(), textMakingInfo));
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.text_maker_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("편집 상태를 저장히지 않고 나가시겠습니까?");
        builder.setPositiveButton("나갈란다", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("걍있을께", null);
        builder.show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save_text_sticker) {
            TextMakingInfo info = fragment.getTextMakingInfo();
            if (info.isEmpty() == false) {
                String jsonString = info.toJSONString();

                if (TextUtils.isEmpty(jsonString) == false) {
                    writeTextSticker(jsonString);
                    finish();
                }
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void writeTextSticker(String jsonString) {
        File directory = Constant.getStickerDirectory();
        File file;
        if (TextUtils.isEmpty(fileName) == false) {
            file = new File(directory, fileName);
            if (file != null && file.exists() == true) {
                file.delete();
            }
        }
        file = new File(directory, "textsticker_" + System.currentTimeMillis() + ".json");

        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter (file);
            printWriter.write(jsonString);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }
}
