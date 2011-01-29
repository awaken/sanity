package cri.sanity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class OutgoingReceiver extends BroadcastReceiver
{
	public static String number;
	
	@Override
	public void onReceive(Context ctx, Intent i)
	{
		number = i.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
	}

}
