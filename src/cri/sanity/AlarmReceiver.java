package cri.sanity;

import cri.sanity.util.Dev;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class AlarmReceiver extends BroadcastReceiver
{
	public static final String ACT_UNFLIGHT = "unflight";

	@Override
	public void onReceive(Context ctx, Intent i)
	{
		if(ACT_UNFLIGHT.equals(i.getAction()))
			if(!PhoneListener.isRunning() && Dev.isFlightModeOn())
				Dev.enableFlightMode(false);
	}

}
