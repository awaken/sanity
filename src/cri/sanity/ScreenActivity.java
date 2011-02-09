package cri.sanity;

import cri.sanity.screen.*;

import java.util.HashMap;
import java.util.Map;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


public class ScreenActivity extends PrefActivity
{
	private static final Click clickLogo = new Click(){ public boolean on(){ return A.gotoMarketPub(); }};
	private static final Map<Class<?>,Integer> mapScreenPref = new HashMap<Class<?>,Integer>();
	private static final Map<Class<?>,Integer> mapScreenMenu = new HashMap<Class<?>,Integer>();
	private static final Map<Integer,Class<?>> mapMenuScreen = new HashMap<Integer,Class<?>>();

	//---- Activity override

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    if(mapMenuScreen.isEmpty() && !isMainActivity()) screenerAll();
    final Integer i = mapScreenPref.get(getClass());
    if(i == null) return;
    addPreferencesFromResource(i);
		final Preference p = pref(K.LOGO);
		if(p != null) {
			p.setTitle(A.fullName());
			p.setSummary(A.tr(R.string.app_desc)+"\n"+A.tr(R.string.app_copy));
			p.setPersistent(false);
			on(p, clickLogo);
		}
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
				A.alert(A.tr(R.string.help), getGroupText(getPreferenceScreen(), false));
				return true;
			}
			private String getGroupText(PreferenceGroup pg, boolean all) {
				String msg = "";
				final int n = pg.getPreferenceCount();
				for(int i=0; i<n; i++) {
					final Preference p = pg.getPreference(i);
					if(p instanceof PreferenceGroup)
						msg += "\n** "+p.getTitle().toString().toUpperCase()+"\n\n"+getGroupText((PreferenceGroup)p, true);
					else if(all || !p.getKey().equals(K.LOGO))
						msg += "- "+p.getTitle()+".\n"+p.getSummary()+"\n\n";
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

	public static final boolean alertChangeLog() {
		A.alert(A.tr(R.string.changelog_title), A.tr(R.string.changelog_body));
		return true;
	}

	public final void screener(String key, final Class<?> cls, int idPref, int idMenu) {
		screener(pref(key), cls, idPref, idMenu);
	}
	public final void screener(Preference p, final Class<?> cls, int idPref, int idMenu) {
		screener(cls, idPref, idMenu);
		if(p != null)
			on(p, new Click(){ public boolean on(){ startActivity(new Intent(A.app(), cls)); return true; }});
	}
	public static final void screener(final Class<?> cls, int idPref, int idMenu) {
		screener(cls, idPref);
		if(idMenu <= 0) return;
		mapScreenMenu.put(cls, idMenu);
		mapMenuScreen.put(idMenu, cls);
	}
	public static final void screener(final Class<?> cls, int idPref) { mapScreenPref.put(cls, idPref); }

	//---- protected api

	protected final void screenerAll()
	{
		// all preferences screens
  	screener(K.SCREEN_GENERAL  , GeneralActivity.class  , R.xml.prefs_general  , R.id.menu_general);
  	screener(K.SCREEN_DEVICES  , DeviceActivity.class   , R.xml.prefs_devices  , R.id.menu_devices);
  	screener(K.SCREEN_PROXIMITY, ProximityActivity.class, R.xml.prefs_proximity, R.id.menu_proximity);
  	screener(K.SCREEN_SPEAKER  , SpeakerActivity.class  , R.xml.prefs_speaker  , R.id.menu_speaker);
  	screener(K.SCREEN_VOLUME   , VolumeActivity.class   , R.xml.prefs_volume   , R.id.menu_vol);
  	screener(K.SCREEN_RECORD   , RecordActivity.class   , R.xml.prefs_record   , R.id.menu_rec);
  	screener(K.SCREEN_NOTIFY   , NotifyActivity.class   , R.xml.prefs_notify   , R.id.menu_notify);
  	screener(K.SCREEN_ABOUT    , AboutActivity.class    , R.xml.prefs_about    , R.id.menu_about);
	}

}
