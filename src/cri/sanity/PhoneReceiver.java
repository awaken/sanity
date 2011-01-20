package cri.sanity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class PhoneReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context ctx, Intent intent)
	{
		if(MainService.isRunning() || !A.isEnabled()) return;
		MainService.start();
	}
}
