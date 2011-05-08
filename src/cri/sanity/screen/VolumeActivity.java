package cri.sanity.screen;

import android.media.AudioManager;
import android.os.Bundle;
import cri.sanity.*;
import cri.sanity.pref.*;


public class VolumeActivity extends ScreenActivity
{
	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
		CharSequence[][] arr = getVolumeLevels(AudioManager.STREAM_VOICE_CALL);
  	for(String k : new String[]{ K.VOL_PHONE, K.VOL_WIRED, K.VOL_BT })
  		setVolumeLevels((PList)pref(k + K.WS), arr);
		setVolumeLevels((PList)pref(K.TTS_VOL    +K.WS), TtsActivity.getVolumeStream());
		setVolumeLevels((PList)pref(K.TTS_SMS_VOL+K.WS), TtsActivity.getVolumeStreamSMS());
  	if(!A.is(K.TTS)) {
  		setEnabled(K.TTS_VOL+K.WS, false);
  		if(!A.is(K.TTS_SMS)) setEnabled(K.TTS_SMS_VOL+K.WS, false);
  	}
  	fullOnly(K.TTS_VOL+K.WS, K.TTS_SMS_VOL+K.WS);
  }

	public static CharSequence[][] getVolumeLevels(int stream)
	{
  	final String lev = A.s(R.string.level) + ' ';
  	final int m = A.audioMan().getStreamMaxVolume(stream);
  	final int n = m + 1;
  	final CharSequence[] av = new CharSequence[n+1];
  	final CharSequence[] ae = new CharSequence[n+1];
  	av[0] = "-1";
  	av[1] =  "0";
  	av[n] = Integer.toString(m);
  	ae[0] = A.s(R.string.nochange);
  	ae[1] = lev + av[1] + " - " + A.s(R.string.min);
  	ae[n] = lev + av[n] + " - " + A.s(R.string.max);
  	for(int i=2; i<n; i++) {
  		av[i] = Integer.toString(i-1);
  		ae[i] = lev + av[i];
  	}
  	return new CharSequence[][]{ ae, av };
	}

	public static void setVolumeLevels(PList p, CharSequence[][] arr)
	{
		p.setEntries    (arr[0]);
		p.setEntryValues(arr[1]);
		p.update();
	}

	public static void setVolumeLevels(PList p, int stream)
	{
		setVolumeLevels(p, getVolumeLevels(stream));
	}

}
