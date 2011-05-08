package cri.sanity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


// tool class of application preferences
public final class P
{
	//---- public api

	public static final Map<String,Object> getDefaults() {
		if(defs != null) return defs;
		defs = new HashMap<String,Object>();
		final Object[] list = K.getDefaults();
		final int n = list.length;
		for(int i=0; i<n; i+=2)
			defs.put((String)list[i], list[i+1]);
		return defs;
	}
	public static final void setDefaults() {
		final Map<String,?> bakMap = bakMap();
		A.edit().clear();
		A.putAll(getDefaults()).putAll(bakMap);
		setVer();
	}

	public static final void setDef(String ... keys) { for(String k : keys) setDef(k); }
	public static final void setDef(String key) {
		final Object val = defs.get(key);
		if(val != null) A.put(key, val);
	}

	public static final void setDefIfNew(String ... keys) { for(String k : keys) setDefIfNew(k); }
	public static final void setDefIfNew(String key)      { if(!A.has(key)) setDef(key); }
	
	public static final void renameBool(String dst, String old) {
		if(A.has(old)) A.put(dst, A.is(old)).del(old);
		else setDefIfNew(dst);
	}

	public static final boolean backupExists() {
		final String dir = A.sdcardDir();
		if(dir == null) return false;
		return new File(dir, Conf.BACKUP_FN).exists();
	}

	public static final String[] filterSections() {
		final Object[] shortcuts = PrefGroups.filterShortcuts();
		final int      row       = PrefGroups.SHORTCUT_FILTER_ROW;
		final int      n         = shortcuts.length / row;
		final String[] filters   = new String[n];
		for(int i=0; i<n; i+=row)
			filters[i] = (String)shortcuts[i*row + 2];
		return filters;
	}

	public static final String[] intLabels() {
		Set<String> set = PrefGroups.intLabVals().keySet();
		String[]    vol = PrefGroups.volumes();
		String[]    all = new String[set.size() + vol.length];
		int i = 0;
		for(String k : set) all[i++] = k;
		for(String k : vol) all[i++] = k;
		return all;
	}

	public static final Map<String,Object> skipKeysMap() {
		Map<String,Object> map = new HashMap<String,Object>();
		for(String k : PrefGroups.skipKeys()) map.put(k, null);
		return map;
	}

	// backup all preferences to file, using one text line for each preference (key/value pair): key=(ClassName)value
	public static final boolean backup() {
		final String dir = A.sdcardDir();
		if(dir == null) return false;
		return backup(dir+'/'+Conf.BACKUP_FN);
	}
	public static final boolean backup(String fn) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fn, false));
			final Map<String,?> skipMap = skipKeysMap();
			final Map<String,?> map     = A.prefs().getAll();
			final String[]      keys    = new String[map.size()];
			map.keySet().toArray(keys);
			Arrays.sort(keys);
			for(String k : keys) {
				if(skipMap.containsKey(k)) continue;
				final Object v = map.get(k);
				final String c = v.getClass().getName();
				out.write(k+"=("+c.substring(c.lastIndexOf('.')+1)+')'+v+'\n');
			}
			out.close();
			return true;
		} catch(IOException e) {
			return false;
		}
	}

	// restore all preferences from file, using one text line for each preference (key/value pair): key=(ClassName)value
	public static final boolean restore() {
		final String dir = A.sdcardDir();
		if(dir == null) return false;
		return restore(dir+'/'+Conf.BACKUP_FN);
	}
	public static final boolean restore(String fn) {
		final Map<String,?> loadMap = load(fn);
		if(loadMap == null) return false;
		final Map<String,?> bakMap = bakMap();
		A.edit().clear();
		A.putAll(loadMap).putAll(bakMap).commit();
		upgrade();
		return true;
	}
	public static final Map<String,?> load(String fn) {
		Map<String,Object> m = new HashMap<String,Object>();
		boolean read = false;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fn), 8192);
			for(;;) {
				final String line = in.readLine();
				if(line.length() <= 0) continue;
				int q, p = line.indexOf('=');
				if(p <= 0) continue;
				final String key = line.substring(0,p).trim();
				final String elm = line.substring(p+1).trim();
				if(key.length()<=0 || elm.length()<=0) continue;
				p = elm.indexOf('(') + 1;
				q = elm.indexOf(')', p+1) + 1;
				final String cls = elm.substring(p, q-p);
				final String val = elm.substring(q).trim();
				if(     cls.equals("String" )) m.put(key, val);
				else if(cls.equals("Boolean")) m.put(key, Boolean.parseBoolean(val));
				else if(cls.equals("Integer")) m.put(key, Integer.parseInt(val));
				else if(cls.equals("Float"  )) m.put(key, Float.parseFloat(val));
				else if(cls.equals("Long"   )) m.put(key, Long.parseLong(val));
				else continue;
				read = true;
			}
		} catch(Exception e) {
			try { if(in != null) in.close(); } catch(Exception e2) {}
			return read? m : null;
		}
	}
	
	public static final void removeFilters() {
		for(String k : A.prefs().getAll().keySet()) {
			if(!k.startsWith("filter_")) continue;
			if(k.startsWith("filter_enable_")) continue;
			if(k.contains("_count_")) A.put(k, 0);
			else A.del(k);
		}
		A.commit();
	}

	public static final boolean upgrade() {
		final int ver = verSaved();
		if(A.verCode() == ver) return false;
		upgrade(ver);
		return true;
	}

	//---- private api

	private static Map<String,Object> bakMap() {
		final String[] skipKeys = PrefGroups.skipKeys();
		final Map<String,Object> bakMap = new HashMap<String,Object>(skipKeys.length);
		for(String k : skipKeys) {
			final Object v = A.get(k);
			if(v != null) bakMap.put(k, v);
		}
		return bakMap;
	}

	private static void upgrade(int oldVer) {
		if(oldVer < 17000)
			setDefaults();
		else {
			final Map<String,Object> def = getDefaults();
			K.upgrade(oldVer);
			A.commit();
			for(String k : def.keySet())
				if(!A.has(k)) A.put(k, def.get(k));
			//A.commit();
			//setWrapKeys();
			setVer();
		}
	}
	
	private static int verSaved() {
		try {
			return A.geti(K.VER);
		} catch(Exception e) { try {
			final int ver = (int)(Float.parseFloat(A.gets(K.VER)) * 10000f);
			final int b = A.geti("beta");
			return b<1? ver : ver-100+b;
		} catch(Exception e2) {
			return 0;
		}}
	}

	private static void setVer() { A.putc(K.VER, A.verCode()); }

	private static Map<String,Object> defs;

}
