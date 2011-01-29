package cri.sanity;


public final class Conf
{
	public static final boolean DEBUG = false;
	public static final boolean FULL  = true;

	public static final String AUTHOR       = "Cristiano Tagliamonte";
	public static final String AUTHOR_EMAIL = "cristiano@tagliamonte.net";

	public static final String   DONATE_PKG     = "cri.sanitydonate";
	public static final String   ACTION_DONATE  = DONATE_PKG+".DonateService";
	public static final Class<?> ACTIVITY_CLASS = MainActivity.class;

	public static final String CURRENCY_VAR = "$CURRENCY";
	public static final String DONATE_URL   = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=cristiano%40tagliamonte%2enet&item_name=Cristiano%20Tagliamonte&currency_code="+CURRENCY_VAR+"&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted";
	public static final String EULA_URL     = "http://www.gnu.org/licenses";

	public static final int FORCE_AUTOSPEAKER_DELAY  = 3000;
	public static final int CALL_SCREEN_TIMEOUT      = 300;

	public static final String REC_PREFIX  = "rec_";
	public static final String REC_PATTERN = "yyyy-MM-dd_kk.mm.ss";
	public static final int    REC_FREE_TIMEOUT = 120000;
}
