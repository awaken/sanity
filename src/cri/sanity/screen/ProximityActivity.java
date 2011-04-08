package cri.sanity.screen;

import cri.sanity.*;
import cri.sanity.util.Alert;
import android.os.Bundle;


public class ProximityActivity extends ScreenActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Admin.prefSetup(pref("admin"));
		if(A.sensorProxim() != null) return;
		setEnabled(K.DISABLE_PROXIMITY , false);
		setEnabled(K .ENABLE_PROXIMITY , false);
		setEnabled(K.DISABLE_DELAY+K.WS, false);
		setEnabled(K. ENABLE_DELAY+K.WS, false);
		setEnabled(K.SCREEN_OFF        , false);
		setEnabled(K.SCREEN_ON         , false);
		Alert.msg(A.rawstr(R.raw.proxim_none));
	}

	@Override
	public void onResume()
	{
		Admin.prefCheck(pref("admin"));
		super.onResume();
	}
	
}
