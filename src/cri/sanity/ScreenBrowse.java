package cri.sanity;

import java.io.File;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.format.DateFormat;


public class ScreenBrowse extends ActivityScreen
{
	private static final String ENTRY_FORMAT = Conf.REC_SHOW_PATTERN;
	private String dir;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		screener(ScreenBrowse.class, R.xml.prefs_browse);
		super.onCreate(savedInstanceState);
		final PreferenceCategory pc = (PreferenceCategory)pref(K.REC_BROWSE);
		dir = A.sdcardDir();
		if(dir == null) {
			final Preference p = new Preference(this);
			p.setTitle(R.string.err);
			p.setSummary(R.string.msg_dir_err);
			pc.addPreference(p);
			return;
		}
		String[] recs = new File(dir).list();
		if(recs.length <= 0) {
			final Preference p = new Preference(this);
			p.setTitle(R.string.empty);
			p.setSummary(R.string.msg_rec_empty);
			pc.addPreference(p);
			return;
		}
		for(String fn : recs) {
			if(!fn.startsWith(Conf.REC_PREFIX)) continue;
			pc.addPreference(new Pref(fn));
		}
	}

	@Override
	public boolean isMainActivity() { return true; }

	//---- inner class

	private class Pref extends Preference implements OnPreferenceClickListener
	{
		String fn;

		Pref(String fn) {
			super(ScreenBrowse.this);
			this.fn = fn;
			// set title
			setTitle(DateFormat.format(ENTRY_FORMAT, new File(dir,fn).lastModified()));
			// set summary
			String sep  = Conf.REC_SEP_MAIN;
			String sum  = "";
			fn          = fn.substring(sep.length());
			final int p = fn.lastIndexOf('.');
			if(p == fn.length()-4) {
				sum = fn.substring(p+1);
				fn  = fn.substring(0,p);
				if(     sum.equals("m4a")) sum = "MPEG4  ##  ";
				else if(sum.equals("3gp")) sum = "3GPP  ##  ";
				else if(sum.equals("amr")) sum = "AMR  ##  ";
			}
			String[] sp = fn.split(sep);
			if(sp.length > 3) sum += sp[3];
			setSummary(sum);
			// set listener
			setOnPreferenceClickListener(this);
		}
		
		public boolean onPreferenceClick(Preference p) {
			final Intent i = new Intent(Intent.ACTION_VIEW);
			//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.setDataAndType(Uri.parse("file://"+dir+'/'+fn), "audio/3gp");
			startActivity(i);
			return true;
		}

	}
	
}
