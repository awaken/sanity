package cri.sanity.screen;

import cri.sanity.*;
import cri.sanity.util.*;
import android.os.Bundle;
import android.preference.Preference;


public class ProximityActivity extends ScreenActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		final Preference p = pref("admin");
		if(A.SDK < 8) {
			p.setEnabled(false);
			p.setSummary(R.string.msg_require_froyo);
		}
		else {
			on(p, new Change(){ public boolean on(){
				if((Boolean)value)
					Admin.request(ProximityActivity.this);
				else
					Alert.msg(
						A.rawstr(R.raw.admin_ask_disable),
						new Alert.Click(){ public void on(){ Admin.remove(); adminCheck(); }},
						null,
						Alert.OKCANC
					);
				return false;
			}});
		}
	}

	@Override
	public void onResume()
	{
		adminCheck();
		super.onResume();
	}
	
	private void adminCheck() { setChecked("admin", Admin.isActive()); }

}
