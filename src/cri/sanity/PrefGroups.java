package cri.sanity;

import java.util.HashMap;
import java.util.Map;
import android.util.Pair;
import cri.sanity.screen.*;


public final class PrefGroups
{
	public static final int SHORTCUT_FILTER_ROW = 3;

	public static final String[][] sections() {
		return new String[][]{
			new String[]{ "general_cat", K.ENABLED, K.QUICK_START, K.SILENT_LIMIT, K.AIRPLANE_LIMIT, K.FORCE_BT_AUDIO, K.REVERSE_PROXIMITY, K.PWD, K.PWD_CLEAR },
			new String[]{ "devices_cat", K.AUTO_MOBDATA, K.AUTO_WIFI, K.AUTO_GPS, K.AUTO_BT, K.SKIP_BT, K.SKIP_MOBDATA, K.SKIP_HOTSPOT, K.SKIP_TETHER, K.REVERSE_BT, K.REVERSE_BT_TIMEOUT, K.BT_OFF },
			new String[]{ "proximity_cat", K.DISABLE_PROXIMITY, K.DISABLE_DELAY, K.ENABLE_PROXIMITY, K.ENABLE_DELAY, K.SCREEN_OFF, K.SCREEN_ON },
			new String[]{ "speaker_cat", K.SPEAKER_AUTO, K.SPEAKER_DELAY, K.SPEAKER_CALL, K.SPEAKER_CALL_DELAY, K.SPEAKER_VOL, K.SPEAKER_SILENT_END, K.SPEAKER_ON_COUNT, K.SPEAKER_OFF_COUNT },
			new String[]{ "rec_cat", K.REC, K.REC_FMT, K.REC_SRC, K.REC_START, K.REC_START_DELAY, K.REC_FILTER, K.REC_START_SPEAKER, K.REC_START_HEADSET, K.REC_START_DIR, K.REC_START_TIMES, K.REC_STOP, K.REC_STOP_DELAY, K.REC_STOP_SPEAKER, K.REC_STOP_HEADSET, K.REC_STOP_LIMIT, K.REC_AUTOREMOVE },
			new String[]{ "block_cat", K.BLOCK_FILTER, K.BLOCK_MODE, K.BLOCK_RESUME, K.BLOCK_PICKUP, K.BLOCK_SKIP, K.BLOCK_NOTIFY, K.BLOCK_SMS, K.BLOCK_SMS_FILTER, K.BLOCK_SMS_NOTIFY, K.BLOCK_SMS_MAX },
			new String[]{ "tts_cat", K.TTS, K.TTS_HEADSET, K.TTS_SKIP, K.TTS_SOLO, K.TTS_VOL, K.TTS_TONE, K.TTS_REPEAT, K.TTS_PAUSE, K.TTS_PREFIX, K.TTS_SUFFIX, K.TTS_ANONYM, K.TTS_UNKNOWN, K.TTS_FILTER, K.TTS_STREAM },
			new String[]{ "urgent_cat", K.URGENT_FILTER, K.URGENT_MODE },
			new String[]{ "answer_cat", K.ANSWER, K.ANSWER_HEADSET, K.ANSWER_SKIP, K.ANSWER_DELAY, K.ANSWER_FILTER },
			new String[]{ "anonym_cat", K.ANONYM, K.ANONYM_CONFIRM, K.ANONYM_NOTIFY, K.ANONYM_PREFIX, K.ANONYM_FILTER },
			new String[]{ "vol_cat", K.VOL_PHONE, K.VOL_WIRED, K.VOL_BT, K.VOL_SOLO },
			new String[]{ "vibrate_cat", K.VIBRATE_PICKUP, K.VIBRATE_END, K.VIBRATE_MODE },
			new String[]{ "notify_cat", K.NOTIFY_ENABLE, K.NOTIFY_DISABLE, K.NOTIFY_ACTIVITY, K.NOTIFY_VOLUME, K.NOTIFY_REC_STOP },
		};
	}

	// SHORTCUT_FILTER_ROW contains how many values are in one row!
	public static final Object[] filterShortcuts() {
		return new Object[] {
			R.string.rec_cat     , R.drawable.menu_rec   , "rec",
			R.string.block_cat   , R.drawable.menu_block , "block",
			R.string.blocksms_cat, R.drawable.menu_block , "blocksms",
			R.string.tts_cat     , R.drawable.menu_tts   , "tts",
			R.string.ttsms_cat   , R.drawable.menu_tts   , "ttsms",
			R.string.urgent_cat  , R.drawable.menu_urgent, "urgent",
			R.string.answer_cat  , R.drawable.menu_answer, "answer",
			R.string.anonym_cat  , R.drawable.menu_anonym, "anonym",
		};
	}
	
	public static final String[] skipKeys() {
		return new String[]{ K.BT_COUNT, K.NAG, K.CRON, K.FULL, K.LICVER, K.SMS_COUNT };
	}

	public static final String[] edits() {
		return new String[]{ K.TTS_PREFIX, K.TTS_SUFFIX, K.TTS_ANONYM, K.TTS_ANONYM, K.TTS_SMS_PREFIX, K.TTS_SMS_SUFFIX, K.ANONYM_PREFIX };
	}

	public static final String[] volumes() {
		return new String[]{ K.VOL_PHONE, K.VOL_WIRED, K.VOL_BT, K.SPEAKER_VOL, K.TTS_VOL, K.TTS_SMS_VOL };
	}

