package cri.sanity;


public final class Conf
{
	public static final boolean FULL = false;
	public static final boolean BETA = false;

	public static final String AUTHOR          = "Cristiano Tagliamonte";
	public static final String AUTHOR_EMAIL    = "cristiano@tagliamonte.net";
	public static final String DONATE_PKG      = "cri.sanitydonate";
	public static final String ACTION_DONATE   = "cri.sanitydonate.DonateService";
	public static final String SHORTCUT_PREFIX = "[S] ";

	public static final int NAG_TIMEOUT             = 4*3600*1000;		// 4 hours
	public static final int SERVICE_TIMEOUT         = 2000;
	public static final int DEVS_MIN_RETRY          = 2000;
	public static final int FORCE_AUTOSPEAKER_DELAY = 3000;
	public static final int CALL_SCREEN_TIMEOUT     = 300;
	public static final int VIBRATE_TIME            = 500;
	public static final int TASK_WAIT_SHUTDOWN      = 10000;

	public static final char   REC_SEP           = '_';
	public static final String REC_PREFIX        = "rec_";						// last char must be REC_SEP
	public static final String REC_DATE_PATTERN  = "yyyy-MM-dd";
	public static final String REC_TIME_PATTERN  = "kk.mm.ss";
	public static final int    REC_OFFHOOK_DELAY = 1000;
	public static final int    REC_FREE_LIMIT    = 120*1000;

	public static final String BACKUP_FN       = "sanity_backup.txt";
	public static final String PRF_EXT         = ".prf";
	public static final String PRF_DATE        = "dd/MM/yyyy, kk:mm";
}
