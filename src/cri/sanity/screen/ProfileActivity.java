package cri.sanity.screen;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashMap;
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
	private Pref prefSelected, prefActive;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		prefSelected = null;
		prefActive   = null;
		skipAllKeys  = true;
		screener(ProfileActivity.class, R.xml.prefs_profile, R.layout.img_profiles);
		super.onCreate(savedInstanceState);
		prefGroup = (PreferenceCategory)pref("profile");
		final String dir = A.sdcardDir();
		if(dir == null) { empty(R.string.err_dir); return; }
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
			case R.id.addnew  : addnew();                          break;
			case R.id.activate: activate();                        break;
			case R.id.saveas  : saveas();                          break;
			case R.id.rename  : rename();                          break;
			case R.id.delete  : delete();                          break;
			case R.id.show    : showDetails();                     break;
			case R.id.help    : A.alert(A.rawstr(R.raw.help_prf)); break;
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
				if(A.empty(name)) { A.toast(R.string.err_name); return; }
				String fn = A.sdcardDir();
				if(fn == null) { A.toast(R.string.err_dir); return; }
				final File file = new File(fn += '/'+name+PRF_EXT);
				if(file.exists()) { A.toast(String.format(A.s(R.string.err_exists), name)); return; }
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
				if(A.empty(name)) { A.toast(R.string.err_name); return; }
				if(name.equals(p.name)) return;
				File file = new File(p.file.getParentFile().getAbsolutePath(), name+PRF_EXT);
				if(file.exists()) { A.toast(String.format(A.s(R.string.err_exists), name)); return; }
				try {
					if(!p.file.renameTo(file)) throw new Exception();
					p.name = name;
					p.file = file;
					p.setTitle();
					p.setSummary();
					if(p.isActive()) A.putc(K.PRF_NAME, name);
				} catch(Exception e) {
					A.toast(R.string.err_rename);
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
					A.toast(R.string.err_del);
				}
			}},
			null,
			A.ALERT_OKCANC
		);
	}

	public static final String[][] sections() {
		return new String[][]{
			new String[]{ "general_cat", K.ENABLED, K.FORCE_BT_AUDIO, K.REVERSE_PROXIMITY },
			new String[]{ "devices_cat", K.AUTO_MOBDATA, K.AUTO_WIFI, K.AUTO_GPS, K.AUTO_BT, K.SKIP_BT, K.SKIP_MOBDATA, K.SKIP_HOTSPOT, K.SKIP_TETHER, K.REVERSE_BT, K.REVERSE_BT_TIMEOUT, K.BT_OFF },
			new String[]{ "proximity_cat", K.DISABLE_PROXIMITY, K.DISABLE_DELAY, K.ENABLE_PROXIMITY, K.ENABLE_DELAY, K.SCREEN_OFF, K.SCREEN_ON },
			new String[]{ "speaker_cat", K.SPEAKER_AUTO, K.SPEAKER_DELAY, K.SPEAKER_LOUD, K.SPEAKER_CALL, K.SPEAKER_CALL_DELAY, K.SPEAKER_SILENT_END, K.SPEAKER_ON_COUNT, K.SPEAKER_OFF_COUNT },
			new String[]{ "vol_cat", K.VOL_PHONE, K.VOL_WIRED, K.VOL_BT, K.VOL_SOLO },
			new String[]{ "notify_cat", K.NOTIFY_ENABLE, K.NOTIFY_DISABLE, K.NOTIFY_ACTIVITY, K.NOTIFY_VOLUME, K.NOTIFY_REC_STOP, K.VIBRATE_END },
			new String[]{ "rec_cat", K.REC, K.REC_FMT, K.REC_SRC, K.REC_START, K.REC_START_DELAY, K.REC_FILTER, K.REC_START_SPEAKER, K.REC_START_HEADSET, K.REC_START_DIR, K.REC_START_TIMES, K.REC_STOP, K.REC_STOP_DELAY, K.REC_STOP_SPEAKER, K.REC_STOP_HEADSET, K.REC_STOP_LIMIT, K.REC_AUTOREMOVE },
			new String[]{ "tts_cat", K.TTS, K.TTS_HEADSET, K.TTS_SKIP, K.TTS_SOLO, K.TTS_VOL, K.TTS_TONE, K.TTS_REPEAT, K.TTS_PAUSE, K.TTS_PREFIX, K.TTS_SUFFIX, K.TTS_ANONYM, K.TTS_UNKNOWN, K.TTS_FILTER },
			new String[]{ "block_cat", K.BLOCK, K.BLOCK_SKIP, K.BLOCK_MODE }
		};
	}
	public static final Map<String,Pair<Integer,Integer>> intLabVals() {
		Map<String,Pair<Integer,Integer>> m = new HashMap<String,Pair<Integer,Integer>>();
		Pair<Integer,Integer> pd  = p(R.array.disable_delay_labels, R.array.disable_delay_values);
		Pair<Integer,Integer> psc = p(R.array.speaker_count_labels, R.array.speaker_count_values);
		m.put(K.DISABLE_DELAY     , pd);
		m.put(K.ENABLE_DELAY      , p(R.array.enable_delay_labels, R.array.enable_delay_values));
		m.put(K.SPEAKER_DELAY     , pd);
		m.put(K.SPEAKER_CALL      , p(R.array.speaker_call_labels, R.array.speaker_call_values));
		m.put(K.SPEAKER_CALL_DELAY, pd);
		m.put(K.SPEAKER_ON_COUNT  , psc);
		m.put(K.SPEAKER_OFF_COUNT , psc);
		m.put(K.REC_FMT           , p(R.array.rec_fmt_labels, R.array.rec_fmt_values));
		m.put(K.REC_SRC           , p(R.array.rec_src_labels, R.array.rec_src_values));
		m.put(K.REC_START_DELAY   , pd);
		m.put(K.REC_STOP_DELAY    , pd);
		m.put(K.REC_START_HEADSET , p(R.array.rec_start_headset_labels , R.array.rec_headset_values));
		m.put(K.REC_STOP_HEADSET  , p(R.array.rec_stop_headset_labels  , R.array.rec_headset_values));
		m.put(K.REC_STOP_LIMIT    , p(R.array.rec_stop_limit_labels    , R.array.rec_stop_limit_values));
		m.put(K.REC_START_TIMES   , p(R.array.rec_start_times_labels   , R.array.rec_start_times_values));
		m.put(K.REC_START_DIR     , p(R.array.rec_start_dir_labels     , R.array.rec_start_times_values));
		m.put(K.REC_AUTOREMOVE    , p(R.array.rec_autoremove_labels    , R.array.rec_autoremove_values));
		m.put(K.REVERSE_BT_TIMEOUT, p(R.array.bt_reverse_timeout_labels, R.array.bt_reverse_timeout_values));
		m.put(K.TTS_TONE          , p(R.array.tts_tone_labels          , R.array.tts_tone_values));
		m.put(K.TTS_REPEAT        , p(R.array.tts_repeat_labels        , R.array.tts_repeat_values));
		m.put(K.TTS_PAUSE         , p(R.array.tts_pause_labels         , R.array.tts_pause_values));
		m.put(K.BLOCK_MODE        , p(R.array.block_mode_labels        , R.array.block_mode_values));
		return m;
	}
	private static Pair<Integer,Integer> p(int lab, int val) { return new Pair<Integer,Integer>(lab, val); }
	
	private void showDetails() {
		if(!isSelected()) return;
		final Map<String,?> mapPrf = P.load(prefSelected.file.getAbsolutePath());
		if(mapPrf == null) { A.toast(R.string.err_name); return; }
		final Map<String,Pair<Integer,Integer>> mapArr = intLabVals();
		StringBuilder msg = new StringBuilder(512);
		try {
			for(String[] sect : sections()) {
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
