package com.monpub.sming;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.monpub.sming.attack.AttackAdaptor;
import com.monpub.sming.attack.AttackData;
import com.monpub.sming.attack.AttackManager;
import com.monpub.sming.attack.AttackSettingActivity;
import com.monpub.sming.attack.PlaylistUtil;
import com.monpub.sming.etc.Util;
import com.monpub.sming.sming.MakeSmingActivity;
import com.monpub.sming.sming.SmingManager;
import com.monpub.sming.sming.SmingSettingActivity;
import com.monpub.sming.sming.SmingShotAdaptor;
import com.monpub.sming.sticker.StickerAttacherActivity;
import com.monpub.sming.youtube.YoutubeSmingActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.iwf.photopicker.PhotoPreview;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 100;
    private static final int REQUEST_CODE_MAKE_SMING = 101;
    private static final int PERMISSION_REQUEST_CODE = 101;

    private SmingShotAdaptor smingShotAdaptor;
    private AttackAdaptor attackAdaptor;

    private boolean permissionReady = false;

    private FloatingActionButton fab;

    private boolean useObserve = Build.VERSION.SDK_INT < 21;

    private ProgressBar mAttackProgress;

    private View mPlayList;

    private ViewGroup layout;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && Intent.ACTION_SEND.equals(intent.getAction()) == true) {
            if (handleYoutbue(intent) == false) {
                handleAttackSend(intent);
            }
        }
    }

    /****************************************** Activity Lifecycle methods ************************/



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = (ViewGroup) findViewById(R.id.layout);

        mAttackProgress = (ProgressBar) findViewById(R.id.progress_attack);
        fab = (FloatingActionButton) findViewById(R.id.startstop);

        if (Build.VERSION.SDK_INT >= 21) {
            findViewById(R.id.youtube).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent youtubeIntent = new Intent(MainActivity.this, YoutubeSmingActivity.class);
                    startActivity(youtubeIntent);
                }
            });
        } else {
            findViewById(R.id.layout_youtube).setVisibility(View.GONE);
        }

        if (useObserve == true) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.isSelected() == true) {
                        stopObserver();
                    } else {
                        if (SmingManager.getInstance().isShownUnderVersionGuide() == false) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("안드로이드 5.0 미만이시네요.\n자동캡쳐는 지원되지 않아요ㅠㅠ\n\n수동캡쳐를 하시면 감지해서 짤합치고 스티커를 박습니다.");
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    SmingManager.getInstance().setShownUnderVersionGuide(true);
                                    startObserver();
                                }
                            });
                            builder.setPositiveButton("확인", null);
                            builder.show();
                        } else {
                            startObserver();
                        }
                    }
                }
            });
        } else {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.isSelected() == true) {
                        startService(new Intent(ScreenCaptureService.ACTION_STOP, null, MainActivity.this, ScreenCaptureService.class));
                        v.setSelected(false);
                    } else {
                        startProjection();
                    }
                }
            });
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_sming);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(smingShotAdaptor = new SmingShotAdaptor());
        smingShotAdaptor.setOnItemClickListener(new SmingShotAdaptor.OnItemClickListener() {
            @Override
            public void onItemClick(String imageFilePath) {
                List<File> files = smingShotAdaptor.getFIles();
                ArrayList<String> paths = new ArrayList<String>();

                int position = -1;
                String path;
                for (int i = 0; i < files.size(); i++) {
                    path = files.get(i).getAbsolutePath();

                    if (imageFilePath.equals(path) == true) {
                        position = i;
                    }

                    paths.add(path);
                }

                if (position >= 0) {
                    PhotoPreview.builder()
                            .setPhotos(paths)
                            .setCurrentItem(position)
                            .setShowDeleteButton(false)
                            .start(MainActivity.this);
                }

            }
        });
        smingShotAdaptor.setOnItemDeleteClickListener(new SmingShotAdaptor.OnItemDeleteClickListener() {
            @Override
            public void onItemDeleteClick(String imageFilePath, int position) {
                try {
                    File file = new File(imageFilePath);
                    if (file.delete() == true) {
                        smingShotAdaptor.remove(position);
                        smingShotAdaptor.notifyItemRemoved(position);

                        MediaScannerConnection.scanFile(MainActivity.this, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                            /*
                             *   (non-Javadoc)
                             * @see android.media.MediaScannerConnection.OnScanCompletedListener#onScanCompleted(java.lang.String, android.net.Uri)
                             */
                            public void onScanCompleted(String path, Uri uri) {
                                // do nothing
                            }
                        });
                        checkEmpty();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
        smingShotAdaptor.setOnItemDeleteAllClickListener(new SmingShotAdaptor.OnItemDeleteAllClickListener() {
            @Override
            public void onItemDeleteAllClick() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("스밍짤들 싹다 지웁니다?\n진짜 전부 지웁니다?");
                builder.setPositiveButton("지운다", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<File> smingShots = smingShotAdaptor.getFIles();
                        String[] filePathArray = new String[smingShots.size()];
                        for (int i = 0; i < smingShots.size(); i++) {
                            filePathArray[i] = smingShots.get(i).getAbsolutePath();
                        }

                        File smingDirectory = Constant.getSmingFolder();
                        Util.deleteDir(smingDirectory);

                        if (smingDirectory.exists() == false) {
                            smingDirectory.mkdirs();
                        }

                        loadSmings();
                        Constant.makeAppDefaultDirectory(MainActivity.this);

                        MediaScannerConnection.scanFile(MainActivity.this, filePathArray, null, new MediaScannerConnection.OnScanCompletedListener() {
                            /*
                             *   (non-Javadoc)
                             * @see android.media.MediaScannerConnection.OnScanCompletedListener#onScanCompleted(java.lang.String, android.net.Uri)
                             */
                            public void onScanCompleted(String path, Uri uri) {
                                // do nothing
                            }
                        });
                        checkEmpty();
                    }
                });
                builder.setNegativeButton("잘못눌렀어", null);
                builder.show();
            }
        });
        smingShotAdaptor.setOnItemStickerClickListener(new SmingShotAdaptor.OnItemStickerClickListener() {
            @Override
            public void onItemStickerClick(String imageFilePath) {
                Intent intent = new Intent(MainActivity.this, StickerAttacherActivity.class);
                intent.putExtra(StickerAttacherActivity.EXTRA_SMING_PATH, imageFilePath);
                startActivity(intent);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_attack);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(attackAdaptor = new AttackAdaptor());
        attackAdaptor.setOnItemClickListener(new AttackAdaptor.OnItemClickListener() {
            @Override
            public void onItemClick(AttackData attackData) {
                if (TextUtils.isEmpty(attackData.title) == false) {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("attack title", AttackManager.getInstance().getAttackTitle(attackData.title)));
                }

                Intent intent = new Intent();
                try {
                    String dcPkgName = Util.findDCInsideApp(MainActivity.this);
                    if (dcPkgName != null) {
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(attackData.targetUrl));
                        intent.setPackage(dcPkgName);
                        intent.setClassName(dcPkgName, "com.dcinside.app.IntroActivity");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    } else {
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(attackData.targetUrl));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                } catch (ActivityNotFoundException e) {
                    intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(attackData.targetUrl));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                startActivity(intent);
            }
        });
        attackAdaptor.setOnItemDeleteClickListener(new AttackAdaptor.OnItemDeleteClickListener() {
            @Override
            public void onItemDeleteClick(AttackData attackData, int position) {
                try {
                    AttackManager.getInstance().cancelAttack(MainActivity.this, attackData);

                    attackAdaptor.remove(position);
                    attackAdaptor.notifyItemRemoved(position);

                    checkEmpty();
                    checkPlayList();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            @Override
            public void onAllItemDeleteClick() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setMessage("총공정보 싹다 지웁니다?\n진짜 전부 지웁니다?");
                builder.setPositiveButton("지운다", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AttackManager.getInstance().clearAttackData(MainActivity.this);

                        attackAdaptor.setAttacks(null);
                        attackAdaptor.notifyDataSetChanged();

                        checkEmpty();
                        checkPlayList();
                    }
                });
                builder.setNegativeButton("잘못 눌렀어", null);

                builder.show();
            }
        });

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(configuration);

        initEtc();
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null && Intent.ACTION_SEND.equals(intent.getAction()) == true && intent.hasExtra(Intent.EXTRA_TEXT) == true) {
                if (handleYoutbue(intent) == false) {
                    handleAttackSend(intent);
                }
            }
        }

        mPlayList = findViewById(R.id.play_list);
        mPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() == null) {
                    return;
                }

                final Map<PlaylistUtil.MusicApp, Uri> map = (Map) v.getTag();
                if (map.isEmpty() == true) {
                    return;
                }
                final Set<PlaylistUtil.MusicApp> keySet = map.keySet();
                final ArrayAdapter<PlaylistUtil.MusicApp> arrayAdapter = new ArrayAdapter<PlaylistUtil.MusicApp>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
                for (PlaylistUtil.MusicApp musicApp : keySet) {
                    arrayAdapter.add(musicApp);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PlaylistUtil.MusicApp musicApp = arrayAdapter.getItem(which);
                        Uri uri = map.get(musicApp);

                        Intent intent = null;
                        if (musicApp == PlaylistUtil.MusicApp.YOUTUBE) {
                            intent = new Intent(MainActivity.this, YoutubeSmingActivity.class);
                            intent.setData(uri);
                        } else {
                            intent = new Intent(Intent.ACTION_VIEW, uri);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }

                        if (intent != null) {
                            startActivity(intent);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    private boolean handleYoutbue(Intent intent) {
        String url = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (TextUtils.isEmpty(url) == true) {
            return false;
        }

        Intent youtubeIntent = new Intent(this, YoutubeSmingActivity.class);
        boolean isYoutube = false;
//        if (Pattern.matches("youtu.be\\/(.+)\\?list=(.+)", url) == true) {
//
//            isYoutube = true;
//        } else
        if (Pattern.matches(".*(http:\\/\\/www.youtube.com.*)", url) == true) {
            Matcher matcher = Pattern.compile(".*(http:\\/\\/www.youtube.com.*)").matcher(url);
            if (matcher.find() == true) {
                isYoutube = true;
                String youtubeUrl = matcher.group(1);
                youtubeIntent.setData(Uri.parse(youtubeUrl));
            }
        } else if (Pattern.matches(".*(https:\\/\\/www.youtube.com.*)", url) == true) {
            Matcher matcher = Pattern.compile(".*(https:\\/\\/www.youtube.com.*)").matcher(url);
            if (matcher.find() == true) {
                isYoutube = true;
                String youtubeUrl = matcher.group(1);
                youtubeIntent.setData(Uri.parse(youtubeUrl));
            }
        } else if (Pattern.matches(".*https://youtu.be\\/(.+)", url) == true) {
            isYoutube = true;
            youtubeIntent.setData(Uri.parse(url));
        }

        if (isYoutube == false) {
            return false;
        }

        startActivity(youtubeIntent);

        return true;
    }

    private void handleAttackSend(Intent intent) {
        String url = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (TextUtils.isEmpty(url) == true) {
            Toast.makeText(MainActivity.this, "총공 정보가 제대로 넘어오지 않았습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        if (url.startsWith("http") == true) {
            loadAttack(url);
        } else {
            handleAttackRaw(url);
        }
    }

    private void initEtc() {
        findViewById(R.id.reload_attack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAttackProgress.setVisibility(View.VISIBLE);

                String clipText = AttackManager.getInstance().loadFromClipboard(MainActivity.this);
                handleAttackRaw(clipText);
            }
        });

        findViewById(R.id.attack_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AttackSettingActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.attack_builder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://builder.smingzzik.com"));
                startActivity(intent);
            }
        });

        findViewById(R.id.add_sming).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, MakeSmingActivity.class), REQUEST_CODE_MAKE_SMING);
            }
        });

        findViewById(R.id.edit_sticker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StickerAttacherActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.sming_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SmingSettingActivity.class);
                startActivity(intent);
            }
        });
    }

    private Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            //
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "총공 정보를 가져오지 못했습니다.", Toast.LENGTH_LONG).show();
                    mAttackProgress.setVisibility(View.GONE);
                }
            });
        }

        private static final String REGEX_MONTH_DATE = "(\\d+)월\\s*(\\d+)일";

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String text = null;
            int month = -1;
            int date = -1;
            try {
                String body = response.body().string();

                Document document = null;
                document = Jsoup.parse(body);

                Element element;
                try {
                    String monthDateText = document.getElementsByClass("tit_view").get(0).text().trim();
                    Matcher matcher = Pattern.compile(REGEX_MONTH_DATE).matcher(monthDateText);
                    if (matcher.find() == true) {
                        month = Integer.valueOf(matcher.group(1));
                        date = Integer.valueOf(matcher.group(2));
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                element = document.getElementById("memo_img");

                document.select("br").append("\\n");
                document.select("p").prepend("\\n\\n");
                document.select("div").prepend("\\n");

                text = element.text();

//                String s = element.html().replaceAll("\\\\n", "\n");
//                text = Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
//                text = text.replaceAll("&nbsp;", " ");
            } catch (Throwable t) {
                t.printStackTrace();
            }

            handleAttackRaw(month, date, text);
        }
    };

    private void handleAttackRaw(String text) {
        handleAttackRaw(-1, -1, text);
    }

    private void handleAttackRaw(int month, int date, String text) {
        boolean result = AttackManager.getInstance().analyseAttack(month, date, text, MainActivity.this);

        if (result == true) {
            AttackManager.getInstance().setOnAttackReadyListener(new AttackManager.OnAttackReadyListener() {
                @Override
                public void onAttakReady() {
                    AttackManager.getInstance().setOnAttackReadyListener(null);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadAttacks();
                            checkEmpty();
                            Toast.makeText(MainActivity.this, "총공 정보를 가져왔습니다.", Toast.LENGTH_LONG).show();
                            mAttackProgress.setVisibility(View.GONE);
                        }
                    });
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "총공 정보가 아닌 것 같은데요.", Toast.LENGTH_LONG).show();
                    mAttackProgress.setVisibility(View.GONE);
                }
            });
        }

    }

    private void loadAttack(String url) {
        Uri uri = Uri.parse(url);
        try {
            if (uri.getHost().indexOf("gall") == 0) {
                String id = uri.getQueryParameter("id");
                String no = uri.getQueryParameter("no");

                if (TextUtils.isEmpty(id) == true || TextUtils.isEmpty(no) == true) {
                    String path = uri.getPath();
                    Matcher matcher = Pattern.compile("\\/(.+)\\/(.+)").matcher(path);
                    if (matcher.find() == true) {
                        id = matcher.group(1);
                        no = matcher.group(2);
                    } else {
                        throw new IllegalStateException();
                    }
                }

                url = "http://m.dcinside.com/view.php?id=[[ID]]&no=[[NO]]".replace("[[ID]]", id).replace("[[NO]]", no);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            Toast.makeText(MainActivity.this, "총공 정보가 아닌 것 같은데요.", Toast.LENGTH_LONG).show();
            mAttackProgress.setVisibility(View.GONE);
            return;
        }

        mAttackProgress.setVisibility(View.VISIBLE);
        AttackManager.getInstance().handleAttackPage(MainActivity.this, url, callback);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= 23) {
            int permissionWriteCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permissionReadCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionWriteCheck == PackageManager.PERMISSION_DENIED
                    || permissionReadCheck == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
                permissionReady = false;
            } else {
                permissionReady = true;
            }
        } else {
            permissionReady = true;
        }

        if (permissionReady == true) {
            loadSmings();
            Constant.makeAppDefaultDirectory(this);
            unzipFonts();
        }
        loadAttacks();

        if (MusicListenService.isServiceRunning() == true) {
            fab.setSelected(true);
        } else {
            fab.setSelected(false);
        }

        IntentFilter intentFilter = new IntentFilter(MusicListenService.ACTION_STOP);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);

        checkEmpty();
        checkPlayList();
    }

    private void checkPlayList() {
        boolean showPlayButton = false;
        if (attackAdaptor != null && attackAdaptor.getItemCount() > 0) {
            List<AttackData> attackDatas = attackAdaptor.getAttacks();
            String youtubeListId = attackAdaptor.getYoutubeListId();

            Map<PlaylistUtil.MusicApp, Uri> playMap = new HashMap<>();
            List<String> songIds = new ArrayList<>();
            if (PlaylistUtil.checkMusicApp(this, PlaylistUtil.MusicApp.MELON) == true) {
                songIds.clear();
                for (AttackData attackData : attackDatas) {
                    if (attackData.songIDs != null && attackData.songIDs.isEmpty() == false) {
                        for (AttackData.SongID songID : attackData.songIDs) {
                            if (TextUtils.isEmpty(songID.melonId) == false) {
                                songIds.add(songID.melonId);
                            }
                        }
                    }
                }

                if (songIds.isEmpty() == false) {
                    Uri uri = PlaylistUtil.MusicApp.MELON.getUri(songIds.toArray(new String[0]));
                    playMap.put(PlaylistUtil.MusicApp.MELON, uri);
                    showPlayButton = true;
                }
            }
            if (PlaylistUtil.checkMusicApp(this, PlaylistUtil.MusicApp.GENIE) == true) {
                songIds.clear();
                for (AttackData attackData : attackDatas) {
                    if (attackData.songIDs != null && attackData.songIDs.isEmpty() == false) {
                        for (AttackData.SongID songID : attackData.songIDs) {
                            if (TextUtils.isEmpty(songID.genieId) == false) {
                                songIds.add(songID.genieId);
                            }
                        }
                    }
                }

                if (songIds.isEmpty() == false) {
                    Uri uri = PlaylistUtil.MusicApp.GENIE.getUri(songIds.toArray(new String[0]));
                    playMap.put(PlaylistUtil.MusicApp.GENIE, uri);
                    showPlayButton = true;
                }
            }
            if (PlaylistUtil.checkMusicApp(this, PlaylistUtil.MusicApp.NAVERMUSIC) == true) {
                songIds.clear();
                for (AttackData attackData : attackDatas) {
                    if (attackData.songIDs != null && attackData.songIDs.isEmpty() == false) {
                        for (AttackData.SongID songID : attackData.songIDs) {
                            if (TextUtils.isEmpty(songID.naverMusicId) == false) {
                                songIds.add(songID.naverMusicId);
                            }
                        }
                    }
                }

                if (songIds.isEmpty() == false) {
                    Uri uri = PlaylistUtil.MusicApp.NAVERMUSIC.getUri(songIds.toArray(new String[0]));
                    playMap.put(PlaylistUtil.MusicApp.NAVERMUSIC, uri);
                    showPlayButton = true;
                }
            }
            if (PlaylistUtil.checkMusicApp(this, PlaylistUtil.MusicApp.BUGS) == true) {
                songIds.clear();
                for (AttackData attackData : attackDatas) {
                    if (attackData.songIDs != null && attackData.songIDs.isEmpty() == false) {
                        for (AttackData.SongID songID : attackData.songIDs) {
                            if (TextUtils.isEmpty(songID.bugsId) == false) {
                                songIds.add(songID.bugsId);
                            }
                        }
                    }
                }

                if (songIds.isEmpty() == false) {
                    Uri uri = PlaylistUtil.MusicApp.BUGS.getUri(songIds.toArray(new String[0]));
                    playMap.put(PlaylistUtil.MusicApp.BUGS, uri);
                    showPlayButton = true;
                }
            }
            if (Build.VERSION.SDK_INT >= 21) {
                songIds.clear();

                if (TextUtils.isEmpty(youtubeListId) == true) {
                    for (AttackData attackData : attackDatas) {
                        if (attackData.songIDs != null && attackData.songIDs.isEmpty() == false) {
                            for (AttackData.SongID songID : attackData.songIDs) {
                                if (TextUtils.isEmpty(songID.youtubeId) == false) {
                                    songIds.add(songID.youtubeId);
                                }
                            }
                        }
                    }

                    if (songIds.isEmpty() == false) {
                        Uri uri = PlaylistUtil.MusicApp.YOUTUBE.getUri(songIds.toArray(new String[0]));
                        playMap.put(PlaylistUtil.MusicApp.YOUTUBE, uri);
                        showPlayButton = true;
                    }
                } else {
                    Uri uri = Uri.parse("https://www.youtube.com/playlist?list=" + youtubeListId);
                    playMap.put(PlaylistUtil.MusicApp.YOUTUBE, uri);
                    showPlayButton = true;
                }
            }

            if (playMap.isEmpty() == true) {
                mPlayList.setTag(null);
            } else {
                mPlayList.setTag(playMap);
            }
        }

        mPlayList.setVisibility(showPlayButton == true ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void loadSmings() {
        File directory = Constant.getSmingFolder();
        List<File> smings = new ArrayList<>();
        if (directory.exists() == true) {
            collectSming(directory, smings);

            Collections.sort(smings, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    return Long.valueOf(rhs.lastModified()).compareTo(Long.valueOf(lhs.lastModified()));
                }
            });
        }
        smingShotAdaptor.setFiles(smings.toArray(new File[0]));
        smingShotAdaptor.notifyDataSetChanged();
    }

    private void collectSming(File directory, List<File> smings) {
        File[] fileArray = directory.listFiles();

        if (fileArray != null) {
            for (File file : fileArray) {
                if (file.isDirectory() == true) {
                    collectSming(file, smings);
                } else {
                    smings.add(file);
                }
            }
        }
    }

    private void loadAttacks() {
        List<AttackData> attackDatas = AttackManager.getInstance().getAttackDatas();

        attackAdaptor.setAttacks(attackDatas);
        attackAdaptor.setYoutubeListId(AttackManager.getInstance().getYoutubeListId());
        attackAdaptor.notifyDataSetChanged();

        checkPlayList();
    }

    @Override
    @TargetApi(21)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String uri = data.toUri(0);
            MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Activity.MEDIA_PROJECTION_SERVICE);
            MediaProjection mediaProjection = projectionManager.getMediaProjection(resultCode, data);

            ScreenCaptureService.setMediaProjection(mediaProjection);

            startService(new Intent(ScreenCaptureService.ACTION_START, null, this, ScreenCaptureService.class));
            fab.setSelected(true);
        }

        if (requestCode == REQUEST_CODE_MAKE_SMING && resultCode == RESULT_OK) {
            loadSmings();
            checkEmpty();
        }
    }

    /****************************************** UI Widget Callbacks *******************************/
    @TargetApi(21)
    private void startProjection() {
        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Activity.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    private void startObserver() {
        startService(new Intent(ScreenCaptureService.ACTION_START, null, this, CaptureObserveService.class));
        fab.setSelected(true);
    }

    private void stopObserver() {
        startService(new Intent(ScreenCaptureService.ACTION_STOP, null, this, CaptureObserveService.class));
        fab.setSelected(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadSmings();
                    checkEmpty();

                    Constant.makeAppDefaultDirectory(MainActivity.this);
                } else {
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void unzipFonts() {
        File fontsDirectory = Constant.getFontDirectory();
        String[] fonts = fontsDirectory.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename != null && filename.toLowerCase().endsWith(".ttf");
            }
        });
        if (fonts != null && fonts.length > 0) {
            return;
        }

        try {
            InputStream inputStream = getAssets().open("fonts_e.zip");
            Util.unzipTo(inputStream, fontsDirectory);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void checkEmpty() {
        findViewById(R.id.empty).setVisibility(smingShotAdaptor.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        findViewById(R.id.empty_attack).setVisibility(attackAdaptor.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MusicListenService.ACTION_STOP.equals(intent.getAction())) {
                fab.setSelected(false);
            }
        }
    };
}
