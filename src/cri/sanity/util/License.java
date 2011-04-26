package cri.sanity.util;

import cri.sanity.A;
import cri.sanity.Conf;
import cri.sanity.K;
import cri.sanity.R;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Handler;


public final class License
{
	public  static final String FULL_PKG      = "cri.sanitydonate";
	public  static final String FULL_SERVICE  = "cri.sanitydonate.DonateService";
	public  static final String FULL_ACTIVITY = "cri.sanitydonate.DonateActivity";
	public  static final int    CODE          = 69;
	private static final int RESULT_OK        = Activity.RESULT_OK;
	private static final int RESULT_CANCELED  = Activity.RESULT_CANCELED;
	private static final int RESULT_FAILED    = 666;
	//private static final int RESULT_ERROR     = 777;

	private static boolean completed = true;

	//---- public api

	public static final boolean isCompleted() { return completed; }

	public static final boolean isChecked() {
		if(!Conf.FULL && (!A.isFull() || !ver().equals(A.gets(K.LICVER)))) return false;
		return completed = true;
	}

	public static final boolean check()
	{
		final Activity act = Alert.activity;
		if(act == null) {
			completed = true;
			return false;
		}
		try {
			Intent i = new Intent(Intent.ACTION_MAIN);
			i.setClassName(FULL_PKG, FULL_ACTIVITY);
			act.startActivityForResult(i, CODE);
			completed = false;
			return true;
		} catch(ActivityNotFoundException e) {
			completed = true;
			return false;
		}
	}

	public static final boolean hasOld()
	{
		final Activity act = Alert.activity;
		if(act == null) return false;
		return act.startService(new Intent(FULL_SERVICE)) != null;
	}
	
	public static final boolean result(int res, Handler handler, final Runnable onAfter) throws IllegalStateException
	{
		if(completed) {
			if(hasOld()) handler.post(new Runnable(){ public void run(){ Alert.msg(A.rawstr(R.raw.license_upgrade)); }});
			throw new IllegalStateException();
		}
		switch(res) {
			case RESULT_OK      : return allow   (handler                     , onAfter);
			case RESULT_CANCELED: return disallow(handler, R.raw.license_retry, onAfter);
			case RESULT_FAILED  : return disallow(handler, R.raw.license_deny , onAfter);
			default             : return disallow(handler, R.raw.license_err  , onAfter);
		}
	}
	
	//---- private api
	
	private static final String ver() {
		final int b = A.beta();
		return A.verName() + (b>0? "b"+b : "");
	}

	private static boolean allow(Handler handler, final Runnable onAfter)
	{
		handler.post(new Runnable(){ public void run(){
			A.put(K.LICVER, ver());
			A.setFull(true);
			if(onAfter != null) onAfter.run();
		}});
		return true;
	}

	private static boolean disallow(Handler handler, final int msgRawId, final Runnable onAfter)
	{
		handler.post(new Runnable(){ public void run(){
			A.setFull(false);
			Alert.msg(
				A.rawstr(msgRawId),
				new Alert.Click(){ public void on(){
					dismiss();
					if(onAfter != null) onAfter.run();
				}},
				null,
				Alert.SIMPLE,
				false
			);
		}});
		return false;
	}
	
}
