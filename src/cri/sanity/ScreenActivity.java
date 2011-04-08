package cri.sanity;

import java.util.HashMap;
import java.util.Map;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import cri.sanity.util.*;
import cri.sanity.screen.*;


public abstract class ScreenActivity extends PrefActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
	private static final String AUTHOR     = "Cristiano Tagliamonte";
	private static final String FIRST_YEAR = "2011";

	private static final Map<Class<?>,Integer> mapScreenPref   = new HashMap<Class<?>,Integer>();
	private static final Map<Class<?>,Integer> mapScreenWidget = new HashMap<Class<?>,Integer>();
	private static final Map<Class<?>,Integer> mapScreenMenu   = new HashMap<Class<?>,Integer>();
	private static final Map<Integer,Class<?>> mapMenuScreen   = new HashMap<Integer,Class<?>>();
	private static final Map<String,Object>    mapSkipKeys     = P.skipKeysMap();

	private   static boolean grant = false;
	protected static boolean nagDefault = true;
	protected boolean nag;
	protected boolean shortcut;
	protected boolean skipAllKeys = false;
	protected boolean secure = true;

	//---- Activity override

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    nag      = nagDefault;
    shortcut = getIntent().getIntExtra(ShortcutActivity.EXTRA_KEY, 0) > 0;		// called through shortcut?
    if(mapMenuScreen.isEmpty() && !isMainActivity()) screenerAll();
    final Integer i = mapScreenPref.get(getClass());
    if(i == null) return;
    addPreferencesFromResource(i);
		final Preference p = pref("logo");
		if(p == null) return;
		p.setTitle(fullName());
		p.setSummary(appDesc());
		p.setPersistent(false);
		final Integer w = mapScreenWidget.get(getClass());
		if(w != null) p.setWidgetLayoutResource(w);
		on(p, new Click(){ public boolean on(){ return Goto.marketSearchPub(AUTHOR); }});
  }

	@Override
	public void onResume()
	{
		super.onResume();
    protect();
		nag();
		A.prefs().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		A.prefs().unregisterOnSharedPreferenceChangeListener(this);
		nag = nagDefault;
		if(!shortcut) return;
		ungrant();
		finish();		// finish to allow other screens to be called through shortcut (otherwise this screen will be shown again)
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    final Integer i = mapScreenMenu.get(getClass());
    if(i!=null && menu.findItem(i)!=null)
    	menu.removeItem(i);
    MenuItem m = menu.add(R.string.help);
    m.setIcon(android.R.drawable.ic_menu_help);
    m.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Alert.msg(A.s(R.string.help), getGroupText(getPreferenceScreen(), false));
				return true;
			}
			private String getGroupText(PreferenceGroup pg, boolean all) {
				String msg = "";
				final int n = pg.getPreferenceCount();
				for(int i=0; i<n; i++) {
					final Preference p = pg.getPreference(i);
					if(p instanceof PreferenceGroup)
						msg += "\n** "+p.getTitle().toString().toUpperCase()+"\n\n"+getGroupText((PreferenceGroup)p, true);
					else if(all || !p.getKey().equals("logo")) {
						msg += "- "+p.getTitle()+".\n"+p.getSummary()+'\n';
						try { msg += A.s(A.rstring("help_"+p.getKey()))+'\n'; }
						catch(Exception e) {}
						msg += '\n';
					}
				}
				return msg;
			}
		});
    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final int      id  = item.getItemId();
		final Class<?> cls = mapMenuScreen.get(id);
		if(cls == null) return super.onOptionsItemSelected(item);
		if(!isMainActivity()) finish();		// to avoid big activity stack, terminate current activity if it isn't the main one
		startActivity(new Intent(A.app(), cls));
		return true;
	}

	//---- public api

	public static final String fullName() {
		String v = A.name() + "  v" + A.ver();
		if(Conf.BETA > 0) { v += " beta "; if(Conf.BETA > 1) v += Conf.BETA; }
		return v;
	}
	public static final String appDesc() {
		String year = DateFormat.format("yyyy", A.time()).toString();
		if(!year.equals(FIRST_YEAR)) year = FIRST_YEAR+'-'+year;
		return A.s(R.string.app_desc)+"\n(C) "+year+", "+AUTHOR+'.';
	}

	public static final boolean alertChangeLog() {
		Alert.msg(A.s(R.string.changelog_title), A.rawstr(R.raw.changelog), Alert.NONE);
		return true;
	}

	public final void screener(String key, final Class<?> cls, int idPref, int idMenu, int widget) {
		screener(pref(key), cls, idPref, idMenu, widget);
	}
	public final void screener(Preference p, final Class<?> cls, int idPref, int idMenu, int widget) {
		mapScreenWidget.put(cls, widget);
		screener(p, cls, idPref, idMenu);
	}
	public final void screener(String key, final Class<?> cls, int idPref, int idMenu) {
		screener(pref(key), cls, idPref, idMenu);
	}
	public final void screener(Preference p, final Class<?> cls, int idPref, int idMenu) {
		screener(cls, idPref);
		if(idMenu > 0) {
			mapScreenMenu.put(cls, idMenu);
			mapMenuScreen.put(idMenu, cls);
		}
		if(p == null) return;
		on(p, new Click(){ public boolean on(){
			Intent i = new Intent(A.app(), cls);
			startActivity(i);
			return true;
		}});
	}
	public static final void screener(final Class<?> cls, int idPref) {
		mapScreenPref.put(cls, idPref);
	}
	public static final void screener(final Class<?> cls, int idPref, int widget) {
		mapScreenWidget.put(cls, widget);
		mapScreenPref  .put(cls, idPref);
	}

	//---- protected api

	protected final void screenerAll()
	{
		// all preferences screens
  	screener("screen_general"  , GeneralActivity.class  , R.xml.prefs_general  , R.id.menu_general  , R.layout.img_general);
  	screener("screen_devices"  , DevicesActivity.class  , R.xml.prefs_devices  , R.id.menu_devices  , R.layout.img_devices);
  	screener("screen_proximity", ProximityActivity.class, R.xml.prefs_proximity, R.id.menu_proximity, R.layout.img_proximity);
  	screener("screen_speaker"  , SpeakerActivity.class  , R.xml.prefs_speaker  , R.id.menu_speaker  , R.layout.img_speaker);
  	screener("screen_volume"   , VolumeActivity.class   , R.xml.prefs_volume   , R.id.menu_vol      , R.layout.img_vol);
  	screener("screen_record"   , RecordActivity.class   , R.xml.prefs_record   , R.id.menu_rec      , R.layout.img_rec);
  	screener("screen_block"    , BlockerActivity.class  , R.xml.prefs_block    , R.id.menu_block    , R.layout.img_block);
  	screener("screen_tts"      , TtsActivity.class      , R.xml.prefs_tts      , R.id.menu_tts      , R.layout.img_tts);
  	screener("screen_urgent"   , UrgentActivity.class   , R.xml.prefs_urgent   , R.id.menu_urgent   , R.layout.img_urgent);
  	screener("screen_answer"   , AnswerActivity.class   , R.xml.prefs_answer   , R.id.menu_answer   , R.layout.img_answer);
  	screener("screen_notify"   , NotifyActivity.class   , R.xml.prefs_notify   , R.id.menu_notify   , R.layout.img_notify);
  	screener("screen_about"    , AboutActivity.class    , R.xml.prefs_about    , R.id.menu_about    , R.layout.img_about);
	}

	protected void nag()
	{
		if(!nag || A.isFull()) return;
		final long now = A.time();
		try {
			if(now-A.getl(K.NAG) < Conf.NAG_TIMEOUT) return;
		} catch(Exception e) {
			A.del(K.NAG);
		}
		A.putc(K.NAG, now);
		Alert.msg(
			A.rawstr(R.raw.nag),
			new Alert.Click(){ public void on(){ nag = true; Goto.marketDetails(License.FULL_PKG); }},
			new Alert.Click(){ public void on(){ nag = true; }}
		);
		nag = false;
	}

	protected void ungrant()
	{
		grant = false;
		Alert.resetPwd();
	}

	//---- private api

	private void protect()
	{
    if(grant || !secure) return;
    final String pwd = A.gets(K.PWD);
    if(pwd.length() <= 0) return;
  	Alert.pwdAsk(
  		new Alert.Edited(){ public void on(String text){
  			if(text.equals(pwd)) grant = true;
  			else protect();
  		}},
  		new Alert.Click(){ public void on(){ finish(); }}
  	);
	}
	
	//---- OnSharedPreferenceChangeListener implementation

	public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
	{
		if(skipAllKeys || mapSkipKeys.containsKey(key) || key.startsWith("priv_")) return;
		if(A.has(K.PRF_NAME)) A.delc(K.PRF_NAME);
	}

}
