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
	private static final int NID = 666;
	private static boolean running = false;
	private static boolean full = false;
	private static Rec rec;
	private static Notification notif;
	private static PendingIntent pendingIntent;
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
						if(!A.empty(s)) rec.suffix = '_' + s.replace("+", "00").replace("*", "").replace("#", "");
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
			final Intent i = new Intent(ctx, RecActivity.class);
			i.setFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING|Intent.FLAG_ACTIVITY_NEW_TASK);
			pendingIntent = PendingIntent.getActivity(ctx, 0, i, 0);
			notif = new Notification();
			notif.flags = Notification.FLAG_ONGOING_EVENT;
		}
		final boolean started = rec!=null && rec.isStarted();
		notif.icon = started? R.drawable.ic_rec : R.drawable.ic_bar;
		notif.when = A.now();
		notif.setLatestEventInfo(ctx, A.tr(R.string.msg_rec_title), A.tr(started? R.string.msg_rec_stop : R.string.msg_rec_start), pendingIntent);
		A.notifyCanc();
		A.notifMan().notify(NID, notif);
	}
	
	private static void setupLimit()
	{
		if(full) return;
		breakLimit();
		taskLimit = new TimerTask(){ public void run(){
			if(rec==null || !rec.isStarted()) return;
			rec.stop();
			A.notify(A.tr(R.string.msg_rec_free_limit));
			A.notifyCanc();
			notifyStatus();
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
