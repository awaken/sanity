package cri.sanity;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;


public class ScreenGeneral extends ActivityScreen
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		on(P.FORCE_BT_AUDIO, new Change(){ boolean on(){
			if(!(Boolean)value) return true;
			A.alert(
				A.tr(R.string.msg_force_bt_audio),
				new A.Click(){ void on(){ ((CheckBoxPreference)pref).setChecked(true); }},
				null,
				A.ALERT_YESNO,
				false
			);
			return false;
		}});

		Preference p = findPref(P.REVERSE_PROXIMITY);
		p.setEnabled(p.isEnabled() && Dev.sensorProxim()!=null);
		on(p, new Change(){ boolean on(){
			if(!(Boolean)value) return true;
			A.alert(
				A.tr(R.string.msg_reverse_proximity),
				new A.Click(){ void on(){ ((CheckBoxPreference)pref).setChecked(true); }},
				null,
				A.ALERT_YESNO,
				false
			);
			return false;
		}});

		on(P.RESET_PREFS, new Click(){ boolean on(){
			A.alert(
				A.tr(R.string.msg_reset_prefs),
				new A.Click(){ void on(){
					final boolean agree = A.is(P.AGREE);
					final String    ver = A.gets(P.VER);
					final int   btcount = A.geti(P.BT_COUNT);
					A.edit().clear();
					P.setDefaults();
					setChecked(P.ENABLED, A.isEnabled());
					updatePref(P.SKIP_HEADSET);
					updatePref(P.FORCE_BT_AUDIO);
					updatePref(P.REVERSE_PROXIMITY);
					A.put(P.AGREE,agree).put(P.VER,ver).putc(P.BT_COUNT,btcount);
				}},
				null
			);
			return true;
		}});

		if(A.SDK < 8) {
			p = findPref(P.ADMIN);
			p.setEnabled(false);
			p.setSummary(R.string.msg_require_froyo);
		}
		else {
			on(P.ADMIN, new Change(){ boolean on(){
				final boolean admin = (Boolean)value;
				if(admin) Admin.request(ScreenGeneral.this);
				else      Admin.remove();
				return admin == Admin.isActive();
			}});
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
		setChecked(P.ADMIN, Admin.isActive());		
	}

}
