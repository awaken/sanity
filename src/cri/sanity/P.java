package cri.sanity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// tool class of application preferences
public final class P
{
	//---- public api

	public static final Map<String,Object> getDefaults() { return defsBuild(); }
	public static final void               setDefaults() { A.putAll(defsBuild()); setVer(); }

	public static final void setDef(String ... keys) { for(final String k : keys) setDef(k); }
	public static final void setDef(String key) {
		final Object val = defs.get(key);
		if(val != null) A.put(key, val);
	}

	public static final void setDefIfNew(String ... keys) { for(final String k : keys) setDefIfNew(k); }
	public static final void setDefIfNew(String key)      { if(!A.has(key)) setDef(key); }
	
	public static final void renameBool(String dst, String old) {
		if(A.has(old)) A.put(dst, A.is(old)).del(old);
		else setDefIfNew(dst);
	}

	public static final boolean backupExists() {
		final String dir = A.sdcardDir();
		if(dir == null) return false;
		return new File(dir+'/'+Conf.BACKUP_FN).exists();
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
			Map<String,Object> skipMap = skipKeysMap();
			BufferedWriter out = new BufferedWriter(new FileWriter(fn, false));
			final Map<String,?> map = A.prefs().getAll();
			for(Map.Entry<String,?> e : map.entrySet()) {
				final String k = e.getKey();
				if(skipMap.containsKey(k)) continue;
				final Object v = e.getValue();
				final String c = v.getClass().getName();
				out.write(k+"=("+c.substring(c.lastIndexOf('.')+1)+')'+v+'\n');
			}
			out.flush();
			out.close();
			return true;
		} catch(IOException e) {
			//A.logd("error writing file \""+fn+"\":"+e.getMessage());
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
				if(     cls.equals("String" )) A.put(key, val);
				else if(cls.equals("Boolean")) A.put(key, Boolean.parseBoolean(val));
				else if(cls.equals("Integer")) A.put(key, Integer.parseInt(val));
				else if(cls.equals("Float"  )) A.put(key, Float.parseFloat(val));
				else if(cls.equals("Long"   )) A.put(key, Long.parseLong(val));
				else continue;
				read = true;
				//A.logd("restore: "+key+"=("+cls+')'+val);
			}
		} catch(Exception e) {
			try { if(in != null) in.close(); } catch(Exception e2) {}
			//if(!read) A.logd("error reading file \""+fn+"\":"+e.getMessage());
		}
		if(read) { A.commit(); upgrade(); }
		return read;
	}

	public static final boolean upgrade() {
		final String ver = A.gets(K.VER);
		if(A.ver().equals(ver)) return false;
		upgrade(verNum(ver));
		return true;
	}

	//---- private api

	private P() { }

	private static Map<String,Object> defsBuild() {
		if(defs == null) defs = K.getDefaults();
		return defs;
	}

	private static void upgrade(float oldVer) {
		if(oldVer < 0.1)
			setDefaults();
		else {
			defsBuild();
			K.upgrade(oldVer);
			setVer();
		}
	}

	private static float verNum(String v) {
		for(;;) {
			if(A.empty(v)) return 0;
			try { return Float.parseFloat(v); } catch(Exception e) {}
			final int p = v.lastIndexOf('.');
			if(p < 1) return 0;
			v = v.substring(0, p);
		}
	}

	private static void setVer() { A.putc(K.VER, A.ver()); }

	private static Map<String,Object> defs;

}
