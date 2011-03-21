package cri.sanity.screen;

import cri.sanity.*;
import cri.sanity.util.*;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.CheckBoxPreference;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;


public class GroupsActivity extends ScreenActivity
{
	private static final String SEP = FilterActivity.SEP;

	private PreferenceCategory prefGroup;
	private String sect;
	private boolean changed;

	//---- overriding

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		skipAllKeys = true;
		screener(GroupsActivity.class, R.xml.filter_groups, R.layout.img_groups);
		super.onCreate(savedInstanceState);
		prefGroup = (PreferenceCategory)pref("filter_groups");
		Intent i  = getIntent();
		sect      = i.getStringExtra(FilterActivity.EXTRA_SECT );
		String t  = i.getStringExtra(FilterActivity.EXTRA_TITLE);
		if(!A.empty(t)) prefGroup.setTitle(prefGroup.getTitle()+"  ("+t+')');
		readGroups();
		changed = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.contacts, menu);
    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()) {
			case R.id.selall : selall();  break;
			case R.id.selnone: selnone(); break;
			case R.id.canc   : canc();    break;
			default: return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	@Override
	public void onBackPressed()
	{
		if(isFinishing()) return;
		Intent i = new Intent();
		i.putExtra(FilterActivity.EXTRA_SECT, saveList());
		setResult(RESULT_OK, i);
		super.onBackPressed();
	}
	
	//---- private api

	private void readGroups()
	{
		String[][] groups = Contacts.fullGroups();
		final int n = groups.length;
		if(n <= 0) return;
		final String sysgroup = "* "+A.s(R.string.sysgroup)+" *";
		Map<String,Pref> prefs = new HashMap<String,Pref>(n);
		Pref[] arr = new Pref[n];
		// read all groups
		int i = 0;
		for(String[] g : groups) {
			String id = g[Contacts.GRP_ID];
			Pref   p  = new Pref(id, g[Contacts.GRP_TITLE]);
			prefs.put(id, p);
			arr[i++] = p;
			String sum = g[Contacts.GRP_ACC];
			if(!A.empty(g[Contacts.GRP_SID])) {
				if(sum.length() > 0) sum += '\n';
				sum += sysgroup;
			}
			p.setSummary(sum);
		}
		// first, put all already selected groups
		for(String id : A.gets(keyAll()).split(SEP)) {
			Pref p = prefs.get(id);
			if(p == null) continue;
			prefGroup.addPreference(p);
			p.setChecked(true);
		}
		// after, put all remaining groups
		Arrays.sort(arr);
		for(Pref p : arr)
			if(!p.isChecked())
				prefGroup.addPreference(p);
	}

	private int saveList()
	{
		if(changed)
			for(String id : A.gets(keyAll()).split(SEP))
				A.del(keySect(id));
		String ids = "";
		int k = 0;
		int n = prefGroup.getPreferenceCount();
		if(n > 0) {
			final StringBuilder sb = new StringBuilder(256);
			boolean first = true;
			for(int i=0; i<n; i++) {
				Pref pref = (Pref)prefGroup.getPreference(i);
				if(!pref.isChecked()) continue;
				++k;
				if(!changed) continue;
				String id = pref.id;
				A.put(keySect(id), true);
				if(first) first = false;
				else      sb.append(SEP);
				sb.append(id);
			}
			if(n > k) ids = sb.toString();
		}
		if(changed)
			A.put(keyAll(), ids).putc(keyCount(), k);
		return k;		// return number of selected groups
	}

	private String keyAll()            { return "filter_groups_"+sect;       }
	private String keyCount()          { return "filter_groups_count_"+sect; }
	private String keySect(String val) { return "filter_group_"+val+sect;    }

	private void selall()
	{
		final int n = prefGroup.getPreferenceCount();
		if(n < 1) return;
		for(int i=0; i<n; i++)
			((Pref)prefGroup.getPreference(i)).setChecked(true);
		A.toast(String.format(A.s(R.string.msg_selected_all), n+""));
		changed = true;
	}
	
	private void selnone()
	{
		final int n = prefGroup.getPreferenceCount();
		if(n < 1) return;
		for(int i=0; i<n; i++)
			((Pref)prefGroup.getPreference(i)).setChecked(false);
		changed = true;
	}
	
	private void canc()
	{
		if(!changed) { finish(); return; }
		Alert.msg(
			A.s(R.string.ask_canc_all),
			new Alert.Click(){ public void on(){ changed = false; dismiss(); finish(); }},
			null,
			Alert.OKCANC
		);
	}

	//---- inner class

	private class Pref extends CheckBoxPreference implements OnPreferenceChangeListener
	{
		private String id, title;

		private Pref(String id, String title) {
			super(GroupsActivity.this);
			this.id    = id;
			this.title = title;
			setPersistent(false);
			setTitle(title);
			setOnPreferenceChangeListener(this);
		}
		
		@Override
		public boolean onPreferenceChange(Preference p, Object v) { return changed = true; }
		
		@Override
		public int compareTo(Preference p) { return title.compareTo(((Pref)p).title); }

	}

}
