package cri.sanity.screen;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;
import cri.sanity.*;
import cri.sanity.pref.*;
import cri.sanity.util.*;


public class TtsActivity extends ScreenActivity
{
	private static final String KEY_GLOBAL      = "tts_global";
	private static final String KEY_TEST        = "tts_test";
	private static final String KEY_FILTER      = "filter_tts";
	private static final String KEY_SMS_FILTER  = "filter_ttsms";
	private static final String KEY_SMS_SHARED  = "ttsms_shared";
	private static final String KEY_VOL         = K.TTS_VOL     + K.WS;
	private static final String KEY_SMS_VOL     = K.TTS_SMS_VOL + K.WS;
	private static final int    CODE_CHECK      = 1;
	private static final int    TEST_MIN_REPEAT = 10;
	private static final int    TEST_MAX_REPEAT = 10;
	private static final String TEST_TXT_REPEAT = A.name();

	private TTS     tts;
	private Handler handler;

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    handler = new Handler();
 		on(K.TTS, new Change(){ public boolean on(){
 			if(!(Boolean)value) return true;
 			try { startActivityForResult(new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA), CODE_CHECK); }
 			catch(Exception e) { ttsBroken(); }
 	 		return false;
 		}});
 		if(A.is(K.TTS_SOLO)) fixAudio();
 		on(K.TTS_SOLO, new Change(){ public boolean on(){
 			if((Boolean)value) fixAudio();
 			return true;
 		}});
 		on(K.TTS_STREAM, new Change(){ public boolean on(){
 			final boolean alt = (Boolean)value;
 			final int stream = getVolumeStream(alt);
 			int vol = A.geti(K.TTS_VOL);
 			if(vol >= 0) {
 				final int volmax = A.audioMan().getStreamMaxVolume(stream);
 				if(vol > volmax) {
 					A.put(K.TTS_VOL, volmax);
 					((PList)pref(KEY_VOL)).setValue(volmax);
 				}
 			}
 			vol = A.geti(K.TTS_SMS_VOL);
 			if(vol >= 0) {
 				final int volmax = A.audioMan().getStreamMaxVolume(stream);
 				if(vol > volmax) {
 					A.put(K.TTS_VOL, volmax);
 					((PList)pref(KEY_SMS_VOL)).setValue(volmax);
 				}
 			}
 			setVolumeLevels   (stream);
 			setVolumeLevelsSMS(stream);
 			return true;
 		}});
 		setVolumeLevels();
 		setVolumeLevelsSMS();
 		on(KEY_GLOBAL, new Click(){ public boolean on(){
 			Intent i = new Intent();
 			i.addCategory(Intent.CATEGORY_LAUNCHER);
 			i.setComponent(new ComponentName("com.android.settings", "com.android.settings.TextToSpeechSettings"));
 			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			try { startActivity(i); } catch(Exception e) {}
 			return true;
 		}});
 		on(KEY_TEST, new Click(){ public boolean on(){
 			pref.setEnabled(false);
 			ttsFree();
 			tts = new TTS(TEST_TXT_REPEAT, false, false, false) {
 				@Override
 				protected void onError() { A.toast(R.string.err_tts_init); }
 				@Override
 				public void onInit(int status) {
 					super.onInit(status);
 					if(repeat <= 0) return;
 					if(repeat > TEST_MAX_REPEAT) repeat = TEST_MIN_REPEAT;
 					Toast.makeText(A.app(), A.s(R.string.announce)+": \""+id+'"', Toast.LENGTH_LONG).show();
 				}
 				@Override
 				public void onUtteranceCompleted(String idUtter) {
 					super.onUtteranceCompleted(idUtter);
 					if(repeat <= 0) handler.post(new Runnable() {
 						@Override
 						public void run() {
	 						ttsFree();
	 						pref.setEnabled(true);
 						}
 					});
 				}
 			};
 			return true;
 		}});
    on(KEY_SMS_SHARED, new Change(){ public boolean on(){
    	final boolean on = (Boolean)value;
    	final PFilter p  = prefSmsFilter();
    	p.updateSum(!on);
    	p.setEnabled(!on);
    	return true;
    }});
 		fullOnly(K.TTS_HEADSET, K.TTS_SOLO, KEY_VOL, KEY_SMS_VOL, K.TTS_REPEAT+K.WS, K.TTS_PAUSE+K.WS, KEY_FILTER,
 		         K.TTS_SMS_PREFIX, K.TTS_SMS_SUFFIX, KEY_SMS_SHARED, KEY_SMS_FILTER);
  }

	@Override
	public void onResume()
	{
		super.onResume();
		setChecked(KEY_SMS_SHARED, !A.is(K.TTS_SMS_FILTER));
	}

	@Override
	public void onPause()
	{
		ttsFree();
		super.onPause();
	}

	@Override
	protected void onActivityResult(int request, int res, Intent i)
	{
		if(request != CODE_CHECK) return;
		if(res == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
			setChecked(K.TTS, true);
		else {
			Alert.msg(
				A.rawstr(R.raw.tts_install),
				new Alert.Click(){ public void on(){
					try { startActivity(new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)); }
					catch(Exception e) { ttsBroken(); }
				}},
				null,
				Alert.OKCANC
			);
		}
	}

	public  static final int getVolumeStream()               { return getVolumeStream   (A.is(K.TTS_STREAM)); }
	public  static final int getVolumeStreamSMS()            { return getVolumeStreamSMS(A.is(K.TTS_STREAM)); }
	private static final int getVolumeStream   (boolean alt) { return alt? TTS.STREAM2_INT : TTS.STREAM1_INT; }
	private static final int getVolumeStreamSMS(boolean alt) { return alt? TTS.STREAM3_INT : TTS.STREAM1_INT; }
	private void setVolumeLevels   (int stream)              { VolumeActivity.setVolumeLevels((PList)pref(KEY_VOL    ), stream); }
	private void setVolumeLevelsSMS(int stream)              { VolumeActivity.setVolumeLevels((PList)pref(KEY_SMS_VOL), stream); }
	private void setVolumeLevels()                           { setVolumeLevels   (getVolumeStream   ()); }
	private void setVolumeLevelsSMS()                        { setVolumeLevelsSMS(getVolumeStreamSMS()); }

	private void ttsBroken() { Alert.msg(A.rawstr(R.raw.tts_broken)); }

	private void ttsFree()
	{
		if(tts == null) return;
		tts.shutdown();
		tts = null;
	}

	private PFilter prefSmsFilter() { return (PFilter)pref(KEY_SMS_FILTER); }

	private void fixAudio() {
		if(isAudioWarn())
			setChecked(K.TTS_STREAM, true);
	}

	private static boolean isAudioWarn() {
		return !A.is(K.TTS_STREAM) && Dev.getSysInt("notifications_use_ring_volume")>0;
	}

}
