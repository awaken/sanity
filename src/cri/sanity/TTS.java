package cri.sanity;

import cri.sanity.util.*;
import java.util.HashMap;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.*;


public class TTS implements OnInitListener, OnUtteranceCompletedListener
{
	public  final static int    STREAM     = AudioManager.STREAM_NOTIFICATION;
	private final static String STREAM_STR = "5"; 	// string value of STREAM

	protected TextToSpeech tts;
	protected String  id;
	protected int     repeat = 0;
	protected int     pause;
	protected int     vol    = -1;
	protected boolean solo   = false;
	protected boolean force  = false;
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
		tts.stop();
	}

	public final void shutdown()
	{
		stop();
		tts.shutdown();
		if(vol >= 0) A.audioMan().setStreamVolume(STREAM, vol, 0);
		if(solo) A.audioMan().setStreamMute(AudioManager.STREAM_RING, false);
	}

	public void onError() { A.notify(A.s(R.string.err_tts_init)); }

	@Override
	public void onInit(int status)
	{
		if(status != TextToSpeech.SUCCESS) { onError(); return; }
		if(filter) {
			// check if announce
			final CallFilter cf = CallFilter.instance();
			if(!cf.includes(id, "tts", true)) return;
			if(A.empty(id)) {
				id = A.gets(K.TTS_ANONYM);
				if(id.length() <= 0) return;
			} else {
				id = cf.searchName(id);
				if(A.empty(id)) {
					id = A.gets(K.TTS_UNKNOWN);
					if(id.length() <= 0) return;
				}
			}
		}
		if(!force && !Dev.isRinging()) return;
		// volume setup
		final int vol = A.geti(K.TTS_VOL);
		if(vol >= 0) {
			this.vol = A.audioMan().getStreamVolume(STREAM);
			A.audioMan().setStreamVolume(STREAM, vol, 0);
		}
		if(solo = A.is(K.TTS_SOLO)) A.audioMan().setStreamMute(AudioManager.STREAM_RING, true);
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
		if(--repeat>0 && (force || Dev.isRinging())) {
			tts.playSilence(pause, TextToSpeech.QUEUE_ADD, null);
			tts.speak      (id   , TextToSpeech.QUEUE_ADD, pars);
		} else if(solo) {
			new Task(){ public void run(){
				A.audioMan().setStreamMute(AudioManager.STREAM_RING, solo = false);
			}}.exec(Conf.TTS_UNMUTE_DELAY);
		}
	}

}
