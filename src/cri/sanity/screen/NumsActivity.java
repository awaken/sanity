package cri.sanity.screen;

import cri.sanity.*;

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


public class NumsActivity extends ScreenActivity
{
	private static final String SEP = Conf.FILTER_SEP+"";

	private PreferenceCategory prefGroup;
	private Stack<Pref> selected = new Stack<Pref>();
	private String sect;
	private boolean changed;

	//---- overriding

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		skipAllKeys = true;
		screener(NumsActivity.class, R.xml.filter_nums, R.layout.img_list);
		super.onCreate(savedInstanceState);
		changed   = false;
		prefGroup = (PreferenceCategory)pref("filter_nums");
		Intent i  = getIntent();
		sect      = i.getStringExtra(FilterActivity.EXTRA_SECT);
		String t  = i.getStringExtra(FilterActivity.EXTRA_TITLE);
		if(!A.empty(t))
			prefGroup.setTitle(prefGroup.getTitle() + "  (" + t + ')');
		String nums = A.gets(keyAll());
		if(A.empty(nums)) A.toast(R.string.msg_empty_list);
		else for(String num : nums.split(SEP))
			prefGroup.addPreference(new Pref(num));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.lista, menu);
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

	private String keyAll()            { return "filter_nums_"+sect; }
	private String keyCount()          { return "filter_nums_count_"+sect; }
	private String keySect(String val) { return "filter_num_"+val+sect; }

	private void addnew()
	{
		A.alertText(A.s(R.string.msg_nums_edit), new A.Edited() {
			@Override
			public void on(String num) {
				num = num.trim();
				if(A.empty(num)) { A.toast(R.string.err_name); return; }
				prefGroup.addPreference(new Pref(num));
				changed = true;
			}
		}).setInputType(InputType.TYPE_CLASS_PHONE);
	}
	
	private void change()
	{
		if(selected.isEmpty()) return;
		final Pref prefSel  = selected.peek();
		final String numSel = prefSel.getNum();
		A.alertText(A.s(R.string.msg_nums_edit), numSel, new A.Edited() {
			@Override
			public void on(String num) {
				num = num.trim();
				if(A.empty(num)) { A.toast(R.string.err_name); return; }
				if(num.equals(numSel)) return;
				prefSel.setNum(num);
				changed = true;
			}
		}).setInputType(InputType.TYPE_CLASS_PHONE);
	}

	private void delete()
	{
		final int n = selected.size();
		if(n < 1) return;
		A.alert(n>1? String.format(A.s(R.string.ask_del_all), n+"") : A.s(R.string.ask_del_one),
			new A.Click() {
				@SuppressWarnings("unchecked")
				public void on() {
					if(selected.isEmpty()) return;
					for(Pref p : (Stack<Pref>)selected.clone()) {
						prefGroup.removePreference(p);
						selected.remove(p);
					}
					changed = true;
				}
			},
			null,
			A.ALERT_OKCANC
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
		A.alert(
			A.s(R.string.ask_canc_all),
			new A.Click(){ public void on(){ changed = false; dismiss(); finish(); }},
			null,
			A.ALERT_OKCANC
		);
	}

	//---- inner class

	private class Pref extends CheckBoxPreference implements OnPreferenceChangeListener
	{
		Pref(String num) {
			super(NumsActivity.this);
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
