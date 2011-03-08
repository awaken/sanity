package cri.sanity;

import java.util.HashMap;
import java.util.Map;


// tool class: all preference keys; the default values; the upgrade phase.
public final class K
{
	// general
	public static final String ENABLED            = "enabled";
	public static final String FORCE_BT_AUDIO     = "force_bt_audio";
	public static final String REVERSE_PROXIMITY  = "reverse_proximity";
	// devices
	public static final String AUTO_MOBDATA       = "mobdata";
	public static final String AUTO_WIFI          = "wifi";
	public static final String AUTO_GPS           = "gps";
	public static final String AUTO_BT            = "bt";
	public static final String SKIP_BT            = "bt_skip";
	public static final String SKIP_MOBDATA       = "mobdata_skip";
	public static final String SKIP_HOTSPOT       = "hotspot_skip";
	public static final String SKIP_TETHER        = "tether_skip";
	public static final String REVERSE_BT         = "bt_reverse";
	public static final String REVERSE_BT_TIMEOUT = "bt_reverse_timeout";
	public static final String BT_OFF             = "bt_off";
	// proximity
	public static final String DISABLE_PROXIMITY  = "disable_proximity";
	public static final String DISABLE_DELAY      = "disable_delay";
	public static final String ENABLE_PROXIMITY   = "enable_proximity";
	public static final String ENABLE_DELAY       = "enable_delay";
	public static final String SCREEN_OFF         = "screen_off";
	public static final String SCREEN_ON          = "screen_on";
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
	public static final String VOL_PHONE          = "vol_phone";
	public static final String VOL_WIRED          = "vol_wired";
	public static final String VOL_BT             = "vol_bt";
	public static final String VOL_SOLO           = "vol_solo";
	// notification
	public static final String NOTIFY_ENABLE      = "notify_enable";
	public static final String NOTIFY_DISABLE     = "notify_disable";
	public static final String NOTIFY_ACTIVITY    = "notify_activity";
	public static final String NOTIFY_VOLUME      = "notify_volume";
	public static final String NOTIFY_REC_STOP    = "notify_rec_stop";
	public static final String VIBRATE_END        = "vibrate_end";
	// call recorder
	public static final String REC                = "rec";
	public static final String REC_FMT            = "rec_fmt";
	public static final String REC_SRC            = "rec_src";
	public static final String REC_START          = "rec_start";
	public static final String REC_STOP           = "rec_stop";
	public static final String REC_START_DELAY    = "rec_start_delay";
	public static final String REC_STOP_DELAY     = "rec_stop_delay";
	public static final String REC_START_SPEAKER  = "rec_start_speaker";
	public static final String REC_STOP_SPEAKER   = "rec_stop_speaker";
	public static final String REC_START_HEADSET  = "rec_start_headset";
	public static final String REC_STOP_HEADSET   = "rec_stop_headset";
	public static final String REC_START_TIMES    = "rec_start_times";
	public static final String REC_STOP_LIMIT     = "rec_stop_limit";
	public static final String REC_START_DIR      = "rec_start_dir";
	public static final String REC_AUTOREMOVE     = "rec_autoremove";
	public static final String REC_CALLSCREEN     = "rec_callscreen";
	public static final String REC_FILTER         = "filter_enable_rec";
	// announce caller (text to speech)
	public static final String TTS                = "tts";
	public static final String TTS_HEADSET        = "tts_headset";
	public static final String TTS_SKIP           = "tts_skip";
	public static final String TTS_SOLO           = "tts_solo";
	public static final String TTS_VOL            = "tts_vol";
	public static final String TTS_TONE           = "tts_tone";
	public static final String TTS_REPEAT         = "tts_repeat";
	public static final String TTS_PAUSE          = "tts_pause";
	public static final String TTS_ANONYM         = "tts_anonym";
	public static final String TTS_UNKNOWN        = "tts_anonym";
	public static final String TTS_PREFIX         = "tts_prefix";
	public static final String TTS_SUFFIX         = "tts_suffix";
	public static final String TTS_FILTER         = "filter_enable_tts";
	// call blocker
	public static final String BLOCK              = "block";
	public static final String BLOCK_SKIP         = "block_skip";
	public static final String BLOCK_MODE         = "block_mode";
	public static final String BLOCK_FILTER       = "filter_enable_block";

