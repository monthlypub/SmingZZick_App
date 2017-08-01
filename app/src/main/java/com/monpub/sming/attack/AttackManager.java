package com.monpub.sming.attack;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.WebView;

import com.monpub.sming.SmingApplication;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by small-lab on 2016-08-17.
 */
public class AttackManager {
    private static final String REGEX_TIME = "(\\d\\d?):(\\d\\d?).*";
    private static final String REGEX_TITLE = "(\\[.*\\].+)";
    private static final String REGEX_TITLE2 = "총공명\\s*:\\s*(.+)";
    private static final String REGEX_TARGET_URL = "((http|https):\\/\\/\\S+).*";
    private static final String REGEX_ATTACK_DATA = "(\\d+)\\|\\|(\\D+)\\|\\|(.+)";
    private static final String REGEX_SMING = "스밍[^:]*:\\s?(.+)";
    private static final String REGEX_SMING_URL = "(http[s]?:\\/\\/\\S+)$";
    private static final String REGEX_SONG_ID = "((sid)|(SID))\\s(.+)";
    private static final String REGEX_YOUTUBE_LIST = "(?i)YOUTUBE\\s+LIST\\s*:\\s*(.*)\\s*";

    private static AttackManager ourInstance = new AttackManager();

    public static AttackManager getInstance() {
        return ourInstance;
    }

    private AttackManager() {
        loadAttackData();
    }

    private List<AttackData> attackDatas;
    private String youtubeListId;

    private AttackSetting attackSetting = AttackSetting.getInstance();
    private OnAttackReadyListener onAttackReadyListener;

    public void setOnAttackReadyListener(OnAttackReadyListener listener) {
        onAttackReadyListener = listener;
    }

