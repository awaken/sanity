package cri.sanity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;


public class MainActivity extends ActivityScreen
{
	private static final String VER   = "ver";
	private static final String AGREE = "agree";

	//---- Activity override

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
		screener(getClass(), R.xml.prefs);
    super.onCreate(savedInstanceState);
    final String old = getOldVersion();
  	setupScreens();
  	setupProximity();
    setupDonate();
		if(!A.is(AGREE))
			askLicense();
		else if(old.length() > 0) {
			setupUpgrade(old);
			alertChangeLog();
		}
		else if(!A.isFull())
			askDonate();
  }

	//---- private api

	private void setupScreens()
	{
  	screener("screen_devices"  , ScreenDevices.class  , R.xml.prefs_devices  , R.id.menu_devices);
  	screener("screen_devices"  , ScreenDevices.class  , R.xml.prefs_devices  , R.id.menu_devices);
  	screener("screen_proximity", ScreenProximity.class, R.xml.prefs_proximity, R.id.menu_proximity);
  	screener("screen_speaker"  , ScreenSpeaker.class  , R.xml.prefs_speaker  , R.id.menu_speaker);
  	screener("screen_volume"   , ScreenVolume.class   , R.xml.prefs_volume   , R.id.menu_vol);
  	screener("screen_notify"   , ScreenNotify.class   , R.xml.prefs_notify   , R.id.menu_notify);
  	screener("screen_about"    , ScreenAbout.class    , R.xml.prefs_about    , R.id.menu_about);
	}

	private void setupProximity()
	{
  	if(Dev.sensorProxim() == null)
  		setEnabled("screen_proximity", false);
	}
	
	private static String getOldVersion()
	{
		final String ver = A.ver();
		final String old = A.gets(VER);
		final boolean ok = old.equals(ver);
		if(!ok) A.putc(VER, ver);
		return ok ? "" : old;
	}

	private void setDefaultConf()
	{
		setChecked(A.ENABLED_KEY , true);		// main prefs
		setChecked("skip_headset", true);
		A.put("mobdata"          , false)		// devices prefs
		 .put("wifi"             , true)
		 .put("gps"              , false)
		 .put("bt"               , true)
		 .put("bt_skip"          , true)
		 .put("proximity"        , true)		// proximity prefs
		 .put("disable_delay"    , "1000")
		 .put("enable_delay"     , "3000")
		 .put("restore_far"      , true)
		 .put("screen_off"       , true)
		 .put("speaker_call"     , false)		// speaker prefs
		 .put("loud_speaker"     , true)
		 .put("auto_speaker"     , true)
		 .put("delay_speaker"    , "0")
		 .put("vol_phone"        , "0")				// volume prefs
		 .put("vol_wired"        , "0")
		 .put("vol_bt"           , "0")
		 .put("notify_enable"    , true)		// notify prefs
		 .put("notify_disable"   , true)
		 .put("notify_activity"  , true)
		 .commit();
	}

	private void setupUpgrade(String oldVer)
	{
		// put here preferences changes for upgrading!
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

	private void askDonate()
	{
		A.alert(
			A.tr(R.string.msg_donate),
			new A.DlgClick(){ void on(){ A.gotoMarketDetails(Conf.DONATE_PKG); }},
			null
		);
	}

	private void askLicense()
	{
		A.alert(
		  A.tr(R.string.msg_eula_title),
			getAppFullName()+"\n\n"+A.tr(R.string.app_desc)+"\n\n"+A.tr(R.string.msg_eula),
			new A.DlgClick(){ void on(){ A.putc(AGREE,true); setDefaultConf(); }},
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

}