	// internals (hidden to user)
	public static final String FULL     = "full";
	public static final String BETA     = "beta";
	public static final String AGREE    = "agree";
	public static final String VER      = "ver";
	public static final String NAG      = "nag";
	public static final String BT_COUNT = "bt_count";
	public static final String PRF_NAME = "prf_name";
	public static final String CRON     = "cron";

	public static final String WS = "_s";		// wrap suffix for string values of integer keys

	//--- inner class
	
	//--- methods: only class P should call these methods!

	static final String[] skipKeys() { return new String[]{ BT_COUNT, NAG, CRON }; }

	static final String[] wrapIntKeys() {
		return new String[]{
			DISABLE_DELAY, ENABLE_DELAY, SPEAKER_DELAY, SPEAKER_CALL, SPEAKER_CALL_DELAY, SPEAKER_ON_COUNT, SPEAKER_OFF_COUNT,
			VOL_PHONE, VOL_WIRED, VOL_BT, REC_SRC, REC_FMT, REC_START_DELAY, REC_STOP_DELAY, REC_START_HEADSET, REC_STOP_HEADSET,
			REC_STOP_LIMIT, REC_START_TIMES, REC_START_DIR, REC_AUTOREMOVE, REVERSE_BT_TIMEOUT, TTS_VOL, TTS_TONE, TTS_REPEAT, TTS_PAUSE,
			BLOCK_MODE
		};
	}

	static final Map<String,Object> getDefaults() {
		final Map<String,Object> m = new HashMap<String,Object>();
		// all preferences default values
		m.put(ENABLED            , true);				// main
		m.put(FORCE_BT_AUDIO     , false);
		m.put(REVERSE_PROXIMITY  , false);
		m.put(AUTO_MOBDATA       , false);			// devices
		m.put(AUTO_WIFI          , false);
		m.put(AUTO_BT            , false);
		m.put(AUTO_GPS           , false);
		m.put(SKIP_BT            , true);
		m.put(SKIP_MOBDATA       , false);
		m.put(SKIP_HOTSPOT       , true);
		m.put(SKIP_TETHER        , true);
		m.put(REVERSE_BT         , false);
		m.put(REVERSE_BT_TIMEOUT , 30*1000);
		m.put(BT_OFF             , false);
		m.put(DISABLE_PROXIMITY  , false);			// proximity
		m.put(DISABLE_DELAY      , 2000);
		m.put(ENABLE_DELAY       , 4000);
		m.put(ENABLE_PROXIMITY   , false);
		m.put(SCREEN_OFF         , true);
		m.put(SCREEN_ON          , true);
		m.put(SPEAKER_AUTO       , false);			// speaker
		m.put(SPEAKER_DELAY      , 0);
		m.put(SPEAKER_LOUD       , true);
		m.put(SPEAKER_CALL       , 0);
		m.put(SPEAKER_CALL_DELAY , 0);
		m.put(SPEAKER_SILENT_END , true);
		m.put(SPEAKER_ON_COUNT   , 0);
		m.put(SPEAKER_OFF_COUNT  , 0);
		m.put(VOL_PHONE          , -1);					// volume
		m.put(VOL_WIRED          , -1);
		m.put(VOL_BT             , -1);
		m.put(VOL_SOLO           , false);
		m.put(NOTIFY_ENABLE      , true);				// notify
		m.put(NOTIFY_DISABLE     , true);
		m.put(NOTIFY_ACTIVITY    , true);
		m.put(NOTIFY_VOLUME      , false);
		m.put(NOTIFY_REC_STOP    , true);
		m.put(VIBRATE_END        , false);
		m.put(REC                , false);			// call recorder
		m.put(REC_SRC            , Rec.DEF_SRC);
		m.put(REC_FMT            , Rec.DEF_FMT);
		m.put(REC_START          , false);
		m.put(REC_STOP           , false);
		m.put(REC_START_DELAY    , 3000);
		m.put(REC_STOP_DELAY     , 3000);
		m.put(REC_START_SPEAKER  , true);
		m.put(REC_STOP_SPEAKER   , true);
		m.put(REC_START_HEADSET  , 0);
		m.put(REC_STOP_HEADSET   , 0);
		m.put(REC_STOP_LIMIT     , 0);
		m.put(REC_START_TIMES    , 0);
		m.put(REC_START_DIR      , 0);
		m.put(REC_AUTOREMOVE     , 0);
		m.put(REC_CALLSCREEN     , true);
		m.put(REC_FILTER         , false);
		m.put(TTS                , false);			// announce caller
		m.put(TTS_HEADSET        , false);
		m.put(TTS_SKIP           , true);
		m.put(TTS_SOLO           , false);
		m.put(TTS_VOL            , -1);
		m.put(TTS_TONE           ,  0);
		m.put(TTS_REPEAT         , 1000);
		m.put(TTS_PAUSE          , 1000);
		m.put(TTS_ANONYM         , A.s(R.string.anonym ));
		m.put(TTS_UNKNOWN        , A.s(R.string.unknown));
		m.put(TTS_PREFIX         , "");
		m.put(TTS_SUFFIX         , "");
		m.put(TTS_FILTER         , false);
		m.put(BLOCK              , false);
		m.put(BLOCK_SKIP         , false);
		m.put(BLOCK_MODE         , Blocker.MODE_FLIGHT);
		m.put(BLOCK_FILTER       , true);
		return m;
	}

