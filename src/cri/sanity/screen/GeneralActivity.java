package cri.sanity.screen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import cri.sanity.*;
import cri.sanity.util.*;


public class GeneralActivity extends ScreenActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Admin.prefSetup(pref("admin"));

		on(K.QUICK_START, new Change(){ public boolean on(){
			final Context ctx = A.app();
			final Intent  i   = new Intent(ctx, BootService.class);
			if((Boolean)value) ctx.startService(i);
			else               ctx. stopService(i);
			return true;
		}});

		on(K.FORCE_BT_AUDIO, new Change(){ public boolean on(){
			if(!(Boolean)value) return true;
			Alert.msg(
				A.rawstr(R.raw.force_bt),
				new Alert.Click(){ public void on(){ setChecked(pref, true); }},
				null,
				Alert.YESNO,
				false
			);
			return false;
		}});

		final Preference p = pref(K.REVERSE_PROXIMITY);
		p.setEnabled(p.isEnabled() && A.sensorProxim()!=null);
		on(p, new Change(){ public boolean on(){
			if(!(Boolean)value) return true;
			Alert.msg(
				A.rawstr(R.raw.reverse_proximity),
				new Alert.Click(){ public void on(){ setChecked(pref, true); }},
				null,
				Alert.YESNO,
				false
			);
			return false;
		}});

		on("profile", new Click(){ public boolean on(){
			startActivity(new Intent(A.app(), ProfileActivity.class));
			return true;
		}});

		on("backup_prefs", new Click(){ public boolean on(){
			Alert.msg(
				A.s(R.string.msg_backup_prefs),
				new Alert.Click(){ public void on(){
					final boolean ok = P.backup();
					A.toast(ok? R.string.msg_backup_success : R.string.msg_backup_failed);
				}},
				new Alert.Click(){ public void on(){
					if(!P.backupExists())
						Alert.msg(A.s(R.string.msg_backup_no));
					else {
						skipAllKeys = true;
						final boolean ok = P.restore();
						A.toast(ok? R.string.msg_restore_success : R.string.msg_restore_failed);
						if(ok) updateScreenPrefs();
						skipAllKeys = false;
					}
				}},
				Alert.BAKRES
			);
			return true;
		}});

		on("reset_prefs", new Click(){ public boolean on(){
			Alert.msg(
				A.rawstr(R.raw.reset),
				new Alert.Click(){ public void on(){
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
		
		on("delete_filters", new Click(){ public boolean on(){
			Alert.msg(
				A.rawstr(R.raw.delete_filters),
				new Alert.Click(){ public void on(){
					P.removeFilters();
					pref.setEnabled(false);
				}},
				null
			);
			return true;
		}});
	}

	@Override
	public void onResume()
	{
		Admin.prefCheck(pref("admin"));
		super.onResume();
	}

	private void updateScreenPrefs()
	{
		updatePrefs(K.FORCE_BT_AUDIO, K.REVERSE_PROXIMITY);
		updatePrefsNoDep(K.SILENT_LIMIT, K.AIRPLANE_LIMIT, K.PWD_CLEAR, K.PWD);
	}

}
