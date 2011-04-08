package cri.sanity.screen;

import android.content.Intent;
import android.preference.Preference;
import cri.sanity.*;
import cri.sanity.util.*;


public class SmsHistoryActivity extends HistoryActivity
{
	private static final char   SEP     = Conf.SMS_SEP;
	private static final String SEP_S   = SEP + "";
	private static final String UNSEP   = Conf.SMS_UNSEP;
	private static final String UNSLASH = Conf.SMS_UNSLASH;

	@Override
	protected String   fnHistory() { return Conf.SMS_FN; }
	@Override
	protected String   mainTitle() { return A.s(R.string.history_sms); }
	@Override
	protected Class<?> prefClass() { return Pref.class; }
	@Override
	protected int      lineItems() { return 4; }
	@Override
	protected char     sep      () { return SEP; }

	@Override
	protected void onClear() { A.putc(K.SMS_COUNT, 0); }

	//---- inner class

	public class Pref extends Preference implements Preference.OnPreferenceClickListener
	{
		private String body;

		public Pref(String[] items)
		{
			super(SmsHistoryActivity.this);
			String dt   = items[0];
			String name = items[1];
			String num  = items[2];
			body = items[3].replace("\\n","\n").replace(UNSLASH,"\\").replace(UNSEP, SEP_S);
			if(name.length() <= 0) name = num;
			else if(num.length() > 0) dt += "        "+num;
			setTitle(name);
			setSummary(dt);
			setOnPreferenceClickListener(this);
		}

		@Override
		public boolean onPreferenceClick(Preference p)
		{
			Alert.msg(
				getTitle().toString(),
				body,
				new Alert.Click(){ public void on(){
					A.clipMan().setText(body);
					A.toast(A.s(R.string.msg_clipboard_copied));
				}},
				new Alert.Click(){ public void on(){
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setType("text/plain");
					i.putExtra(Intent.EXTRA_TEXT, body);
					i = Intent.createChooser(i, A.s(R.string.share)+" SMS");
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(i);
				}},
				Alert.COPYSHARE
			);
			return true;
		}
	}

}
