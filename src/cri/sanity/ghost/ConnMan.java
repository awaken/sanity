package cri.sanity.ghost;

import cri.sanity.A;


public final class ConnMan extends GhostObj
{
	public ConnMan() { init(A.connMan()); }

	public String[] getTetheredIfaces()      { return callStrArr("getTetheredIfaces");      }
	public String[] getTetherableUsbRegexs() { return callStrArr("getTetherableUsbRegexs"); }

	public boolean isTetheringOn()
	{
    final String[] ifaces = getTetheredIfaces();
    if(ifaces==null || ifaces.length<=0) return false;

    final String[] regexs = getTetherableUsbRegexs();
    if(regexs==null || regexs.length<=0) return false;

    for(String iface : ifaces)
    	for(String regex : regexs)
    		if(iface.matches(regex)) return true;

    return false;
	}
	
}
