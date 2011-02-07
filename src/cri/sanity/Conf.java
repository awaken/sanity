package cri.sanity;


public final class Conf
{
	public static final boolean DEBUG = false;
	public static final boolean FULL  = false;

	public static final String AUTHOR        = "Cristiano Tagliamonte";
	public static final String AUTHOR_EMAIL  = "cristiano@tagliamonte.net";

	public static final int    NAG_TIMEOUT   = 4*3600*1000;
	public static final String DONATE_PKG    = "cri.sanitydonate";
	public static final String ACTION_DONATE = "cri.sanitydonate.DonateService";

	public static final String CURRENCY_VAR  = "$CURRENCY";
	public static final String DONATE_URL    = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=cristiano%40tagliamonte%2enet&item_name=Cristiano%20Tagliamonte&currency_code="+CURRENCY_VAR+"&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted";
	public static final String EULA_URL      = "http://www.gnu.org/licenses";

	public static final int  FORCE_AUTOSPEAKER_DELAY = 3000;
	public static final int  CALL_SCREEN_TIMEOUT     = 300;
	public static final long VIBRATE_TIME            = 500;

	public static final char   REC_SEP           = '_';
	public static final String REC_PREFIX        = "rec_";
	public static final String REC_DATE_PATTERN  = "yyyy-MM-dd";
	public static final String REC_TIME_PATTERN  = "kk.mm.ss";
	public static final int    REC_OFFHOOK_DELAY = 1000;
	public static final int    REC_MIN_SRV_RETRY = 2000;
	public static final int    REC_FREE_LIMIT    = 120*1000;
}
