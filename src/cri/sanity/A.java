package cri.sanity;

import java.io.File;
import java.util.Map;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.location.LocationManager;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.hardware.SensorManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.os.Environment;
import android.os.PowerManager;
import android.os.Vibrator;
import android.net.ConnectivityManager;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.widget.Toast;
//import android.os.SystemClock;


public final class A extends Application
{
	public static final boolean  DEBUG = Conf.DEBUG;
	public static final boolean  FULL  = Conf.FULL;
	public static final int      SDK = android.os.Build.VERSION.SDK_INT;
	public static final int      PRI = 1;
	public static final int      NID = 33;
	public static final int      ALERT_SIMPLE    = 0;
	public static final int      ALERT_OKCANC    = 1;
	public static final int      ALERT_YESNO     = 2;
	public static final int      ALERT_YESNOCANC = 3;
	public static final int      ALERT_OPENDEL   = 4;
	public static final int      ALERT_BAKRES    = 5;
	public static final int      DEF_ALERT       = ALERT_OKCANC;
	public static final int      LAB_OK   = R.string.ok;
	public static final int      LAB_CANC = R.string.canc;
	public static final int      LAB_YES  = R.string.yes;
	public static final int      LAB_NO   = R.string.no;
	public static final int      LAB_OPEN = R.string.open;
	public static final int      LAB_DEL  = R.string.del;
	public static final int      LAB_BAK  = R.string.backup;
	public static final int      LAB_REST = R.string.restore;
	public static final boolean  DEF_CANCELABLE = true;
	public static final String   DEF_STRING  = "";
	public static final boolean  DEF_BOOL    = false;
	public static final int      DEF_INT     = 0;
	public static final long     DEF_LONG    = DEF_INT;
	public static final float    DEF_FLOAT   = DEF_INT;
	public static final String   DEF_SBOOL   = Boolean.toString(DEF_BOOL);
	public static final String   DEF_SINT    = Integer.toString(DEF_INT );
	public static final String   DEF_SLONG   = DEF_SINT;
	public static final String   DEF_SFLOAT  = DEF_SINT;

	//---- data

	public  static Activity                 activity;
	
	private static A                        a;
	private static String                   name;
	private static Resources                resources;
	private static ContentResolver          ctxRes;
	private static PackageInfo              pkgInfo;
	private static SharedPreferences        prefs;
	private static SharedPreferences.Editor edit;
	private static Notification             notif;
	private static PendingIntent            notifIntent;

	private static NotificationManager      notifMan;
	private static AudioManager             audioMan;
	private static TelephonyManager         telMan;
	private static BluetoothAdapter         btAdapter;
	private static WifiManager              wifiMan;
	private static PowerManager             powerMan;
	private static LocationManager          locMan;
	private static SensorManager            sensorMan;
	private static ConnectivityManager      connMan;
	private static KeyguardManager          keyguardMan;
	private static DevicePolicyManager      devpolMan;

	//---- inner classes

	public static class Click implements DialogInterface.OnClickListener
	{
		public DialogInterface dlg;
		public int id;
		public void onClick(DialogInterface dlg, int id) {
			this.dlg = dlg;
			this.id  = id;
			on();
		}
		// just override this method (default action is to close dialog)
		public void on() { dlg.cancel(); }
	}

	//---- methods

	@Override
	public void onCreate() {
		a       = this;
		name    = A.tr(R.string.app);
		prefs   = PreferenceManager.getDefaultSharedPreferences(a);
		edit    = prefs.edit();
		pkgInfo = _pkgInfo();
	}

	//---- static methods

	// basic
	public static final A                         app() { return a;     }
	public static final String                   name() { return name;  }
	public static final SharedPreferences       prefs() { return prefs; }
	public static final SharedPreferences.Editor edit() { return edit;  }
	public static final String                    pkg() { return pkgInfo.packageName; }
	public static final Resources           resources() { if(resources==null) resources=a.getResources(); return resources; }
	public static final ContentResolver      resolver() { if(ctxRes==null) ctxRes=a.getContentResolver(); return ctxRes; }
	public static final PackageInfo           pkgInfo() { return pkgInfo; }
	public static final String                    ver() { return pkgInfo.versionName; }
	public static final String               fullName() { return name + "  v" + ver(); }

	// log
	//public static final int logd(Object o, String method)
	//                                    { return !DEBUG? 0 : Log.d(name, o.getClass().getSimpleName()+'.'+method); }
	//public static final int logd(Throwable t) { return !DEBUG? 0 : Log.wtf(name, t); }
	public static final int logd(String msg)  { return !DEBUG? 0 : Log.d(name, msg); }
	//public static final int logi(String msg)  { return !DEBUG? 0 : Log.i(name, msg); }
	//public static final int logv(String msg)  { return !DEBUG? 0 : Log.v(name, msg); }
	//public static final int logw(String msg)  { return !DEBUG? 0 : Log.w(name, msg); }
	//public static final int loge(String msg)  { return !DEBUG? 0 : Log.e(name, msg); }

