package cri.sanity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;


public class PhoneReceiver extends BroadcastReceiver
{
	public static String number;

	@Override
	public void onReceive(Context ctx, Intent i)
	{
		if(MainService.isRunning() || !A.isEnabled()) return;
		String s = i.getStringExtra(TelephonyManager.EXTRA_STATE);
		if(s.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
			number = null;
			return;
		}
		if(s.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
			s = i.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
			if(!A.empty(s)) number = s;
		}
		MainService.start();
	}

}
