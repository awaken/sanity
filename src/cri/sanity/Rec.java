package cri.sanity;

import java.io.IOException;
import android.media.MediaRecorder;
import android.media.MediaRecorder.*;
import android.text.format.DateFormat;


public class Rec
{
	public static final int    SRC_MIC      = AudioSource.MIC;
	public static final int    SRC_CALL     = AudioSource.VOICE_CALL;
	public static final int    SRC_CAM      = AudioSource.CAMCORDER;
	public static final int    SRC_IN       = AudioSource.VOICE_UPLINK;
	public static final int    SRC_OUT      = AudioSource.VOICE_DOWNLINK;
	public static final int    SRC_RECOGN   = AudioSource.VOICE_RECOGNITION;
	public static final int    FMT_MP4      = OutputFormat.MPEG_4;
	public static final int    FMT_3GP      = OutputFormat.THREE_GPP;
	public static final int    FMT_AMR      = OutputFormat.RAW_AMR;
	public static final int    DEF_SRC      = SRC_MIC;
	public static final int    DEF_FMT      = FMT_MP4;
	public static final String DEF_PREFIX   = Conf.REC_PREFIX;
	public static final String DEF_SUFFIX   = "";
	public static final String FILE_PATTERN = Conf.REC_DATE_PATTERN+Conf.REC_SEP+Conf.REC_TIME_PATTERN;

	public int    src, fmt;
	public String prefix, suffix, fn;

	private boolean started = false;
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

	public final String  fn()        { return fn; }
	public final boolean isStarted() { return started; }

	public final void start()
	{
		try {
			if(started || !init()) return;
			mediaRec.prepare();
			mediaRec.start();
			started = true;
			//A.logd("rec started");
		} catch(Exception e) {
			A.notify(A.tr(R.string.msg_rec_err));
			//A.logd(e);
		}
	}

	public final void stop()
	{
		if(!started || mediaRec==null) return;
		started = false;
		try {
			mediaRec.stop();
			mediaRec.reset();
		} catch(Exception e) {}
		//A.logd("rec stopped");
	}

	public final void release()
	{
		if(mediaRec == null) return;
		try {
			if(started) stop();
			mediaRec.release();
		} catch(Exception e) {}
		mediaRec = null;
	}

	//---- private api

	private boolean init()
	{
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
			A.notify(A.tr(R.string.msg_dir_err));
			throw new IOException();
		}
		fn += '/' + prefix + DateFormat.format(FILE_PATTERN, A.now()) + suffix;
		switch(fmt) {
			case FMT_3GP: fn += ".3gp"; break;
			case FMT_MP4: fn += ".m4a"; break;
			case FMT_AMR: fn += ".amr"; break;
		}
		return fn;
	}

}
