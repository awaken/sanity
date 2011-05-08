package cri.sanity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class BootService extends Service
{
	private static boolean  serviceRunning = false;
	private        boolean instanceRunning = false;

	public static final boolean isRunning() { return serviceRunning; }

	@Override
	public IBinder onBind(Intent i) { return null; }

	@Override
	public int onStartCommand(Intent i, int flags, int id)
	{
		if(serviceRunning) stopSelf();
		else serviceRunning = instanceRunning = true;
		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		if(instanceRunning) serviceRunning = instanceRunning = false;
		super.onDestroy();
	}

}
