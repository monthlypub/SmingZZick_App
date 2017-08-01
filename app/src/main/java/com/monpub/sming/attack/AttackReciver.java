package com.monpub.sming.attack;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.monpub.sming.MainActivity;
import com.monpub.sming.R;
import com.monpub.sming.etc.Util;

public class AttackReciver extends BroadcastReceiver {
    public static final String ACTION_ATTACK_NOTI_CLICK = "ACTION_ATTACK_NOTI_CLICK";
    public static final String ACTION_SMING_NOTI_CLICK = "ACTION_SMING_NOTI_CLICK";
    public static final String ACTION_ATTACK_NOTI = "ACTION_ATTACK_NOTI";
    public static final String ACTION_SMING_NOTI = "ACTION_SMING_NOTI";

    public static final String EXTRA_TARGET_NAME = "extra_target_name";
    public static final String EXTRA_ATTACK_MINUTE = "extra_attack_minute";
    public static final String EXTRA_ALARM_BEFORE = "extra_alarm_before";
    public static final String EXTRA_TITLE = "extra_title";

    public static final String EXTRA_MAKETIME_TEXT = "extra_maketime_text";

    public AttackReciver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();

        switch (action) {
            case ACTION_ATTACK_NOTI :
                notiAttack(context, intent);
                return;
            case ACTION_SMING_NOTI :
                notiMakeTime(context, intent);
                return;
            case ACTION_ATTACK_NOTI_CLICK :
                clickAttackNoti(context, intent);
                return;
            case Intent.ACTION_BOOT_COMPLETED :
            case Intent.ACTION_MY_PACKAGE_REPLACED :
                AttackManager.getInstance().clearMakeTimeNoti(context);
                AttackManager.getInstance().registMakeTimeNoti(context);
                AttackManager.getInstance().refreshAttackAlarm(context);
                return;
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void clickAttackNoti(Context context, Intent intent) {
        String title = intent.getStringExtra(EXTRA_TITLE);
        Uri uri = intent.getData();

        if (uri == null) {
            return;
        }

        if (TextUtils.isEmpty(title) == false) {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(ClipData.newPlainText("attack", AttackManager.getInstance().getAttackTitle(title)));
        }


        Intent goIntent = new Intent();

        try {
            String dcPkgName = Util.findDCInsideApp(context);
            if (dcPkgName != null) {
                goIntent.setAction(Intent.ACTION_VIEW);
                goIntent.setData(uri);
                goIntent.setPackage(dcPkgName);
                goIntent.setClassName(dcPkgName, "com.dcinside.app.IntroActivity");
                goIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else {
                goIntent = new Intent();
                goIntent.setAction(Intent.ACTION_VIEW);
                goIntent.setData(uri);
                goIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        } catch (ActivityNotFoundException e) {
            goIntent = new Intent();
            goIntent.setAction(Intent.ACTION_VIEW);
            goIntent.setData(uri);
            goIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        context.startActivity(goIntent);
    }

    private void notiAttack(Context context, Intent intent) {
        String targetName = intent.getStringExtra(EXTRA_TARGET_NAME);
        String title = intent.getStringExtra(EXTRA_TITLE);
        int minute = intent.getIntExtra(EXTRA_ATTACK_MINUTE, -1);
        int before = intent.getIntExtra(EXTRA_ALARM_BEFORE, -1);
        Uri uri = intent.getData();

        if (uri == null) {
            return;
        }

        if (TextUtils.isEmpty(targetName) == true || before == -1) {
            return;
        }

        String notiText = "'" + targetName + "'로 가자!";
        if (minute >= 0) {
            notiText = minute + "분에 " + notiText;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        intent.setAction(ACTION_ATTACK_NOTI_CLICK);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.status_rec_music);
        builder.setContentTitle("총공 " + before + "분전!");
        builder.setContentText(notiText);
        builder.setPriority(Notification.PRIORITY_HIGH);

        int notiType = AttackSetting.getInstance().getAlarmNoti();
        switch (notiType) {
            case AttackSetting.NOTI_TYPE_ALL :
                builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                break;
            case AttackSetting.NOTI_TYPE_SOUND:
                builder.setDefaults(Notification.DEFAULT_SOUND);
                break;
            case AttackSetting.NOTI_TYPE_VIBRATE :
                builder.setDefaults(Notification.DEFAULT_VIBRATE);
                break;

        }

        builder.setContentIntent(PendingIntent.getBroadcast(context, 0, intent, 0));

        notificationManager.notify(101, builder.build());
    }

    private void notiMakeTime(Context context, Intent intent) {
        String makeTimeText = intent.getStringExtra(EXTRA_MAKETIME_TEXT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.status_rec_music);
        builder.setContentTitle("총공 스밍 찔 시간이다!");
        builder.setContentText(makeTimeText);
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(makeTimeText));

        int notiType = AttackSetting.getInstance().getAlarmNoti();
        switch (notiType) {
            case AttackSetting.NOTI_TYPE_ALL :
                builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                break;
            case AttackSetting.NOTI_TYPE_SOUND:
                builder.setDefaults(Notification.DEFAULT_SOUND);
                break;
            case AttackSetting.NOTI_TYPE_VIBRATE :
                builder.setDefaults(Notification.DEFAULT_VIBRATE);
                break;
        }

        builder.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT));

        notificationManager.notify(101, builder.build());
    }
}
