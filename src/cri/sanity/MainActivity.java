package cri.sanity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;


public class MainActivity extends ActivityScreen
{
	//---- Activity override

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
		screener(getClass(), R.xml.prefs);
    super.onCreate(savedInstanceState);
    A.activity = this;
    final String oldVer = getOldVersion();
  	setupScreens();
  	setupProximity();
    setupDonate();
		if(!A.is(P.AGREE))
			askLicense();
		else if(oldVer.length() > 0) {
			if(!setupUpgrade(Float.parseFloat(oldVer)))
				alertChangeLog();
		}
		else if(!A.isFull())
			askDonate();
  }
	
	@Override
	public void onStart()
	{
		final boolean enabled = A.isEnabled();
		setEnabled(P.SCREEN_DEVICES  , enabled);
		setEnabled(P.SCREEN_PROXIMITY, enabled);
		setEnabled(P.SCREEN_SPEAKER  , enabled);
		setEnabled(P.SCREEN_VOLUME   , enabled);
		setEnabled(P.SCREEN_NOTIFY   , enabled);
		super.onStart();
	}

	@Override
	public void onDestroy()
	{
		A.activity = null;
		super.onDestroy();
	}

	//---- private api

	private void setupScreens()
	{
  	screener(P.SCREEN_GENERAL  , ScreenGeneral.class  , R.xml.prefs_general  , R.id.menu_general);
  	screener(P.SCREEN_DEVICES  , ScreenDevices.class  , R.xml.prefs_devices  , R.id.menu_devices);
  	screener(P.SCREEN_PROXIMITY, ScreenProximity.class, R.xml.prefs_proximity, R.id.menu_proximity);
  	screener(P.SCREEN_SPEAKER  , ScreenSpeaker.class  , R.xml.prefs_speaker  , R.id.menu_speaker);
  	screener(P.SCREEN_VOLUME   , ScreenVolume.class   , R.xml.prefs_volume   , R.id.menu_vol);
  	screener(P.SCREEN_NOTIFY   , ScreenNotify.class   , R.xml.prefs_notify   , R.id.menu_notify);
  	screener(P.SCREEN_ABOUT    , ScreenAbout.class    , R.xml.prefs_about    , R.id.menu_about);
	}

	private void setupProximity()
	{
  	if(Dev.sensorProxim() == null)
  		setEnabled(P.SCREEN_PROXIMITY, false);
	}

	private static String getOldVersion()
	{
		final String ver = A.ver();
		final String old = A.gets(P.VER);
		final boolean ok = old.equals(ver);
		if(!ok) A.putc(P.VER, ver);
		return ok ? "" : old;
	}

	public static final void setDefaultPrefs()
	{
		A.put(P.ENABLED          , true)		// main prefs
		 .put(P.SKIP_HEADSET     , true)
		 .put(P.FORCE_BT_AUDIO   , false)
		 .put(P.REVERSE_PROXIMITY, false)
		 .put(P.AUTO_MOBDATA     , false)		// devices prefs
		 .put(P.SKIP_MOBDATA     , false)
		 .put(P.AUTO_WIFI        , true)
		 .put(P.AUTO_GPS         , false)
		 .put(P.AUTO_BT          , true)
		 .put(P.SKIP_BT          , true)
		 .put(P.DISABLE_PROXIMITY, true)		// proximity prefs
		 .put(P.DISABLE_DELAY    , "1000")
		 .put(P.ENABLE_DELAY     , "3000")
		 .put(P.ENABLE_PROXIMITY , true)
		 .put(P.SCREEN_OFF       , true)
		 .put(P.SPEAKER_CALL     , false)		// speaker prefs
		 .put(P.SPEAKER_LOUD     , true)
		 .put(P.SPEAKER_AUTO     , true)
		 .put(P.SPEAKER_DELAY    , "0")
		 .put(P.VOL_PHONE        , "0")			// volume prefs
		 .put(P.VOL_WIRED        , "0")
		 .put(P.VOL_BT           , "0")
		 .put(P.NOTIFY_ENABLE    , true)		// notify prefs
		 .put(P.NOTIFY_DISABLE   , true)
		 .put(P.NOTIFY_ACTIVITY  , true);
		A.commit();
	}
	
	private boolean setupUpgrade(float oldVer)
	{
		// put here preferences changes for upgrading (from older version)
		// return true only if this method shows a dialog (to avoid showing another dialog after this one)
		boolean commit = false;
		if(oldVer < 0.9) {
			if(!A.has(P.SCREEN_OFF)) A.put(P.SCREEN_OFF, true);
			A.put(P.SKIP_BT, true)
			 .put(P.DISABLE_PROXIMITY, A.is("proximity"))
			 .put(P.ENABLE_PROXIMITY , A.is("restore_far"))
			 .del("proximity")
			 .del("restore_far");
			commit = true;
		}
		if(commit) A.commit();
		return false;
	}
	
	private void setupDonate()
	{
		final Preference p = findPref("donate");
    if(!A.isFull() && !startDonateApp())
   		on(p, new Click(){ boolean on(){ return A.gotoMarketDetails(Conf.DONATE_PKG); }});
    else {
    	p.setEnabled(false);
    	if(A.FULL) {
	    	p.setTitle(R.string.full_title);
	    	p.setSummary(R.string.full_sum);
    	} else {
	    	p.setTitle(R.string.donated_title);
	    	p.setSummary(R.string.donated_sum);
    	}
    }
	}

	private void askDonate()
	{
		A.alert(
			A.tr(R.string.msg_donate),
			new A.Click(){ void on(){ A.gotoMarketDetails(Conf.DONATE_PKG); }},
			null
		);
	}

	private void askLicense()
	{
		A.alert(
		  A.tr(R.string.msg_eula_title),
			getAppFullName()+"\n\n"+A.tr(R.string.app_desc)+"\n\n"+A.tr(R.string.msg_eula),
			new A.Click(){ void on(){ A.putc(P.AGREE,true); setDefaultPrefs(); }},
			new A.Click(){ void on(){ finish(); }},
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
