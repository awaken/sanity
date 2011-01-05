package cri.sanity;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;


public abstract class PrefActivity extends PreferenceActivity
{
	private boolean quitting = false;
	
	//---- inner classes

	public static abstract class Click implements OnPreferenceClickListener
	{
		protected Preference pref;
		public boolean onPreferenceClick(Preference pref) {
			this.pref = pref;
			return on();
		}
		abstract boolean on();
	}

	public static abstract class Change implements OnPreferenceChangeListener
	{
		protected Preference pref;
		protected Object     value;
		public boolean onPreferenceChange(Preference pref, Object value) {
			this.pref  = pref;
			this.value = value;
			return on();
		}
		abstract boolean on();
	}

	//---- methods

	public final boolean isQuitting() { return quitting; }
	
	public final Preference findPref(String key)
	{
		Preference p = findPreference(key);
		if(p == null) A.logd("Unable to find Preference "+key);
		return p;
	}

	public final boolean is(String key)   { return ((CheckBoxPreference)findPref(key)).isChecked(); }
	public final boolean is(Preference p) { return ((CheckBoxPreference)p            ).isChecked(); }
	
	public final void setChecked(String key, boolean checked) {
		((CheckBoxPreference)findPref(key)).setChecked(checked);
	}
	public final void setChecked(Preference p, boolean checked) {
		((CheckBoxPreference)p).setChecked(checked);
	}
	public final void setEnabled(String key, boolean enabled) {
		findPref(key).setEnabled(enabled);
	}
	
	public final void on(String key, Click  listener)            { on(findPref(key), listener);               }
	public final void on(String key, Change listener)            { on(findPref(key), listener);               }
	public final void on(String key, Click click, Change change) { on(findPref(key), click, change);          }
	public final void on(Preference p, Click  listener)          { p.setOnPreferenceClickListener (listener); }
	public final void on(Preference p, Change listener)          { p.setOnPreferenceChangeListener(listener); }
	public final void on(Preference p, Click click, Change change) {
		p.setOnPreferenceClickListener(click);
		p.setOnPreferenceChangeListener(change);
	}

	//---- Activity override

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    A.activity = this;
  }

	@Override
	public void onDestroy()
	{
		A.activity = null;
		quitting   = false;
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed()
	{
		quitting = true;
		super.onBackPressed();
	}

}
