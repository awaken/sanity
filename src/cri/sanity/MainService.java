package cri.sanity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public final class MainService extends Service
{
	private static boolean running = false;

	private PhoneListener phoneListener;

	//---- static methods

	public static final boolean isRunning() { return running; }

	public static final void start() { A.app().startService(new Intent(A.app(), MainService.class)); }
	public static final void stop () { A.app(). stopService(new Intent(A.app(), MainService.class)); }

	public static final void notifyRun()
	{
		if(!A.is("notify_activity")) return;
		A.notify(A.tr(R.string.msg_running));
	}

	//---- methods

	@Override
	public IBinder onBind(Intent intent) { return null; }

	@Override
	public int onStartCommand(Intent intent, int flags, int id)
	{
		running = true;
		super.onStartCommand(intent, flags, id);
		notifyRun();
		A.logd("MainService started");
		if(phoneListener == null) phoneListener = new PhoneListener();
		phoneListener.startup();
		A.telMan().listen(phoneListener, PhoneListener.LISTEN);
		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		A.telMan().listen(phoneListener, PhoneListener.LISTEN_NONE);
		A.notifyCanc();
		A.logd("MainService destroyed");
		running = false;
		super.onDestroy();
	}

}
