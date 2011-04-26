package cri.sanity;

import android.content.Context;
import android.content.Intent;
import cri.sanity.util.*;


public final class MainService extends WakeService
{
	private static boolean running = false;
	private static PhoneListener phoneListener;

	//---- static methods

	public static boolean isRunning() { return running ; }

	public static void start()
	{
		final Context ctx = A.app();
		ctx.startService(new Intent(ctx, MainService.class));
	}

	public static void stop()
	{
		final Context ctx = A.app();
		ctx.stopService(new Intent(ctx, MainService.class)); 
	}

	//---- methods

	@Override
	public void onCreate()
	{
		super.onCreate();
		if(phoneListener == null) phoneListener = new PhoneListener();
	}

	@Override
	public int onStartCommand(Intent i, int flags, int id)
	{
		if(i==null || running || PhoneListener.isRunning()) return START_STICKY;
		if(Dev.isIdle()) { stopSelf(); RecService.stop(); return START_STICKY; }
		running = true;
		P.upgrade();
		if(A.is(K.NOTIFY_ACTIVITY)) A.notify(A.s(R.string.msg_running));
		phoneListener.startup();
		A.telMan().listen(phoneListener, PhoneListener.LISTEN);
		//A.logd("MainService started");
		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		try { A.telMan().listen(phoneListener, PhoneListener.LISTEN_NONE); } catch(Exception e) {}
		A.notifyCanc();
		//A.logd("MainService destroyed");
		running = false;
		super.onDestroy();
	}

}
