package cri.sanity;

import java.util.Currency;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.text.Html;


public class MainActivity extends PrefActivity
{
	private static final String VER   = "ver";
	private static final String AGREE = "agree";

	//---- Activity override

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.prefs);
    //getWindow().setBackgroundDrawableResource(R.drawable.bg);
    try {
      final String old = getOldVersion();
    	setupListeners();
    	setupProximity();
    	setupVolumeLevels();
      setupDonate();
  		if(!A.is(AGREE))
  			checkLicense();
  		else if(old.length() > 0) {
  			setupUpgrade(old);
  			alertChangeLog();
  		}
  		else
  			checkDonate();
    }
    catch(Exception e) {
    	if(!A.DEBUG) return;
    	String msg = "Activity Exception: "+e.getMessage();
    	A.loge(msg);
    	A.loge(e);
    	A.notify(msg);
    }
  }

	//---- private api

	private static String getOldVersion()
	{
		final String ver = A.tr(R.string.app_ver);
		final String old = A.gets(VER);
		final boolean ok = old.equals(ver);
		if(!ok) A.putc(VER, ver);
		return ok ? "" : old;
	}

	private void setupUpgrade(String oldVer)
	{
		// put here preferences changes for upgrading!
	}
	
	private void alertChangeLog() {
		A.alert(A.tr(R.string.changelog_title), A.tr(R.string.changelog_body));
	}

	private void setupListeners()
	{
		// setup click listeners for fake preferences
  	final Click cl = new Click(){ boolean on(){ return A.gotoMarketPub(); }};
  	for(int i=1; i<=Conf.LOGO_COUNT; ++i)
  		on("app_logo"+i, cl);
  	on("eula"     , new Click(){ boolean on(){ return A.gotoUrl(Conf.EULA_URL); }});
  	on("comment"  , new Click(){ boolean on(){ return A.gotoMarketDetails();    }});
  	on("changelog", new Click(){ boolean on(){ alertChangeLog(); return true;   }});
  	on("mail"     , new Click(){ boolean on(){ return mailToDeveloper();        }});
  	on("paypal"   , new Click(){ boolean on(){ return A.gotoUrl(Conf.DONATE_URL.replace(Conf.CURRENCY_VAR, Currency.getInstance(Locale.getDefault()).getCurrencyCode())); }});
	}

	private void setupProximity()
	{
  	// setup preferences when proximity sensor exists or not
  	if(Dev.sensorProxim() != null) {
  		// disable "loud_speaker" when both "auto_speaker" and "speaker_call" are unchecked
    	on("auto_speaker", new Click(){ boolean on(){
    		findPref("loud_speaker").setEnabled(is(pref) || is("speaker_call"));
    		return false;
    	}});
    	on("speaker_call", new Click(){ boolean on(){
    		findPref("loud_speaker").setEnabled(is(pref) || is("auto_speaker"));
    		return false;
    	}});
  	}
  	else {
  		// if no proximity sensor found: disable all proximity options
  		setEnabled("screen_proximity", false);
  		setEnabled("auto_speaker"    , false);
  		setChecked("speaker_call"    , false);
  		findPref("loud_speaker").setDependency("speaker_call");
  	}
	}
	
	private void setupVolumeLevels()
	{
  	// setup volume ranges for ListPreferences
  	final String lev = A.tr(R.string.level) + " ";
  	final int m = Dev.getVolumeMax(Dev.VOLUME_CALL);
  	final CharSequence[] av = new CharSequence[m+1];
  	final CharSequence[] ae = new CharSequence[m+1];
  	av[0] = "0";
  	av[1] = "1";
  	av[m] = Integer.toString(m);
  	ae[0] = A.tr(R.string.nochange);
  	ae[1] = lev + av[1] + " ("+A.tr(R.string.min)+")";
  	ae[m] = lev + av[m] + " ("+A.tr(R.string.max)+")";
  	for(int i=2; i<m; ++i) {
  		av[i] = Integer.toString(i);
  		ae[i] = lev + av[i];
  	}
  	for(String k : new String[]{ "vol_phone", "vol_wired", "vol_bt" }) {
  		ListPreference lp = (ListPreference)findPref(k);
  		lp.setEntries    (ae);
  		lp.setEntryValues(av);
  	}
	}
	
	private void setupDonate()
	{
		final Preference p = findPref("donate");
    if(!A.isFull() && !startDonateApp())
   		on(p, new Click(){ boolean on(){ return A.gotoMarketDetails(Conf.DONATE_PKG); }});
    else {
    	p.setTitle(R.string.donated_title);
    	p.setSummary(R.string.donated_sum);
    	p.setEnabled(false);
    }
	}
	
	private void checkDonate()
	{
		if(A.isFull()) return;
		A.alert(
			A.tr(R.string.msg_donate),
			new A.DlgClick(){ void on(){ A.gotoMarketDetails(Conf.DONATE_PKG); }},
			null
		);
	}

	private void checkLicense()
	{
		A.alert(
		  A.tr(R.string.msg_eula_title),
			A.tr(R.string.app_fullname)+"\n\n"+A.tr(R.string.app_desc)+"\n\n"+A.tr(R.string.msg_eula),
			new A.DlgClick(){ void on(){ A.putc(AGREE,true); setChecked(A.ENABLED_KEY,true); }},
			new A.DlgClick(){ void on(){ finish(); }},
			A.ALERT_OKCANC, false
		);
	}

	private boolean startDonateApp()
	{
		final boolean done = startService(new Intent(Conf.ACTION_DONATE)) != null;
		if(done) A.setFull();
		return done;
	}
	
	private boolean mailToDeveloper()
	{
		final Intent i = new Intent(android.content.Intent.ACTION_SEND);
		i.setType("text/html");
		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{ Conf.AUTHOR_EMAIL });
		i.putExtra(Intent.EXTRA_SUBJECT, A.tr(R.string.app_fullname));
		i.putExtra(Intent.EXTRA_TEXT   , Html.fromHtml(A.tr(R.string.msg_email_body)+"<br />"));
		startActivity(Intent.createChooser(i, A.tr(R.string.msg_email_choose)));
		return true;
	}
	
}
