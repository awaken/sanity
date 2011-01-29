package cri.sanity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;


public final class MainService extends Service
{
	private static final int START  = START_STICKY;
	private static boolean running  = false;

	private static PhoneListener phoneListener;

	//---- static methods

	public static final boolean isRunning () { return running ; }

	public static final void start() { A.app().startService(new Intent(A.app(), MainService.class)); }
	public static final void stop () { A.app(). stopService(new Intent(A.app(), MainService.class)); }

	//---- methods

	@Override
	public IBinder onBind(Intent intent) { return null; }

	@Override
	public int onStartCommand(Intent intent, int flags, int id)
	{
		if(PhoneListener.isRunning()) return START;
		final int state = A.telMan().getCallState();
		if(state == TelephonyManager.CALL_STATE_IDLE) { stopSelf(); return START_NOT_STICKY; }
		running = true;
		P.upgrade();
		if(phoneListener == null) phoneListener = new PhoneListener();
		phoneListener.startup();
		A.telMan().listen(phoneListener, PhoneListener.LISTEN);
		if(A.is(K.NOTIFY_ACTIVITY)) A.notify(A.tr(R.string.msg_running));
		//A.logd("MainService started");
		return START;
	}

	@Override
	public void onDestroy()
	{
		A.telMan().listen(phoneListener, PhoneListener.LISTEN_NONE);
		A.notifyCancAll();
		//A.logd("MainService destroyed");
		running = false;
		super.onDestroy();
	}

}
