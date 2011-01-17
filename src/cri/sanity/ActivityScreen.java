package cri.sanity;

import java.util.HashMap;
import java.util.Map;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


public class ActivityScreen extends PrefActivity
{
	private static final Click clickLogo = new Click(){ boolean on(){ return A.gotoMarketPub(); }};
	private static final Map<Class<?>,Integer> mapScreenPref = new HashMap<Class<?>,Integer>();
	private static final Map<Class<?>,Integer> mapScreenMenu = new HashMap<Class<?>,Integer>();
	private static final Map<Integer,Class<?>> mapMenuScreen = new HashMap<Integer,Class<?>>();

	//---- Activity override

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    final Integer i = mapScreenPref.get(getClass());
    if(i == null) return;
    addPreferencesFromResource(i.intValue());
		final Preference p = findPref("logo");
		if(p != null) {
			p.setTitle(getAppFullName());
			p.setSummary(R.string.app_desc);
			p.setPersistent(false);
			on(p, clickLogo);
		}
  }
	
	@Override
	public void onStart()
	{
		super.onStart();
		A.activity = this;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    final Integer i = mapScreenMenu.get(getClass());
    if(i != null) {
    	MenuItem mi = menu.findItem(i.intValue());
    	if(mi != null) mi.setEnabled(false);
    }
    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final int id = item.getItemId();
		Class<?> cls = mapMenuScreen.get(id);
		if(cls == null) return super.onOptionsItemSelected(item);
		if(!(this instanceof MainActivity)) finish();		// to avoid big activity stack, terminate current activity if it isn't the main one
		startActivity(new Intent(A.app(), cls));
		return true;
	}
	
	//---- public api

	public static final String getAppFullName() { 
		return A.tr(R.string.app) + "  v" + A.ver();
	}

	public static final boolean alertChangeLog() {
		A.alert(A.tr(R.string.changelog_title), A.tr(R.string.changelog_body));
		return true;
	}

	public final void screener(String key, final Class<?> cls, int idPref, int idMenu) {
		on(key, new Click(){ boolean on(){ startActivity(new Intent(A.app(), cls)); return true; }});
		screener(cls, idPref, idMenu);
	}
	public final void screener(final Class<?> cls, int idPref, int idMenu) {
		mapScreenPref.put(cls, idPref);
		if(idMenu <= 0) return;
		mapScreenMenu.put(cls, idMenu);
		mapMenuScreen.put(idMenu, cls);
	}
	public final void screener(final Class<?> cls, int idPref) {	screener(cls, idPref, 0); }

}
