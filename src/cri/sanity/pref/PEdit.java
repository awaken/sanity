package cri.sanity.pref;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.AttributeSet;
import cri.sanity.*;


public class PEdit extends EditTextPreference implements OnPreferenceChangeListener
{
	private OnPreferenceChangeListener listener = null;
	private CharSequence sum = null;
	
	public String prefix;
	public String suffix;

	public PEdit(Context ctx)                                { super(ctx);               init(null );              }
	public PEdit(Context ctx, AttributeSet attrs)            { super(ctx, attrs);        init(attrs); updateSum(); }
	public PEdit(Context ctx, AttributeSet attrs, int style) { super(ctx, attrs, style); init(attrs); updateSum(); }

	//---- ListPreference override

	@Override
	public void setText(String text) { text = fullText(text); super.setText(text); updateSum(text); }
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
		updateSum(fullText(o));
		if(isWrap()) A.putc(getWrapKey(), Integer.parseInt((String)o));		// wrap key found: convert to integer
		return true;
	}

	//---- public api

	public final void updateSum() { updateSum(getText()); }

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
			return Integer.parseInt(getText());
		} catch(Exception e) {
			return 0;
		}
	}

	//---- private api

	private void init(AttributeSet attrs) {
		super.setOnPreferenceChangeListener(this);
		if(attrs != null) {
			prefix = attrs.getAttributeValue(null, "prefix");
			suffix = attrs.getAttributeValue(null, "suffix");
		}
		if(prefix == null) prefix = "";
		if(suffix == null) suffix = "";
		if(!isWrap()) return;
		setPersistent(false);
		final String k = getWrapKey();
		if(A.has(k)) super.setText(Integer.toString(A.geti(k)));
	}

	private String fullText(Object text) {
		final String t = (String)text;
		return A.empty(t)? "" : prefix+t.trim()+suffix;
	}

	private void updateSum(String text) {
		if(sum  == null) sum  = getSummary();
		if(text != null) text = text.trim();
		text = A.empty(text)? "" : " ("+text+')';
		super.setSummary(sum + text);
	}

}
