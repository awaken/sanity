package cri.sanity;

import java.util.HashMap;
import java.util.Map;

import android.util.Pair;


// tool class: all preference keys; the default values; the upgrade phase.
public final class K
{
	// general
	public static final String ENABLED           = "enabled";
	public static final String SKIP_HEADSET      = "skip_headset";
	public static final String FORCE_BT_AUDIO    = "force_bt_audio";
	public static final String REVERSE_PROXIMITY = "reverse_proximity";
	public static final String PRF               = "profile";           // non persistent
	public static final String BACKUP_PREFS      = "backup_prefs";			// non persistent
	public static final String RESET_PREFS       = "reset_prefs";				// non persistent
	// devices
	public static final String AUTO_MOBDATA = "mobdata";
	public static final String AUTO_WIFI    = "wifi";
	public static final String AUTO_GPS     = "gps";
	public static final String AUTO_BT      = "bt";
	public static final String SKIP_BT      = "bt_skip";
	public static final String SKIP_MOBDATA = "mobdata_skip";
	public static final String SKIP_HOTSPOT = "hotspot_skip";
	public static final String SKIP_TETHER  = "tether_skip";
	public static final String REVERSE_BT   = "bt_reverse";
	public static final String REVERSE_BT_TIMEOUT = "bt_reverse_timeout";
	// proximity
	public static final String DISABLE_PROXIMITY = "disable_proximity";
	public static final String DISABLE_DELAY     = "disable_delay";
	public static final String ENABLE_PROXIMITY  = "enable_proximity";
	public static final String ENABLE_DELAY      = "enable_delay";
	public static final String SCREEN_OFF        = "screen_off";
	public static final String SCREEN_ON         = "screen_on";
	public static final String ADMIN             = "admin";							// non persistent
	// speaker
	public static final String SPEAKER_AUTO       = "auto_speaker";
	public static final String SPEAKER_DELAY      = "delay_speaker";
	public static final String SPEAKER_LOUD       = "loud_speaker";
	public static final String SPEAKER_CALL       = "speaker_call";
	public static final String SPEAKER_CALL_DELAY = "delay_speaker_call";
	public static final String SPEAKER_SILENT_END = "silent_end_speaker";
	public static final String SPEAKER_ON_COUNT   = "speaker_on_count";
	public static final String SPEAKER_OFF_COUNT  = "speaker_off_count";
	// volume
	public static final String VOL_PHONE = "vol_phone";
	public static final String VOL_WIRED = "vol_wired";
	public static final String VOL_BT    = "vol_bt";
	public static final String VOL_SOLO  = "vol_solo";
	// notification
	public static final String NOTIFY_ENABLE   = "notify_enable";
	public static final String NOTIFY_DISABLE  = "notify_disable";
	public static final String NOTIFY_ACTIVITY = "notify_activity";
	public static final String NOTIFY_VOLUME   = "notify_volume";
	public static final String NOTIFY_REC_STOP = "notify_rec_stop";
	public static final String VIBRATE_END     = "vibrate_end";
	// call recorder
	public static final String REC               = "rec";
	public static final String REC_FMT           = "rec_fmt";
	public static final String REC_SRC           = "rec_src";
	public static final String REC_BROWSE        = "rec_browse";			// non persistent
	public static final String REC_SCAN          = "rec_scan";				// non persistent
	public static final String REC_START         = "rec_start";
	public static final String REC_STOP          = "rec_stop";
	public static final String REC_START_DELAY   = "rec_start_delay";
	public static final String REC_STOP_DELAY    = "rec_stop_delay";
	public static final String REC_START_SPEAKER = "rec_start_speaker";
	public static final String REC_STOP_SPEAKER  = "rec_stop_speaker";
	public static final String REC_START_HEADSET = "rec_start_headset";
	public static final String REC_STOP_HEADSET  = "rec_stop_headset";
	public static final String REC_START_TIMES   = "rec_start_times";
	public static final String REC_STOP_LIMIT    = "rec_stop_limit";
	public static final String REC_START_DIR     = "rec_start_dir";
	//public static final String REC_START_ALL     = "rec_start_all";

	// internals (hidden to user)
	public static final String FULL     = "full";
	public static final String AGREE    = "agree";
	public static final String VER      = "ver";
	public static final String NAG      = "nag";
	public static final String BT_COUNT = "bt_count";
	public static final String PRF_NAME = "prf_name";
	// non persistent tool keys
	public static final String LOGO        = "logo";
	public static final String EULA        = "eula";
	public static final String MAIL        = "mail";
	public static final String PAYPAL      = "paypal";
	public static final String COMMENT     = "comment";
	public static final String CHANGELOG   = "changelog";
	public static final String UNINSTALL   = "uninstall";
	// screen activities (non persistent)
	public static final String SCREEN_GENERAL   = "screen_general";
	public static final String SCREEN_DEVICES   = "screen_devices";
	public static final String SCREEN_PROXIMITY = "screen_proximity";
	public static final String SCREEN_SPEAKER   = "screen_speaker";
	public static final String SCREEN_VOLUME    = "screen_volume";
	public static final String SCREEN_RECORD    = "screen_record";
	public static final String SCREEN_NOTIFY    = "screen_notify";
	public static final String SCREEN_ABOUT     = "screen_about";

