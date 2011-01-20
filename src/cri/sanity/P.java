package cri.sanity;


// tool class of all preference keys
public final class P
{
	// general
	public static final String ENABLED           = "enabled";
	public static final String SKIP_HEADSET      = "skip_headset";
	public static final String FORCE_BT_AUDIO    = "force_bt_audio";
	public static final String REVERSE_PROXIMITY = "reverse_proximity";
	// devices
	public static final String SKIP_MOBDATA = "mobdata_skip";
	public static final String AUTO_MOBDATA = "mobdata";
	public static final String AUTO_WIFI    = "wifi";
	public static final String AUTO_GPS     = "gps";
	public static final String AUTO_BT      = "bt";
	public static final String SKIP_BT      = "bt_skip";
	// proximity
	public static final String DISABLE_PROXIMITY = "disable_proximity";
	public static final String DISABLE_DELAY     = "disable_delay";
	public static final String ENABLE_PROXIMITY  = "enable_proximity";
	public static final String ENABLE_DELAY      = "enable_delay";
	public static final String SCREEN_OFF        = "screen_off";
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
	// screen activities
	public static final String SCREEN_GENERAL   = "screen_general";
	public static final String SCREEN_DEVICES   = "screen_devices";
	public static final String SCREEN_PROXIMITY = "screen_proximity";
	public static final String SCREEN_SPEAKER   = "screen_speaker";
	public static final String SCREEN_VOLUME    = "screen_volume";
	public static final String SCREEN_NOTIFY    = "screen_notify";
	public static final String SCREEN_ABOUT     = "screen_about";
}
