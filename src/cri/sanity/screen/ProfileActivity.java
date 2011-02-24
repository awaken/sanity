package cri.sanity.screen;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Map;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.format.DateFormat;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import cri.sanity.*;


public class ProfileActivity extends ScreenActivity implements FilenameFilter
{
	private static final String TITLE_ACTIVE_PREFIX = ">> ";
	private static final String TITLE_ACTIVE_SUFFIX = " <<";
	private static final String SUM_ACTIVE   = "\n>>>  " + A.s(R.string.active_prf) + "  <<<";
	private static final String LASTMOD      = A.s(R.string.msg_last_modified) + ":  ";
	private static final String PRF_EXT      = Conf.PRF_EXT;

	private PreferenceCategory prefGroup;
	private Pref               prefSelected = null;
	private Pref               prefActive   = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		prefSelected = null;
		prefActive   = null;
		skipAllKeys  = true;
		screener(ProfileActivity.class, R.xml.prefs_profile, R.layout.img_profiles);
		super.onCreate(savedInstanceState);
		prefGroup = (PreferenceCategory)pref(K.PRF);
		final String dir = A.sdcardDir();
		if(dir == null) { empty(R.string.msg_dir_err); return; }
		File[] files = new File(dir).listFiles(this);
		if(files==null || files.length<=0) { empty(R.string.msg_prf_empty); return; }
		Arrays.sort(files, 0, files.length);
		final String prfName = A.gets(K.PRF_NAME);
		for(File f : files)
			prefGroup.addPreference(new Pref(f, prfName));
		if(prefSelected==null && A.has(K.PRF_NAME)) A.delc(K.PRF_NAME);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.profile, menu);
    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()) {
			case R.id.prf_addnew  : addnew();                          break;
			case R.id.prf_activate: activate();                        break;
			case R.id.prf_saveas  : saveas();                          break;
			case R.id.prf_rename  : rename();                          break;
			case R.id.prf_delete  : delete();                          break;
			case R.id.prf_show    : showDetails();                     break;
			case R.id.prf_help    : A.alert(A.rawstr(R.raw.help_prf)); break;
			default: return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public boolean isMainActivity() { return true; }

	@Override
	public boolean accept(File dir, String fn) { return fn.endsWith(PRF_EXT); }

	//---- private api
	
	private void empty(int msgId) { A.toast(msgId); }

	private boolean isEmpty() {
		if(prefGroup.getPreferenceCount() > 0) return false;
		A.toast(R.string.msg_prf_empty);
		return true;
	}
	
	private boolean isSelected() {
		if(isEmpty()) return false;
		if(prefSelected != null) return true;
		A.toast(R.string.msg_prf_nosel);
		return false;
	}

	private void addnew() {
		if(prefActive == null) addnewReally();
		else A.alert(
			A.s(R.string.ask_prf_addnew),
			new A.Click(){ public void on(){ dismiss(); addnewReally(); }},
			null,
			A.ALERT_OKCANC
		);
	}
	
	private void addnewReally() {
		A.alertText(A.s(R.string.msg_prf_addnew), new A.Edited() {
			@Override
			public void on(String name) {
				name = A.cleanFn(name, true);
				if(A.empty(name)) { A.toast(R.string.msg_err_name); return; }
				String fn = A.sdcardDir();
				if(fn == null) { A.toast(R.string.msg_dir_err); return; }
				final File file = new File(fn += '/'+name+PRF_EXT);
				if(file.exists()) { A.toast(String.format(A.s(R.string.msg_exists_err), name)); return; }
				final String prfOld = A.gets(K.PRF_NAME);
				A.putc(K.PRF_NAME, name);
				if(P.backup(fn))
					prefGroup.addPreference(new Pref(file, name));
				else {
					if(!A.empty(prfOld)) A.putc(K.PRF_NAME, prfOld);
					A.toast(R.string.msg_backup_failed);
				}
			}
		});
	}

	private void activate() {
		if(!isSelected() || prefSelected.isActive()) return;
		if(A.has(K.PRF_NAME)) activateReally();
		else A.alert(
			A.s(R.string.ask_prf_activate),
			new A.Click(){ public void on(){ dismiss(); activateReally(); }},
			null,
			A.ALERT_OKCANC
		);
	}
	
	private void activateReally() {
		if(!P.restore(prefSelected.file.getAbsolutePath()))
			A.alert(A.s(R.string.msg_restore_failed));
		else
			prefSelected.setActive(true);
	}

	private void saveas() {
		if(!isSelected()) return;
		final Pref p = prefSelected;
		A.alert(
			String.format(A.s(R.string.ask_prf_saveas), p.name),
			new A.Click(){ public void on(){
				final String prfOld = A.gets(K.PRF_NAME);
				A.putc(K.PRF_NAME, p.name);
				if(P.backup(p.file.getAbsolutePath())) {
					if(p.isActive()) p.setSummary();				// refresh last modified in summary
					else             p.setActive(true);
				} else {
					A.putc(K.PRF_NAME, prfOld);
					A.toast(R.string.msg_backup_failed);
				}
			}},
			null,
			A.ALERT_OKCANC
		);
	}

	private void rename() {
		if(!isSelected()) return;
		final Pref p = prefSelected;
		A.alertText(A.s(R.string.msg_prf_rename), p.name, new A.Edited() {
			@Override
			public void on(String name) {
				name = A.cleanFn(name, true);
				if(A.empty(name)) { A.toast(R.string.msg_err_name); return; }
				if(name.equals(p.name)) return;
				File file = new File(p.file.getParentFile().getAbsolutePath(), name+PRF_EXT);
				if(file.exists()) { A.toast(String.format(A.s(R.string.msg_exists_err), name)); return; }
				try {
					if(!p.file.renameTo(file)) throw new Exception();
					p.name = name;
					p.file = file;
					p.setTitle();
					p.setSummary();
					if(p.isActive()) A.putc(K.PRF_NAME, name);
				} catch(Exception e) {
					A.toast(R.string.msg_err_rename);
				}
			}
		});
	}

	private void delete() {
		if(!isSelected()) return;
		A.alert(
			String.format(A.s(R.string.ask_prf_delete), prefSelected.name),
			new A.Click(){ public void on(){
				try {
					if(!prefSelected.file.delete()) throw new Exception();
					prefGroup.removePreference(prefSelected);
					if(prefSelected.isActive()) {
						prefSelected = prefActive = null;
						A.delc(K.PRF_NAME);
					}
					else if(prefActive != null)
						prefActive.setChecked(true);
					else prefSelected = null;
				} catch(Exception e) {
					A.toast(R.string.msg_del_err);
				}
			}},
			null,
			A.ALERT_OKCANC
		);
	}
	
	private void showDetails() {
		if(!isSelected()) return;
		final Map<String,?> mapPrf = P.load(prefSelected.file.getAbsolutePath());
		if(mapPrf == null) { A.toast(R.string.msg_err_name); return; }
		final Map<String,Pair<Integer,Integer>> mapArr = K.intLabVals();
		StringBuilder msg = new StringBuilder();
		try {
			for(String[] sect : K.sections()) {
				msg.append("** ").append(A.s(A.rstring(sect[0]))).append('\n');
				final int n = sect.length;
				for(int i=1; i<n; i++) {
					try {
						final String k = sect[i];
						final String m = "- " + A.s(A.rstring(k+"_title"));
						msg.append(m).append(": ").append(valShow(mapArr, k, mapPrf.get(k))).append('\n');
					} catch(Exception exp) {}
				}
				msg.append('\n');
			}
		} catch(Exception e) {}
		A.alert(prefSelected.name, msg.toString());
	}

	private static String valShow(Map<String,Pair<Integer,Integer>> map, String k, Object o) {
		if(o instanceof Integer) {
			final int v = (Integer)o;
			final Pair<Integer,Integer> p = map.get(k);
			if(p != null) {
				final String[] labs = A.resources().getStringArray(p.first);
				final String[] vals = A.resources().getStringArray(p.second);
				final String   s    = Integer.toString(v);
				final int      n    = vals.length;
				for(int i=0; i<n; i++)
					if(vals[i].equals(s)) return labs[i];
			}
			if(v == -1) return A.s(R.string.nochange);
			return Integer.toString(v);
		}
		if(o instanceof Boolean) return A.s((Boolean)o? R.string.active : R.string.disactive);
		if(o instanceof Long   ) return Long.toString((Long)o);
		if(o instanceof Float  ) return Float.toString((Float)o);
		if(o instanceof String ) return (String)o;
		return "[...]";
	}

	//---- inner class
	
	private class Pref extends CheckBoxPreference implements OnPreferenceChangeListener
	{
		private File   file;
		private String name;

		private Pref(File f, String activePrfName) {
			super(ProfileActivity.this);
			file = f;
			name = f.getName();
			name = name.substring(0, name.lastIndexOf('.'));
			setPersistent(false);
			if(name.equals(activePrfName)) {
				setChecked(true);
				setActive(true);
			} else {
				setTitle(name);
				setSummary();
			}
			setWidgetLayoutResource(R.layout.checkbox_radio);
			setOnPreferenceChangeListener(this);
		}

		@Override
		public void setChecked(boolean checked) {
			if(checked == isChecked()) return;
			super.setChecked(checked);
			if(!checked) return;
			if(prefSelected != null) prefSelected.setChecked(false);
			prefSelected = this;
		}

		private boolean isActive() { return this == prefActive; }

		private void setActive(boolean active) {
			if(active == isActive()) return;
			if(!active)
				prefActive = null;
			else {
				if(prefActive != null) prefActive.setActive(false);
				prefActive = this;
			}
			setTitle();
			setSummary();
		}
		
		private void setTitle() {
			setTitle(isActive()? TITLE_ACTIVE_PREFIX+name+TITLE_ACTIVE_SUFFIX : name);
		}

		private void setSummary() {
			String sum = LASTMOD + DateFormat.format(Conf.PRF_DATE, file.lastModified());
			if(isActive()) sum += SUM_ACTIVE;
			setSummary(sum);
		}
		
		@Override
		public boolean onPreferenceChange(Preference p, Object v) {
			return (Boolean)v || !isChecked();
		}
	}

}