	public static final String WS = "_s";		// wrap suffix for string values of integer keys

	//--- inner class
	
	//--- methods: only class P should call these methods!

	static final String[] skipKeys() {
		return new String[]{ BT_COUNT, NAG };
	}

	static final String[] wrapIntKeys() {
		return new String[]{
			DISABLE_DELAY, ENABLE_DELAY, SPEAKER_DELAY, SPEAKER_CALL, SPEAKER_CALL_DELAY, SPEAKER_ON_COUNT, SPEAKER_OFF_COUNT,
			VOL_PHONE, VOL_WIRED, VOL_BT, REC_SRC, REC_FMT, REC_START_DELAY, REC_STOP_DELAY, REC_START_HEADSET, REC_STOP_HEADSET,
			REC_STOP_LIMIT, REC_START_TIMES, REC_START_DIR, REVERSE_BT_TIMEOUT
		};
	}

	public static final Map<String,Pair<Integer,Integer>> intLabVals() {
		Map<String,Pair<Integer,Integer>> m = new HashMap<String,Pair<Integer,Integer>>();
		Pair<Integer,Integer> pd  = p(R.array.disable_delay_labels, R.array.disable_delay_values);
		Pair<Integer,Integer> psc = p(R.array.speaker_count_labels, R.array.speaker_count_values);
		m.put(DISABLE_DELAY     , pd);
		m.put(ENABLE_DELAY      , p(R.array.enable_delay_labels, R.array.enable_delay_values));
		m.put(SPEAKER_DELAY     , pd);
		m.put(SPEAKER_CALL      , p(R.array.speaker_call_labels, R.array.speaker_call_values));
		m.put(SPEAKER_CALL_DELAY, pd);
		m.put(SPEAKER_ON_COUNT  , psc);
		m.put(SPEAKER_OFF_COUNT , psc);
		m.put(REC_FMT           , p(R.array.rec_fmt_labels, R.array.rec_fmt_values));
		m.put(REC_SRC           , p(R.array.rec_src_labels, R.array.rec_src_values));
		m.put(REC_START_DELAY   , pd);
		m.put(REC_STOP_DELAY    , pd);
		m.put(REC_START_HEADSET , p(R.array.rec_start_headset_labels , R.array.rec_headset_values));
		m.put(REC_STOP_HEADSET  , p(R.array.rec_stop_headset_labels  , R.array.rec_headset_values));
		m.put(REC_STOP_LIMIT    , p(R.array.rec_stop_limit_labels    , R.array.rec_stop_limit_values));
		m.put(REC_START_TIMES   , p(R.array.rec_start_times_labels   , R.array.rec_start_times_values));
		m.put(REC_START_DIR     , p(R.array.rec_start_dir_labels     , R.array.rec_start_times_values));
		m.put(REVERSE_BT_TIMEOUT, p(R.array.bt_reverse_timeout_labels, R.array.bt_reverse_timeout_values));
		return m;
	}
	private static Pair<Integer,Integer> p(int lab, int val) { return new Pair<Integer,Integer>(lab, val); }
	
	public static final String[][] sections() {
		return new String[][]{
			new String[]{ "general_cat", ENABLED, SKIP_HEADSET, FORCE_BT_AUDIO, REVERSE_PROXIMITY },
			new String[]{ "devices_cat", AUTO_MOBDATA, AUTO_WIFI, AUTO_GPS, AUTO_BT, SKIP_BT, SKIP_MOBDATA, SKIP_HOTSPOT, SKIP_TETHER, REVERSE_BT, REVERSE_BT_TIMEOUT },
			new String[]{ "proximity_cat", DISABLE_PROXIMITY, DISABLE_DELAY, ENABLE_PROXIMITY, ENABLE_DELAY, SCREEN_OFF, SCREEN_ON },
			new String[]{ "speaker_cat", SPEAKER_AUTO, SPEAKER_DELAY, SPEAKER_LOUD, SPEAKER_CALL, SPEAKER_CALL_DELAY, SPEAKER_SILENT_END, SPEAKER_ON_COUNT, SPEAKER_OFF_COUNT },
			new String[]{ "vol_cat", VOL_PHONE, VOL_WIRED, VOL_BT, VOL_SOLO },
			new String[]{ "notify_cat", NOTIFY_ENABLE, NOTIFY_DISABLE, NOTIFY_ACTIVITY, NOTIFY_VOLUME, NOTIFY_REC_STOP, VIBRATE_END },
			new String[]{ "rec_cat", REC, REC_FMT, REC_SRC, REC_START, REC_START_DELAY, REC_START_SPEAKER, REC_START_HEADSET, REC_START_DIR, REC_START_TIMES, REC_STOP, REC_STOP_DELAY, REC_STOP_SPEAKER, REC_STOP_HEADSET, REC_STOP_LIMIT }
		};
	}

