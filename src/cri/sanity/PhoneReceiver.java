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
		final String s = i.getStringExtra(TelephonyManager.EXTRA_STATE);
		if(TelephonyManager.EXTRA_STATE_RINGING.equals(s))
			number = i.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
		else if(TelephonyManager.EXTRA_STATE_OFFHOOK.equals(s))
			PickupService.notifyOffhook();
		else
			return;
		MainService.start();
	}

}
