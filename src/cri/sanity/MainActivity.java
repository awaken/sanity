package cri.sanity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;


public class MainActivity extends ActivityScreen
{
	@Override
  public void onCreate(Bundle savedInstanceState)
  {
		screener(getClass(), R.xml.prefs);
    super.onCreate(savedInstanceState);
    A.activity = this;
  	setupScreens();
  	setupProximity();
    setupDonate();
		if(!A.is(K.AGREE))   askLicense();
		else if(P.upgrade()) alertChangeLog();
		else if(!A.isFull()) {
			final Preference p = findPref(K.SCREEN_RECORD);
			p.setSummary(p.getSummary()+" "+A.tr(R.string.rec_sum_free));
			askDonate();
		}
  }

	@Override
	public void onStart()
	{
		updateOptions();
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
  	screener(K.SCREEN_GENERAL  , ScreenGeneral.class  , R.xml.prefs_general  , R.id.menu_general);
  	screener(K.SCREEN_DEVICES  , ScreenDevices.class  , R.xml.prefs_devices  , R.id.menu_devices);
  	screener(K.SCREEN_PROXIMITY, ScreenProximity.class, R.xml.prefs_proximity, R.id.menu_proximity);
  	screener(K.SCREEN_SPEAKER  , ScreenSpeaker.class  , R.xml.prefs_speaker  , R.id.menu_speaker);
  	screener(K.SCREEN_VOLUME   , ScreenVolume.class   , R.xml.prefs_volume   , R.id.menu_vol);
  	screener(K.SCREEN_RECORD   , ScreenRecord.class   , R.xml.prefs_record   , R.id.menu_rec);
  	screener(K.SCREEN_NOTIFY   , ScreenNotify.class   , R.xml.prefs_notify   , R.id.menu_notify);
  	screener(K.SCREEN_ABOUT    , ScreenAbout.class    , R.xml.prefs_about    , R.id.menu_about);
	}

	private void setupProximity()
	{
  	if(Dev.sensorProxim() == null)
  		setEnabled(K.SCREEN_PROXIMITY, false);
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
			new A.Click(){ void on(){ A.put(K.AGREE,true); P.setDefaults(); updateOptions(); }},
			new A.Click(){ void on(){ finish(); }},
			A.ALERT_OKCANC,
			false
		);
	}
	
	private void updateOptions()
	{
		final boolean enabled = A.isEnabled();
		setEnabled(K.SCREEN_DEVICES  , enabled);
		setEnabled(K.SCREEN_PROXIMITY, enabled);
		setEnabled(K.SCREEN_SPEAKER  , enabled);
		setEnabled(K.SCREEN_VOLUME   , enabled);
		setEnabled(K.SCREEN_NOTIFY   , enabled);
	}

	private boolean startDonateApp()
	{
		final boolean done = startService(new Intent(Conf.ACTION_DONATE)) != null;
		if(done) A.setFull();
		return done;
	}

}