    public String loadFromClipboard(Context context) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Activity.CLIPBOARD_SERVICE);
        if (clipboardManager.hasPrimaryClip() == false) {
            return null;
        }

        ClipData clipData = clipboardManager.getPrimaryClip();
        ClipData.Item item = null;
        if (clipData.getItemCount() >= 1) {
            item = clipData.getItemAt(0);
        }

        if (item == null || item.getText() == null) {
            return null;
        }

        String clipText = (String) item.getText().toString();
        if (TextUtils.isEmpty(clipText)) {
            return null;
        }


        return clipText;
    }

    private static final long MILLS_MINUTE = 60 * 1000;
    public boolean analyseAttack(int month, int date, String attackString, Context context) {
        if (attackString == null) {
            return false;
        }

        attackString = attackString.replaceAll("\\\\n", "\n");

        String[] sprits = attackString.split("\n");
        List<String> sprites = new ArrayList<>(Arrays.asList(sprits));
        while(sprites.remove("")){};
        while(sprites.remove(" ")){};
        sprits = sprites.toArray(new String[0]);

        List<AttackData> attackDatas = new ArrayList<>();
        AttackData data;

        String trim;

        long time = -1;
        String title = null;
        String targetUrl = null;
        String smingTarget = null;
        String smingUrl = null;
        List<AttackData.SongID> songIDs = null;

        String youtubeListId = null;

        long lastMills = 0;
        for (String split : sprits) {
            trim = new String(split);
            trim = trim.replaceAll("" + (char) 160, " ");
            trim = trim.trim();

            if (TextUtils.isEmpty(trim) == true) {
                continue;
            }

            if (trim.matches(REGEX_TARGET_URL) == true) {
                if (TextUtils.isEmpty(targetUrl) == false) {
                    data = AttackData.generateAttackData(time, title, targetUrl, smingTarget, smingUrl, songIDs);

                    if (data != null) {
                        lastMills = time;
                        attackDatas.add(data);
                    }
                    time = -1;
                    title = null;
                    targetUrl = null;
                    smingTarget = null;
                    smingUrl = null;
                    songIDs = null;
                }
                Matcher matcher = Pattern.compile(REGEX_TARGET_URL).matcher(trim);
                if (matcher.find() == true) {
                    targetUrl = matcher.group(0);
                    targetUrl = targetUrl.replace("dcinside.co.kr", "dcinside.com");
                    targetUrl = targetUrl.replaceAll("컴", "com");
                }
                continue;
            }

            if (trim.matches(REGEX_TIME) == true) {
                if (time > 0) {
                    data = AttackData.generateAttackData(time, title, targetUrl, smingTarget, smingUrl, songIDs);

                    if (data != null) {
                        lastMills = time;
                        attackDatas.add(data);
                    }
                    time = -1;
                    title = null;
                    targetUrl = null;
                    smingTarget = null;
                    smingUrl = null;
                    songIDs = null;
                }
                Matcher matcher = Pattern.compile(REGEX_TIME).matcher(trim);
                if (matcher.find() == true) {
                    String hour = matcher.group(1);
                    String minute = matcher.group(2);

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.SECOND, 00);
                    calendar.set(Calendar.MILLISECOND, 00);

                    if (month  >= 0 && date >= 0) {
                        calendar.set(Calendar.MONTH, month - 1);
                        calendar.set(Calendar.DAY_OF_MONTH, date);
                    }

                    calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
                    calendar.set(Calendar.MINUTE, Integer.valueOf(minute));

                    if (calendar.getTimeInMillis() < lastMills) {
                        calendar.add(Calendar.DATE, 1);
                    }

                    time = calendar.getTimeInMillis();
                }

                continue;
            }

            if (trim.matches(REGEX_TITLE2) == true) {
                if (TextUtils.isEmpty(title) == false) {
                    data = AttackData.generateAttackData(time, title, targetUrl, smingTarget, smingUrl, songIDs);

                    if (data != null) {
                        lastMills = time;
                        attackDatas.add(data);
                    }
                    time = -1;
                    title = null;
                    targetUrl = null;
                    smingTarget = null;
                    smingUrl = null;
                    songIDs = null;
                }
                Matcher matcher = Pattern.compile(REGEX_TITLE2).matcher(trim);
                if (matcher.find() == true) {
                    title = matcher.group(1).trim();
                }

                continue;
            } else if (trim.matches(REGEX_TITLE) == true) {
                if (TextUtils.isEmpty(title) == false) {
                    data = AttackData.generateAttackData(time, title, targetUrl, smingTarget, smingUrl, songIDs);

                    if (data != null) {
                        lastMills = time;
                        attackDatas.add(data);
                    }
                    time = -1;
                    title = null;
                    targetUrl = null;
                    smingTarget = null;
                    smingUrl = null;
                    songIDs = null;
                }
                title = trim;
                continue;
            }



            if (trim.matches(REGEX_SMING) == true) {
                if (TextUtils.isEmpty(smingTarget) == false || TextUtils.isEmpty(smingUrl) == false)  {
                    data = AttackData.generateAttackData(time, title, targetUrl, smingTarget, smingUrl, songIDs);

                    if (data != null) {
                        lastMills = time;
                        attackDatas.add(data);
                    }
                    time = -1;
                    title = null;
                    targetUrl = null;
                    smingTarget = null;
                    smingUrl = null;
                    songIDs = null;
                }

                Matcher matcher;
                matcher = Pattern.compile(REGEX_SMING_URL).matcher(trim);
                if (matcher.find() == true) {
                    smingUrl = matcher.group(1).trim();
                    smingUrl = smingUrl.replaceAll("컴", "com");
                    trim = trim.replaceAll(REGEX_SMING_URL, "");
                }

                matcher = Pattern.compile(REGEX_SMING).matcher(trim);
                if (matcher.find() == true) {
                    smingTarget = matcher.group(1).trim();
                }
                continue;
            }
            if (trim.matches(REGEX_SONG_ID) == true) {
                if (songIDs == null) {
                    songIDs = new ArrayList<>();
                }

                Matcher matcher = Pattern.compile(REGEX_SONG_ID).matcher(trim);
                if (matcher.find() == true) {
                    String songIdString = matcher.group(matcher.groupCount()).trim();
                    AttackData.SongID songID = AttackData.SongID.parseString(songIdString);

                    if (songID != null) {
                        songIDs.add(songID);
                    }
                }

                continue;
            }

            if (trim.matches(REGEX_YOUTUBE_LIST) == true) {
                if (songIDs == null) {
                    songIDs = new ArrayList<>();
                }

                Matcher matcher = Pattern.compile(REGEX_YOUTUBE_LIST).matcher(trim);
                if (matcher.find() == true) {
                    youtubeListId = matcher.group(matcher.groupCount()).trim();
                }

                continue;
            }

        }

        data = AttackData.generateAttackData(time, title, targetUrl, smingTarget, smingUrl, songIDs);

        if (data != null) {
            lastMills = time;
            attackDatas.add(data);
        }

        if (attackDatas.isEmpty() == false) {
            if (this.attackDatas != null && this.attackDatas.isEmpty() == false) {
                clearAttackData(context);
            }

            this.attackDatas = attackDatas;
            handleAttackData(context);
        } else {
            return false;
        }

        this.youtubeListId = youtubeListId;
        return true;
    }

    public void clearAttackData(Context context) {
        if (this.attackDatas != null && this.attackDatas.isEmpty() == false) {
            clearMakeTimeNoti(context);
            for (AttackData old : this.attackDatas) {
                cancelAlarm(context, old);
            }
            youtubeListId = null;
            this.attackDatas.clear();

            saveAttackData();
        }
    }

    public void handleAttackPage(final Context context, String url, Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", SmingApplication.getUserAgent())
                .build();
        client.newCall(request).enqueue(callback);
    }

    private static final String REGEX_HTML_TITLE = ".*<meta name=\"title\" content=\"(.+)\">";
    private void handleAttackData(final Context context) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();

        for (final AttackData attackData : attackDatas) {
            Request request = new Request.Builder()
                    .url(attackData.targetUrl)
                    .addHeader("Accept", "text/plain")
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    attackData.setTargetName("알수없는 갤러리");
                    saveAttackData();

                    if (checkNameReady() == true) {
                        handleNameReady(context);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String body = response.body().string();
                    Matcher matcher = Pattern.compile(REGEX_HTML_TITLE).matcher(body);
                    if (matcher.find() == true) {
                        String name = matcher.group(1);
                        attackData.setTargetName(name);
                    } else {
                        attackData.setTargetName("알수없는 갤러리");
                    }

                    saveAttackData();

                    if (checkNameReady() == true) {
                        handleNameReady(context);
                    }
                }
            });
        }

        saveAttackData();
    }

    private static final String PREF_KEY_ATTACK_DATA = "prefKeyAttackData";
    private static final String PREF_KEY_ATTACK_DATA_COUNT = "prefKeyAttackDataCount";
    private static final String PREF_KEY_ATTACK_YOUTUBE_LIST_ID = "prefKeyAttackYoutubeListId";

    private static final String PREFIX_NAME= "NAME";
    private static final String PREFIX_TITLE = "TITLE";
    private static final String PREFIX_TIME = "TIME";
    private static final String PREFIX_TARGET = "TARGET";
    private static final String PREFIX_SMING = "SMING";
    private static final String PREFIX_SMING_URL = "SMING_URL";
    private static final String PREFIX_SONG_ID = "SONGID";

    private void loadAttackData() {

        int attackDataCount = SmingApplication.getPreference().getInt(PREF_KEY_ATTACK_DATA_COUNT, 0);
        if (attackDataCount == 0) {
            return;
        }
        youtubeListId = SmingApplication.getPreference().getString(PREF_KEY_ATTACK_YOUTUBE_LIST_ID);

        Set<String> stringSet = SmingApplication.getPreference().getStringSet(PREF_KEY_ATTACK_DATA);
        String[] nameArray = new String[attackDataCount];
        String[] titleArray = new String[attackDataCount];
        long[] timeAttay = new long[attackDataCount];
        String[] targetArray = new String[attackDataCount];
        String[] smingArray = new String[attackDataCount];
        String[] smingUrlArray = new String[attackDataCount];
        ArrayList<AttackData.SongID>[] songIDsArray = new ArrayList[attackDataCount];


        Arrays.fill(songIDsArray, null);

        int index;
        String type;
        String value;

        for (String string : stringSet) {
            Matcher matcher = Pattern.compile(REGEX_ATTACK_DATA).matcher(string);
            if (matcher.find() == true) {
                index = Integer.valueOf(matcher.group(1));
                type = matcher.group(2);
                value = matcher.group(3);

                switch (type) {
                    case PREFIX_NAME:
                        nameArray[index] = value;
                        break;
                    case PREFIX_TITLE :
                        titleArray[index] = value;
                        break;
                    case PREFIX_TIME :
                        timeAttay[index] = Long.valueOf(value);
                        break;
                    case PREFIX_TARGET :
                        targetArray[index] = value;
                        break;
                    case PREFIX_SMING :
                        smingArray[index] = value;
                        break;
                    case PREFIX_SMING_URL :
                        smingUrlArray[index] = value;
                        break;
                    case PREFIX_SONG_ID :
                        if (songIDsArray[index] == null) {
                            songIDsArray[index] = new ArrayList<>();
                        }

                        AttackData.SongID songID = AttackData.SongID.parseString(value);
                        if (songID != null) {
                            songIDsArray[index].add(AttackData.SongID.parseString(value));
                        }
                        break;
                }
            }
        }

        if (attackDatas == null) {
            attackDatas = new ArrayList<>();
        }
        for (int i = 0; i < attackDataCount; i++) {
            AttackData attackData = AttackData.generateAttackData(timeAttay[i], titleArray[i], targetArray[i], smingArray[i], smingUrlArray[i], songIDsArray[i]);
            if (attackData != null && nameArray[i] != null) {
                attackData.setTargetName(nameArray[i]);
                attackDatas.add(attackData);
            }
        }
    }

    private boolean checkNameReady() {
        for (AttackData attackData : attackDatas) {
            if (attackData.getTargetName() == null) {
                return false;
            }
        }

        return true;
    }

    private void handleNameReady(Context context) {
        clearMakeTimeNoti(context);
        registMakeTimeNoti(context);
        registAttacks(context);
        if (onAttackReadyListener != null) {
            onAttackReadyListener.onAttakReady();
        }
    }

    private void saveAttackData() {
        synchronized (this) {
            Set<String> stringSet = new HashSet<>();

            for (int i = 0; i < attackDatas.size(); i++) {
                stringSet.add(i + "||" + PREFIX_NAME + "||" + attackDatas.get(i).getTargetName());
                if (TextUtils.isEmpty(attackDatas.get(i).title) == false) {
                    stringSet.add(i + "||" + PREFIX_TITLE + "||" + attackDatas.get(i).title);
                }
                stringSet.add(i + "||" + PREFIX_TIME + "||" + attackDatas.get(i).time.getTime());
                stringSet.add(i + "||" + PREFIX_TARGET + "||" + attackDatas.get(i).targetUrl);
                stringSet.add(i + "||" + PREFIX_SMING + "||" + attackDatas.get(i).smingTarget);
                if (TextUtils.isEmpty(attackDatas.get(i).smingUrl) == false) {
                    stringSet.add(i + "||" + PREFIX_SMING_URL + "||" + attackDatas.get(i).smingUrl);
                }
                if (attackDatas.get(i).songIDs != null && attackDatas.get(i).songIDs.isEmpty() == false) {
                    for (AttackData.SongID songID : attackDatas.get(i).songIDs) {
                        stringSet.add(i + "||" + PREFIX_SONG_ID + "||" + songID.toString());
                    }
                }
            }

            SmingApplication.getPreference().put(PREF_KEY_ATTACK_DATA_COUNT, attackDatas.size());
            SmingApplication.getPreference().put(PREF_KEY_ATTACK_DATA, stringSet);

            SmingApplication.getPreference().put(PREF_KEY_ATTACK_YOUTUBE_LIST_ID, youtubeListId);
        }
    }

    private static final int MILLS_HOUR = 60 * 60 * 1000;

    private void registAttacks(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        for (AttackData attackData : attackDatas) {
            registAttack(context, alarmManager, attackData);
        }
    }

    private void registAttack(Context context, AlarmManager alarmManager, AttackData attackData) {
        int beforeMinute = attackSetting.getAlarmBefore();

        if (beforeMinute < 0) {
            return;
        }
        long triggerAtMills = attackData.time.getTime() - beforeMinute * 60 * 1000;
        if (triggerAtMills < System.currentTimeMillis()) {
            return;
        }

        PendingIntent pendingIntent = generatePendingIntent(context, attackData);

        registAlarm(alarmManager, triggerAtMills, pendingIntent);
    }

    private PendingIntent generatePendingIntent(Context context, AttackData attackData) {
        String targetUrl = attackData.targetUrl;

        Uri data = Uri.parse(attackData.targetUrl + "#" + attackData.time.getTime());

        Intent intent = new Intent(AttackReciver.ACTION_ATTACK_NOTI, data);
        intent.putExtra(AttackReciver.EXTRA_TARGET_NAME, attackData.getTargetName());
        intent.putExtra(AttackReciver.EXTRA_ATTACK_MINUTE, attackData.getMinute());
        intent.putExtra(AttackReciver.EXTRA_ALARM_BEFORE, attackSetting.getAlarmBefore());
        intent.putExtra(AttackReciver.EXTRA_TITLE, attackData.title);

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void registAlarm(AlarmManager alarmManager, long triggerAtMills, PendingIntent pendingIntent) {
        if (System.currentTimeMillis() > triggerAtMills) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMills, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMills, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMills, pendingIntent);
        }
    }

    public void cancelAttack(Context context, AttackData attackData) {
        clearMakeTimeNoti(context);
        attackDatas.remove(attackData);
        saveAttackData();

        cancelAlarm(context, attackData);
        registMakeTimeNoti(context);
    }

    private void cancelAlarm(Context context, AttackData attackData) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        try {
            alarmManager.cancel(generatePendingIntent(context, attackData));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void cancelAlarm(Context context, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        try {
            alarmManager.cancel(pendingIntent);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    public void refreshAttackAlarm(Context context) {
        if (attackDatas == null) {
            return;
        }
        List<AttackData> copied = new ArrayList<>(attackDatas);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        for (AttackData attackData : copied) {
            cancelAlarm(context, attackData);
            registAttack(context, alarmManager, attackData);
        }
    }

    public void clearMakeTimeNoti(Context context) {
        if (attackDatas == null) {
            return;
        }

        Map<Long, String> makeMap = new HashMap<>();
        for (AttackData attackData : attackDatas) {
            long mills = attackData.time.getTime();
            long hourCut = mills - mills % MILLS_HOUR;

            String makeTimeText = makeMap.get(hourCut);
            String target = attackData.getTargetName() + " - " + attackData.getTargetName();
            if (TextUtils.isEmpty(makeTimeText) == false) {
                makeTimeText += ",\n" + target;
            } else {
                makeTimeText = target;
            }
            makeMap.put(hourCut, makeTimeText);
        }

        Set<Long> keySet = makeMap.keySet();
        for (Long mills : keySet) {
            PendingIntent pendingIntent = generateMakeTimePendingIntent(context, mills, makeMap.get(mills));
            cancelAlarm(context, pendingIntent);
        }

    }

    public void registMakeTimeNoti(Context context) {
        if (attackDatas == null) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Map<Long, String> makeMap = new HashMap<>();
        for (AttackData attackData : attackDatas) {
            long mills = attackData.time.getTime();
            long hourCut = mills - mills % MILLS_HOUR;

            String makeTimeText = makeMap.get(hourCut);
            String target = attackData.getTargetName() + " - " + attackData.smingTarget;
            if (TextUtils.isEmpty(makeTimeText) == false) {
                makeTimeText += ",\n" + target;
            } else {
                makeTimeText = target;
            }
            makeMap.put(hourCut, makeTimeText);
        }

        Set<Long> keySet = makeMap.keySet();
        for (Long mills : keySet) {
            PendingIntent pendingIntent = generateMakeTimePendingIntent(context, mills, makeMap.get(mills));
            registAlarm(alarmManager, mills, pendingIntent);
        }
    }

    private PendingIntent generateMakeTimePendingIntent(Context context, long mills, String makeTImeText) {
        Uri data = Uri.parse("sming://maketime#" + mills);

        Intent intent = new Intent(AttackReciver.ACTION_SMING_NOTI, data);
        intent.putExtra(AttackReciver.EXTRA_MAKETIME_TEXT, makeTImeText);

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public String getAttackTitle(String rawTitle) {
        String specialWord = attackSetting.getSpecialWord();

        if (TextUtils.isEmpty(specialWord) == true) {
            return rawTitle;
        }

        boolean replaced = false;
        while (rawTitle.indexOf("특문") >= 0) {
            rawTitle = rawTitle.replace("특문", specialWord);
            replaced = true;
        }

        if (replaced == false) {
            rawTitle += specialWord;
        }

        return rawTitle;
    }

    public List<AttackData> getAttackDatas() {
        return attackDatas;
    }

    public String getYoutubeListId() {
        return youtubeListId;
    }

    public interface OnAttackReadyListener {
        public void onAttakReady();
    }
}
