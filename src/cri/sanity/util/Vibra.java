package cri.sanity.util;

import android.os.Vibrator;
import cri.sanity.*;


public final class Vibra
{
	private static final long  OFF1_TIME = 100;
	private static final long  OFF2_TIME = 170;
	private static final long SHORT_TIME = 200;
	private static final long   MID_TIME = 400;
	private static final long  LONG_TIME = 600;
	private static long[] pattern;

	public static final void vibra()
	{
		Vibrator v = (Vibrator)A.app().getSystemService(A.VIBRATOR_SERVICE);
		if(v != null) v.vibrate(pattern, -1);
	}

	public static final void setMode() { setMode(A.geti(K.VIBRATE_MODE)); }

	public static final void setMode(int mode)
	{
		final long[] pat = getPattern(mode);
		pattern = pat==null ? getPattern(12) : pat;
	}

	private static long[] getPattern(int mode)
	{
		switch(mode) {
			case 11: return new long[]{ 0, SHORT_TIME };
			case 12: return new long[]{ 0,   MID_TIME };
			case 13: return new long[]{ 0,  LONG_TIME };
			case 21: return new long[]{ 0, SHORT_TIME, OFF1_TIME, SHORT_TIME };
			case 22: return new long[]{ 0,   MID_TIME, OFF1_TIME,   MID_TIME };
			case 23: return new long[]{ 0,  LONG_TIME, OFF2_TIME,  LONG_TIME };
			case 31: return new long[]{ 0, SHORT_TIME, OFF1_TIME, SHORT_TIME, OFF1_TIME, SHORT_TIME };
			case 32: return new long[]{ 0,   MID_TIME, OFF1_TIME,   MID_TIME, OFF1_TIME,   MID_TIME };
			case 33: return new long[]{ 0,  LONG_TIME, OFF2_TIME,  LONG_TIME, OFF2_TIME,  LONG_TIME };
			default: return null;
		}
	}

	static { setMode(); }

}
