package cri.sanity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;


public class MainActivity extends PrefActivity
{
	private boolean isDonateAsking = false;

	//---- Activity override

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.prefs);
    try {
    	setupListeners();
    	setupProximity();
    	setupVolumeLevels();
    	setupVersion();
      setupDonate();
  		if(A.is("agree")) checkDonate ();
  		else              checkLicense();
    }
    catch(Exception e) {
    	String msg = "Activity Exception: "+e.getMessage();
    	A.loge(msg);
    	A.loge(e);
    	if(A.DEBUG) A.notify(msg);
    }
  }

	@Override
	public void onResume()
	{
		super.onResume();
		A.notifyCanc();
	}
	
	@Override
	public void onDestroy()
	{
		A.notifyCanc();
		super.onDestroy();
	}
	
	//---- private api

	private void setupVersion()
	{
		final String ver = A.tr(R.string.app_ver);
		final String old = A.gets("ver");
		if(old.equals(ver)) return;
		// put here upgrade changes
		A.put("agree", false).putc("ver", ver);
	}

	private void setupListeners()
	{
  	// setup click listener for app logo
  	Click cl = new Click(){ boolean on(){ return A.gotoAuthorApps(); }};
  	for(int i=1; i<=Conf.LOGO_COUNT; ++i)
  		on("app_logo"+i, cl);
  	// setup click listener for eula
  	on("eula"   , new Click(){ boolean on(){ return A.gotoUrl(Conf.EULA_URL); }});
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
		Preference p = findPref("donate");
    if(!A.isFull() && !startDonateApp()) {
      on(p, new Click(){ boolean on() {
      	A.gotoAuthorApps();
  	    return true;
      }});
    }
    else {
    	p.setTitle(R.string.donated_title);
    	p.setSummary(R.string.donated_sum);
    	p.setEnabled(false);
    }
	}
	
	private void checkDonate()
	{
		if(isDonateAsking || A.isFull()) return;
		isDonateAsking = true;
		A.alert(
			A.tr(R.string.msg_donate),
			new A.DlgClick(){ void on(){ A.gotoAuthorApps(); isDonateAsking = false; }},
			new A.DlgClick(){ void on(){                     isDonateAsking = false; }}
		);
	}

	private void checkLicense()
	{
		A.alert(
		  A.tr(R.string.msg_eula_title),
			A.tr(R.string.app_fullname)+"\n\n"+A.tr(R.string.app_desc)+"\n\n"+A.tr(R.string.msg_eula),
			new A.DlgClick(){ void on(){ A.putc("agree",true); setChecked(A.ENABLED_KEY,true); }},
			new A.DlgClick(){ void on(){ finish(); }},
			A.ALERT_OKCANC, false
		);
	}

	private boolean startDonateApp()
	{
		boolean done = startService(new Intent(Conf.ACTION_DONATE)) != null;
		if(done) A.setFull();
		return done;
	}
	
}
