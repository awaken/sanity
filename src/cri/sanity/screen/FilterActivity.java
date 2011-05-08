package cri.sanity.screen;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import cri.sanity.*;
import cri.sanity.pref.*;


public class FilterActivity extends ScreenActivity implements OnPreferenceChangeListener
{
	public  static final String EXTRA_SECT  = "cri.sanity.Filter.id";
	public  static final String EXTRA_TITLE = "cri.sanity.Filter.title";
	public  static final String SEP         = Conf.FILTER_SEP+"";
	private static final String ITEMS_FMT   = A.s(R.string.msg_items);
	private static final int CODE_NUMS      = 1;
	private static final int CODE_CONTACTS  = 2;
	private static final int CODE_GROUPS    = 3;
	private static final int CODE_DATETIME  = 4;
	private static final int CODE_PREFIX    = 5;

	public static PFilter pref;
	private String sect, title, sumNums, sumContacts, sumGroups, sumDt, sumPrefix;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		skipAllKeys = true;
		screener(FilterActivity.class, R.xml.prefs_filter, R.layout.img_call);
		super.onCreate(savedInstanceState);
		Intent i = getIntent();
		sect     = i.getStringExtra(EXTRA_SECT );
		title    = i.getStringExtra(EXTRA_TITLE);
		if(!A.empty(title)) {
			Preference p = pref("filter");
			p.setTitle(p.getTitle()+"  -  "+title);
		}
		initCheckbox("filter_enable", "filter_all", "filter_anonym", "filter_unknown", "filter_allcontacts", "filter_star");
		initList("filter_mode");
		Preference p = pref("filter_dt");
		sumDt = p.getSummary().toString();
		setSumDt(A.is(keySect("filter_dt")), p);
		on(p, new Click(){ public boolean on(){ return startAct(DateTimeActivity.class, CODE_DATETIME); }});
		p = pref("filter_nums");
		sumNums = p.getSummary().toString();
		setSumNums(A.geti(keySect("filter_nums_count")), p);
		on(p, new Click(){ public boolean on(){ return startAct(NumsActivity.class, CODE_NUMS); }});
		p = pref("filter_contacts");
		sumContacts = p.getSummary().toString();
		setSumContacts(A.geti(keySect("filter_contacts_count")), p);
		on(p, new Click(){ public boolean on(){ return startAct(ContactsActivity.class, CODE_CONTACTS); }});
		p = pref("filter_groups");
		sumGroups = p.getSummary().toString();
		setSumGroups(A.geti(keySect("filter_groups_count")), p);
		on(p, new Click(){ public boolean on(){ return startAct(GroupsActivity.class, CODE_GROUPS); }});
		p = pref("filter_prefix");
		sumPrefix = p.getSummary().toString();
		setSumPrefix(A.geti(keySect("filter_prefix_count")), p);
		on(p, new Click(){ public boolean on(){ return startAct(PrefixActivity.class, CODE_PREFIX); }});
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(pref == null) return;
		pref.updateSum();
		pref = null;
	}

	@Override
	public void onActivityResult(int code, int res, Intent i)
	{
		if(i==null || res!=RESULT_OK) return;
		final int k = i.getIntExtra(EXTRA_SECT, -666);
		if(k == -666) return;
		switch(code) {
			case CODE_NUMS:
				setSumNums(k, null);
				break;
			case CODE_CONTACTS:
				if(k >= 0)
					setSumContacts(k, null);
				else {
					setSumContacts(0, null);
					setChecked("filter_allcontacts", true);
					setEnabled("filter_contacts",   false);
					setEnabled("filter_groups"  ,   false);
				}
				break;
			case CODE_GROUPS:
				setSumGroups(k, null);
				break;
			case CODE_DATETIME:
				setSumDt(k>0, null);
				break;
			case CODE_PREFIX:
				setSumPrefix(k, null);
				break;
		}
	}

	//---- private api

	private boolean startAct(Class<?> cls, int code) {
		Intent i = new Intent(A.app(), cls);
		i.putExtra(EXTRA_SECT , sect );
		i.putExtra(EXTRA_TITLE, title);
		startActivityForResult(i, code);
		return true;
	}

	private String keySect(String k) { return k+'_'+sect;  }

	private void initList(String ... keys) {
		for(String k : keys) {
			PList p = (PList)pref(k);
			p.setValue(Integer.toString(A.geti(keySect(k))));
			p.setOnPreferenceChangeListener(this);
		}
	}
	private void initCheckbox(String ... keys) {
		for(String k : keys) {
			CheckBoxPreference p = (CheckBoxPreference)pref(k);
			boolean on = A.is(keySect(k));
			p.setChecked(on);
			p.setOnPreferenceChangeListener(this);
			updateDeps(k, on);
		}
	}

	private void updateDeps(String key, boolean on) {
		if(key.equals("filter_allcontacts")) {
			pref("filter_star"    ).setEnabled(!on);
			pref("filter_contacts").setEnabled(!on);
			pref("filter_groups"  ).setEnabled(!on);
		}
		else if(key.equals("filter_unknown"))
			pref("filter_nums").setEnabled(!on);
		else if(key.equals("filter_all")) {
			pref("filter_anonym").setEnabled(!on);
			pref("filter_prefix").setEnabled(!on);
			CheckBoxPreference p = (CheckBoxPreference)pref("filter_allcontacts");
			p.setEnabled(!on);
			boolean fon = !on && !p.isChecked();
			pref("filter_star"    ).setEnabled(fon);
			pref("filter_contacts").setEnabled(fon);
			pref("filter_groups"  ).setEnabled(fon);
			p = (CheckBoxPreference)pref("filter_unknown");
			p.setEnabled(!on);
			fon = !on && !p.isChecked();
			pref("filter_nums").setEnabled(!on && !p.isChecked());
		}
	}

	private void setSumDt(boolean active, Preference p) {
		if(p == null) p = pref("filter_dt");
		p.setSummary(sumDt+" ("+A.s(active? R.string.active : R.string.always)+')');
	}
	private void setSumNums(int cnt, Preference p) {
		if(p == null) p = pref("filter_nums");
		p.setSummary(sumNums + itemsFmt(cnt));
	}
	private void setSumPrefix(int cnt, Preference p) {
		if(p == null) p = pref("filter_prefix");
		p.setSummary(sumPrefix + itemsFmt(cnt));
	}
	private void setSumContacts(int cnt, Preference p) {
		if(p == null) p = pref("filter_contacts");
		p.setSummary(sumContacts + itemsFmt(cnt));
	}
	private void setSumGroups(int cnt, Preference p) {
		if(p == null) p = pref("filter_groups");
		p.setSummary(sumGroups + itemsFmt(cnt));
	}
	private static String itemsFmt(int cnt) {
		return " ("+String.format(ITEMS_FMT, Integer.toString(cnt))+')';
	}

	@Override
	public boolean onPreferenceChange(Preference p, Object v) {
		final String kp = p.getKey();
		final String ks = keySect(kp);
		if(p instanceof PList)
			A.putc(ks, Integer.parseInt((String)v));
		else if(p instanceof CheckBoxPreference) {
			final boolean on = (Boolean)v;
			A.putc(ks, on);
			updateDeps(kp, on);
		}
		return true;
	}

}
