package cri.sanity.screen;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import cri.sanity.*;


public class GeneralActivity extends ScreenActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		on(K.FORCE_BT_AUDIO, new Change(){ public boolean on(){
			if(!(Boolean)value) return true;
			A.alert(
				A.rawstr(R.raw.force_bt),
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
				A.rawstr(R.raw.reverse_proximity),
				new A.Click(){ public void on(){ setChecked(pref, true); }},
				null,
				A.ALERT_YESNO,
				false
			);
			return false;
		}});

		on("profile", new Click(){ public boolean on(){
			startActivity(new Intent(A.app(), ProfileActivity.class));
			return true;
		}});

		on("backup_prefs", new Click(){ public boolean on(){
			A.alert(
				A.s(R.string.msg_backup_prefs),
				new A.Click(){ public void on(){
					final boolean ok = P.backup();
					A.toast(ok? R.string.msg_backup_success : R.string.msg_backup_failed);
				}},
				new A.Click(){ public void on(){
					if(!P.backupExists())
						A.alert(A.s(R.string.msg_backup_no));
					else {
						skipAllKeys = true;
						final boolean ok = P.restore();
						A.toast(ok? R.string.msg_restore_success : R.string.msg_restore_failed);
						if(ok) updateScreenPrefs();
						skipAllKeys = false;
					}
				}},
				A.ALERT_BAKRES
			);
			return true;
		}});

		on("reset_prefs", new Click(){ public boolean on(){
			A.alert(
				A.rawstr(R.raw.reset),
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
	}

	private void updateScreenPrefs() { updatePrefs(K.FORCE_BT_AUDIO, K.REVERSE_PROXIMITY); }

}
