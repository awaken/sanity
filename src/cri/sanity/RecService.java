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
	private static final int NID       = 666;
	private static boolean running     = false;
	private static boolean full        = false;
	private static long ts             = 0;
	private static int autoStartDelay  = 0;
	private static int autoStopDelay   = 0;
	private static int autoStopLimit   = 0;
	private static boolean notifyLimit = true;
	private static Rec rec;
	private static Notification notif;
	private static PendingIntent notifIntent;
	private static Timer timer;
	private static TimerTask taskExec, taskLimit;

	//---- static api

	public static final boolean isRunning() { return running; }
	public static final boolean isRecord () { return rec!=null && rec.isStarted(); }

	public static final void start() {
		if(running) return;
		full = A.isFull();
		A.audioMan().setMicrophoneMute(false);
		startService();
	}
	public static final void stop() {
		if(!running) return;
		stopService();
		if(timer != null) { timer.cancel(); timer = null; }
		if(rec   != null) { rec.release();  rec   = null; }
		taskExec    = null;
		taskLimit   = null;
		notif       = null;
		notifIntent = null;
		A.notifyCanc(NID);
	}
	
	public static final boolean recStart(long delay) {
		if(taskExec != null) { taskExec.cancel(); taskExec = null; }
		if(isRecord()) return false;
		taskExec = new TimerTask(){ public void run(){ exec(); taskExec = null; }};
		schedule(taskExec, delay);
		return true;
	}
	public static final boolean recStop(long delay) {
		if(taskExec != null) { taskExec.cancel(); taskExec = null; }
		if(!isRecord()) return false;
		taskExec = new TimerTask(){ public void run(){ exec(); taskExec = null; }};
		schedule(taskExec, delay);
		return true;
	}

	public static final boolean checkAutoRec(PhoneListener pl) {
		boolean accept = true;
		// TODO: call number filter!
		final boolean autoStart = accept && A.is(K.REC_START);
		final boolean autoStop  = accept && A.is(K.REC_STOP);
	  autoStopLimit  = !full? Conf.REC_FREE_LIMIT : (autoStop? A.getsi(K.REC_STOP_LIMIT)*60000 : 0);
		autoStartDelay = autoStart? A.getsi(K.REC_START_DELAY) : 0;
		autoStopDelay  = autoStop ? A.getsi(K.REC_STOP_DELAY ) : 0;
		notifyLimit    = A.is(K.NOTIFY_REC_STOP);
		if(!setSpeakerListener(pl,autoStart,autoStop) && autoStart) recStartOffhook(pl);
		return true;
	}
	
	//---- public Service override

	@Override
	public IBinder onBind(Intent intent) { return null; }

	@Override
	public int onStartCommand(Intent intent, int flags, int id)
	{
		running = true;
		final long now = A.now();
		if(now-ts < Conf.REC_MIN_SRV_RETRY) return START_STICKY;
		ts = now;
		return exec();
	}

	@Override
	public void onDestroy()
	{
		running = false;
		super.onDestroy();
	}

	//---- private api
	
	private static int exec() {
		if(rec == null)
			rec = new Rec(A.getsi(K.REC_SRC), A.getsi(K.REC_FMT));
		else if(rec.isStarted())
			rec.stop();
		else {
			if(A.empty(rec.suffix)) {
				final PhoneListener pl = PhoneListener.getActiveInstance();
				if(pl == null) { stopService(); return START_NOT_STICKY; }
				final String s = pl.callNumber();
				if(!A.empty(s)) rec.suffix = Conf.REC_SEP + A.cleanFn(s);
			}
			rec.start();
			applyLimit();
		}
		notifyStatus();
		return START_STICKY;
	}

	private static void startService() { A.app().startService(new Intent(A.app(), RecService.class)); }
	private static void  stopService() { A.app(). stopService(new Intent(A.app(), RecService.class)); }
	
	private static void notifyStatus() {
		final Context ctx = A.app();
		if(notif == null) {
			notifIntent = PendingIntent.getService(ctx, 0, new Intent(ctx, RecService.class), 0);
			notif       = new Notification();
			notif.flags = Notification.FLAG_ONGOING_EVENT|Notification.FLAG_NO_CLEAR;
		}
		final boolean started = isRecord();
		notif.icon = started? R.drawable.ic_rec : R.drawable.ic_bar;
		notif.when = A.now();
		notif.setLatestEventInfo(ctx, A.tr(R.string.msg_rec_title), A.tr(started? R.string.msg_rec_stop : R.string.msg_rec_start), notifIntent);
		A.notifyCanc();
		A.notifMan().notify(NID, notif);
	}
	
	private static void applyLimit() {
		breakLimit();
		if(autoStopLimit <= 0) return;
		taskLimit = new TimerTask(){ public void run(){
			if(!isRecord()) return;
			rec.stop();
			if(notifyLimit) A.notify(A.tr(full? R.string.msg_rec_limit : R.string.msg_rec_free_limit));
			notifyStatus();
			taskLimit = null;
		}};
		schedule(taskLimit, autoStopLimit);
	}
	private static void breakLimit() {
		if(taskLimit != null) {
			taskLimit.cancel();
			taskLimit = null;
		}
		if(timer != null)
			timer.purge();
	}

	private static void schedule(TimerTask task, long delay) {
		if(timer == null) timer = new Timer();
		timer.schedule(task, delay);
	}
	
	private static boolean recStartOffhook(PhoneListener pl) {
		final long delay = (pl.isOutgoing() ? Conf.FORCE_AUTOSPEAKER_DELAY : 0) + Conf.REC_OFFHOOK_DELAY;
		return recStart(Math.max(autoStartDelay, delay));
	}

	private static boolean setSpeakerListener(PhoneListener pl, boolean autoStart, boolean autoStop) {
		final boolean startSpeaker = autoStart && A.is(K.REC_START_SPEAKER);
		final boolean stopSpeaker  = autoStop  && A.is(K.REC_STOP_SPEAKER);
		if(!startSpeaker && !stopSpeaker) return false;
		pl.speakerListener = new SpeakerListener() {
			private boolean start = startSpeaker;
			private boolean stop  = stopSpeaker;
			public void onSpeakerChanged(boolean enabled) {
				if(enabled) {
					if(start) recStart(autoStartDelay);
				} else {
					if(stop) recStop(autoStopDelay);
				}
			}
		};
		return startSpeaker;
	}

}