	static final void upgrade(float oldVer, int beta) {
		// upgrade current preferences from an older existing version
		if(oldVer < 0.9f) {
			P.renameBool(DISABLE_PROXIMITY, "proximty");
			P.renameBool( ENABLE_PROXIMITY, "restore_far");
		}
		if(oldVer < 1.2f) {
			P.setDef(SKIP_BT, SKIP_HOTSPOT, SKIP_TETHER);
			P.setDefIfNew(NOTIFY_ACTIVITY, SCREEN_OFF, SCREEN_ON);
		}
		if(oldVer < 1.5f) P.setDef(REC, REC_FMT);
		if(oldVer < 1.8f) P.setDef(REC_SRC);
		if(oldVer < 1.9f) P.setDef(NOTIFY_REC_STOP, VIBRATE_END, REC_START, REC_STOP, REC_START_DELAY, REC_STOP_DELAY, REC_START_SPEAKER, REC_STOP_SPEAKER, REC_STOP_LIMIT);
		if(oldVer < 1.93f) P.setDef(REC_START_TIMES);
		if(oldVer < 1.95f) {
			P.setDef(SPEAKER_CALL_DELAY, SPEAKER_SILENT_END, REC_START_HEADSET, REC_STOP_HEADSET);
			for(String k : new String[]{ VOL_PHONE, VOL_WIRED, VOL_BT }) {
				try {
					switch(A.getsi(k)) {
						case 0: P.setDef(k); break;
						case 1: A.put(k, 0); break;
					}
				} catch(Exception e) { try {
					A.put(k, A.has(k)? A.geti(k) : -1);
				} catch(Exception e2) {
					A.put(k, -1);
				}}
			}
		}
		if(oldVer < 1.96f) A.put(SPEAKER_CALL, A.is(SPEAKER_CALL)? 3 : 0);
		if(oldVer < 1.97f) P.setDef(SPEAKER_ON_COUNT, SPEAKER_OFF_COUNT, REC_START_DIR);
		if(oldVer < 1.99f) P.setDef(REVERSE_BT, REVERSE_BT_TIMEOUT);
		if(oldVer < 2.00f) P.setDef(BT_OFF, REC_FILTER, REC_AUTOREMOVE, TTS, TTS_HEADSET, TTS_SOLO, TTS_VOL, TTS_TONE, TTS_REPEAT, TTS_PAUSE, TTS_PREFIX, TTS_SUFFIX, TTS_ANONYM, TTS_UNKNOWN, TTS_FILTER);
		if(oldVer < 2.02f) P.setDef(REC_CALLSCREEN, TTS_SKIP, BLOCK, BLOCK_SKIP, BLOCK_MODE, BLOCK_FILTER);
	}

}
