package cri.sanity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;


public class PhoneReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context ctx, Intent i)
	{
		if(MainService.isRunning() || !A.isEnabled()) return;
		if(i.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE))
			OutgoingReceiver.number = null;
		else
			MainService.start();
	}
}
