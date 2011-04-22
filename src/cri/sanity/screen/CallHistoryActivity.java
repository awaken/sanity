package cri.sanity.screen;

import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.provider.ContactsContract;
import cri.sanity.*;
import cri.sanity.util.*;


public class CallHistoryActivity extends HistoryActivity
{
	@Override
	protected String   fnHistory() { return Conf.BLOCK_FN; }
	@Override
	protected String   mainTitle() { return A.s(R.string.history_call); }
	@Override
	protected Class<?> prefClass() { return Pref.class; }
	@Override
	protected int      lineItems() { return 3; }
	@Override
	protected char     sep      () { return Blocker.SEP; }

	//---- inner class

	public class Pref extends Preference implements Preference.OnPreferenceClickListener
	{
		private String name, num;

		public Pref(String[] items)
		{
			super(CallHistoryActivity.this);
			String dt   = items[0];
			String name = items[1];
			String num  = items[2];
			this.name   = name;
			this.num    = num;
			if(name.length() <= 0) name = num;
			else if(num.length() > 0) dt += "           "+num;
			setTitle(name);
			setSummary(dt);
			if(num.length() < 1) setSelectable(false);
			else setOnPreferenceClickListener(this);
		}

		@Override
		public boolean onPreferenceClick(Preference p)
		{
			final boolean known = name.length() > 0;
			final int[] items = new int[5];
			items[0] = R.string.call;
			items[1] = R.string.send_sms;
			items[2] = known? R.string.view_contact : R.string.add_contact;
			items[3] = R.string.copy;
			items[4] = R.string.share;
			Alert.choose(known? name+" ("+num+')' : num, items, new Alert.Click(){ public void on(){
				Intent i = new Intent();
				switch(which) {
					case 0:
						i.setAction(Intent.ACTION_CALL);
						i.setData(Uri.parse("tel:"+num));
						break;
					case 1:
						i.setAction(Intent.ACTION_VIEW);
						i.setData(Uri.parse("smsto:"+num));
						break;
					case 2:
						i.setAction(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT);
						i.setData(Uri.parse("tel:"+num));
						i.putExtra(ContactsContract.Intents.EXTRA_FORCE_CREATE, !known);
						break;
					case 3:
						A.clipMan().setText(num);
						A.toast(A.s(R.string.msg_clipboard_copied));
						return;
					case 4:
						i.setAction(Intent.ACTION_VIEW);
						i.setType("text/plain");
						i.putExtra(Intent.EXTRA_TEXT, num);
						i = Intent.createChooser(i, A.s(R.string.share)+' '+num);
						break;
					default:
						return;
				}
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}});
			return true;
		}
	}

}
