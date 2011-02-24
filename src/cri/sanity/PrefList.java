package cri.sanity;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.AttributeSet;


public class PrefList extends ListPreference implements OnPreferenceChangeListener
{
	OnPreferenceChangeListener listener = null;
	CharSequence sum = null;

	public PrefList(Context ctx) {
		super(ctx);
		init();
	}
	public PrefList(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		init();
		update();
	}

	//---- ListPreference override

	@Override
	public void setValue(String val) { super.setValue(val); update(findEntry(val)); }
	@Override
	public void setValueIndex(int idx) { super.setValueIndex(idx); update(getEntryValues()[idx]); }

	@Override
	public void setSummary(int id) { super.setSummary(sum = A.s(id)); }
	@Override
	public void setSummary(CharSequence text) { super.setSummary(sum = text); }

	@Override
	public OnPreferenceChangeListener getOnPreferenceChangeListener() { return listener; }
	@Override
	public void setOnPreferenceChangeListener(OnPreferenceChangeListener l) { listener = l; }

	@Override
	public boolean onPreferenceChange(Preference p, Object o) {
		if(listener!=null && !listener.onPreferenceChange(p, o)) return false;
		update(findEntry(o));
		final String key = getKey();
		if(key.endsWith(K.WS))																																	// is this key a wrap one?
			A.putc(key.substring(0, key.length()-K.WS.length()), Integer.parseInt((String)o));		// wrap key found: convert to integer
		return true;
	}

	//---- public api

	public final void update() { update(getEntry()); }

	public final CharSequence findEntry(Object findValue) {
		CharSequence[] vals = getEntryValues();
		if(vals == null) return null;
		final int n = vals.length;
		for(int i=0; i<n; i++)
			if(vals[i].equals(findValue)) return getEntries()[i];
		return null;
	}

	//---- private api

	private void init() {
		super.setOnPreferenceChangeListener(this);
	}

	private void update(Object entry) {
		if(sum == null) sum = getSummary();
		final String e = entry==null? "" : " ("+entry+')';
		super.setSummary(sum + e);
	}

}
