package cri.sanity;


public final class Conf
{
	public static final boolean FULL = false;

	public static final int NAG_TIMEOUT             = 4*3600*1000;				// 4 hours
	public static final int SERVICE_TIMEOUT         = 2000;
	public static final int DEVS_MIN_RETRY          = 2000;
	public static final int FORCE_AUTOSPEAKER_DELAY = 3000;
	public static final int TRACKER_SWITCH_DELAY    = 200;
	public static final int TTS_UNMUTE_DELAY        = 200;
	public static final int BLOCK_LOCK_DELAY        = 2000;
	public static final int TASK_WAIT_SHUTDOWN      = 10000;

	public static final float PROXIM_MIN = 0.5f;
	public static final float PROXIM_MAX = 5.0f;

	public static final char   REC_SEP           = '_';
	public static final String REC_PREFIX        = "rec_";								// last char must be REC_SEP
	public static final String REC_DATE_PATTERN  = "yyyy-MM-dd";
	public static final String REC_TIME_PATTERN  = "kk.mm.ss";
	public static final int    REC_OFFHOOK_DELAY = 3000;
	public static final int    REC_FREE_LIMIT    = 120*1000;

	public static final String PRF_EXT      = ".prf";
	public static final String BACKUP_FN    = "sanity_backup.txt";
	public static final String BLOCK_FN     = "block_history.txt";
	public static final String SMS_FN       = "sms_history.txt";
	public static final String DATE_PATTERN = "dd/MM/yyyy, kk:mm";

	public static final char   FILTER_SEP   = '§';
	public static final char   BLOCK_SEP    = '|';
	public static final char   SMS_SEP      = '§';
	public static final String SMS_UNSEP    = "{[(S)]}";
	public static final String SMS_UNSLASH  = "{[(\\)]}";
}
