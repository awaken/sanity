package cri.sanity;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;


public class RecService extends Service
{	
	private static final int NID        = 666;
	private static final int TASK_EXEC  = Task.idNew();
	private static final int TASK_LIMIT = Task.idNew();
	private static boolean running      = false;
	private static boolean full         = false;
	private static long ts              = 0;
	private static int autoStartDelay   = 0;
	private static int autoStopDelay    = 0;
	private static int autoStopLimit    = 0;
	private static String notifLimit;
	private static Rec rec;
	private static Notification notif;
	private static PendingIntent notifIntent;

	//---- inner classes

	private static final Task taskRecStart = new Task(){
		@Override
		public void run() {
			if(rec.isStarted()) return;
			if(A.empty(rec.suffix)) {
				final PhoneListener pl = PhoneListener.getActiveInstance();
				if(pl == null) { stopService(); return; }
				rec.suffix = Conf.REC_SEP + (pl.isOutgoing()? "out" : "in");
				final String s = pl.callNumber();
				if(!A.empty(s)) rec.suffix += Conf.REC_SEP + A.cleanFn(s);
			}
			rec.start();
			applyLimit();
			notifyStatus();
		}
	};

	private static final Task taskRecStop = new Task() {
		@Override
		public void run() {
			if(!rec.isStarted()) return;
			rec.stop();
			notifyStatus();
		}
	};

	//---- static api

	public static final boolean isRunning() { return running; }
	public static final boolean isRecord () { return rec!=null && rec.isStarted(); }

	public static final void start() {
		if(running) return;
		full = A.isFull();
		rec  = new Rec(A.getsi(K.REC_SRC), A.getsi(K.REC_FMT));
		notifLimit = A.is(K.NOTIFY_REC_STOP) ? A.tr(full? R.string.msg_rec_limit : R.string.msg_rec_free_limit) : null;
		if(rec.src == Rec.SRC_MIC) A.audioMan().setMicrophoneMute(false);
		startService();
		notifyStatus();
	}
	public static final void stop() {
		if(!running) return;
		stopService();
		Task.stop(TASK_LIMIT, TASK_EXEC);
		if(rec != null) { rec.release(); rec = null; }
		notif       = null;
		notifIntent = null;
		notifLimit  = null;
		A.notifyCanc(NID);
	}

	public static final void recStart(long delay) { taskRecStart.replace(TASK_EXEC, delay); }
	public static final void recStop (long delay) { taskRecStop .replace(TASK_EXEC, delay); }

	public static final boolean checkAutoRec(PhoneListener pl) {
		boolean accept = true;
		// TODO: call number filter!
		final boolean autoStart = accept && A.is(K.REC_START);
		final boolean autoStop  = accept && A.is(K.REC_STOP);
	  autoStopLimit  = !full? Conf.REC_FREE_LIMIT : (autoStop? A.getsi(K.REC_STOP_LIMIT)*60000 : 0);
		autoStartDelay = autoStart? A.getsi(K.REC_START_DELAY) : 0;
		autoStopDelay  = autoStop ? A.getsi(K.REC_STOP_DELAY ) : 0;
		if(!setSpeakerListener(pl,autoStart,autoStop) && autoStart) recStartOffhook(pl);
		return true;
	}
	
	//---- public Service override

	@Override
	public IBinder onBind(Intent intent) { return null; }

	@Override
	public int onStartCommand(Intent intent, int flags, int id) {
		final long now = A.now();
		if(now-ts < Conf.REC_MIN_SRV_RETRY) return START_STICKY;
		ts = now;
		if(!running)
			running = true;
		else if(rec.isStarted())
			recStop(0);
		else
			recStart(0);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		running = false;
		super.onDestroy();
	}

	//---- private api

	private static void startService() { A.app().startService(new Intent(A.app(), RecService.class)); }
	private static void  stopService() { A.app(). stopService(new Intent(A.app(), RecService.class)); }
	
	private static void notifyStatus() {
		final Context ctx = A.app();
		if(notif == null) {
			notifIntent = PendingIntent.getService(ctx, 0, new Intent(ctx, RecService.class), 0);
			notif       = new Notification();
			notif.flags = Notification.FLAG_ONGOING_EVENT|Notification.FLAG_NO_CLEAR;
		}
		final boolean started = rec.isStarted();
		notif.icon = started? R.drawable.ic_rec : R.drawable.ic_bar;
		notif.when = A.now();
		notif.setLatestEventInfo(ctx, A.tr(R.string.msg_rec_title), A.tr(started? R.string.msg_rec_stop : R.string.msg_rec_start), notifIntent);
		A.notifyCanc();
		A.notifMan().notify(NID, notif);
	}
	
	private static void applyLimit() {
		if(autoStopLimit <= 0) return;
		new Task() {
			@Override
			public void run() {
				if(!rec.isStarted()) return;
				rec.stop();
				if(notifLimit != null) A.notify(notifLimit);
				notifyStatus();
		 }
		}.replace(TASK_LIMIT, autoStopLimit);
	}

	private static void recStartOffhook(PhoneListener pl) {
		final long delay = (pl.isOutgoing() ? Conf.FORCE_AUTOSPEAKER_DELAY : 0) + Conf.REC_OFFHOOK_DELAY;
		recStart(Math.max(autoStartDelay, delay));
	}

	private static boolean setSpeakerListener(PhoneListener pl, boolean autoStart, boolean autoStop) {
		final boolean startSpeaker = autoStart && A.is(K.REC_START_SPEAKER);
		final boolean stopSpeaker  = autoStop  && A.is(K.REC_STOP_SPEAKER);
		if(!startSpeaker && !stopSpeaker) return false;
		pl.speakerListener = new SpeakerListener() {
			private boolean start = startSpeaker;
			private boolean stop  = stopSpeaker;
			private int     times = A.getsi(K.REC_START_TIMES);
			public void onSpeakerChanged(boolean enabled) {
				if(enabled) {
					if(start) {
						recStart(autoStartDelay);
						start = --times != 0;
					}
				} else {
					if(stop)
						recStop(autoStopDelay);
				}
			}
		};
		return startSpeaker;
	}

}
