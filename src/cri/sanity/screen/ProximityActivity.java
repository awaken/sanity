package cri.sanity.screen;

import cri.sanity.*;
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
					A.alert(
						A.rawstr(R.raw.admin_ask_disable),
						new A.Click(){ public void on(){ Admin.remove(); adminCheck(); }},
						null,
						A.ALERT_OKCANC
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
