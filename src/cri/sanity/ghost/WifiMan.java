package cri.sanity.ghost;

import cri.sanity.A;


public final class WifiMan extends GhostObj
{
	public static final int HOTSPOT_DISABLING = 0;
	public static final int HOTSPOT_DISABLED  = 1;
	public static final int HOTSPOT_ENABLING  = 2;
	public static final int HOTSPOT_ENABLED   = 3;
	public static final int HOTSPOT_FAILED    = 4;

	public WifiMan() { init(A.wifiMan()); }

	public int getWifiApState() { return callInt("getWifiApState"); }

	public boolean isHotspotOn() {
		final int s = getWifiApState();
		return s==HOTSPOT_ENABLING || s==HOTSPOT_ENABLED;
	}

}
