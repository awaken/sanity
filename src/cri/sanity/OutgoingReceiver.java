package cri.sanity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class OutgoingReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context ctx, Intent i)
	{
		PhoneReceiver.number = i.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
	}
}
