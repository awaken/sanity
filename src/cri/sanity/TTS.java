package cri.sanity;

import java.util.HashMap;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;


public class TTS implements OnInitListener, OnUtteranceCompletedListener
{
	public  final static int    STREAM     = Dev.VOL_ALARM;
	private final static String STREAM_STR = "4"; 	// string value of STREAM

	protected TextToSpeech tts;
	protected String  id;
	protected int     repeat = 0;
	protected int     pause;
	protected int     vol    = -1;
	protected boolean solo   = false;
	protected boolean filter;
	protected HashMap<String,String> pars;

	public TTS(String text, boolean filterNum)
	{
		id     = text;
		filter = filterNum;
		tts    = new TextToSpeech(A.app(), this);
	}

	public final void stop()
	{
		repeat = 0;
		if(tts.isSpeaking()) tts.stop();
	}

	public final void shutdown()
	{
		stop();
		tts.shutdown();
		if(vol >= 0) Dev.setVolume(STREAM, vol, 0);
		if(solo) Dev.mute(Dev.VOL_RING, false);
	}
	
	public void onError() { A.notify(A.s(R.string.err_tts_init)); }

	@Override
	public void onInit(int status)
	{
		if(status != TextToSpeech.SUCCESS) { onError(); return; }
		if(filter) {
			// check if announce
			if(!CallFilter.includes(id, "tts", true)) return;
			if(A.empty(id)) {
				id = A.gets(K.TTS_ANONYM);
				if(id.length() <= 0) return;
			} else {
				id = CallFilter.searchName(id);
				if(A.empty(id)) {
					id = A.gets(K.TTS_UNKNOWN);
					if(id.length() <= 0) return;
				}
			}
		}
		// volume setup
		int vol = A.geti(K.TTS_VOL);
		if(vol >= 0) {
			this.vol = Dev.getVolume(STREAM);
			Dev.setVolume(STREAM, vol);
		}
		if(solo = A.is(K.TTS_SOLO)) Dev.mute(Dev.VOL_RING, true);
		// tone setup
		final int tone = A.geti(K.TTS_TONE);
		if(tone > 0) tts.setPitch(tone * (1f/100f));
		// what and how announce
		id     = (A.gets(K.TTS_PREFIX) + id + A.gets(K.TTS_SUFFIX)).trim();
		repeat =  A.geti(K.TTS_REPEAT);
		pause  =  A.geti(K.TTS_PAUSE);
		// finally speak!
		pars = new HashMap<String,String>();
		pars.put(Engine.KEY_PARAM_UTTERANCE_ID, STREAM_STR);
		pars.put(Engine.KEY_PARAM_STREAM      , STREAM_STR);
		tts.setOnUtteranceCompletedListener(this);
		tts.speak(id, TextToSpeech.QUEUE_FLUSH, pars);
	}

	@Override
	public void onUtteranceCompleted(String idUtter)
	{
		if(--repeat > 0) {
			tts.playSilence(pause, TextToSpeech.QUEUE_ADD, null);
			tts.speak      (id   , TextToSpeech.QUEUE_ADD, pars);
		}
		else if(solo)
			new Task(){ public void run(){ Dev.mute(Dev.VOL_RING, solo = false); }}.exec(100);
	}

}
