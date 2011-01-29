package cri.sanity;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;


public class RecService extends Service
{
	private static final int NID   = 666;
	private static boolean running = false;
	private static boolean full    = false;
	private static long    ts      = 0;
	private static Rec rec;
	private static Notification notif;
	private static PendingIntent notifIntent;
	private static Timer timer;
	private static TimerTask taskLimit;

	//---- static api

	public static final boolean isRunning() { return running; }

	public static final void start()
	{
		if(running) return;
		timer = new Timer();
		full  = A.isFull();
		A.app().startService(new Intent(A.app(), RecService.class));
	}

	public static final void stop()
	{
		if(!running) return;
		A.app().stopService(new Intent(A.app(), RecService.class));
		if(timer != null) { timer.cancel(); timer = null; }
		if(rec != null) {
			if(rec.isStarted()) rec.stop();
			rec.release();
			rec = null;
		}
		taskLimit   = null;
		notif       = null;
		notifIntent = null;
		A.notifyCanc(NID);
	}

	//---- public api

	@Override
	public IBinder onBind(Intent intent) { return null; }

	@Override
	public int onStartCommand(Intent intent, int flags, int id)
	{
		if(timer == null) return START_NOT_STICKY;
		running = true;
		final long now = A.now();
		if(now-ts < Conf.REC_MIN_TIME) return START_STICKY;
		ts = now;
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(rec == null)
					rec = new Rec(A.getsi(K.REC_SRC), A.getsi(K.REC_FMT));
				else if(rec.isStarted())
					rec.stop();
				else {
					final PhoneListener pl = PhoneListener.getActiveInstance();
					if(pl != null) {
						final String s = pl.callNumber();
						if(!A.empty(s)) rec.suffix = '_' + A.cleanFn(s);
					}
					rec.start();
					setupLimit();
				}
				notifyStatus();
			}
		}, 0);
		return START_STICKY;
	}
	
	@Override
	public void onDestroy()
	{
		running = false;
		super.onDestroy();
	}

	//---- private api

	private static void notifyStatus()
	{
		final Context ctx = A.app();
		if(notif == null) {
			notifIntent = PendingIntent.getService(ctx, 0, new Intent(ctx, RecService.class), 0);
			notif       = new Notification();
			notif.flags = Notification.FLAG_ONGOING_EVENT|Notification.FLAG_NO_CLEAR;
		}
		final boolean started = rec!=null && rec.isStarted();
		notif.icon = started? R.drawable.ic_rec : R.drawable.ic_bar;
		notif.when = A.now();
		notif.setLatestEventInfo(ctx, A.tr(R.string.msg_rec_title), A.tr(started? R.string.msg_rec_stop : R.string.msg_rec_start), notifIntent);
		A.notifyCanc();
		A.notifMan().notify(NID, notif);
	}
	
	private static void setupLimit()
	{
		breakLimit();
		if(full) return;
		taskLimit = new TimerTask(){ public void run(){
			if(rec==null || !rec.isStarted()) return;
			rec.stop();
			A.notify(A.tr(R.string.msg_rec_free_limit));
			A.notifyCanc();
			notifyStatus();
			taskLimit = null;
		}};
		timer.schedule(taskLimit, Conf.REC_FREE_TIMEOUT);
	}
	
	private static void breakLimit()
	{
		if(taskLimit != null) {
			taskLimit.cancel();
			taskLimit = null;
		}
		timer.purge();
	}

}
