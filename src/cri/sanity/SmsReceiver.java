package cri.sanity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.format.DateFormat;
import cri.sanity.util.*;


public class SmsReceiver extends BroadcastReceiver
{
	private static final char   SEP     = Conf.SMS_SEP;
	private static final String SEP_S   = SEP + "";
	private static final String UNSEP   = Conf.SMS_UNSEP;
	private static final String UNSLASH = Conf.SMS_UNSLASH;

	private FileWriter logFile;
	private static CallFilter cf;

	public static final CallFilter callFilter() { return cf; }

	@Override
	public void onReceive(Context ctx, Intent intent) 
	{
		if(!A.isEnabled() || !A.is(K.BLOCK_SMS) || !A.is(K.BLOCK_FILTER)) return;
		final Bundle extras = intent.getExtras();
		if(extras == null) return;
    final Object[] pdus = (Object[])extras.get("pdus");
    if(pdus == null) return;
    final int n = pdus.length;
    if(n <= 0) return;
    if(cf == null) cf = new CallFilter();
		final String sect = A.is(K.BLOCK_SMS_FILTER) ? "blocksms" : "block";
    final int max = A.geti(K.BLOCK_SMS_MAX);
		int cnt = 0;
    for(int i=0; i<n; i++) {
  		try {
	    	final SmsMessage msg = SmsMessage.createFromPdu((byte[])pdus[i]);
	      final String    from = msg.getOriginatingAddress();
	      if(!cf.includes(from, sect, false)) continue;
	      if(++cnt == 1) abortBroadcast();
	      if(max != 0) {
	      	final String num = cf.lastNum();
	      	log(num, cf.searchName(num), msg.getMessageBody(), msg.getTimestampMillis());
	      }
  		} catch(Exception e) {}
    }
		if(cnt > 0) {
			if(A.is(K.BLOCK_SMS_NOTIFY))
				Blocker.notification(true);
			if(max != 0) {
				logClose();
				if(max > 0) logTrunc(max, cnt + A.geti(K.SMS_COUNT));
			}
		}
    cf.close();
	}

	private void log(String num, String name, String body, long time) throws IOException
	{
		if(logFile == null) logFile = new FileWriter(smsFn(), true);
		if(num  == null) num  = "";
		if(name == null) name = "";
		final String date = DateFormat.format(Conf.DATE_PATTERN, time).toString();
		body = body.replace("\r","").replace("\\",UNSLASH).replace("\n","\\n").replace(SEP_S, UNSEP);
		logFile.append(date + SEP + name + SEP + num + SEP + body + '\n');
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

	private void logTrunc(int max, int cnt)
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
			in = new BufferedReader(new FileReader(fn));
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
  		else throw new Exception(getClass().getName());
  	} catch(Exception e) {
  		A.putc(K.SMS_COUNT, getClass().getName().equals(e.getMessage()) ? cnt : 0);
  		try { new File(tmp).delete(); } catch(Exception e2) {}
  	}
	}

	private String smsFn() { return A.sdcardDir()+'/'+Conf.SMS_FN; }

}
