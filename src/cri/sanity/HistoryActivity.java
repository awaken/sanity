package cri.sanity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.util.Stack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import cri.sanity.util.*;


public abstract class HistoryActivity extends ScreenActivity
{
	private PreferenceGroup prefGroup;
	private boolean         load = false;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg==null || msg.obj==null) return;
			prefGroup.addPreference((Preference)msg.obj);
		}
	};

	//---- Activity override

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		skipAllKeys = true;
		screener(getClass(), R.xml.prefs_history, R.layout.img_history);
		super.onCreate(savedInstanceState);
		prefGroup = (PreferenceGroup)pref("history");
		Preference p = pref("clear");
		on(p, new Click(){ public boolean on(){
			if(prefGroup.getPreferenceCount() < 1) return true;
			Alert.msg(
				A.s(R.string.ask_clear_history),
				new Alert.Click(){ public void on(){
					try {
						if(!file().delete()) throw new Exception();
						prefGroup.removeAll();
						onClear();
					} catch(Exception e) {
						A.toast(R.string.err);
					}
				}},
				null
			);
			return true;
		}});
		p.setEnabled(false);
		pref("history_main").setTitle(mainTitle());
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		if(load)return;
		new Async(){ public void run(){
			readHistory(this);
			if(isCancelled()) return;
			handler.post(new Runnable(){ public void run(){ pref("clear").setEnabled(true); }});
		}}.execute();
	}

	//---- abstract api

	protected abstract String   fnHistory();
	protected abstract String   mainTitle();
	protected abstract Class<?> prefClass();
	protected abstract int      lineItems();
	protected abstract char     sep();

	//---- protected api

	protected void onClear() { }

	protected final void postErr(final int idMsg) {
		handler.post(new Runnable(){ public void run(){ A.toast(idMsg); }});
	}

	//---- private api

	private void readHistory(Async async)
	{
		prefGroup.removeAll();
		final File f = file();
		if(!f.exists()) { postErr(R.string.empty); return; }
		Stack<Preference> stack = new Stack<Preference>();
		final char sep = sep();
		boolean read = false;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(f), 8192);
			final Constructor<?> constr = prefClass().getDeclaredConstructors()[0];
			final int n = lineItems() - 1;
			String[] items = new String[lineItems()];
			for(;;) {
				if(async!=null && async.isCancelled()) return;
				final String line = in.readLine();
				if(line.length() <= 0) continue;
				int p = 0;
				for(int i=0; i<n; i++) {
					final int q = line.indexOf(sep, p);
					items[i] = line.substring(p, q).trim();
					p = q + 1;
				}
				items[n] = line.substring(p).trim();
				stack.push((Preference)constr.newInstance(this, items));
				read = true;
			}
		} catch(Exception e) {
			try { if(in != null) in.close(); } catch(Exception e2) {}
			if(!read) return;
		}
		int i = 0;
		while(!stack.isEmpty()) {
			Preference p = stack.pop();
			p.setTitle(++i+".   "+p.getTitle());
			Message msg = new Message();
			msg.obj = p;
			handler.sendMessage(msg);
			if(async!=null && async.isCancelled()) return;
		}
		load = true;
	}

	private File file() { return new File(A.sdcardDir(), fnHistory()); }

	//---- inner class

	private abstract class Async extends A.Async {}

}