	static final Map<String,Object> getDefaults() {
		final Map<String,Object> m = new HashMap<String,Object>();
		// all preferences default values
		m.put(ENABLED           , true);			// main
		m.put(SKIP_HEADSET      , true);
		m.put(FORCE_BT_AUDIO    , false);
		m.put(REVERSE_PROXIMITY , false);
		m.put(AUTO_MOBDATA      , false);			// devices
		m.put(AUTO_WIFI         , true);
		m.put(AUTO_BT           , true);
		m.put(AUTO_GPS          , false);
		m.put(SKIP_BT           , true);
		m.put(SKIP_MOBDATA      , false);
		m.put(SKIP_HOTSPOT      , true);
		m.put(SKIP_TETHER       , true);
		m.put(REVERSE_BT        , false);
		m.put(REVERSE_BT_TIMEOUT, 30*1000);
		m.put(DISABLE_PROXIMITY , true);			// proximity
		m.put(DISABLE_DELAY     , 2000);
		m.put(ENABLE_DELAY      , 4000);
		m.put(ENABLE_PROXIMITY  , true);
		m.put(SCREEN_OFF        , true);
		m.put(SCREEN_ON         , true);
		m.put(SPEAKER_AUTO      , true);			// speaker
		m.put(SPEAKER_DELAY     , 0);
		m.put(SPEAKER_LOUD      , true);
		m.put(SPEAKER_CALL      , 0);
		m.put(SPEAKER_CALL_DELAY, 0);
		m.put(SPEAKER_SILENT_END, true);
		m.put(SPEAKER_ON_COUNT  , 0);
		m.put(SPEAKER_OFF_COUNT , 0);
		m.put(VOL_PHONE         , -1);				// volume
		m.put(VOL_WIRED         , -1);
		m.put(VOL_BT            , -1);
		m.put(VOL_SOLO          , false);
		m.put(NOTIFY_ENABLE     , true);			// notify
		m.put(NOTIFY_DISABLE    , true);
		m.put(NOTIFY_ACTIVITY   , true);
		m.put(NOTIFY_VOLUME     , false);
		m.put(NOTIFY_REC_STOP   , true);
		m.put(VIBRATE_END       , false);
		m.put(REC               , false);			// call recorder
		m.put(REC_SRC           , Rec.DEF_SRC);
		m.put(REC_FMT           , Rec.DEF_FMT);
		m.put(REC_START         , false);
		m.put(REC_STOP          , false);
		m.put(REC_START_DELAY   , 3000);
		m.put(REC_STOP_DELAY    , 3000);
		m.put(REC_START_SPEAKER , true);
		m.put(REC_STOP_SPEAKER  , true);
		m.put(REC_START_HEADSET , 0);
		m.put(REC_STOP_HEADSET  , 0);
		m.put(REC_STOP_LIMIT    , 0);
		m.put(REC_START_TIMES   , 0);
		m.put(REC_START_DIR     , 0);
		return m;
	}

	static final void upgrade(float oldVer) {
		// upgrade current preferences from an older existing version
		if(oldVer < 0.9) {
			P.renameBool(DISABLE_PROXIMITY, "proximty");
			P.renameBool( ENABLE_PROXIMITY, "restore_far");
		}
		if(oldVer < 1.2) {
			P.setDef(SKIP_BT, SKIP_HOTSPOT, SKIP_TETHER);
			P.setDefIfNew(NOTIFY_ACTIVITY, SCREEN_OFF, SCREEN_ON);
		}
		if(oldVer < 1.5) P.setDef(REC, REC_FMT);
		if(oldVer < 1.8) P.setDef(REC_SRC);
		if(oldVer < 1.9) P.setDef(NOTIFY_REC_STOP, VIBRATE_END, REC_START, REC_STOP, REC_START_DELAY, REC_STOP_DELAY, REC_START_SPEAKER, REC_STOP_SPEAKER, REC_STOP_LIMIT);
		if(oldVer < 1.93) P.setDef(REC_START_TIMES);
		if(oldVer < 1.95) {
			P.setDef(SPEAKER_CALL_DELAY, SPEAKER_SILENT_END, REC_START_HEADSET, REC_STOP_HEADSET);
			for(String k : new String[]{ VOL_PHONE, VOL_WIRED, VOL_BT }) {
				switch(A.getsi(k)) {
					case 0: P.setDef(k); break;
					case 1: A.put(k, 0); break;
				}
			}
		}
		if(oldVer < 1.96) A.put(SPEAKER_CALL, A.is(SPEAKER_CALL)? 3 : 0);
		if(oldVer < 1.97) P.setDef(SPEAKER_ON_COUNT, SPEAKER_OFF_COUNT, REC_START_DIR);
		if(oldVer < 1.99) P.setDef(REVERSE_BT, REVERSE_BT_TIMEOUT);
	}

	private K() { }

}
