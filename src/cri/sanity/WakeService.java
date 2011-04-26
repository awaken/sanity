package cri.sanity;

import cri.sanity.util.*;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager.WakeLock;


public abstract class WakeService extends Service
{
	private WakeLock wakeLock;

	@Override
	public IBinder onBind(Intent i) { return null; }

	@Override
	public void onCreate() {
		super.onCreate();
		wakeLock = Dev.newCpuWakeLock();
		wakeLock.acquire();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		wakeLock.release();
		wakeLock = null;
	}

}
