package cri.sanity;

import java.util.HashMap;
import java.util.Map;

// tool class: all preference keys; the default values; the upgrade phase.
public final class K
{
	// general
	public static final String ENABLED           = "enabled";
	public static final String SKIP_HEADSET      = "skip_headset";
	public static final String FORCE_BT_AUDIO    = "force_bt_audio";
	public static final String REVERSE_PROXIMITY = "reverse_proximity";
	// devices
	public static final String AUTO_MOBDATA = "mobdata";
	public static final String AUTO_WIFI    = "wifi";
	public static final String AUTO_GPS     = "gps";
	public static final String AUTO_BT      = "bt";
	public static final String SKIP_BT      = "bt_skip";
	public static final String SKIP_MOBDATA = "mobdata_skip";
	public static final String SKIP_HOTSPOT = "hotspot_skip";
	public static final String SKIP_TETHER  = "tether_skip";
	// proximity
	public static final String DISABLE_PROXIMITY = "disable_proximity";
	public static final String DISABLE_DELAY     = "disable_delay";
	public static final String ENABLE_PROXIMITY  = "enable_proximity";
	public static final String ENABLE_DELAY      = "enable_delay";
	public static final String SCREEN_OFF        = "screen_off";
	public static final String SCREEN_ON         = "screen_on";
	public static final String ADMIN             = "admin";					// non persistent
	// speaker
	public static final String SPEAKER_CALL  = "speaker_call";
	public static final String SPEAKER_LOUD  = "loud_speaker";
	public static final String SPEAKER_AUTO  = "auto_speaker";
	public static final String SPEAKER_DELAY = "delay_speaker";
	// volume
	public static final String VOL_PHONE = "vol_phone";
	public static final String VOL_WIRED = "vol_wired";
	public static final String VOL_BT    = "vol_bt";
	public static final String VOL_SOLO  = "vol_solo";
	// call recorder
	public static final String REC        = "rec";
	public static final String REC_FMT    = "rec_fmt";
	public static final String REC_SRC    = "rec_src";
	public static final String REC_BROWSE = "rec_browse";			// non persistent
	// notification
	public static final String NOTIFY_ENABLE   = "notify_enable";
	public static final String NOTIFY_DISABLE  = "notify_disable";
	public static final String NOTIFY_ACTIVITY = "notify_activity";
	public static final String NOTIFY_VOLUME   = "notify_volume";

	// internals (hidden to user)
	public static final String FULL     = "full";
	public static final String AGREE    = "agree";
	public static final String VER      = "ver";
	public static final String BT_COUNT = "bt_count";
	// non persistent tool keys
	public static final String LOGO        = "logo";
	public static final String EULA        = "eula";
	public static final String MAIL        = "mail";
	public static final String PAYPAL      = "paypal";
	public static final String COMMENT     = "comment";
	public static final String CHANGELOG   = "changelog";
	public static final String UNINSTALL   = "uninstall";
	public static final String RESET_PREFS = "reset_prefs";
	// screen activities (non persistent)
	public static final String SCREEN_GENERAL   = "screen_general";
	public static final String SCREEN_DEVICES   = "screen_devices";
	public static final String SCREEN_PROXIMITY = "screen_proximity";
	public static final String SCREEN_SPEAKER   = "screen_speaker";
	public static final String SCREEN_VOLUME    = "screen_volume";
	public static final String SCREEN_RECORD    = "screen_record";
	public static final String SCREEN_NOTIFY    = "screen_notify";
	public static final String SCREEN_ABOUT     = "screen_about";

	//--- methods: only class P should call these methods!
	
	static final Map<String,Object> getDefaults()
	{
		final Map<String,Object> m = new HashMap<String,Object>();
		// all preferences default values
		m.put(ENABLED          , true);			// main
		m.put(SKIP_HEADSET     , true);
		m.put(FORCE_BT_AUDIO   , false);
		m.put(REVERSE_PROXIMITY, false);
		m.put(AUTO_MOBDATA     , false);		// devices
		m.put(AUTO_WIFI        , true);
		m.put(AUTO_BT          , true);
		m.put(AUTO_GPS         , false);
		m.put(SKIP_BT          , true);
		m.put(SKIP_MOBDATA     , false);
		m.put(SKIP_HOTSPOT     , true);
		m.put(SKIP_TETHER      , true);
		m.put(DISABLE_PROXIMITY, true);			// proximity
		m.put(DISABLE_DELAY    , "1000");
		m.put(ENABLE_DELAY     , "3000");
		m.put(ENABLE_PROXIMITY , true);
		m.put(SCREEN_OFF       , true);
		m.put(SCREEN_ON        , true);
		m.put(SPEAKER_CALL     , false);		// speaker
		m.put(SPEAKER_LOUD     , true);
		m.put(SPEAKER_AUTO     , true);
		m.put(SPEAKER_DELAY    , "0");
		m.put(VOL_PHONE        , "0");			// volume
		m.put(VOL_WIRED        , "0");
		m.put(VOL_BT           , "0");
		m.put(VOL_SOLO         , false);
		m.put(REC              , false);		// call recorder
		m.put(REC_SRC          , Rec.DEF_SRC+"");
		m.put(REC_FMT          , Rec.DEF_FMT+"");
		m.put(NOTIFY_ENABLE    , true);			// notify
		m.put(NOTIFY_DISABLE   , true);
		m.put(NOTIFY_ACTIVITY  , true);
		m.put(NOTIFY_VOLUME    , false);
		return m;
	}

	static final void upgrade(float oldVer)
	{
		// upgrade current preferences from an older existing version
		if(oldVer < 0.9) {
			P.renameBool(DISABLE_PROXIMITY, "proximty");
			P.renameBool( ENABLE_PROXIMITY, "restore_far");
		}
		if(oldVer < 1.2) {
			P.setDef(SKIP_BT);
			P.setDef(SKIP_HOTSPOT);
			P.setDef(SKIP_TETHER);
			P.setDefIfNew(NOTIFY_ACTIVITY);
			P.setDefIfNew(SCREEN_OFF);
			P.setDefIfNew(SCREEN_ON);
		}
		if(oldVer < 1.5) {
			P.setDef(REC);
			P.setDef(REC_SRC);
			P.setDef(REC_FMT);
		}
	}

	private K() { }

}
