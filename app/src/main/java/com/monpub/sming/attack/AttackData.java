package com.monpub.sming.attack;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by small-lab on 2016-08-17.
 */
public class AttackData {
    public final Date time;
    public final String title;
    public final String targetUrl;
    public final String smingTarget;
    public final String smingUrl;
    public final List<SongID> songIDs;
    private String targetName;

    public AttackData(long time, String title, String targetUrl, String smingTarget, String smingUrl, List<SongID> songIDs) {
        this.time = new Date(time);
        this.title = title;
        this.targetUrl = targetUrl;
        this.smingTarget = smingTarget;
        this.smingUrl = smingUrl;
        if (songIDs != null) {
            this.songIDs = new ArrayList<>(songIDs);
        } else {
            this.songIDs = new ArrayList<>();
        }
    }

    public static AttackData generateAttackData(long time, String title, String targetUrl, String smingTarget, String smingUrl, List<SongID> songIDs) {
        if (time <= 0 || TextUtils.isEmpty(targetUrl) == true) {
            return null;
        }

        if (TextUtils.isEmpty(smingTarget) == true) {
            smingTarget = "스밍 미정";
        }

        return new AttackData(time, title, targetUrl, smingTarget, smingUrl, songIDs);
    }

    public boolean equals(String targetUrl, long time) {
        if (targetUrl != null && targetUrl.equals("targetUrl") == true && time == this.time.getTime()) {
            return true;
        }
        return false;
    }

    public String getTargetName() {
        return targetName;
    }

    public int getMinute() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        return calendar.get(Calendar.MINUTE);
    }


    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    private static final int MILLS_ONEDAY = 24 * 60 * 60 * 1000;
    public int getNotiID() {
        return (int) (time.getTime() % MILLS_ONEDAY);
    }

    public static final class SongID {
        public final String melonId;
        public final String genieId;
        public final String naverMusicId;
        public final String bugsId;
        public final String youtubeId;

        public SongID(String melonId, String genieId, String naverMusicId, String bugsId, String youtubeId) {
            this.melonId = melonId;
            this.genieId = genieId;
            this.naverMusicId = naverMusicId;
            this.bugsId = bugsId;
            this.youtubeId = youtubeId;
        }

        private static final String REGEX_SONG_ID_MELON =       "\\s*(M|m)\\s*:\\s*(\\d+)\\s*";
        private static final String REGEX_SONG_ID_GENIE =       "\\s*(G|g)\\s*:\\s*(\\d+)\\s*";
        private static final String REGEX_SONG_ID_NAVERMUSIC = "\\s*(N|n)\\s*:\\s*(\\d+)\\s*";
        private static final String REGEX_SONG_ID_BUGS =        "\\s*(B|b)\\s*:\\s*(\\d+)\\s*";
        private static final String REGEX_SONG_ID_YOUTUBE =     "\\s*(Y|y)\\s*:\\s*([^|\\s]*)\\s*";

        public static SongID parseString(String value) {
            if (TextUtils.isEmpty(value) == true || TextUtils.isEmpty(value.trim()) == true) {
                return null;
            }

            SongID songID = null;
            String[] splits = value.split("\\|");

            String melonId, genieId, naverMusicId, bugsId, youtubeId;
            melonId = genieId = naverMusicId = bugsId = youtubeId = null;

            Matcher matcher;
            for (String splite : splits) {
                matcher = Pattern.compile(REGEX_SONG_ID_MELON).matcher(splite);
                if (matcher.find() == true) {
                    melonId = matcher.group(2).trim();
                } else if ((matcher = Pattern.compile(REGEX_SONG_ID_GENIE).matcher(splite)).find() == true) {
                    genieId = matcher.group(2).trim();
                } else if ((matcher = Pattern.compile(REGEX_SONG_ID_NAVERMUSIC).matcher(splite)).find() == true) {
                    naverMusicId = matcher.group(2).trim();
                } else if ((matcher = Pattern.compile(REGEX_SONG_ID_BUGS).matcher(splite)).find() == true) {
                    bugsId = matcher.group(2).trim();
                } else if ((matcher = Pattern.compile(REGEX_SONG_ID_YOUTUBE).matcher(splite)).find() == true) {
                    youtubeId = matcher.group(2).trim();
                }

        }

            if (melonId != null || genieId != null || naverMusicId != null || bugsId != null || youtubeId != null) {
                songID = new AttackData.SongID(melonId, genieId, naverMusicId, bugsId, youtubeId);
            }

            return songID;
        }

        @Override
        public String toString() {
            return String.format("M:%s|G:%s|N:%s|B:%s|Y:%s",
                    melonId == null ? "" : melonId,
                    genieId == null ? "" : genieId,
                    naverMusicId == null ? "" : naverMusicId,
                    bugsId == null ? "" : bugsId,
                    youtubeId == null ? "" : youtubeId);
        }
    }
}
