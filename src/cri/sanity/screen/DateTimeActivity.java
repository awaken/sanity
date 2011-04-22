package cri.sanity.screen;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import cri.sanity.*;
import cri.sanity.util.*;


public class DateTimeActivity extends ScreenActivity implements OnPreferenceChangeListener
{
	private static final char   SEP_TIME   = ':';
	private static final String SEP_RANGE  = "   >>>   ";
	private static final String FILTER_DAY = "filter_dt_day";
	private final static String[] DAYS     = new String[]{ "2", "3", "4", "5", "6", "7", "1" };	// mon, tue, wed, thu, fri, sat, sun

	private PreferenceGroup prefGroup, prefTimes;
	private boolean  changedDay, changedTime;
	private String   sect;

	//---- public api

	@Override
	public void onCreate(Bundle savedInstanceState) {
		skipAllKeys = true;
		screener(DateTimeActivity.class, R.xml.filter_datetime, R.layout.img_history);
		super.onCreate(savedInstanceState);
		prefGroup = (PreferenceGroup)pref("group");
		prefTimes = (PreferenceGroup)pref("times");
		Intent i  = getIntent();
		sect      = i.getStringExtra(FilterActivity.EXTRA_SECT );
		String t  = i.getStringExtra(FilterActivity.EXTRA_TITLE);
		if(!A.empty(t)) prefGroup.setTitle(prefGroup.getTitle()+"  ("+t+')');
		initCheckbox("filter_dt");
		initDays();
		initTimes();
		changedDay = changedTime = false;
	}
	
	@Override
	public void onBackPressed() {
		if(isFinishing()) return;
		final boolean hasDt = is("filter_dt");
		if(hasDt && !hasDays()) {
			Alert.msg(A.s(R.string.err_datetime_day));
			return;
		}
		if(changedDay) {
			String k = keySect("filter_dt_days");
			String selDays = "";
			if(hasDt)
				for(String d : DAYS)
					if(is(FILTER_DAY+d)) selDays += d;
			if(!hasDt || selDays.length()==7) A.del(k);
			else A.put(k, selDays);
		}
		if(changedTime) {
			final String keyCount = keySect("filter_dt_time_count");
			final int oldTimes = A.geti(keyCount);
			final int newTimes = prefTimes.getPreferenceCount() - 1;
			for(int i=1; i<=newTimes; i++)
				A.put(keySect("filter_dt_time"+i), ((Pref)prefTimes.getPreference(i)).getTime());
			for(int i=newTimes+1; i<=oldTimes; i++)
				A.del(keySect("filter_dt_time"+i));
			A.put(keyCount, newTimes);
		}
		if(changedDay || changedTime) A.commit();
		Intent i = new Intent();
		i.putExtra(FilterActivity.EXTRA_SECT, hasDt ? 1 : 0);
		setResult(RESULT_OK, i);
		super.onBackPressed();
	}

	//---- private api

	private void initCheckbox(String key) {
		final CheckBoxPreference p = (CheckBoxPreference)pref(key);
		p.setChecked(A.is(keySect(key)));
		p.setOnPreferenceChangeListener(this);
	}
	
	private void initDays() {
		on("alldays", new Click(){ public boolean on(){
			final boolean enable = !hasAllDays();
			for(String d : DAYS) setChecked(FILTER_DAY+d, enable);
			return changedDay = true;
		}});
		final String checkedDays = A.gets(keySect("filter_dt_days"));
		final boolean all = checkedDays.length() == 0;
		final Change change = new Change(){ public boolean on(){ return changedDay = true; }};
		for(String d : DAYS) {
			final CheckBoxPreference p = (CheckBoxPreference)pref(FILTER_DAY+d);
			p.setChecked(all || checkedDays.indexOf(d)>=0);
			on(p, change);
		}
	}
	
	private void initTimes()
	{
		Preference p = pref("add");
		p.setTitle(">>  "+p.getTitle()+"  <<");
		on(p, new Click(){ public boolean on(){
			time(0,0,0,0, new OnTime(){ public void got(int h1, int m1, int h2, int m2){ new Pref(h1, m1, h2, m2); }});
			return true;
		}});
		final int n = A.geti(keySect("filter_dt_time_count"));
		for(int i=1; i<=n; i++) {
			final int[] t = int2range(A.geti(keySect("filter_dt_time"+i)));
			new Pref(t[0], t[1], t[2], t[3]);
		}
	}
	
	private static void time(final int h1, final int m1, final int h2, final int m2, final OnTime onTime) {
		Alert.time(A.s(R.string.filter_dt_add_start), new Alert.Timed(h1, m1){ public void on() {
			dlg.dismiss();
			final int hour1 = hour;
			final int mins1 = mins;
			Alert.time(A.s(R.string.filter_dt_add_stop), new Alert.Timed(h2>0?h2:hour, h2>0?m2:mins){ public void on() {
				if(hour1>hour || (hour1==hour && mins1>=mins))
					A.toast(R.string.err_datetime_time);
				else
					onTime.got(hour1, mins1, hour, mins);
			}});
		}});
	}
	
	private String keySect(String k) { return k+'_'+sect;  }

	private boolean hasAllDays() {
		boolean all = true;
		for(String d : DAYS)
			all &= is(FILTER_DAY+d);
		return all;
	}
	
	private boolean hasDays() {
		boolean has = false;
		for(String d : DAYS)
			has |= is(FILTER_DAY+d);
		return has;
	}

	private static int[]  int2range(int i) { return new int[]{ (i>>24)&0xff, (i>>16)&0xff, (i>>8)&0xff, i&0xff }; }
	private static int    range2int(int h1, int m1, int h2, int m2) { return (h1<<24) | (m1<<16) | (h2<<8) | m2; }
	private static String range2str(int h1, int m1, int h2, int m2) { return v(h1)+SEP_TIME+v(m1)+SEP_RANGE+v(h2)+SEP_TIME+v(m2); }
	private static String v(int v) { return v>9 ? Integer.toString(v) : "0"+v; }

	//---- interface implementation

	@Override
	public boolean onPreferenceChange(Preference p, Object v) {
		A.putc(keySect(p.getKey()), (Boolean)v);
		return true;
	}

	//---- inner class

	private interface OnTime { public void got(int h1, int m1, int h2, int m2); }

	private final class Pref extends Preference implements OnPreferenceClickListener, OnTime
	{
		private int h1, m1, h2, m2;
		private Pref(int h1, int m1, int h2, int m2) {
			super(DateTimeActivity.this);
			setPersistent(false);
			setOnPreferenceClickListener(this);
			prefTimes.addPreference(this);
			setDependency("filter_dt");
			got(h1, m1, h2, m2);
		}
		private int getTime() { return range2int(h1, m1, h2, m2); }
		@Override
		public boolean onPreferenceClick(Preference p) {
			Alert.choose(getTitle().toString(), new int[]{ R.string.change, R.string.del, R.string.canc }, new Alert.Click() {
				@Override
				public void on(){
					switch(which) {
						case 0:		// change
							time(h1, m1, h2, m2, Pref.this);
							break;
						case 1:		// delete
							prefTimes.removePreference(Pref.this);
							changedTime = true;
							break;
					}
				}
			});
			return true;
		}
		@Override
		public void got(int h1, int m1, int h2, int m2) {
			setTitle(range2str(this.h1=h1, this.m1=m1, this.h2=h2, this.m2=m2));
			changedTime = true;
		}
	}

}
