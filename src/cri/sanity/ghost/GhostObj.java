package cri.sanity.ghost;


public class GhostObj
{
	private Object   obj;
	private Class<?> cls;
	private boolean  err;

	public GhostObj() {}
	public GhostObj(Object obj) { init(obj); }

	public final Object   obj()     { return obj; }
	public final Class<?> cls()     { return cls; }
	public final boolean  isValid() { return obj != null; }
	public final boolean  isErr()   { return err; }

	public final Object call(String method)
	{
		try {
			final Object res = cls.getMethod(method).invoke(obj);
			err = false;
			return res;
		} catch(Exception e) {
			err = true;
			return null;
		}
	}
	
	public final boolean callBool(String method)
	{
		final Object res = call(method);
		return !err && res!=null && res instanceof Boolean && (Boolean)res;
	}

	public final int callInt(String method)
	{
		final Object res = call(method);
		return !err && res!=null && res instanceof Integer ? (Integer)res : -1;
	}

	public final String[] callStrArr(String method)
	{
		final Object res = call(method);
		return !err && res!=null && res instanceof String[] ? (String[])res : null;
	}

	public final boolean callable(String method)
	{
		call(method);
		return !err;
	}

	public final boolean callable(String ... methods)
	{
		for(final String m : methods) {
			call(m);
			if(err) return false;
		}
		return true;
	}

	public final void init(Object obj)
	{
		this.err = obj == null;
		this.cls = err? null : obj.getClass();
		this.obj = obj;
	}

}
