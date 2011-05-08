package cri.sanity.pref;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;
import cri.sanity.screen.FilterActivity;
import cri.sanity.*;


public final class PFilter extends Preference implements OnPreferenceClickListener
{
	private CharSequence sum;
	
	public PFilter(Context ctx)                                { super(ctx);               init(); }
	public PFilter(Context ctx, AttributeSet attrs)            { super(ctx, attrs);        init(); }
	public PFilter(Context ctx, AttributeSet attrs, int style) { super(ctx, attrs, style); init(); }

	private void init() {
		CharSequence t = getTitle();
		if(t==null || t.length()<=0) setTitle(R.string.filter_cat);
		sum = getSummary();
		if(sum==null || sum.length()<=0) sum = A.s(R.string.filter_sum);
		updateSum();
		setPersistent(false);
		setWidgetLayoutResource(R.layout.img_call);
		setOnPreferenceClickListener(this);
	}

	public final String sect() {
		final String key = getKey();
		return key.substring(key.lastIndexOf('_') + 1);
	}
	
	public final String filterKey() { return "filter_enable_"+sect(); }

	public final void updateSum() {
		setSummary(sum + " (" + A.s(A.is(filterKey())? R.string.active : R.string.inactive) + ')');
	}

	public final void updateSum(boolean enable) {
		A.putc(filterKey(), enable);
		updateSum();
	}

	@Override
	public boolean onPreferenceClick(Preference p) {
		final String sect = sect();
		if(A.empty(sect)) return true;
		String title = null;
		try {	title = A.s(A.rstring(getKey()));    } catch(Exception e ) {
		try { title = A.s(A.rstring(sect+"_cat")); } catch(Exception e2) {}}
		Intent i = new Intent(A.app(), FilterActivity.class);
		i.putExtra(FilterActivity.EXTRA_SECT , sect );
		i.putExtra(FilterActivity.EXTRA_TITLE, title);
		FilterActivity.pref = this;
		getContext().startActivity(i);
		return true;
	}

}
