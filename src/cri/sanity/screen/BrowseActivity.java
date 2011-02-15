package cri.sanity.screen;

import cri.sanity.*;
import java.io.File;
import java.util.Comparator;
import java.util.Stack;
import java.util.Arrays;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.CheckBoxPreference;
import android.provider.ContactsContract.PhoneLookup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


public class BrowseActivity extends ScreenActivity
{
	private static final String SEP_SUM  = "  *  ";
	private static final char   SEP_SHOW = '/';
	private static final String SEP_MAIN = Conf.REC_SEP+"";
	private static final String SEP_DATE = Conf.REC_DATE_PATTERN.charAt(4)+"";
	private static final String PREFIX   = Conf.REC_PREFIX;
	private static final int  PREFIX_LEN = PREFIX.length();
	private static final String IN       = " ("+A.tr(R.string.call_in)+')';
	private static final String OUT      = " ("+A.tr(R.string.call_out)+')';
	private static final String UNKNOWN  = A.tr(R.string.unknown);
	private static final ContentResolver resolver = A.resolver();
	private static final String[] projection = new String[]{ PhoneLookup.DISPLAY_NAME };

	private PreferenceCategory prefGroup;
	private Stack<Pref> selected = new Stack<Pref>();
	private String dir;
	private boolean empty = false;

	//---- overriding

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		skipAllKeys = true;
		screener(BrowseActivity.class, R.xml.prefs_browse);
		super.onCreate(savedInstanceState);
		prefGroup = (PreferenceCategory)pref(K.REC_BROWSE);
		dir = A.sdcardDir();
		if(dir == null) {
			empty(R.string.err, R.string.msg_dir_err);
			return;
		}
		String[] recs = new File(dir).list();
		if(recs.length <= 0) {
			empty(); 
			return; 
		}
		Arrays.sort(recs, 0, recs.length, new Comparator<String>() {
			public int compare(String s1, String s2) { return s2.compareTo(s1); }
		});
		for(final String fn : recs) {
			if(!fn.startsWith(PREFIX) || fn.endsWith(".txt") || fn.endsWith(".prf")) continue;
			prefGroup.addPreference(new Pref(fn));
		}
		if(prefGroup.getPreferenceCount() <= 0) empty();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		if(empty) return super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.browse, menu);
    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(empty) return super.onOptionsItemSelected(item);
		switch(item.getItemId()) {
			case R.id.browse_open   : open();    break;
			case R.id.browse_del    : delete();  break;
			case R.id.browse_selall : selall();  break;
			case R.id.browse_selnone: selnone(); break;
			default: return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	@Override
	public boolean isMainActivity() { return !empty; }

	//---- private api

	private void empty() { empty(R.string.empty, R.string.msg_rec_empty); }

	private void empty(int idTitle, int idSummary)
	{
		final Preference p = new Preference(this);
		p.setPersistent(false);
		p.setSelectable(false);
		p.setTitle(idTitle);
		p.setSummary(idSummary);
		prefGroup.addPreference(p);
		empty = true;
	}

	private void open()
	{
		if(selected.isEmpty()) return;
		final Intent i = new Intent(Intent.ACTION_VIEW);
		//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setDataAndType(Uri.parse("file://"+dir+'/'+selected.peek().fn), "audio/3gp");
		startActivity(i);
	}

	private void delete()
	{
		final int n = selected.size();
		if(n < 1) return;
		A.alert(n>1? String.format(A.tr(R.string.ask_del_all), n+"") : A.tr(R.string.ask_del_one),
			new A.Click() {
				@SuppressWarnings("unchecked")
				public void on() {
					int err = 0;
					for(final Pref p : (Stack<Pref>)selected.clone()) {
						if(!new File(dir,p.fn).delete())
							++err;
						else {
							prefGroup.removePreference(p);
							selected.remove(p);
						}
					}
					if(err > 0) A.alert(String.format(A.tr(R.string.msg_del_err), err+""));
				}
			},
			null,
			A.ALERT_OKCANC
		);
	}
	
	private void selall()
	{
		final int n = prefGroup.getPreferenceCount();
		if(n <= 0) return;
		selected.clear();
		for(int i=0; i<n; i++) {
			final Pref p = (Pref)prefGroup.getPreference(i);
			p.setChecked(true);
			selected.add(p);
		}
		if(n > 2)
			A.toast(String.format(A.tr(R.string.msg_selected_all), n+""));
	}
	
	private void selnone()
	{
		for(final Pref p : selected)
			p.setChecked(false);
		selected.clear();
	}

	//---- inner class

	private class Pref extends CheckBoxPreference implements OnPreferenceChangeListener
	{
		private String fn;

		Pref(String fn) {
			super(BrowseActivity.this);
			setPersistent(false);
			this.fn = new String(fn);
			// split filename + ext
			fn = fn.substring(PREFIX_LEN);
			String ext;
			final int p = fn.lastIndexOf('.');
			if(p != fn.length()-4)
				ext = "";
			else {
				ext = fn.substring(p+1);
				fn  = fn.substring(0,p);
			}
			// split each filename part separated by SEP_MAIN
			final String[] fnSplit = fn.split(SEP_MAIN);
			// set title
			String date = fnSplit[0];
			String time = fnSplit[1];
			final String[] dSplit = date.split(SEP_DATE);
			date = dSplit[2] + SEP_SHOW + dSplit[1] + SEP_SHOW + dSplit[0];
			setTitle(date + ", " + time);
			// set summary
			if(     ext.equals("m4a")) ext = "MPEG4";
			else if(ext.equals("3gp")) ext = "3GPP";
			else if(ext.equals("amr")) ext = "AMR";
			String sum;
			final int n = fnSplit.length;
			if(n<=2 || A.empty(fnSplit[2]))
				sum = UNKNOWN+SEP_SUM+ext;
			else {
				String num;
				if(n > 3) {
					num = fnSplit[3];
					sum = num + (fnSplit[2].equals("in") ? IN : OUT);
				}
				else {
					num = fnSplit[2];
					if(!num.equals("in") && !num.equals("out"))
						sum = new String(num);										// Sanity versions prior to 1.93 have no "in" or "out" in filename!
					else {
						sum = UNKNOWN + (num.equals("in") ? IN : OUT);
						num = "";
					}
				}
				if(num.length() > 1) {
					final Cursor c = resolver.query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num)), projection, null, null, null);
					if(c != null) {
						if(c.moveToFirst()) {
							final String name = c.getString(c.getColumnIndex(PhoneLookup.DISPLAY_NAME));
							if(!A.empty(name)) sum += '\n'+name;
						}
						c.close();
					}
				}
				sum += SEP_SUM+ext;
			}
			setSummary(sum);
			// set listener
			setOnPreferenceChangeListener(this);
		}

		public boolean onPreferenceChange(Preference p, Object v) {
			if((Boolean)v) selected.push(this);
			else           selected.remove(this);
			return true;
		}
	
	}
}
