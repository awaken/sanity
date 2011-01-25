package cri.sanity;

import java.util.HashMap;
import java.util.Map;


// tool class of application preferences
public final class P
{
	// general
	public static final String ENABLED           = "enabled";
	public static final String SKIP_HEADSET      = "skip_headset";
	public static final String FORCE_BT_AUDIO    = "force_bt_audio";
	public static final String REVERSE_PROXIMITY = "reverse_proximity";
	public static final String ADMIN             = "admin";
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
	// notification
	public static final String NOTIFY_ENABLE   = "notify_enable";
	public static final String NOTIFY_DISABLE  = "notify_disable";
	public static final String NOTIFY_ACTIVITY = "notify_activity";
	public static final String NOTIFY_VOLUME   = "notify_volume";

	// internals (not shown to user)
	public static final String FULL     = "full";
	public static final String AGREE    = "agree";
	public static final String VER      = "ver";
	public static final String BT_COUNT = "bt_count";
	// non persistent keys (tool keys)
	public static final String LOGO        = "logo";
	public static final String EULA        = "eula";
	public static final String MAIL        = "mail";
	public static final String PAYPAL      = "paypal";
	public static final String COMMENT     = "comment";
	public static final String CHANGELOG   = "changelog";
	public static final String RESET_PREFS = "reset_prefs";
	// screen activities (non persistent)
	public static final String SCREEN_GENERAL   = "screen_general";
	public static final String SCREEN_DEVICES   = "screen_devices";
	public static final String SCREEN_PROXIMITY = "screen_proximity";
	public static final String SCREEN_SPEAKER   = "screen_speaker";
	public static final String SCREEN_VOLUME    = "screen_volume";
	public static final String SCREEN_NOTIFY    = "screen_notify";
	public static final String SCREEN_ABOUT     = "screen_about";

	//---- public api

	public static final Map<String,Object> getDefaults() { return defs; }
	public static final void               setDefaults() { defsBuild(); A.putcAll(defs); }

	public static final boolean upgrade()
	{
		final String ver = A.gets(VER);
		if(A.ver().equals(ver)) return false;
		upgrade(ver);
		return true;
	}

	public static final void upgrade(String oldVer)
	{
		float ver;
		try { ver = Float.parseFloat(oldVer); }
		catch(Exception e) { ver = 0; }
		upgrade(ver);
	}

	public static final void upgrade(float oldVer)
	{
		defsBuild();
		if(oldVer < 0.1)
			setDefaults();	// 1st time installation: set default preferences
		else {
			// upgrade current preferences from an older version
			if(oldVer < 0.9) {
				renameBool(DISABLE_PROXIMITY, "proximty");
				renameBool( ENABLE_PROXIMITY, "restore_far");
			}
			if(oldVer < 1.2) {
				setDef(SKIP_BT);
				setNewDef(NOTIFY_ACTIVITY);
				setNewDef(SCREEN_OFF);
				setNewDef(SCREEN_ON);
				setDef(SKIP_HOTSPOT);
				setDef(SKIP_TETHER);
			}
		}
		A.putc(VER, A.ver());
	}

	//---- private api

	private P() { }

	private static void setDef(String key)
	{
		final Object val = defs.get(key);
		if(val != null) A.put(key, val);
	}

	private static void setNewDef(String key) { if(!A.has(key)) setDef(key); }
	
	private static void renameBool(String dst, String old) { A.put(dst, A.is(old)).del(old); }

	private static void defsBuild()
	{
		if(defs != null) return;
		defs = new HashMap<String,Object>();
		// these are all preferences default values
		defs.put(ENABLED          , true);		// main
		defs.put(SKIP_HEADSET     , true);
		defs.put(FORCE_BT_AUDIO   , false);
		defs.put(REVERSE_PROXIMITY, false);
		defs.put(ADMIN            , false);
		defs.put(AUTO_MOBDATA     , false);		// devices
		defs.put(AUTO_WIFI        , true);
		defs.put(AUTO_BT          , true);
		defs.put(AUTO_GPS         , false);
		defs.put(SKIP_BT          , true);
		defs.put(SKIP_MOBDATA     , false);
		defs.put(SKIP_HOTSPOT     , true);
		defs.put(SKIP_TETHER      , true);
		defs.put(DISABLE_PROXIMITY, true);		// proximity
		defs.put(DISABLE_DELAY    , "1000");
		defs.put(ENABLE_DELAY     , "3000");
		defs.put(ENABLE_PROXIMITY , true);
		defs.put(SCREEN_OFF       , true);
		defs.put(SCREEN_ON        , true);
		defs.put(SPEAKER_CALL     , false);		// speaker
		defs.put(SPEAKER_LOUD     , true);
		defs.put(SPEAKER_AUTO     , true);
		defs.put(SPEAKER_DELAY    , "0");
		defs.put(VOL_PHONE        , "0");			// volume
		defs.put(VOL_WIRED        , "0");
		defs.put(VOL_BT           , "0");
		defs.put(VOL_SOLO         , false);
		defs.put(NOTIFY_ENABLE    , true);		// notify
		defs.put(NOTIFY_DISABLE   , true);
		defs.put(NOTIFY_ACTIVITY  , true);
		defs.put(NOTIFY_VOLUME    , false);
	}

	private static Map<String,Object> defs;

}
