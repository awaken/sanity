package cri.sanity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public final class MainService extends Service
{
	private static final int START = START_STICKY;
	private static boolean running = false;

	private PhoneListener phoneListener;

	//---- static methods

	public static final boolean isRunning() { return running; }

	public static final void start() { A.app().startService(new Intent(A.app(), MainService.class)); }
	public static final void stop () { A.app(). stopService(new Intent(A.app(), MainService.class)); }

	public static final void notifyRun()
	{
		if(!A.is(P.NOTIFY_ACTIVITY)) return;
		A.notify(A.tr(R.string.msg_running));
	}

	//---- methods

	@Override
	public IBinder onBind(Intent intent) { return null; }

	@Override
	public int onStartCommand(Intent intent, int flags, int id)
	{
		if(PhoneListener.isRunning()) return START;
		running = true;
		if(phoneListener == null) phoneListener = new PhoneListener();
		A.telMan().listen(phoneListener, PhoneListener.LISTEN);
		phoneListener.startup();
		notifyRun();
		A.logd("MainService started");
		return START;
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
