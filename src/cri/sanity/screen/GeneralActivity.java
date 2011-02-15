package cri.sanity.screen;

import cri.sanity.*;
import android.os.Bundle;
import android.preference.Preference;


public class GeneralActivity extends ScreenActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		on(K.FORCE_BT_AUDIO, new Change(){ public boolean on(){
			if(!(Boolean)value) return true;
			A.alert(
				A.tr(R.string.ask_force_bt_audio),
				new A.Click(){ public void on(){ setChecked(pref, true); }},
				null,
				A.ALERT_YESNO,
				false
			);
			return false;
		}});

		final Preference p = pref(K.REVERSE_PROXIMITY);
		p.setEnabled(p.isEnabled() && Dev.sensorProxim()!=null);
		on(p, new Change(){ public boolean on(){
			if(!(Boolean)value) return true;
			A.alert(
				A.tr(R.string.ask_reverse_proximity),
				new A.Click(){ public void on(){ setChecked(pref, true); }},
				null,
				A.ALERT_YESNO,
				false
			);
			return false;
		}});

		on(K.RESET_PREFS, new Click(){ public boolean on(){
			A.alert(
				A.tr(R.string.ask_reset_prefs),
				new A.Click(){ public void on(){
					final boolean agree = A.is(K.AGREE);
					final String    ver = A.gets(K.VER);
					final int   btcount = A.geti(K.BT_COUNT);
					A.edit().clear();
					P.setDefaults();
					setChecked(K.ENABLED, A.isEnabled());
					updateScreenPrefs();
					A.put(K.AGREE,agree).put(K.VER,ver).putc(K.BT_COUNT,btcount);
				}},
				null
			);
			return true;
		}});

		on(K.BACKUP_PREFS, new Click(){ public boolean on(){
			A.alert(
				A.tr(R.string.msg_backup_prefs),
				new A.Click(){ public void on(){
					final boolean ok = P.backup();
					A.toast(ok? R.string.msg_backup_success : R.string.msg_backup_failed);
				}},
				new A.Click(){ public void on(){
					if(!P.backupExists())
						A.alert(A.tr(R.string.msg_backup_no));
					else {
						final boolean ok = P.restore();
						A.toast(ok? R.string.msg_restore_success : R.string.msg_restore_failed);
						if(ok) updateScreenPrefs();
					}
				}},
				A.ALERT_BAKRES
			);
			return true;
		}});
	}

	private void updateScreenPrefs() { updatePrefs(K.SKIP_HEADSET, K.FORCE_BT_AUDIO, K.REVERSE_PROXIMITY); }

}
