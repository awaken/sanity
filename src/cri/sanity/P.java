package cri.sanity;

import java.util.Map;


// tool class of application preferences
public final class P
{
	//---- public api

	public static final Map<String,Object> getDefaults() { return defsBuild(); }
	public static final void               setDefaults() { A.putAll(defsBuild()); setVer(); }

	public static final boolean upgrade()
	{
		final String ver = A.gets(K.VER);
		if(A.ver().equals(ver)) return false;
		upgrade(verNum(ver));
		return true;
	}

	public static final void setDef(String key)
	{
		final Object val = defs.get(key);
		if(val != null) A.put(key, val);
	}

	public static final void setDefIfNew(String key) { if(!A.has(key)) setDef(key); }
	
	public static final void renameBool(String dst, String old) {
		if(A.has(old)) A.put(dst, A.is(old)).del(old);
		else setDefIfNew(dst);
	}
	
	//---- private api

	private P() { }

	private static Map<String,Object> defsBuild()
	{
		if(defs == null) defs = K.getDefaults();
		return defs;
	}

	private static void upgrade(float oldVer)
	{
		if(oldVer < 0.1)
			setDefaults();
		else {
			defsBuild();
			K.upgrade(oldVer);
			setVer();
		}
	}

	private static float verNum(String v) {
		try { return Float.parseFloat(v); }
		catch(Exception e) { return 0; }
	}

	private static void setVer() { A.putc(K.VER, A.ver()); }

	private static Map<String,Object> defs;

}
