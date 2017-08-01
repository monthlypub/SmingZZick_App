package com.monpub.sming.attack;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;

import org.jsoup.helper.StringUtil;

import java.util.List;

/**
 * Created by small-lab on 2016-09-08.
 */
public final class PlaylistUtil {
    public static enum MusicApp {
        MELON("멜론", "melonapp://play?ctype=1&menuid=0&cid=", ",", "7870472"),
        NAVERMUSIC("네이버뮤직", "comnhncorpnavermusic://listen?version=3&trackIds=", ",", "5740454"),
        GENIE("지니", "cromegenie://scan/?landing_type=31&landing_target=;", ";", "85121813"),
        BUGS("벅스", "bugs3://app/tracks/lists?title=전체듣기&miniplay=Y&track_ids=", "|", "4587284"),
        YOUTUBE("유튭", "http://www.youtube.com/watch_videos?video_ids=", ",", "5740454");

        public final String name;
        private final String shcemePrefix;
        private final String delimeter;
        private final String testSongId;

        private MusicApp(String name, String shcemePrefix, String delimeter, String testSongId) {
            this.name = name;
            this.shcemePrefix = shcemePrefix;
            this.delimeter = delimeter;
            this.testSongId = testSongId;
        }

        public Uri getUri(String... songids) {
            return Uri.parse(shcemePrefix + TextUtils.join(delimeter, songids));
        }

        public Uri getTestUri() {
            return getUri(testSongId);
        }

        @Override
        public String toString() {
            return name;
        }
    }


    public static boolean checkMusicApp(Context context, MusicApp musicApp) {
        Intent intent = new Intent(Intent.ACTION_VIEW, musicApp.getTestUri());
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0);

        return list == null || list.isEmpty() == false;
    }
}
