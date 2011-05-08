package cri.sanity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import cri.sanity.util.*;


public class SmsReceiver extends BroadcastReceiver
{
	private static final char   SEP     = Conf.SMS_SEP;
	private static final String SEP_S   = SEP + "";
	private static final String UNSEP   = Conf.SMS_UNSEP;
	private static final String UNSLASH = Conf.SMS_UNSLASH;

	private FileWriter logFile;
	private String idTTS;
	private static String anonym, unknown;
	private static CallFilter cf;

	// used by TTS and Blocker
	public static final CallFilter callFilter() { return cf==null? new CallFilter() : cf; }

	@Override
	public void onReceive(Context ctx, Intent i) 
	{
		if(i==null || !A.isEnabled()) return;
		final Bundle extras = i.getExtras();
		if(extras == null) return;
    Object[] pdus = (Object[])extras.get("pdus");
    if(pdus==null || pdus.length<=0) return;

		final AudioManager am = A.audioMan();
		final boolean tts = A.is(K.TTS_SMS) && !MainService.isRunning()
			&& (!A.is(K.TTS_SKIP) || am.getRingerMode()==AudioManager.RINGER_MODE_NORMAL)
			&& (!A.is(K.TTS_HEADSET) || am.isWiredHeadsetOn() || am.isBluetoothA2dpOn() || am.isBluetoothScoOn());

    idTTS = null;
		if(A.is(K.BLOCK_SMS) && A.is(K.BLOCK_FILTER)) {
			pdus = block(pdus, extras, getSectTTS(tts));
	    if(cf != null) cf.close();
			if(pdus == null) return;
		}
		else if(tts) filterTTS(pdus);

		if(pdus!=null && pdus.length>0 && A.is(K.SMS_ALERT))
			smsAlert(SmsMessage.createFromPdu((byte[])pdus[0]));
		
		if(A.empty(idTTS)) return;
		new TTS(new String(idTTS), false, false, true);
	}

	private Object[] block(Object[] pdus, Bundle extras, String sectTTS)
	{
    if(cf == null) cf = new CallFilter();
    final int max = A.geti(K.BLOCK_SMS_MAX);
    final int n   = pdus.length;
    final Map<String,Integer> freeNames = sectTTS==null? null : new HashMap<String,Integer>(n);
		final String sectBlock = A.is(K.BLOCK_SMS_FILTER) ? "blocksms" : "block";
    final boolean   log     = max != 0;
    final boolean[] blocked = new boolean[n];
    SmsMessage msg; String num = null;
		int blockCount = 0;
    for(int i=0; i<n; i++) {
  		try {
	    	msg = SmsMessage.createFromPdu((byte[])pdus[i]);
	    	num = msg.getDisplayOriginatingAddress();
	      if(!cf.includes(num, sectBlock, false)) throw new Exception();
	      if(log) log(num, cf.searchName(num), msg.getMessageBody(), msg.getTimestampMillis());
	      blocked[i] = true;
	      ++blockCount;
  		} catch(Exception e) {
  			blocked[i] = false;
  			if(freeNames != null) filterTTS(cf, sectTTS, num, freeNames);
  		}
    }
    if(freeNames!=null && !freeNames.isEmpty())
    	idTTS = TextUtils.join(", ", freeNames.keySet());
    if(blockCount <= 0)
    	return pdus;
		if(log) {
			logClose();
			if(max > 0) logTrunc(max, blockCount + A.geti(K.SMS_COUNT));
		}
		if(A.is(K.BLOCK_SMS_NOTIFY)) Blocker.notification(true);
    if(blockCount == n) {
    	abortBroadcast();
    	return null;
    }
  	final Object[] free = new Object[n - blockCount];
  	for(int k=0, i=0; i<n; i++)
  		if(!blocked[i]) free[k++] = pdus[i];
  	extras.putSerializable("pdus", free);
  	setResultExtras(extras);
  	return free;
	}

	private void log(String num, String name, String body, long time) throws IOException
	{
		if(logFile == null) logFile = new FileWriter(smsFn(), true);
		if(num  == null) num  = "";
		if(name == null) name = "";
		body = body.replace("\r","").replace("\\",UNSLASH).replace("\n","\\n").replace(SEP_S, UNSEP);
		logFile.append(A.date(time) + SEP + name + SEP + num + SEP + body + '\n');
	}
	
