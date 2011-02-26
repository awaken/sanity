package cri.sanity;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import cri.sanity.screen.CallFilterActivity;


public class PrefCall extends Preference implements Preference.OnPreferenceClickListener
{
	public String id;

	public PrefCall(Context ctx)                                { super(ctx);               init(); }
	public PrefCall(Context ctx, AttributeSet attrs)            { super(ctx, attrs);        init(); }
	public PrefCall(Context ctx, AttributeSet attrs, int style) { super(ctx, attrs, style); init(); }

	private void init() {
		CharSequence s = getTitle();
		if(s==null || s.length()<=0) setTitle(R.string.callfilter_cat);
		s = getSummary();
		if(s==null || s.length()<=0) setTitle(R.string.callfilter_sum);
		setWidgetLayoutResource(R.layout.img_call);
	}

	@Override
	public boolean onPreferenceClick(Preference p) {
		if(A.empty(id)) return true;
		Intent i = new Intent(A.app(), CallFilterActivity.class);
		i.putExtra(CallFilterActivity.EXTRA_TITLE , getTitle());
		i.putExtra(CallFilterActivity.EXTRA_ID    , id);
		A.app().startActivity(i);
		return true;
	}
}
