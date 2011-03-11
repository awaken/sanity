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


// tool class of application preferences
public final class P
{
	//---- public api

	public static final Map<String,Object> getDefaults() {
		if(defs == null) defs = K.getDefaults();
		return defs;
	}
	public static final void setDefaults() {
		A.edit().clear();
		A.putAll(getDefaults()).commit();
		setWrapKeys();
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
	
	public static final Map<String,Object> skipKeysMap() {
		Map<String,Object> map = new HashMap<String,Object>();
		for(String k : K.skipKeys()) map.put(k, null);
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
		Map<String,?> m = load(fn);
		if(m == null) return false;
		A.edit().clear();
		A.putAll(m).commit();
		upgrade();
		return true;
	}
	public static final Map<String,?> load(String fn) {
		Map<String,Object> m = new HashMap<String,Object>();
		boolean read = false;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fn));
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

	public static final boolean upgrade() {
		final String ver = A.gets(K.VER );
		final int   beta = A.geti(K.BETA);
		if(A.ver().equals(ver) && beta==Conf.BETA) return false;
		upgrade(verNum(ver), beta);
		return true;
	}

	/*
	public static final Map<String,?> getAll(String prefix, String suffix) {
		Map<String,?>      all = A.prefs().getAll();
		Map<String,Object> map = new HashMap<String,Object>(all.size());
		for(String key : all.keySet())
			if((prefix==null || key.startsWith(prefix)) && (suffix==null || key.endsWith(suffix)))
				map.put(key, all.get(key));
		return map;
	}
	*/

	//---- private api

	private static void setWrapKeys() {
		for(String ki : K.wrapIntKeys()) {
			final String ks = ki + K.WS;
			try {
				A.put(ks, Integer.toString(A.geti(ki)));
			}	catch(Exception e) { try {
				final String s = A.gets(ki);
				A.put(ki, Integer.parseInt(s)).put(ks, s);
			} catch(Exception e2) { try {
				A.put(ki, A.getsi(ks));
			} catch(Exception e3) {
				final int v = (Integer)getDefaults().get(ki);
				A.put(ki, v).put(ks, Integer.parseInt(ks));
			}}}
		}
	}

	private static void upgrade(float oldVer, int beta) {
		if(oldVer < 0.1f)
			setDefaults();
		else {
			getDefaults();
			K.upgrade(oldVer, beta);
			A.commit();
			setWrapKeys();
			setVer();
		}
	}

	private static float verNum(String v) {
		try { return Float.parseFloat(v); }
		catch(Exception e) { return 0f; }
	}

	private static void setVer() { A.put(K.VER, A.ver()).putc(K.BETA, Conf.BETA); }

	private static Map<String,Object> defs;

}