	// misc
	//public static final boolean isEmpty(String s) { return s==null || s.trim().length()<=0; }

	public static final String tr(int id) { return (String)resources().getText(id); }
	public static final boolean empty(String s) { return s==null || s.length()<=0; }

	public static final long now() { return System.currentTimeMillis(); }
	//public static final long uptime() { return SystemClock.uptimeMillis(); }

	//public static final boolean gotoMarketPkg()             { return gotoMarketPkg(a.getPackageName()); }
	//public static final boolean gotoMarketPkg(String pkg)   { return gotoMarketUrl("search?q=pname:\""+pkg+'"'); }
	public static final boolean gotoMarketPub()               { return gotoMarketUrl("search?q=pub:\""+Conf.AUTHOR+'"'); }
	public static final boolean gotoMarketDetails()           { return gotoMarketDetails(a.getPackageName()); }
	public static final boolean gotoMarketDetails(String pkg) { return gotoMarketUrl("details?id="+pkg); }
	public static final boolean gotoMarketUrl(String query) {
		final boolean res = gotoUrl("market://"+query);
		if(!res) alert(A.tr(R.string.msg_market_err));
		return res;
	}
	public static final boolean gotoUrl(String url) {
		final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			a.startActivity(i);
			return true;
		} catch(ActivityNotFoundException e) {
			return false;
		}
	}
	
	public static final String sdcardDir() {
		final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + '/' + A.name();
		final File  file = new File(dir);
		return file.isDirectory()||file.mkdir() ? dir : null;
	}

	public static final String cleanFn(String fn) {
		for(String s : new String[]{ "?", ":", "*", "\""})
			fn = fn.replace(s, "");
		return fn;
	}
	
	// string conversion
	/*
	public int    s2b(String  s) { return Boolean.parseBoolean(s); }
	public int    s2i(String  s) { return Integer.parseInt    (s); }
	public long   s2l(String  s) { return Long   .parseLong   (s); }
	public float  s2f(String  s) { return Float  .parseFloat  (s); }
	public String b2s(boolean b) { return Boolean.toString    (b); }
	public String i2s(int     i) { return Integer.toString    (i); }
	public String l2s(long    l) { return Long   .toString    (l); }
	public String f2s(float   f) { return Float  .toString    (f); }
	*/

	// basic notification/interaction
	public static final void toast(Context ctx, String msg) { Toast.makeText(ctx, msg  , Toast.LENGTH_SHORT).show(); }
	public static final void toast(Context ctx, int  resId) { Toast.makeText(ctx, resId, Toast.LENGTH_SHORT).show(); }
	public static final void toast(String msg)              { Toast.makeText(a  , msg  , Toast.LENGTH_SHORT).show(); }
	public static final void toast(int  resId)              { Toast.makeText(a  , resId, Toast.LENGTH_SHORT).show(); }

	public static final void notify(String msg)                          { notify(name , msg, NID, now()); }
	public static final void notify(String msg, int id)                  { notify(name , msg, id , now()); }
	public static final void notify(String msg, long when)               { notify(name , msg, NID, when);  }
	public static final void notify(String msg, int id, long when)       { notify(name , msg, id , when);  }
	public static final void notify(String title, String msg)            { notify(title, msg, NID, now()); }
	public static final void notify(String title, String msg, int id)    { notify(title, msg, id , now()); }
	public static final void notify(String title, String msg, long when) { notify(title, msg, NID, when);  }
	public static final void notify(String title, String msg, int id, long when) {
		if(notif == null) {
			notif = new Notification(R.drawable.ic_bar, msg, when);
			final Intent i = new Intent(a, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			notifIntent = PendingIntent.getActivity(a, 0, i, 0);
		}	else {
			notif.tickerText = msg;
			notif.when       = when;
		}
		notif.setLatestEventInfo(a, title, msg, notifIntent);
		notifMan().notify(id, notif);
	}
	public static final void notifyCanc()       { notifMan().cancel(NID); }
	public static final void notifyCanc(int id) { notifMan().cancel( id); }
	public static final void notifyCancAll()    { notifMan().cancelAll(); }

	public static final AlertDialog alert(String msg) {
		return alert(name, msg, null, null, null, ALERT_SIMPLE, DEF_CANCELABLE);
	}
	public static final AlertDialog alert(String msg, Click pos, Click neg) {
		return alert(name, msg, pos, neg, null, DEF_ALERT, DEF_CANCELABLE);
	}
	public static final AlertDialog alert(String msg, Click pos, Click neg, int type) {
		return alert(name, msg, pos, neg, null, type, DEF_CANCELABLE);
	}
  public static final AlertDialog alert(String msg, Click pos, Click neg, int type, boolean cancelable) {
  	return alert(name, msg, pos, neg, null, type, cancelable);
  }
  public static final AlertDialog alert(String msg, Click pos, Click neg, Click neu, int type) {
  	return alert(name, msg, pos, neg, neu, type, DEF_CANCELABLE);
  }
  public static final AlertDialog alert(String msg, Click pos, Click neg, Click neu, int type, boolean cancelable) {
  	return alert(name, msg, pos, neg, neu, type, cancelable);
  }
  public static final AlertDialog alert(String title, String msg, Click pos, Click neg, Click neu, int type, boolean cancelable) {
  	return alert(title, msg, pos, neg, neu, type, cancelable, activity);
  }
  public static final AlertDialog alert(String title, String msg, Click pos, Click neg, Click neu, int type, boolean cancelable, Context ctx) {
  	int idPos=0, idNeg=0, idNeu=0;
  	switch(type) {
  		case ALERT_SIMPLE:    idPos = LAB_OK  ;                                     break;
  		case ALERT_OKCANC:    idPos = LAB_OK  ; idNeg = LAB_CANC;                   break;
  		case ALERT_YESNO:     idPos = LAB_YES ; idNeg = LAB_NO  ;                   break;
  		case ALERT_YESNOCANC: idPos = LAB_YES ; idNeu = LAB_NO  ; idNeg = LAB_CANC; break;
  		case ALERT_OPENDEL:   idPos = LAB_OPEN; idNeg = LAB_DEL ;                   break;
  		case ALERT_BAKRES:    idPos = LAB_BAK ; idNeg = LAB_REST;                   break;
  	}
		final AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
		adb.setIcon(R.drawable.ic_bar);
		adb.setTitle(title);
		adb.setMessage(msg);
		adb.setCancelable(cancelable);
		if(idPos > 0) adb.setPositiveButton(idPos, pos==null? new Click() : pos);
		if(idNeg > 0) adb.setNegativeButton(idNeg, neg==null? new Click() : neg);
		if(idNeu > 0) adb.setNeutralButton (idNeu, neu==null? new Click() : neu);
		return adb.show();
	}
  public static final AlertDialog alert(String title, String msg, Click pos, Click neg, Click neu, int type) {
  	return alert(title, msg, pos, neg, neu, type, DEF_CANCELABLE);
  }
  public static final AlertDialog alert(String title, String msg, Click pos, Click neg, int type, boolean cancelable) {
  	return alert(title, msg, pos, neg, null, type, cancelable);
  }
  public static final AlertDialog alert(String title, String msg, Click pos, Click neg, int type) {
  	return alert(title, msg, pos, neg, null, type, DEF_CANCELABLE);
  }
  public static final AlertDialog alert(String title, String msg, Click pos, Click neg) {
  	return alert(title, msg, pos, neg, null, DEF_ALERT, DEF_CANCELABLE);
  }
  public static final AlertDialog alert(String title, String msg) {
  	return alert(title, msg, null, null, null, ALERT_SIMPLE, DEF_CANCELABLE);
  }

  //---- preferences

	public static final boolean isEnabled () { return prefs.getBoolean(K.ENABLED , false); }
	public static final boolean isFull()     { return FULL || prefs.getBoolean(K.FULL, false); }
	public static final void    setFull()    { putc(K.FULL, true); }

	public static final boolean is(String key)                { return prefs.getBoolean(key, DEF_BOOL  ); }
	public static final boolean is(String key, boolean def)   { return prefs.getBoolean(key, def       ); }
	public static final String  gets(String key)              { return prefs.getString (key, DEF_STRING); }
	public static final String  gets(String key, String def)  { return prefs.getString (key, def       ); }
	public static final int     geti(String key)              { return prefs.getInt    (key, DEF_INT   ); }
	public static final int     geti(String key, int def)     { return prefs.getInt    (key, def       ); }
	public static final long    getl(String key)              { return prefs.getLong   (key, DEF_LONG  ); }
	public static final long    getl(String key, long def)    { return prefs.getLong   (key, def       ); }
	public static final float   getf(String key)              { return prefs.getFloat  (key, DEF_FLOAT ); }
	public static final float   getf(String key, float def)   { return prefs.getFloat  (key, def       ); }
	public static final int     getsi(String key)             { return Integer.parseInt  (prefs.getString(key, DEF_SINT  )); }
	public static final int     getsi(String key, String def) { return Integer.parseInt  (prefs.getString(key, def       )); }
	public static final long    getsl(String key)             { return Long   .parseLong (prefs.getString(key, DEF_SLONG )); }
	public static final long    getsl(String key, String def) { return Long   .parseLong (prefs.getString(key, def       )); }
	public static final float   getsf(String key)             { return Float  .parseFloat(prefs.getString(key, DEF_SFLOAT)); }
	public static final float   getsf(String key, String def) { return Float  .parseFloat(prefs.getString(key, def       )); }

	public static final Map<String,?> allPrefs() { return prefs.getAll(); }
	public static final boolean has (String key) { return prefs.contains(key); }
	public static final A       del (String key) { edit.remove(key);          return a; }
	public static final A       delc(String key) { edit.remove(key).commit(); return a; }

	public static final A put (String key, int    val) { edit.putInt   (key, val); return a; }
	public static final A putc(String key, int    val) { edit.putInt   (key, val).commit(); return a; }
	public static final A put (String key, long   val) { edit.putLong  (key, val); return a; }
	public static final A putc(String key, long   val) { edit.putLong  (key, val).commit(); return a; }
	public static final A put (String key, float  val) { edit.putFloat (key, val); return a; }
	public static final A putc(String key, float  val) { edit.putFloat (key, val).commit(); return a; }
	public static final A put (String key, String val) { edit.putString(key, val); return a; }
	public static final A putc(String key, String val) { edit.putString(key, val).commit(); return a; }

	public static final A putc(String key, Object val) { return put(key, val).commit(); }
	public static final A put (String key, Object val) {
		if(     val instanceof Boolean) edit().putBoolean(key, (Boolean)val);
		else if(val instanceof Integer) edit().putInt    (key, (Integer)val);
		else if(val instanceof Float  ) edit().putFloat  (key, (Float  )val);
		else if(val instanceof Long   ) edit().putLong   (key, (Long   )val);
		else                            edit().putString (key, val.toString());
		return a;
	}
	
	public static final A putcAll(Map<String,?> map) { return putAll(map).commit(); }
	public static final A putAll (Map<String,?> map) {
		for(Map.Entry<String,?> entry : map.entrySet())
			put(entry.getKey(), entry.getValue());
		return a; 
	}

	public static final A commit() { edit.commit(); return a; }

	//---- manage devices

	public static final NotificationManager notifMan() {
		if(notifMan == null) notifMan = (NotificationManager)a.getSystemService(Context.NOTIFICATION_SERVICE);
		return notifMan;
	}
	public static final AudioManager audioMan() { 
		if(audioMan == null) audioMan = (AudioManager)a.getSystemService(AUDIO_SERVICE);
		return audioMan;
	}
	public static final TelephonyManager telMan() {
		if(telMan == null) telMan = (TelephonyManager)a.getSystemService(TELEPHONY_SERVICE);
		return telMan;
	}
	public static final WifiManager wifiMan() {
		if(wifiMan == null) wifiMan = (WifiManager)a.getSystemService(WIFI_SERVICE);
		return wifiMan;
	}
	public static final ConnectivityManager connMan() {
		if(connMan == null) connMan = (ConnectivityManager)a.getSystemService(CONNECTIVITY_SERVICE);
		return connMan;
	}
	public static final KeyguardManager keyguardMan() {
		if(keyguardMan == null) keyguardMan = (KeyguardManager)a.getSystemService(KEYGUARD_SERVICE);
		return keyguardMan;
	}
	public static final DevicePolicyManager devpolMan() {
		if(devpolMan == null) devpolMan = (DevicePolicyManager)a.getSystemService(DEVICE_POLICY_SERVICE);
		return devpolMan;
	}
	public static final PowerManager powerMan() {
		if(powerMan == null) powerMan = (PowerManager)a.getSystemService(POWER_SERVICE);
		return powerMan;
	}
	public static final LocationManager locMan() {
		if(locMan == null) locMan = (LocationManager)a.getSystemService(LOCATION_SERVICE);
		return locMan;
	}
	public static final SensorManager sensorMan() {
		if(sensorMan == null) sensorMan = (SensorManager)a.getSystemService(SENSOR_SERVICE);
		return sensorMan;
	}
	public static final BluetoothAdapter btAdapter() {
		if(btAdapter == null) btAdapter = BluetoothAdapter.getDefaultAdapter();
		return btAdapter;
	}
	public static final void vibrate() {
		((Vibrator)A.app().getSystemService(VIBRATOR_SERVICE)).vibrate(Conf.VIBRATE_TIME);
	}

	//---- private api
	
	private static PackageInfo _pkgInfo() {
		try {
			return a.getPackageManager().getPackageInfo(a.getPackageName(), 0);
		} catch(NameNotFoundException e) {
			return null;
		}
	}

}
