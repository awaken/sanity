package cri.sanity.pref;

import android.content.Context;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;
import cri.sanity.*;
import cri.sanity.util.Alert;


public class PPwd extends Preference implements OnPreferenceClickListener
{
	private OnPreferenceClickListener listener;
	private CharSequence sum;
	
	public PPwd(Context ctx)                                { super(ctx);               init();              }
	public PPwd(Context ctx, AttributeSet attrs)            { super(ctx, attrs);        init(); updateSum(); }
	public PPwd(Context ctx, AttributeSet attrs, int style) { super(ctx, attrs, style); init(); updateSum(); }

	//---- ListPreference override

	@Override
	public void setSummary(int id) { super.setSummary(sum = A.s(id)); }
	@Override
	public void setSummary(CharSequence text) { super.setSummary(sum = text); }

	@Override
	public OnPreferenceClickListener getOnPreferenceClickListener() { return listener; }
	@Override
	public void setOnPreferenceClickListener(OnPreferenceClickListener l) { listener = l; }

	@Override
	public boolean onPreferenceClick(Preference p) {
		if(listener!=null && listener.onPreferenceClick(p)) return true;
		Alert.pwdChoose(pwd(), new Alert.Edited() {
			@Override
			public void on(String text){
				A.putc(getKey(), text);
				updateSum();
			}
		});
		return true;
	}

	public void updateSum() {
		if(sum == null) sum = getSummary();
		super.setSummary(sum + " (" + A.s(A.empty(pwd())? R.string.empty : R.string.active) + ')');
	}
	
	public String pwd() { return A.gets(getKey()); }

	//---- private api

	private void init() {
		super.setOnPreferenceClickListener(this);
		setWidgetLayoutResource(R.layout.img_secure);
		Alert.resetPwd();
	}

}
