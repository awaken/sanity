package cri.sanity;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import cri.sanity.util.*;
import cri.sanity.pref.*;


public abstract class PrefActivity extends PreferenceActivity
{
	//---- inner classes

	public static abstract class Click implements OnPreferenceClickListener
	{
		protected Preference pref;
		@Override
		public boolean onPreferenceClick(Preference pref) {
			this.pref = pref;
			return on();
		}
		public abstract boolean on();
	}

	public static abstract class Change implements OnPreferenceChangeListener
	{
		protected Preference pref;
		protected Object     value;
		@Override
		public boolean onPreferenceChange(Preference pref, Object value) {
			this.pref  = pref;
			this.value = value;
			return on();
		}
		public abstract boolean on();
	}

	//---- Activity override

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Alert.activity = this;
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume()
	{
		Alert.activity = this;
		super.onResume();
	}

	//---- general api

	public boolean isMainActivity() { return false; }

	//---- preference api

	public final Preference pref(String key) { return findPreference(key); }

	// enabledDep is true if and only if "key" preference is dependent also to global "enabled" key
	public final void updatePref(String key, boolean enabledDep) {
		final Preference         p   = findPreference(key);
		final String             kd  = p.getDependency();
		final CheckBoxPreference dep = A.empty(kd)? null : (CheckBoxPreference)findPreference(kd);
		enabledDep = !enabledDep || A.isEnabled();
		p.setEnabled(enabledDep && (dep==null || (dep.isEnabled() && dep.isChecked())));
		if(     p instanceof CheckBoxPreference) ((CheckBoxPreference)p).setChecked(A.is(key));
		else if(p instanceof PList             ) { PList q = (PList)p; if(q.isWrap()) q.setValue(A.geti(q.getWrapKey())); else q.setValue(A.gets(key)); }
		else if(p instanceof PFilter           ) ((PFilter)p).updateSum();
		else if(p instanceof PEdit             ) ((PEdit  )p).updateSum();
		else if(p instanceof PPwd              ) ((PPwd   )p).updateSum();
		else if(p instanceof ListPreference    ) ((ListPreference)p).setValue(A.gets(key));
	}
	public final void updatePref      (String     key ) { updatePref(key, true); }
	public final void updatePrefs     (String ... keys) { for(final String k : keys) updatePref(k, true ); }
	public final void updatePrefsNoDep(String ... keys) { for(final String k : keys) updatePref(k, false); }

	public final boolean is(String key)   { return ((CheckBoxPreference)pref(key)).isChecked(); }
	public final boolean is(Preference p) { return ((CheckBoxPreference)p        ).isChecked(); }

	public final void setChecked(String key, boolean checked) {
		((CheckBoxPreference)pref(key)).setChecked(checked);
	}
	public final void setChecked(Preference p, boolean checked) {
		((CheckBoxPreference)p).setChecked(checked);
	}
	public final void setEnabled(String key, boolean enabled) {
		pref(key).setEnabled(enabled);
	}
	public final void setEnabled(Preference p, boolean enabled) {
		p.setEnabled(enabled);
	}
	
	public final void on(String key, Click  listener)            { on(pref(key), listener);               }
	public final void on(String key, Change listener)            { on(pref(key), listener);               }
	public final void on(String key, Click click, Change change) { on(pref(key), click, change);          }
	public final void on(Preference p, Click  listener)          { p.setOnPreferenceClickListener (listener); }
	public final void on(Preference p, Change listener)          { p.setOnPreferenceChangeListener(listener); }
	public final void on(Preference p, Click click, Change change) {
		p.setOnPreferenceClickListener(click);
		p.setOnPreferenceChangeListener(change);
	}

	public final void fullOnly(Preference ... prefs) { for(Preference p : prefs) fullOnly(p); }
	public final void fullOnly(String     ... keys ) { for(String     k : keys ) fullOnly(k); }
	public final void fullOnly(String         key  ) { fullOnly(pref(key)); }
	public final void fullOnly(Preference p) {
		if(A.isFull()) return;
		if(p instanceof CheckBoxPreference || p instanceof ListPreference || p instanceof EditTextPreference)
			on(p, new Change(){ public boolean on(){ Alert.msg(A.s(R.string.msg_option_full)); return false; }});
		else
			on(p, new Click(){ public boolean on(){ Alert.msg(A.s(R.string.msg_option_full)); return true ; }});
	}

}
