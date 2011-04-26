package cri.sanity.screen;

import cri.sanity.*;
import cri.sanity.pref.*;
import cri.sanity.util.Alert;
import android.media.AudioManager;
import android.os.Bundle;


public class SpeakerActivity extends ScreenActivity
{
	private static final String SPEAKER_CALL_DELAY = K.SPEAKER_CALL_DELAY + K.WS;
	private static final String SPEAKER_CALL       = K.SPEAKER_CALL       + K.WS;
	private static final String SPEAKER_VOL        = K.SPEAKER_VOL        + K.WS;

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    final boolean speakerCall = A.geti(K.SPEAKER_CALL) > 0;
  	pref(SPEAKER_CALL_DELAY).setEnabled(speakerCall);
  	final PList p = (PList)pref(SPEAKER_VOL);
  	p.setEnabled(speakerCall || is(K.SPEAKER_AUTO));
  	VolumeActivity.setVolumeLevels(p, AudioManager.STREAM_VOICE_CALL);

  	on(SPEAKER_CALL, new Change(){ public boolean on(){
  		final boolean enabled = !value.equals("0");
  		pref(SPEAKER_VOL).setEnabled(enabled || is(K.SPEAKER_AUTO));
  		pref(SPEAKER_CALL_DELAY).setEnabled(enabled);
  		return true;
  	}});

  	if(A.sensorProxim() != null) {
  		// disable "vol_speaker" when both "auto_speaker" and "speaker_call" are unchecked
    	on(K.SPEAKER_AUTO, new Click(){ public boolean on(){
    		pref(SPEAKER_VOL).setEnabled(is(pref) || A.geti(K.SPEAKER_CALL)>0);
    		return false;
    	}});
  	} else {
  		// if no proximity sensor found, disable automatic speaker
  		setChecked(K.SPEAKER_AUTO, false);
  		setEnabled(K.SPEAKER_AUTO, false);
  		Alert.msg(A.rawstr(R.raw.proxim_none));
  	}
	}

}
