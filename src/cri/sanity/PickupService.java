package cri.sanity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import cri.sanity.util.*;


public final class PickupService extends Service implements Runnable
{
	private static final int OFFHOOK_TIMEOUT  =   6*1000;
	private static final int TASK_TIMEOUT     = 120*1000;
	private static final int TASK_ID          = Task.idNew();

	private static          boolean running   = false;
	private static volatile boolean terminate = false;	
	private static volatile boolean notified  = false;
	private static final    Object  monitor   = new Object();

	//---- static api

	public static void start() {
		if(running) return;
		terminate = false;
		notified  = false;
		final Context ctx = A.app();
		ctx.startService(new Intent(ctx, PickupService.class));
	}

	public static void stop() {
		if(!running) return;
		terminate = true;
		synchronized(monitor) { monitor.notifyAll(); }
		final Context ctx = A.app();
		ctx.stopService(new Intent(ctx, PickupService.class));
		Task.stop(TASK_ID);
	}

	public static void notifyOffhook() {
		synchronized(monitor) {
			notified = true;
			monitor.notifyAll();
		}
	}

	//---- Service implementation

	@Override
	public IBinder onBind(Intent i) { return null; }

	@Override
	public int onStartCommand(Intent i, int flags, int id) {
		if(i==null || running) return START_STICKY;
		running = true;
		new Thread(this).start();
		new Task(){ public void run(){ stop(); }}.exec(TASK_ID, TASK_TIMEOUT);
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		running = false;
		super.onDestroy();
	}

	//---- Runnable implementation

	@Override
	public void run() {
		Process           logProc = null;
    BufferedReader         br = null;
    final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    final Calendar        cal = Calendar.getInstance();
    final String         year = cal.get(Calendar.YEAR) + "-";
    final long            now = A.time();
    synchronized(monitor) {
    	if(!notified) {
	    	try { monitor.wait(OFFHOOK_TIMEOUT); } catch(InterruptedException e) {}
    		if(!notified || terminate) { stop(); return; }
			}
      notified = false;
    }
    try {
    	logProc = Runtime.getRuntime().exec("logcat -b radio -v time");
      br = new BufferedReader(new InputStreamReader(logProc.getInputStream()), 8192);
      for(String line; !terminate && (line=br.readLine())!=null;) {
      	if(!line.contains("GET_CURRENT_CALLS") || !line.contains("ACTIVE")) continue;
      	try {
        	cal.setTimeInMillis(df.parse(year + line.substring(0, 14)).getTime());
      		if(cal.getTimeInMillis() < now) continue;
      		Vibra.vibra();
       		break;
      	} catch(Exception e) {}
      }
    } catch(Exception e) {}
  	if(br      != null) try { br.close();        } catch(Exception e) {}
    if(logProc != null) try { logProc.destroy(); } catch(Exception e) {}
  	stop();
	}

}
