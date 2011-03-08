package cri.sanity;

import android.media.AudioManager;


public final class Blocker
{
	public  static final int MODE_HANGUP = 0;
	public  static final int MODE_FLIGHT = 1;
	public  static final int MODE_SILENT = 2;
	private static final int MODE_NONE   = -1;
	
	private static int mode = MODE_NONE;
	private static int ring;
	
	public static final boolean apply(int mode)
	{
		switch(mode) {
			case MODE_HANGUP:
				try {
					if(!Dev.iTel().endCall()) return false;
				} catch(Exception e) {
					return false;
				}
				break;
			case MODE_FLIGHT:
				if(!Dev.enableFlightMode(true)) return false;
				break;
			case MODE_SILENT:
				ring = A.audioMan().getRingerMode();
				if(ring == AudioManager.RINGER_MODE_SILENT) return false;
				A.audioMan().setRingerMode(AudioManager.RINGER_MODE_SILENT);
				Blocker.mode = mode;
			default:
				return false;
		}
		Blocker.mode = mode;
		return true;
	}
	
	public static final void shutdown()
	{
		switch(mode) {
			case MODE_FLIGHT:
				Dev.enableFlightMode(false);
				break;
			case MODE_SILENT:
				A.audioMan().setRingerMode(ring);
				break;
		}
		mode = MODE_NONE;
	}

}
