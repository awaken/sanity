package cri.sanity;

import cri.sanity.util.*;
import java.io.File;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;


public class RecService extends Service
{
	public static final int NID              = 2;
	public static final int INCOMING         = 1;
	public static final int OUTGOING         = 2;
	public static final int ACT_HEADSET_SKIP = 0;
	public static final int ACT_HEADSET_ON   = 1;
	//public static final int ACT_HEADSET_OFF  = 2;

	private static final int TASK_EXEC  = Task.idNew();
	private static final int TASK_LIMIT = Task.idNew();

	private static long    ts        = 0;
	private static boolean running   = false;
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
	private static String notifLimit, notifTitle;
	private static Notification notif;
	private static PendingIntent notifIntent;
	private static Task taskRecStart, taskRecStop, taskRecLimit;

	//---- public static api

	public static final boolean isRunning() { return running; }
	public static final boolean isRecord () { return rec!=null && rec.isStarted(); }

	public static final void start(PhoneListener phoneListener) {
		if(running) return;
		ts  = 0;
		pl  = phoneListener;
		rec = new Rec(A.geti(K.REC_SRC), A.geti(K.REC_FMT));
		notifLimit = A.is(K.NOTIFY_REC_STOP) ? A.s(A.isFull()? R.string.msg_rec_limit : R.string.msg_rec_free_limit) : null;
		autoInit();
		buildTasks();
		startService();
		notifyStatus();
	}
	public static final void stop() {
		if(!running) return;
		stopService();
		Task.stop(TASK_LIMIT, TASK_EXEC);
		if(rec != null) { rec.release(); rec = null; }
		if(pl  != null) {	pl.speakerListener = null; pl = null; }
		notif        = null;
		notifIntent  = null;
		notifLimit   = null;
		taskRecStart = null;
		taskRecStop  = null;
		taskRecLimit = null;
		//running      = false;
		A.notifyCanc(NID);
	}

	public static final void recStart(int delay) { if(taskRecStart != null) taskRecStart.exec(TASK_EXEC, delay); }
	public static final void recStop (int delay) { if(taskRecStop  != null) taskRecStop .exec(TASK_EXEC, delay); }

	public static final void checkAutoRec() {
		if(rec==null || rec.isStarted()) return;
		final int d = A.geti(K.REC_START_DIR);
		if(     d == INCOMING) { if( pl.isOutgoing()) noAutoStart(); }
		else if(d == OUTGOING) { if(!pl.isOutgoing()) noAutoStart(); }
		if(autoStart && !CallFilter.instance().includes(pl.phoneNumber(),"rec",true)) noAutoStart();
		setSpeakerListener();
		if(!autoStart) return;
		if(headsetStart) {
			if(headsetOnStart == pl.isHeadsetOn())
				recStartOffhook();
		}
		else if(!autoStartSpeaker || A.audioMan().isSpeakerphoneOn())
			recStartOffhook();
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
	
	public static final void cron() {
		final int daysLife = A.geti(K.REC_AUTOREMOVE);
		if(daysLife <= 0) return;
		final long now     = A.time();
		final long recheck = daysLife>3 ? daysLife>7 ? 86400000*3 : 86400000 : 86400000/2;
		try { if(now-A.getl(K.CRON) < recheck) return; } catch(Exception e) {}
		final String dir = A.sdcardDir();
		if(dir == null) return;
		final String prefix  = Conf.REC_PREFIX;
		final String extprf  = Conf.PRF_EXT;
		final long threshold = now - ((long)daysLife)*86400000;
		for(File f : new File(dir).listFiles()) {
			final String name = f.getName();
			if(name.startsWith(prefix) && !name.endsWith(extprf) && !name.endsWith(".txt") && f.isFile() && f.lastModified()<threshold)
				f.delete();
		}
		A.putc(K.CRON, now);
	}

	//---- public Service override

	@Override
	public IBinder onBind(Intent intent) { return null; }

	@Override
	public int onStartCommand(Intent i, int flags, int id) {
		if(!MainService.isRunning()) return quit();
		if(i == null) return START_STICKY;
		final long now = SystemClock.elapsedRealtime();
		if(now-ts < Conf.SERVICE_TIMEOUT) return START_STICKY;
		ts = now;
		if(!running) running = true;
		else if(rec == null) return quit();
		else {
			if(rec.isStarted()) recStop (0);
			else                recStart(0);
			try { Dev.iTel().showCallScreen(); } catch(Exception e) {}
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		A.notifyCanc(NID);
		running = false;
		super.onDestroy();
	}

	//---- private api
	
	private int quit() {
		A.notifyCanc(NID);
		stopSelf();
		return START_STICKY;
	}

	private static void startService() {
		final Context ctx = A.app();
		ctx.startService(new Intent(ctx, RecService.class));
	}
	private static void stopService() {
		final Context ctx = A.app();
		ctx.stopService(new Intent(ctx, RecService.class));
	}

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
			notifTitle  = A.s(R.string.msg_rec_title);
		}
		if(rec == null) {
			A.notifyCanc(NID);
			return;
		}
		if(rec.isStarted()) {
			notif.icon = R.drawable.ic_rec_bar;
			notif.setLatestEventInfo(ctx, notifTitle, A.s(R.string.msg_rec_stop), notifIntent);
		} else {
			notif.icon = R.drawable.ic_bar;
			notif.setLatestEventInfo(ctx, notifTitle, A.s(R.string.msg_rec_start), notifIntent);
		}
		notif.when = A.time();
		A.notifyCanc();
		A.notifMan().notify(NID, notif);
	}
	
