package cri.sanity;

import cri.sanity.util.*;
import java.util.HashMap;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.*;


public class TTS implements OnInitListener, OnUtteranceCompletedListener
{
	public  final static int    STREAM1_INT = AudioManager.STREAM_NOTIFICATION;
	public  final static int    STREAM2_INT = AudioManager.STREAM_ALARM;
	public  final static int    STREAM3_INT = AudioManager.STREAM_SYSTEM;
	private final static String STREAM1_STR = "5"; 	// string value of STREAM1_INT
	private final static String STREAM2_STR = "4"; 	// string value of STREAM2_INT
	private final static String STREAM3_STR = "1"; 	// string value of STREAM3_INT

	protected TextToSpeech tts;
	protected String  id;
	protected int     vol    = -1;
	protected int     repeat = 0;
	protected int     pause;
	protected int     stream;
	protected boolean solo   = false;
	protected boolean force  = false;
	protected boolean filter;
	protected boolean sms;
	protected HashMap<String,String> pars;

	public TTS(String text, boolean filterNum, boolean ringOnly, boolean smsSpeak)
	{
		id     = text;
		filter = filterNum;
		force  = !ringOnly;
		sms    = smsSpeak;
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
		if(vol >= 0) A.audioMan().setStreamVolume(stream, vol, 0);
		if(solo)
			if(sms) A.audioMan().setStreamSolo(stream, false);
			else    A.audioMan().setStreamMute(AudioManager.STREAM_RING, false);
	}
	
	protected void onError() { A.notify(A.s(R.string.err_tts_init)); }

	@Override
	public void onInit(int status)
	{
		if(status != TextToSpeech.SUCCESS) { onError(); off(); return; }
		if(filter) {
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
		String streamStr;
		if(A.is(K.TTS_STREAM)) { stream = STREAM2_INT; streamStr = STREAM2_STR; }
		else if(!sms)          { stream = STREAM1_INT; streamStr = STREAM1_STR; }
		else                   { stream = STREAM3_INT; streamStr = STREAM3_STR; }
		final AudioManager audioMan = A.audioMan();
		// tone setup
		final int tone = A.geti(K.TTS_TONE);
		if(tone > 0) tts.setPitch(tone * (1f/100f));
		// what and how announce
		String keyPrefix, keySuffix;
		int vol;
		if(sms) {
			if(solo = A.is(K.TTS_SOLO)) audioMan.setStreamSolo(stream, true);
			keyPrefix = K.TTS_SMS_PREFIX;
			keySuffix = K.TTS_SMS_SUFFIX;
			repeat    = 1;
			vol       = A.geti(K.TTS_SMS_VOL);
		} else {
			if(solo = A.is(K.TTS_SOLO)) audioMan.setStreamMute(AudioManager.STREAM_RING, true);
			keyPrefix = K.TTS_PREFIX;
			keySuffix = K.TTS_SUFFIX;
			repeat    = A.geti(K.TTS_REPEAT);
			pause     = A.geti(K.TTS_PAUSE);
			vol       = A.geti(K.TTS_VOL);
		}
		if(vol >= 0) {
			this.vol = audioMan.getStreamVolume(stream);
			audioMan.setStreamVolume(stream, vol, 0);
		}
		id = (A.gets(keyPrefix) + id + A.gets(keySuffix)).trim();
		// finally speak!
		pars = new HashMap<String,String>();
		pars.put(Engine.KEY_PARAM_UTTERANCE_ID, streamStr);
		pars.put(Engine.KEY_PARAM_STREAM      , streamStr);
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
		} else off();
	}

	private void off()
	{
		if(!sms) return;
		shutdown();
	}

}
