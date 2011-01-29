package cri.sanity;

import android.os.Bundle;
import android.preference.Preference;


public class ScreenProximity extends ActivityScreen
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		final Preference p = findPref(K.ADMIN);
		if(A.SDK < 8) {
			p.setEnabled(false);
			p.setSummary(R.string.msg_require_froyo);
		}
		else {
			on(p, new Change(){ boolean on(){
				final boolean admin = (Boolean)value;
				if(admin) Admin.request(ScreenProximity.this);
				else      Admin.remove();
				return admin == Admin.isActive();
			}});
		}
	}
	
	@Override
	public void onStart()
	{
		setChecked(K.ADMIN, Admin.isActive());		
		super.onStart();
	}

}
