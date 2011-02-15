package cri.sanity;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;


public class RecService extends Service
{
	public static final int NID              = 666;
	public static final int INCOMING         = 1;
	public static final int OUTGOING         = 2;
	public static final int ACT_HEADSET_SKIP = 0;
	public static final int ACT_HEADSET_ON   = 1;
	//public static final int ACT_HEADSET_OFF  = 2;

	private static final int TASK_EXEC  = Task.idNew();
	private static final int TASK_LIMIT = Task.idNew();

	private static long    ts      = 0;
	private static boolean running = false;
	private static boolean full    = false;
	private static boolean autoStart = false;
	private static boolean autoStop  = false;
	private static boolean autoStartSpeaker = false;
	private static boolean autoStopSpeaker  = false;
	private static int     autoStartDelay = 0;
	private static int     autoStopDelay  = 0;
	private static int     autoStopLimit  = 0;
	private static int     autoStartTimes = 0;
	private static boolean headsetStart   = false;
	private static boolean headsetStop    = false;
	private static boolean headsetOnStart = false;
	private static boolean headsetOnStop  = false;
	private static PhoneListener pl;
	private static Rec rec;
	private static String notifLimit;
	private static Notification notif;
	private static PendingIntent notifIntent;
	private static Task taskRecStart, taskRecStop;

	//---- public static api

	public static final boolean isRunning() { return running; }
	public static final boolean isRecord () { return rec!=null && rec.isStarted(); }

	public static final void start(PhoneListener phoneListener) {
		if(running) return;
		pl   = phoneListener;
		full = A.isFull();
		rec  = new Rec(A.getsi(K.REC_SRC), A.getsi(K.REC_FMT));
		notifLimit = A.is(K.NOTIFY_REC_STOP) ? A.tr(full? R.string.msg_rec_limit : R.string.msg_rec_free_limit) : null;
		if(rec.src == Rec.SRC_MIC) A.audioMan().setMicrophoneMute(false);
		buildTasks();
		startService();
		notifyStatus();
		autoInit();
	}
	public static final void stop() {
		if(!running) return;
		stopService();
		Task.stop(TASK_LIMIT, TASK_EXEC);
		if(rec != null) { rec.release(); rec = null; }
		pl          = null;
		notif       = null;
		notifIntent = null;
		notifLimit  = null;
		A.notifyCanc(NID);
	}

	public static final void recStart(long delay) { taskRecStart.exec(TASK_EXEC, delay); }
	public static final void recStop (long delay) { taskRecStop .exec(TASK_EXEC, delay); }

	public static final void checkAutoRec() {
		if(rec.isStarted()) return;
		// TODO: phone number filter!
		final int d = A.getsi(K.REC_START_DIR);
		if(     d == INCOMING) { if( pl.isOutgoing()) autoStart = false; }
		else if(d == OUTGOING) { if(!pl.isOutgoing()) autoStart = false; }
		if(!autoStart)
			noAutoStart();
		else if(headsetStart) {
			if(headsetOnStart == pl.isHeadsetOn())
				recStartOffhook();
		}
		else if(!autoStartSpeaker)
			recStartOffhook();
		setSpeakerListener();
	}
	
	public static final void updateHeadset(boolean on) {
		if(rec == null) return;
		if(rec.isStarted()) {
			if(!headsetStop || on!=headsetOnStop) return;
			recStopAuto();
		} else {
			if(!headsetStart || on!=headsetOnStart) return;
			recStartAuto();
		}
	}

	//---- public Service override

	@Override
	public IBinder onBind(Intent intent) { return null; }

	@Override
	public int onStartCommand(Intent intent, int flags, int id) {
		final long now = A.now();
		if(now-ts < Conf.SERVICE_TIMEOUT) return START_STICKY;
		ts = now;
		if(!running)             running = true;
		else if(rec == null)   { A.notifyCanc(NID); stopSelf(); return START_NOT_STICKY; }
		else if(rec.isStarted()) recStop(0);
		else                     recStart(0);
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

	private static void recStartAuto() {
		if(!autoStart) return;
		recStart(autoStartDelay);
		autoStart = --autoStartTimes != 0;
	}
	private static void recStopAuto() {
		if(!autoStop) return;
		recStop(autoStopDelay);
	}
	
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
		}.exec(TASK_LIMIT, autoStopLimit);
	}

	// for automatic start recording on offhook
	private static void recStartOffhook() {
		final long delay = (pl.isOutgoing() ? Conf.FORCE_AUTOSPEAKER_DELAY : 0) + Conf.REC_OFFHOOK_DELAY;
		recStart(Math.max(autoStartDelay, delay));
	}

	// setup auto start/stop when speaker is turned on/off
	private static void setSpeakerListener() {
		autoStartSpeaker = autoStart && A.is(K.REC_START_SPEAKER);
		autoStopSpeaker  = autoStop  && A.is(K.REC_STOP_SPEAKER);
		if(!autoStartSpeaker && !autoStopSpeaker) return;
		pl.speakerListener = new SpeakerListener() {
			public void onSpeakerChanged(boolean enabled) {
				if(enabled) { if(autoStartSpeaker) recStartAuto(); } 
				else        { if(autoStopSpeaker ) recStopAuto (); }
			}
		};
	}

	private static void buildTasks() {
		if(taskRecStart == null) {
			taskRecStart = new Task() {
				@Override
				public void run() {
					if(rec==null || rec.isStarted()) return;
					if(A.empty(rec.suffix)) {
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
		}
		if(taskRecStop == null) {
			taskRecStop = new Task() {
				@Override
				public void run() {
					if(rec==null || !rec.isStarted()) return;
					rec.stop();
					notifyStatus();
				}
			};
		}
	}

	private static void autoInit() {
		autoStart     = A.is(K.REC_START);
		autoStop      = A.is(K.REC_STOP);
	  autoStopLimit = !full? Conf.REC_FREE_LIMIT : (autoStop? A.getsi(K.REC_STOP_LIMIT)*60000 : 0);
	  // setup auto start
		if(!autoStart)
			noAutoStart();
		else {
			autoStartDelay = A.getsi(K.REC_START_DELAY);
			autoStartTimes = A.getsi(K.REC_START_TIMES);
			final int act  = A.getsi(K.REC_START_HEADSET);
			headsetStart   = act != ACT_HEADSET_SKIP;
			headsetOnStart = act == ACT_HEADSET_ON;
		}
		// setup auto stop
		if(autoStop) {
			autoStopDelay = A.getsi(K.REC_STOP_DELAY);
			final int act = A.getsi(K.REC_STOP_HEADSET);
			headsetStop   = act != ACT_HEADSET_SKIP;
			headsetOnStop = act == ACT_HEADSET_ON;
		} else {
			autoStopDelay = 0;
			headsetStop   = false;
		}
	}
	
	private static void noAutoStart() {
		autoStartDelay = 0;
		autoStartTimes = 0;
		headsetStart   = false;
	}

}
