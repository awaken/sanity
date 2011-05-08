package cri.sanity.util;

import java.io.IOException;
import android.media.MediaRecorder;
import android.media.MediaRecorder.*;
import android.text.format.DateFormat;
import cri.sanity.*;

public class Rec
{
	public  static final int    DEF_SRC      = AudioSource.MIC;
	public  static final int    DEF_FMT      = OutputFormat.THREE_GPP;
	private static final String DEF_PREFIX   = Conf.REC_PREFIX;
	private static final String DEF_SUFFIX   = "";
	private static final String FILE_PATTERN = Conf.REC_DATE_PATTERN+Conf.REC_SEP+Conf.REC_TIME_PATTERN;

	public int    src, fmt;
	public String prefix, suffix, fn;

	private boolean started = false;
	private boolean vanilla = true;
	private MediaRecorder mediaRec;

	//---- public api

	public Rec()                                               { setup(DEF_SRC, DEF_FMT, DEF_PREFIX, DEF_SUFFIX); }
	public Rec(int src, int fmt)                               { setup(src    , fmt    , DEF_PREFIX, DEF_SUFFIX); }
	public Rec(int src, int fmt, String suffix)                { setup(src    , fmt    , DEF_PREFIX, suffix    ); }
	public Rec(int src, int fmt, String prefix, String suffix) { setup(src    , fmt    , prefix    , suffix    ); }

	public void setup(int src, int fmt, String prefix, String suffix)
	{
		if(src    >= 0   ) this.src    = src;
		if(fmt    >= 0   ) this.fmt    = fmt;
		if(prefix != null) this.prefix = prefix;
		if(suffix != null) this.suffix = suffix;
		//A.logd("rec { src="+this.src+", fmt="+this.fmt+", prefix=\""+this.prefix+"\", suffix=\""+this.suffix+"\" }");
	}

	public final String  fn()        { return fn;      }
	public final boolean isStarted() { return started; }
	public final boolean isVanilla() { return vanilla; }

	// FIX: remove synchronized in start(), stop(), release()???

	public final synchronized void start()
	{
		try {
			if(started || !init()) return;
			mediaRec.prepare();
			mediaRec.start();
			started = true;
			vanilla = false;
			A.audioMan().setMicrophoneMute(false);	// FIX: remove???
			//A.logd("rec started");
		} catch(Exception e) {
			A.notify(A.s(R.string.err_rec));
			started = true;
			stop();
			//A.logd(e);
		}
	}

	public final synchronized void stop()
	{
		if(!started) return;
		started = false;
		if(mediaRec == null) return;
		try {
			mediaRec.stop();
			mediaRec.reset();
		} catch(Exception e) {}
		//A.logd("rec stopped");
	}

	public final synchronized void release()
	{
		stop();
		if(mediaRec == null) return;
		try { if(!vanilla) mediaRec.release(); } catch(Exception e) {}
		mediaRec = null;
		vanilla  = true;
	}

	//---- private api

	private boolean init()
	{
		A.audioMan().setMicrophoneMute(false);
		System.gc();
		if(mediaRec == null) mediaRec = new MediaRecorder();
		mediaRec.setAudioSource(src);
		mediaRec.setOutputFormat(fmt);
		mediaRec.setAudioEncoder(AudioEncoder.AMR_NB);
		try {
			mediaRec.setOutputFile(fn = getAudioFn());
			return true;
		} catch(IOException e) {
			fn = null;
			return false;
		}
	}

	private String getAudioFn() throws IOException
	{
		String fn = A.sdcardDir();
		if(fn == null) {
			A.notify(A.s(R.string.err_dir));
			throw new IOException();
		}
		fn += '/' + prefix + DateFormat.format(FILE_PATTERN, A.time()) + suffix;
		switch(fmt) {
			case OutputFormat.THREE_GPP: fn += ".3gp"; break;
			case OutputFormat.MPEG_4   : fn += ".m4a"; break;
			case OutputFormat.RAW_AMR  : fn += ".amr"; break;
		}
		return fn;
	}

}
