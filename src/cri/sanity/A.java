package cri.sanity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Vector;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.location.LocationManager;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.ClipboardManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.net.ConnectivityManager;
import android.widget.Toast;
import android.text.format.DateFormat;


public final class A extends Application
{
	public  static final boolean DEBUG = android.util.Config.DEBUG;
	public  static final int     SDK   = android.os.Build.VERSION.SDK_INT;
	private static final int     NID   = 1;

	//---- inner classes

	public abstract static class Async extends AsyncTask<Void,Void,Void> {
		public Void doInBackground(Void ...v) {
			run();
			return null;
		}
		public abstract void run();
	}

	//---- data

	private static A                        a;
	private static String                   name;
	private static Resources                resources;
	private static ContentResolver          resolver;
	private static PackageInfo              pkgInfo;
	private static SharedPreferences        prefs;
	private static SharedPreferences.Editor edit;
	private static Notification             notif;
	private static PendingIntent            notifIntent;
	private static boolean                  full;

	private static NotificationManager      notifMan;
	private static AudioManager             audioMan;
	private static TelephonyManager         telMan;
	private static BluetoothAdapter         btAdapter;
	private static WifiManager              wifiMan;
	private static SensorManager            sensorMan;
	private static PowerManager             powerMan;
	private static LocationManager          locMan;
	private static DevicePolicyManager      devpolMan;

	//---- methods