	public static final Object[] screens() {
		return new Object[] {
	  	"screen_general"  , GeneralActivity.class  , R.xml.prefs_general  , R.id.menu_general  , R.layout.img_general,
	  	"screen_devices"  , DevicesActivity.class  , R.xml.prefs_devices  , R.id.menu_devices  , R.layout.img_devices,
	  	"screen_proximity", ProximityActivity.class, R.xml.prefs_proximity, R.id.menu_proximity, R.layout.img_proximity,
	  	"screen_speaker"  , SpeakerActivity.class  , R.xml.prefs_speaker  , R.id.menu_speaker  , R.layout.img_speaker,
	  	"screen_volume"   , VolumeActivity.class   , R.xml.prefs_volume   , R.id.menu_vol      , R.layout.img_vol,
	  	"screen_record"   , RecordActivity.class   , R.xml.prefs_record   , R.id.menu_rec      , R.layout.img_rec,
	  	"screen_block"    , BlockerActivity.class  , R.xml.prefs_block    , R.id.menu_block    , R.layout.img_block,
	  	"screen_tts"      , TtsActivity.class      , R.xml.prefs_tts      , R.id.menu_tts      , R.layout.img_tts,
	  	"screen_urgent"   , UrgentActivity.class   , R.xml.prefs_urgent   , R.id.menu_urgent   , R.layout.img_urgent,
	  	"screen_answer"   , AnswerActivity.class   , R.xml.prefs_answer   , R.id.menu_answer   , R.layout.img_answer,
	  	"screen_anonym"   , AnonymActivity.class   , R.xml.prefs_anonym   , R.id.menu_anonym   , R.layout.img_anonym,
	  	"screen_vibra"    , VibraActivity.class    , R.xml.prefs_vibra    , R.id.menu_vibra    , R.layout.img_vibra,
	  	"screen_notify"   , NotifyActivity.class   , R.xml.prefs_notify   , R.id.menu_notify   , R.layout.img_notify,
	  	"screen_about"    , AboutActivity.class    , R.xml.prefs_about    , R.id.menu_about    , R.layout.img_about,				
		};
	}

	public static final Map<String,Pair<Integer,Integer>> intLabVals() {
		Map<String,Pair<Integer,Integer>> m = new HashMap<String,Pair<Integer,Integer>>();
		Pair<Integer,Integer> pd  = p(R.array.disable_delay_labels, R.array.disable_delay_values);
		Pair<Integer,Integer> psc = p(R.array.speaker_count_labels, R.array.speaker_count_values);
		m.put(K.DISABLE_DELAY     , pd);
		m.put(K.ENABLE_DELAY      , p(R.array.enable_delay_labels, R.array.enable_delay_values));
		m.put(K.SPEAKER_DELAY     , pd);
		m.put(K.SPEAKER_CALL      , p(R.array.speaker_call_labels, R.array.speaker_call_values));
		m.put(K.SPEAKER_CALL_DELAY, pd);
		m.put(K.SPEAKER_ON_COUNT  , psc);
		m.put(K.SPEAKER_OFF_COUNT , psc);
		m.put(K.REC_FMT           , p(R.array.rec_fmt_labels, R.array.rec_fmt_values));
		m.put(K.REC_SRC           , p(R.array.rec_src_labels, R.array.rec_src_values));
		m.put(K.REC_START_DELAY   , pd);
		m.put(K.REC_STOP_DELAY    , pd);
		m.put(K.REC_START_HEADSET , p(R.array.rec_start_headset_labels , R.array.rec_headset_values));
		m.put(K.REC_STOP_HEADSET  , p(R.array.rec_stop_headset_labels  , R.array.rec_headset_values));
		m.put(K.REC_STOP_LIMIT    , p(R.array.rec_stop_limit_labels    , R.array.rec_stop_limit_values));
		m.put(K.REC_START_TIMES   , p(R.array.rec_start_times_labels   , R.array.rec_start_times_values));
		m.put(K.REC_START_DIR     , p(R.array.rec_start_dir_labels     , R.array.rec_start_dir_values));
		m.put(K.REC_AUTOREMOVE    , p(R.array.rec_autoremove_labels    , R.array.rec_autoremove_values));
		m.put(K.REVERSE_BT_TIMEOUT, p(R.array.bt_reverse_timeout_labels, R.array.bt_reverse_timeout_values));
		m.put(K.BLOCK_MODE        , p(R.array.block_mode_labels        , R.array.block_mode_values));
		m.put(K.BLOCK_RESUME      , p(R.array.block_resume_labels      , R.array.block_resume_values));
		m.put(K.BLOCK_SMS_MAX     , p(R.array.blocksms_max_labels      , R.array.blocksms_max_values));
		m.put(K.TTS_TONE          , p(R.array.tts_tone_labels          , R.array.tts_tone_values));
		m.put(K.TTS_REPEAT        , p(R.array.tts_repeat_labels        , R.array.tts_repeat_values));
		m.put(K.TTS_PAUSE         , p(R.array.tts_pause_labels         , R.array.tts_pause_values));
		m.put(K.URGENT_MODE       , p(R.array.urgent_mode_labels       , R.array.urgent_mode_values));
		m.put(K.ANSWER_DELAY      , pd);
		m.put(K.VIBRATE_MODE      , p(R.array.vibrate_mode_labels      , R.array.vibrate_mode_values));
		return m;
	}

	private static Pair<Integer,Integer> p(int lab, int val) { return new Pair<Integer,Integer>(lab, val); }

}
