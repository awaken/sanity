package cri.sanity.ghost;

import java.lang.reflect.Method;
import android.telephony.TelephonyManager;
import cri.sanity.A;


public final class TelMan extends GhostObj
{
	public TelMan()
	{
		try {
			final TelephonyManager tm = A.telMan();
			final Method m = tm.getClass().getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			init(m.invoke(tm));
		} catch(Exception e) {
			init(null);
		}
	}

	// end call or go to the home screen
	public final boolean endCall() { return callBool("endCall"); }
	// answer the currently ringing call
	public final void answerRingingCall() { call("answerRingingCall"); }
	// silence the ringer if an incoming call is currently ringing. (if vibrating, stop the vibrator also.)
	// it's safe to call this if the ringer has already been silenced, or
	// even if there's no incoming call.  (if so, this method will do nothing.)
	public final void silenceRinger() { call("silenceRinger"); }

	// enable mobile data (2g/3g)
	public final boolean  enableDataConnectivity() { return callBool( "enableDataConnectivity"); }
	// disable mobile data (2g/3g)
	public final boolean disableDataConnectivity() { return callBool("disableDataConnectivity"); }

}
