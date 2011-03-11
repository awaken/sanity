package cri.sanity.screen;

import cri.sanity.*;
import cri.sanity.pref.*;
import cri.sanity.util.*;
import android.media.AudioManager;
import android.os.Bundle;


public class SpeakerActivity extends ScreenActivity
{
	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
  	if(Dev.sensorProxim() != null) {
  		// disable "vol_speaker" when both "auto_speaker" and "speaker_call" are unchecked
    	on(K.SPEAKER_AUTO, new Click(){ public boolean on(){
    		pref(K.SPEAKER_VOL+K.WS).setEnabled(is(pref) || A.geti(K.SPEAKER_CALL)>0);
    		return false;
    	}});
  	}
  	else {
  		// if no proximity sensor found, disable automatic speaker
  		setChecked(K.SPEAKER_AUTO, false);
  		setEnabled(K.SPEAKER_AUTO, false);
  	}
  	on(K.SPEAKER_CALL+K.WS, new Change(){ public boolean on(){
  		final boolean enabled = !value.equals("0");
  		pref(K.SPEAKER_VOL+K.WS).setEnabled(enabled || is(K.SPEAKER_AUTO));
  		pref(K.SPEAKER_CALL_DELAY+K.WS).setEnabled(enabled);
  		return true;
  	}});
  	final boolean speakerCall = A.geti(K.SPEAKER_CALL) > 0;
  	pref(K.SPEAKER_CALL_DELAY+K.WS).setEnabled(speakerCall);
  	PList p = (PList)pref(K.SPEAKER_VOL + K.WS);
  	p.setEnabled(speakerCall || is(K.SPEAKER_AUTO));
  	VolumeActivity.setVolumeLevels(p, AudioManager.STREAM_VOICE_CALL);
	}

}
