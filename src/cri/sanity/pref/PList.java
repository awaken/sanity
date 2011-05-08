package cri.sanity.pref;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.AttributeSet;
import cri.sanity.*;


public class PList extends ListPreference implements OnPreferenceChangeListener
{
	private OnPreferenceChangeListener listener = null;
	private CharSequence sum = null;

	public PList(Context ctx)                                { super(ctx);        init();           }
	public PList(Context ctx, AttributeSet attrs)            { super(ctx, attrs); init(); update(); }

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
		if(isWrap()) A.putc(getWrapKey(), Integer.parseInt((String)o));		// wrap key found: convert to integer
		return true;
	}

	//---- public api

	public final void update() { update(getEntry()); }

	public final CharSequence findEntry(Object findValue) {
		if(findValue == null) return null;
		CharSequence[] vals = getEntryValues();
		if(vals == null) return null;
		final int n = vals.length;
		for(int i=0; i<n; i++)
			if(vals[i].equals(findValue)) return getEntries()[i];
		return null;
	}

	public final boolean isWrap() {
		final String key = getKey();
		return key!=null && key.endsWith(K.WS);
	}
	
	public final String getWrapKey() {
		final String key = getKey();
		return key.substring(0, key.length()-K.WS.length());
	}

	public final int getValueInt() {
		try {
			return Integer.parseInt(getValue());
		} catch(Exception e) {
			return 0;
		}
	}

	public final void setValue(int v) { setValue(Integer.toString(v)); }

	//---- private api

	private void init() {
		super.setOnPreferenceChangeListener(this);
		if(!isWrap()) return;
		setPersistent(false);
		final String k = getWrapKey();
		if(A.has(k)) super.setValue(Integer.toString(A.geti(k)));
	}

	private void update(Object entry) {
		if(sum == null) sum = getSummary();
		final String e = entry==null? "" : " ("+entry+')';
		super.setSummary(sum + e);
	}

}
