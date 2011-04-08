package cri.sanity;

import cri.sanity.util.*;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;


public class MainActivity extends ScreenActivity
{
	private static final String KEY_UNINSTALL = "uninstall";

	private Handler handler;

	@Override
  public void onCreate(Bundle savedInstanceState) {
    handler     = new Handler();
    secure      = false;
		nagDefault  = false;
		skipAllKeys = true;
		screener(MainActivity.class, R.xml.prefs);
    super.onCreate(savedInstanceState);
    screenerAll();
    if(nagDefault = setupFull()) startup();
    if(!Dev.isBtOn()) A.putc(K.BT_COUNT, 0);	// recheck sometimes to avoid false counter
  	if(A.SDK < 8) setEnabled(KEY_UNINSTALL, false);
  	else on(KEY_UNINSTALL, new Click(){ public boolean on(){ Alert.msg(A.rawstr(R.raw.uninstall)); return true; }});
  }

	@Override
	public void onResume() {
		updateOptions();
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
		ungrant();
		super.onDestroy();
	}
	
	@Override
	public void onActivityResult(int code, int res, Intent i) {
		if(code != License.CODE) return;
		try {
			final boolean full = License.result(res, handler, new Runnable(){ public void run(){ startup(); }});
			handler.post(new Runnable(){ public void run(){ enableFull(full); }});
		} catch(IllegalStateException e) {}
	}

	@Override
	public boolean isMainActivity() { return true; }

	//---- private api

	private boolean setupFull() {
		if(License.isChecked()) {
			enableFull(true);
			return true;
		}
		if(!License.check()) {
			enableFull(false);
			return true;
		}
		return false;
	}
	
	private void enableFull(boolean enable) {
		Preference p = pref("donate");
    if(enable) {
	  	p.setEnabled(false);
	  	p.setSelectable(false);
	  	if(Conf.FULL) { p.setTitle(R.string.full_title   ); p.setSummary(R.string.full_sum   ); }
	  	else          { p.setTitle(R.string.donated_title); p.setSummary(R.string.donated_sum); }
    } else {
   		on(p, new Click(){ public boolean on(){ return Goto.marketDetails(License.FULL_PKG); }});
			p = pref("screen_record");
			p.setSummary(p.getSummary()+" "+A.s(R.string.rec_cat_sum_free));
    }
	}

	private void startup() {
		if(!A.is(K.AGREE))
			firstRun();
		else if(P.upgrade())
			alertChangeLog();
		else if(License.isCompleted() && !A.isFull())
			nag = nagDefault = true;
	}

	private void firstRun() {
		Alert.msg(
		  A.s(R.string.msg_eula_title),
			fullName()+"\n\n"+appDesc()+"\n\n"+A.rawstr(R.raw.license),
			new Alert.Click(){ public void on(){
				A.put(K.AGREE, true);
				P.setDefaults();
				updateOptions();
				dismiss();
				if(P.backupExists()) askRestore();
				else                 askAdmin();
			}},
			new Alert.Click(){ public void on(){ finish(); }},
			Alert.OKCANC,
			false
		);
	}

	private void askRestore() {
		Alert.msg(
			A.s(R.string.ask_restore),
			new Alert.Click(){ public void on(){
				final boolean ok = P.restore();
				A.toast(ok? R.string.msg_restore_success : R.string.msg_restore_failed);
				if(ok) updateOptions();
				dismiss();
				askAdmin();
			}},
			new Alert.Click(){ public void on(){
				dismiss();
				askAdmin();
			}},
			Alert.OKCANC
		);
	}

	private void askAdmin() {
		if(A.SDK<8 || Admin.isActive()) return;
		Alert.msg(
			A.rawstr(R.raw.admin_ask_enable),
			new Alert.Click(){ public void on(){ Admin.request(MainActivity.this); }},
			null,
			Alert.OKCANC
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

}
