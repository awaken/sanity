package cri.sanity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;


public class MainActivity extends ScreenActivity
{
	@Override
  public void onCreate(Bundle savedInstanceState)
  {
		skipAllKeys = true;
		screener(MainActivity.class, R.xml.prefs);
    super.onCreate(savedInstanceState);
    screenerAll();
  	setupProximity();
    setupDonate();
		nag = false;
		if(!A.is(K.AGREE))   askLicense();
		else if(P.upgrade()) alertChangeLog();
		else if(!A.isFull()) nag = true;
  }

	@Override
	public void onResume()
	{
		updateOptions();
		super.onResume();
	}

	@Override
	public boolean isMainActivity() { return true; }

	//---- private api

	private void setupProximity()
	{
  	if(Dev.sensorProxim() == null)
  		setEnabled(K.SCREEN_PROXIMITY, false);
	}

	private void setupDonate()
	{
		Preference p = pref("donate");
    if(!A.isFull() && !startDonateApp()) {
   		on(p, new Click(){ public boolean on(){ return A.gotoMarketDetails(Conf.DONATE_PKG); }});
			p = pref(K.SCREEN_RECORD);
			p.setSummary(p.getSummary()+" "+A.s(R.string.rec_cat_sum_free));
    }
    else {
	  	p.setEnabled(false);
	  	p.setSelectable(false);
	  	if(Conf.FULL) { p.setTitle(R.string.full_title   ); p.setSummary(R.string.full_sum   ); }
	  	else          { p.setTitle(R.string.donated_title); p.setSummary(R.string.donated_sum); }
    }
	}

	private void askLicense()
	{
		A.alert(
		  A.s(R.string.msg_eula_title),
			A.fullName()+"\n\n"+A.s(R.string.app_desc)+'\n'+A.s(R.string.app_copy)+"\n\n"+A.rawstr(R.raw.license),
			new A.Click(){ public void on(){
				A.put(K.AGREE,true);
				P.setDefaults();
				updateOptions();
				if(P.backupExists()) {
					A.alert(
						A.s(R.string.ask_restore),
						new A.Click(){ public void on(){
							final boolean ok = P.restore();
							A.toast(ok? R.string.msg_restore_success : R.string.msg_restore_failed);
							if(ok) updateOptions();
						}},
						null,
						A.ALERT_OKCANC
					);
				}
			}},
			new A.Click(){ public void on(){ finish(); }},
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
		setEnabled(K.SCREEN_RECORD   , enabled);
	}

	private boolean startDonateApp()
	{
		final boolean done = startService(new Intent(Conf.ACTION_DONATE)) != null;
		if(done) A.setFull();
		return done;
	}

}
