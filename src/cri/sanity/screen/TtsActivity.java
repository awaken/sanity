package cri.sanity.screen;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;
import cri.sanity.*;
import cri.sanity.pref.*;


public class TtsActivity extends ScreenActivity
{
	private static final int CODE_CHECK  = 1;
	private static final int TEST_REPEAT = 2;

	private TTS tts;

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg == null) return;
			ttsFree();
			pref("tts_test").setEnabled(true);
		}
	};

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
 		on(K.TTS, new Change(){ public boolean on(){
 			if(!(Boolean)value) return true;
 	 		startActivityForResult(new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA), CODE_CHECK);
 	 		return false;
 		}});
 		VolumeActivity.setVolumeLevels((PList)pref(K.TTS_VOL+K.WS), TTS.STREAM);
 		fullOnly(K.TTS_HEADSET, K.TTS_SOLO, K.TTS_VOL+K.WS, K.TTS_REPEAT+K.WS, K.TTS_PAUSE+K.WS, "filter_tts");
 		on("tts_global", new Click(){ public boolean on(){
 			Intent i = new Intent();
 			i.addCategory(Intent.CATEGORY_LAUNCHER);
 			i.setComponent(new ComponentName("com.android.settings", "com.android.settings.TextToSpeechSettings"));
 			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			try { startActivity(i); } catch(Exception e) {}
 			return true;
 		}});
 		on("tts_test", new Click(){ public boolean on(){
 			pref.setEnabled(false);
 			ttsFree();
 			tts = new TTS(A.name(), false) {
 				@Override
 				public void onError() { A.toast(R.string.err_tts_init); }
 				@Override
 				public void onInit(int status) {
 					super.onInit(status);
 					if(repeat <= 0) return;
 					if(repeat > TEST_REPEAT) repeat = TEST_REPEAT;
 					Toast.makeText(A.app(), A.s(R.string.announce)+": \""+id+'"', Toast.LENGTH_LONG).show();
 				}
 				@Override
 				public void onUtteranceCompleted(String idUtter) {
 					super.onUtteranceCompleted(idUtter);
 					if(repeat <= 0) handler.sendMessage(new Message());
 				}
 			};
 			return true;
 		}});
  }

	@Override
	public void onPause()
	{
		ttsFree();
		super.onPause();
	}

	@Override
	protected void onActivityResult(int request, int res, Intent data)
	{
		if(request != CODE_CHECK) return;
		if(res == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
			setChecked(K.TTS, true);
		else
			A.alert(
				A.rawstr(R.raw.tts_install),
				new A.Click(){ public void on(){ startActivity(new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)); }},
				null,
				A.ALERT_OKCANC
			);
	}

	private void ttsFree()
	{
		if(tts == null) return;
		tts.shutdown();
		tts = null;
	}
}
