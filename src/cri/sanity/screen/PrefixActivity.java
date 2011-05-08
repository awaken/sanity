package cri.sanity.screen;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.CheckBoxPreference;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import cri.sanity.*;
import cri.sanity.util.*;


public class PrefixActivity extends ScreenActivity
{
	private static final String SEP = FilterActivity.SEP;
	
	private PreferenceCategory prefGroup;
	private Stack<Pref> selected = new Stack<Pref>();
	private Map<String,Pref> prefs = new HashMap<String,Pref>();
	private String sect;
	private boolean changed;

	//---- overriding

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		skipAllKeys = true;
		screener(PrefixActivity.class, R.xml.filter_prefix, R.layout.img_prefix);
		super.onCreate(savedInstanceState);
		changed   = false;
		prefGroup = (PreferenceCategory)pref("filter_prefix");
		Intent i  = getIntent();
		sect      = i.getStringExtra(FilterActivity.EXTRA_SECT);
		String t  = i.getStringExtra(FilterActivity.EXTRA_TITLE);
		if(!A.empty(t))
			prefGroup.setTitle(prefGroup.getTitle()+"  ("+t+ ')');
		String nums = A.gets(keyAll());
		if(A.empty(nums))
			A.toast(R.string.msg_empty_list);
		else
			for(String num : nums.split(SEP))
				addnum(num);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.prefix, menu);
    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()) {
			case R.id.addnew : addnew();  break;
			case R.id.change : change();  break;
			case R.id.del    : delete();  break;
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
		if(changed) {
			for(String num : A.gets(keyAll()).split(SEP))
				A.del(keySect(num.trim()));
			String nums = "";
			final int n = prefGroup.getPreferenceCount();
			if(n > 0) {
				String num = ((Pref)prefGroup.getPreference(0)).getNum();
				A.put(keySect(num), true);
				final StringBuilder sb = new StringBuilder(num);
				for(int i=1; i<n; i++) {
					num = ((Pref)prefGroup.getPreference(i)).getNum();
					A.put(keySect(num), true);
					sb.append(SEP).append(num);
				}
				nums = sb.toString();
			}
			A.put(keyAll(), nums).putc(keyCount(), n);
		}
		Intent i = new Intent();
		i.putExtra(FilterActivity.EXTRA_SECT, prefGroup.getPreferenceCount());
		setResult(RESULT_OK, i);
		super.onBackPressed();
	}
	
	//---- private api

	private String keyAll()            { return "filter_prefix_"+sect; }
	private String keyCount()          { return "filter_prefix_count_"+sect; }
	private String keySect(String val) { return "filter_prefix_"+val+sect; }

	private void addnum(String num)
	{
		Pref pref = new Pref(num);
		prefs.put(num, pref);
		prefGroup.addPreference(pref);
	}

	private void addnew()
	{
		Alert.edit(A.s(R.string.msg_prefix_edit), PrefixNum.get(), new Alert.Edited() {
			@Override
			public void on(String num) {
				num = num.trim();
				if(num.length() < 2) { A.toast(R.string.err_name); return; }
				if(prefs.get(num) != null) { A.toast(String.format(A.s(R.string.err_exists), num)); return; }
				addnum(num);
				changed = true;
			}
		}).setInputType(InputType.TYPE_CLASS_PHONE);
	}
	
	private void change()
	{
		if(selected.isEmpty()) return;
		final Pref prefSel  = selected.peek();
		final String numSel = prefSel.getNum();
		Alert.edit(A.s(R.string.msg_prefix_edit), numSel, new Alert.Edited() {
			@Override
			public void on(String num) {
				num = num.trim();
				if(num.length() < 2) { A.toast(R.string.err_name); return; }
				if(num.equals(numSel)) return;
				if(prefs.get(num) != null) { A.toast(String.format(A.s(R.string.err_exists), num)); return; }
				prefSel.setNum(num);
				changed = true;
			}
		}).setInputType(InputType.TYPE_CLASS_PHONE);
	}

	private void delete()
	{
		final int n = selected.size();
		if(n < 1) return;
		Alert.msg(n>1? String.format(A.s(R.string.ask_del_all), n+"") : A.s(R.string.ask_del_one),
			new Alert.Click() {
				@SuppressWarnings("unchecked")
				public void on() {
					if(selected.isEmpty()) return;
					for(Pref p : (Stack<Pref>)selected.clone()) {
						prefs.remove(p.getNum());
						prefGroup.removePreference(p);
						selected.remove(p);
					}
					changed = true;
				}
			},
			null,
			Alert.OKCANC
		);
	}
	
	private void selall()
	{
		final int n = prefGroup.getPreferenceCount();
		if(n < 1) return;
		selected.clear();
		for(int i=0; i<n; i++) {
			final Pref p = (Pref)prefGroup.getPreference(i);
			p.setChecked(true);
			selected.add(p);
		}
		if(n > 2)
			A.toast(String.format(A.s(R.string.msg_selected_all), n+""));
	}
	
	private void selnone()
	{
		for(Pref p : selected) p.setChecked(false);
		selected.clear();
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

	//---- inner classes

	private class Pref extends CheckBoxPreference implements OnPreferenceChangeListener
	{
		Pref(String num) {
			super(PrefixActivity.this);
			setPersistent(false);
			setNum(num);
			setOnPreferenceChangeListener(this);
		}
		private String getNum()           { return getTitle().toString(); }
		private void   setNum(String num) { setTitle(num.trim());         }
		@Override
		public boolean onPreferenceChange(Preference p, Object v) {
			if((Boolean)v) selected.push(this);
			else           selected.remove(this);
			return true;
		}
	}

}