	@Override
	public void onCreate() {
		a     = this;
		name  = getString(R.string.app);
		prefs = PreferenceManager.getDefaultSharedPreferences(a);
		edit  = prefs.edit();
		try { full = Conf.FULL || prefs.getBoolean(K.FULL, false); }
		catch(Exception e) { setFull(false); }
		try { pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0); }
		catch(NameNotFoundException e) {}
	}

	//---- static methods

	// basic
	public static final A                         app() { return a;     }
	public static final String                   name() { return name;  }
	public static final SharedPreferences       prefs() { return prefs; }
	public static final SharedPreferences.Editor edit() { return edit;  }
	public static final String                    pkg() { return pkgInfo.packageName; }
	public static final Resources           resources() { if(resources==null) resources=a.getResources();       return resources; }
	public static final ContentResolver      resolver() { if(resolver ==null) resolver =a.getContentResolver(); return resolver;  }
	//public static final PackageInfo           pkgInfo() { return pkgInfo; }
	public static final String                verName() { return pkgInfo.versionName; }
	public static final int                   verCode() { return pkgInfo.versionCode; }
	public static final int                      beta() { return pkgInfo.versionCode % 100; }

	// log
	//public static final int logd(Object o, String method) { return Log.d(name, o.getClass().getSimpleName()+'.'+method); }
	public static final int logd(Throwable t) { return Log.wtf(name, t); }
	public static final int logd(String  msg) { return Log.d(name, msg); }
	//public static final int logi(String msg) { return Log.i(name, msg); }
	//public static final int logv(String msg) { return Log.v(name, msg); }
	//public static final int logw(String msg) { return Log.w(name, msg); }
	//public static final int loge(String msg) { return Log.e(name, msg); }

	// misc
	public static final String s(int id) { return a.getString(id); }
	//public static final String s(int id) { return (String)resources().getText(id); }

	public static final boolean empty(String s) { return s==null || s.length()<=0; }

	//public static final Vector<String> split(String sep, String str) { return split(sep, str, 16); }
	public static final Vector<String> split(String sep, String str, int capacity) {
		Vector<String> split = new Vector<String>(capacity);
		final int n = str.length();
		if(n <= 0) return split;
		int p = 0;
		int q = str.indexOf(sep);
		while(q > 0) {
			split.add(str.substring(p, q));
			p = q + 1;
			if(p >= n) {
				split.add("");
				return split;
			}
			q = str.indexOf(sep, p);
		}
		split.add(str.substring(p));
		return split;
	}

	public static final int rstring(String field) throws IllegalAccessException, NoSuchFieldException {
		return R.string.class.getDeclaredField(field).getInt(R.string.class);
	}
	//public static final int rarray(String field) throws IllegalAccessException, NoSuchFieldException {
	//	return R.array.class.getDeclaredField(field).getInt(R.array.class);
	//}

	public static final String rawstr(int resId) {
		try {
			InputStream    is = resources().openRawResource(resId);
			BufferedReader br = new BufferedReader(new InputStreamReader(is), 8192);
			StringBuilder  s  = new StringBuilder(1024);
			String line;
			while((line = br.readLine()) != null)
				s.append(line).append('\n');
			br.close();
			is.close();
			return s.toString();
    } catch(IOException e) {
    	return null;
    }
  }

	public static final long time() { return System.currentTimeMillis(); }

	public static final String date()          { return date(time()); }
	public static final String date(long time) { return DateFormat.format(Conf.DATE_PATTERN, time).toString(); }

	public static final String sdcardDir() {
		File file = Environment.getExternalStorageDirectory();
		if(!file.canWrite()) return null;
		final String dir = file.getAbsolutePath() + '/' + name;
		file = new File(dir);
		return file.isDirectory()||file.mkdir() ? dir : null;
	}

	public static final String cleanFn(String fn, boolean slashClean) {
		for(String s : new String[]{ "?", ":", "*", "\"", "\\", ";", "&", "<", ">", "\r", "\n" })
			fn = fn.replace(s, "");
		if(slashClean) fn = fn.replace("/", "");
		return fn.trim();
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

	public static final void notify(String msg)                          { notify(name , msg, NID, R.drawable.ic_bar, time()); }
	public static final void notify(String msg, int id)                  { notify(name , msg, id , R.drawable.ic_bar, time()); }
	public static final void notify(String msg, long when)               { notify(name , msg, NID, R.drawable.ic_bar, when);  }
	public static final void notify(String msg, int id, long when)       { notify(name , msg, id , R.drawable.ic_bar, when);  }
	public static final void notify(String msg, int id, int icon)        { notify(name , msg, id , icon             , time()); }
	public static final void notify(String title, String msg)            { notify(title, msg, NID, R.drawable.ic_bar, time()); }
	public static final void notify(String title, String msg, int id)    { notify(title, msg, id , R.drawable.ic_bar, time()); }
	public static final void notify(String title, String msg, long when) { notify(title, msg, NID, R.drawable.ic_bar, when);  }
	public static final void notify(String title, String msg, int id, int icon) { notify(title, msg, id, icon, time()); }
	public static final void notify(String title, String msg, int id, int icon, long when) {
		if(notif == null) {
			notif = new Notification(icon, msg, when);
			Intent i = new Intent(a, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			notifIntent = PendingIntent.getActivity(a, 0, i, 0);
		}	else {
			notif.tickerText = msg;
			notif.when       = when;
			notif.icon       = icon;
		}
		notif.setLatestEventInfo(a, title, msg, notifIntent);
		notifMan().notify(id, notif);
	}
	public static final void notifyCanc()       { notifMan().cancel(NID); }
	public static final void notifyCanc(int id) { notifMan().cancel( id); }
	//public static final void notifyCancAll()    { notifMan().cancelAll(); }

  //---- preferences

	public static final boolean isEnabled() { return prefs.getBoolean(K.ENABLED, false); }
	public static final boolean isFull()    { return full; }
	public static final void setFull(boolean full) { putc(K.FULL, A.full=full); }

	public static final boolean is(String key)                { return prefs.getBoolean(key, false); }
	//public static final boolean is(String key, boolean def)   { return prefs.getBoolean(key, def  ); }
	public static final String gets(String key)              { return prefs.getString (key, ""   ); }
	//public static final String gets(String key, String def)  { return prefs.getString (key, def  ); }
	public static final int    geti(String key)              { return prefs.getInt    (key, 0    ); }
	//public static final int    geti(String key, int def)     { return prefs.getInt    (key, def  ); }
	public static final long   getl(String key)              { return prefs.getLong   (key, 0l   ); }
	//public static final long   getl(String key, long def)    { return prefs.getLong   (key, def  ); }
	//public static final float  getf(String key)              { return prefs.getFloat  (key, 0    ); }
	//public static final float  getf(String key, float def)   { return prefs.getFloat  (key, def  ); }
	public static final int    getsi(String key)             { return Integer.parseInt(prefs.getString(key, "0")); }
	//public static final int    getsi(String key, String def) { return Integer.parseInt  (prefs.getString(key, def)); }
	//public static final long   getsl(String key)             { return Long   .parseLong (prefs.getString(key, "0")); }
	//public static final long   getsl(String key, String def) { return Long   .parseLong (prefs.getString(key, def)); }
	//public static final float  getsf(String key)             { return Float  .parseFloat(prefs.getString(key, "0")); }
	//public static final float  getsf(String key, String def) { return Float  .parseFloat(prefs.getString(key, def)); }
	public static final Object get(String key) {
		try { return geti(key); } catch(Exception ei) {
		try { return getl(key); } catch(Exception el) {
		try { return is(key);   } catch(Exception eb) {
		try { return gets(key); } catch(Exception es) {}}}}
    return null;
	}
	
	public static final boolean has (String key) { return prefs.contains(key); }
	public static final A       del (String key) { edit.remove(key);          return a; }
	public static final A       delc(String key) { edit.remove(key).commit(); return a; }

	public static final A put (String key, boolean val) { edit.putBoolean(key, val);          return a; }
	public static final A putc(String key, boolean val) { edit.putBoolean(key, val).commit(); return a; }
	public static final A put (String key, int     val) { edit.putInt    (key, val);          return a; }
	public static final A putc(String key, int     val) { edit.putInt    (key, val).commit(); return a; }
	public static final A put (String key, long    val) { edit.putLong   (key, val);          return a; }
	public static final A putc(String key, long    val) { edit.putLong   (key, val).commit(); return a; }
	public static final A put (String key, float   val) { edit.putFloat  (key, val);          return a; }
	public static final A putc(String key, float   val) { edit.putFloat  (key, val).commit(); return a; }
	public static final A put (String key, String  val) { edit.putString (key, val);          return a; }
	public static final A putc(String key, String  val) { edit.putString (key, val).commit(); return a; }

	public static final A putc(String key, Object val) { return put(key, val).commit(); }
	public static final A put (String key, Object val) {
		if(     val instanceof Boolean) edit.putBoolean(key, (Boolean)val);
		else if(val instanceof Integer) edit.putInt    (key, (Integer)val);
		else if(val instanceof Float  ) edit.putFloat  (key, (Float  )val);
		else if(val instanceof Long   ) edit.putLong   (key, (Long   )val);
		else                            edit.putString (key, val.toString());
		return a;
	}

	public static final A putAll (Map<String,?> map) {
		for(Map.Entry<String,?> e : map.entrySet())
			put(e.getKey(), e.getValue());
		return a;
	}

	public static final A commit() { edit.commit(); return a; }

	//---- manage devices

	public static final NotificationManager notifMan() {
		if(notifMan == null) notifMan = (NotificationManager)a.getSystemService(NOTIFICATION_SERVICE);
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
		return (ConnectivityManager)a.getSystemService(CONNECTIVITY_SERVICE);
	}
	public static final KeyguardManager keyguardMan() {
		return (KeyguardManager)a.getSystemService(KEYGUARD_SERVICE);
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
	public static final AlarmManager alarmMan() {
		return (AlarmManager)a.getSystemService(ALARM_SERVICE);
	}
	public static final ClipboardManager clipMan() {
		return (ClipboardManager)a.getSystemService(CLIPBOARD_SERVICE);
	}
	public static final BluetoothAdapter btAdapter() {
		if(btAdapter == null) btAdapter = BluetoothAdapter.getDefaultAdapter();
		return btAdapter;
	}

	public static final Sensor sensorProxim() {
		final SensorManager sm = sensorMan();
		return sm==null ? null : sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
	}

}
