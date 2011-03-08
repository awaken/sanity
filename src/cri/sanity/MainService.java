package cri.sanity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public final class MainService extends Service
{
	private static boolean running = false;
	private static PhoneListener phoneListener;

	//---- static methods

	public static final boolean isRunning () { return running ; }

	public static final void start() { A.app().startService(new Intent(A.app(), MainService.class)); }
	public static final void stop () { A.app(). stopService(new Intent(A.app(), MainService.class)); }

	//---- methods

	@Override
	public IBinder onBind(Intent intent) { return null; }

	@Override
	public void onCreate()
	{
		if(phoneListener == null) phoneListener = new PhoneListener();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent i, int flags, int id)
	{
		if(running || PhoneListener.isRunning()) return START_STICKY;
		running = true;
		P.upgrade();
		if(A.is(K.NOTIFY_ACTIVITY)) A.notify(A.s(R.string.msg_running));
		//if(phoneListener == null) phoneListener = new PhoneListener();
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
		A.notifyCancAll();
		//A.logd("MainService destroyed");
		running = false;
		super.onDestroy();
		System.gc();
	}

}