	private static void applyLimit() {
		if(taskRecLimit == null) return;
		taskRecLimit.exec(TASK_LIMIT, autoStopLimit);
	}

	// for automatic start recording on offhook
	private static void recStartOffhook() {
		final int delay = (pl.isOutgoing() ? Conf.FORCE_AUTOSPEAKER_DELAY : 0) + Conf.REC_OFFHOOK_DELAY;
		recStart(Math.max(autoStartDelay, delay));
	}

	// setup auto start/stop when speaker is turned on/off
	private static void setSpeakerListener() {
		if(!autoStartSpeaker && !autoStopSpeaker)
			pl.speakerListener = null;
		else
			pl.speakerListener = new SpeakerListener() {
				@Override
				public void onSpeakerChanged(boolean enabled) {
					if(enabled) { if(autoStartSpeaker) recStartAuto(); }
					else        { if(autoStopSpeaker ) recStopAuto (); }
				}
			};
	}

	private static void buildTasks() {
		taskRecStart = new Task() {
			@Override
			public void run() {
				try {
					if(rec==null || rec.isStarted() || !Dev.isOffhook()) return;
					if(A.empty(rec.suffix)) {
						if(pl == null) { stopService(); return; }
						rec.suffix = Conf.REC_SEP + (pl.isOutgoing()? "out" : "in");
						final String s = pl.phoneNumber();
						if(!A.empty(s)) rec.suffix += Conf.REC_SEP + A.cleanFn(s,true);
					}
					rec.start();
					applyLimit();
					notifyStatus();
				} catch(Exception e) {}
			}
		};
		taskRecStop = new Task() {
			@Override
			public void run() {
				try {
					if(rec==null || !rec.isStarted()) return;
					rec.stop();
					notifyStatus();
				} catch(Exception e) {}
			}
		};
		taskRecLimit = autoStopLimit<=0? null : new Task(){
			@Override
			public void run() {
				try {
					if(rec==null || !rec.isStarted()) return;
					rec.stop();
					if(notifLimit != null) A.notify(notifLimit);
					notifyStatus();
				} catch(Exception e) {}
			}
		};
	}

	private static void autoInit() {
		autoStart     = A.is(K.REC_START);
		autoStop      = A.is(K.REC_STOP);
	  autoStopLimit = A.isFull()? autoStop? A.geti(K.REC_STOP_LIMIT)*60000 : 0 : Conf.REC_FREE_LIMIT;
	  // setup auto start
		if(!autoStart)
			noAutoStart();
		else {
			autoStartDelay   = A.geti(K.REC_START_DELAY);
			autoStartTimes   = A.geti(K.REC_START_TIMES);
			autoStartSpeaker = pl.hasAutoSpeaker() && A.is(K.REC_START_SPEAKER);
			final int act    = A.geti(K.REC_START_HEADSET);
			headsetStart     = act != ACT_HEADSET_SKIP;
			headsetOnStart   = act == ACT_HEADSET_ON;
		}
		// setup auto stop
		if(autoStop) {
			autoStopDelay   = A.geti(K.REC_STOP_DELAY);
			autoStopSpeaker = pl.hasAutoSpeaker() && A.is(K.REC_STOP_SPEAKER);
			final int act   = A.geti(K.REC_STOP_HEADSET);
			headsetStop     = act != ACT_HEADSET_SKIP;
			headsetOnStop   = act == ACT_HEADSET_ON;
		} else {
			autoStopDelay   = 0;
			autoStopSpeaker = false;
			headsetStop     = false;
		}
	}

	private static void noAutoStart() {
		autoStart        = false;
		autoStartDelay   = 0;
		autoStartTimes   = 0;
		autoStartSpeaker = false;
		headsetStart     = false;
	}

}