	private void logClose()
	{
    if(logFile == null) return;
  	try {
  		logFile.flush();
  		logFile.close();
  	} catch(IOException e) {}
  	logFile = null;
	}

	private static void logTrunc(int max, int cnt)
	{
  	if(cnt < max+max/2) {
  		A.putc(K.SMS_COUNT, cnt);
  		return;
  	}
  	boolean done = false;
  	BufferedReader in = null;
  	FileWriter     fw = null;
  	String         fn = null;
  	try { fn = smsFn(); } catch(Exception e) { return; }
	  final String tmp = fn + ".tmp";
	  try {
			in = new BufferedReader(new FileReader(fn), 8192);
			for(int n=cnt-max; --n>=0;)
				in.readLine();
	  	fw = new FileWriter(tmp, false);
	  	for(int i=0; i<max; i++)
	  		fw.append(in.readLine()+'\n');
	  	A.putc(K.SMS_COUNT, max);
	  	done = true;
  	} catch(Exception e) {}
  	try { in.close();             } catch(Exception e) {}
  	try { fw.flush(); fw.close(); } catch(Exception e) {}
  	if(!done) {
  		A.putc(K.SMS_COUNT, cnt);
  		try { new File(tmp).delete(); } catch(Exception e) {}
  		return;
  	}
  	try {
  		final File f = new File(fn);
  		if(f.delete()) new File(tmp).renameTo(f);
  		else throw new Exception("SmsReceiver");
  	} catch(Exception e) {
  		A.putc(K.SMS_COUNT, "SmsReceiver".equals(e.getMessage()) ? cnt : 0);
  		try { new File(tmp).delete(); } catch(Exception e2) {}
  	}
	}

	private void filterTTS(Object[] pdus) {
		if(cf == null) cf = new CallFilter();
    final int n = pdus.length;
    final Map<String,Integer> names = new HashMap<String,Integer>(n);
    final String sectTTS = getSectTTS(true);
    for(int i=0; i<n; i++) {
    	try {
    		filterTTS(cf, sectTTS, SmsMessage.createFromPdu((byte[])pdus[i]).getDisplayOriginatingAddress(), names);
    	} catch(Exception e) {}
    }
    cf.close();
    idTTS = names.isEmpty() ? null : TextUtils.join(", ", names.keySet());
	}

	private static void filterTTS(CallFilter cf, String sect, String num, Map<String,Integer> map) {
		if(!cf.includes(num, sect, true)) return;
		if(A.empty(num)) {
			if(anonym == null) anonym = A.gets(K.TTS_ANONYM);
			if(anonym.length() > 0) map.put(anonym, 1);
		} else if(!isTelNum(num)) {
			map.put(num, 1);
		} else {
			final String name = cf.searchName(num);
			if(!A.empty(name))
				map.put(name, 1);
			else {
				if(unknown == null) unknown = A.gets(K.TTS_UNKNOWN);
				if(unknown.length() > 0) map.put(unknown, 1);
			}
		}
	}
	
	private static void smsAlert(SmsMessage sms) {
		final String from = sms.getDisplayOriginatingAddress();
		final String name = new CallFilter().searchName(from);
		final String body = sms.getMessageBody();
		BlankActivity.force = true;
		BlankActivity.postSingleton(new Runnable(){ public void run(){
			Alert.activity = BlankActivity.getInstance();
			Alert.msg(
				String.format(A.s(R.string.msg_sms_from), A.empty(name)? from : name),
				body,
				new Alert.Click(){ public void on(){
					Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+from));
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					A.app().startActivity(i);
				}},
				null,
				Alert.REPLY
			).setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					BlankActivity.getInstance().postFinish();
					Alert.activity = null;
				}
			});
		}});
		Intent i = new Intent(A.app(), BlankActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		A.app().startActivity(i);
	}

	private static boolean isTelNum(String num) {
		for(char c : num.toCharArray())
			if(c!='+' && c!='-' && c<'0' && c>'9') return false;
		return true;
	}

	private static String smsFn() { return A.sdcardDir()+'/'+Conf.SMS_FN; }

	private static String getSectTTS(boolean tts) { return tts? A.is(K.TTS_SMS_FILTER)? "ttsms" : "tts" : null; }

}
