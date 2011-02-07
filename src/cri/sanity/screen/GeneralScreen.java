package cri.sanity.screen;

import cri.sanity.*;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;


public class GeneralScreen extends ScreenActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		on(K.FORCE_BT_AUDIO, new Change(){ public boolean on(){
			if(!(Boolean)value) return true;
			A.alert(
				A.tr(R.string.msg_force_bt_audio),
				new A.Click(){ public void on(){ ((CheckBoxPreference)pref).setChecked(true); }},
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
				A.tr(R.string.msg_reverse_proximity),
				new A.Click(){ public void on(){ ((CheckBoxPreference)pref).setChecked(true); }},
				null,
				A.ALERT_YESNO,
				false
			);
			return false;
		}});

		on(K.RESET_PREFS, new Click(){ public boolean on(){
			A.alert(
				A.tr(R.string.msg_reset_prefs),
				new A.Click(){ public void on(){
					final boolean agree = A.is(K.AGREE);
					final String    ver = A.gets(K.VER);
					final int   btcount = A.geti(K.BT_COUNT);
					A.edit().clear();
					P.setDefaults();
					setChecked(K.ENABLED, A.isEnabled());
					updatePref(K.SKIP_HEADSET);
					updatePref(K.FORCE_BT_AUDIO);
					updatePref(K.REVERSE_PROXIMITY);
					A.put(K.AGREE,agree).put(K.VER,ver).putc(K.BT_COUNT,btcount);
				}},
				null
			);
			return true;
		}});
	}

}
