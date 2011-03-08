package cri.sanity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;


public class MainActivity extends ScreenActivity
{
	@Override
  public void onCreate(Bundle savedInstanceState) {
		skipAllKeys = true;
		screener(MainActivity.class, R.xml.prefs);
    super.onCreate(savedInstanceState);
    screenerAll();
  	setupProximity();
    setupDonate();
		nag = false;
		if(!A.is(K.AGREE))   firstRun();
		else if(P.upgrade()) alertChangeLog();
		else if(!A.isFull()) nag = true;
  }

	@Override
	public void onResume() {
		updateOptions();
		super.onResume();
	}

	@Override
	public boolean isMainActivity() { return true; }

	//---- private api

	private void setupProximity() {
  	if(Dev.sensorProxim() == null)
  		setEnabled("screen_proximity", false);
	}

	private void setupDonate() {
		Preference p = pref("donate");
    if(!A.isFull() && !startDonateApp()) {
   		on(p, new Click(){ public boolean on(){ return A.gotoMarketDetails(Conf.DONATE_PKG); }});
			p = pref("screen_record");
			p.setSummary(p.getSummary()+" "+A.s(R.string.rec_cat_sum_free));
    } else {
	  	p.setEnabled(false);
	  	p.setSelectable(false);
	  	if(Conf.FULL) { p.setTitle(R.string.full_title   ); p.setSummary(R.string.full_sum   ); }
	  	else          { p.setTitle(R.string.donated_title); p.setSummary(R.string.donated_sum); }
    }
	}

	private void firstRun() {
		A.alert(
		  A.s(R.string.msg_eula_title),
			A.fullName()+"\n\n"+A.s(R.string.app_desc)+'\n'+A.s(R.string.app_copy)+"\n\n"+A.rawstr(R.raw.license),
			new A.Click(){ public void on(){
				A.put(K.AGREE,true);
				P.setDefaults();
				updateOptions();
				dismiss();
				if(P.backupExists()) askRestore();
				else                 askAdmin();
			}},
			new A.Click(){ public void on(){ finish(); }},
			A.ALERT_OKCANC,
			false
		);
	}

	private void askRestore() {
		A.alert(
			A.s(R.string.ask_restore),
			new A.Click(){ public void on(){
				final boolean ok = P.restore();
				A.toast(ok? R.string.msg_restore_success : R.string.msg_restore_failed);
				if(ok) updateOptions();
				dismiss();
				askAdmin();
			}},
			new A.Click(){ public void on(){
				dismiss();
				askAdmin();
			}},
			A.ALERT_OKCANC
		);
	}

	private void askAdmin() {
		if(A.SDK<8 || Admin.isActive()) return;
		A.alert(
			A.rawstr(R.raw.admin_ask_enable),
			new A.Click(){ public void on(){ Admin.request(MainActivity.this); }},
			null,
			A.ALERT_OKCANC
		);
	}
	
	private void updateOptions() {
		final boolean enabled = A.isEnabled();
		setEnabled("screen_devices"  , enabled);
		setEnabled("screen_proximity", enabled);
		setEnabled("screen_speaker"  , enabled);
		setEnabled("screen_volume"   , enabled);
		setEnabled("screen_notify"   , enabled);
		setEnabled("screen_record"   , enabled);
		setEnabled("screen_tts"      , enabled);
		setEnabled("screen_block"    , enabled);
	}

	private boolean startDonateApp() {
		final boolean done = startService(new Intent(Conf.ACTION_DONATE)) != null;
		if(done) A.setFull();
		return done;
	}

}
