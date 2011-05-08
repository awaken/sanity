package cri.sanity;

import cri.sanity.util.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;


public class OutgoingReceiver extends BroadcastReceiver implements OnCancelListener, OnDismissListener
{
	private static final String SEP  = ":\n     ";

	private static boolean confirmed = false;
	private static boolean anonym    = false;

	@Override
	public void onReceive(Context ctx, Intent i)
	{
		if(!A.isEnabled()) return;
		final String num = i.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		if(A.empty(num)) return;
		if(!A.is(K.ANONYM)) {
			setCallNum(num);
			return;
		}
		final CallFilter cf = new CallFilter();
		if(!cf.includes(num, "anonym", true)) {
			setCallNum(num);
			return;
		}
		if(!A.is(K.ANONYM_CONFIRM)) confirmed = anonym = true;
		if(confirmed) {
			confirmed = false;
			setCallNum(num);
			if(anonym) {
				anonym = false;
				setResultData(A.gets(K.ANONYM_PREFIX)+num);
				notify(ctx);
			}
			return;
		}
		setResultData(null);
		abortBroadcast();
		alert(num, cf.searchName(num));
	}

	private static void setCallNum(String num)
	{
		if(MainService.isRunning()) return;
		PhoneReceiver.number = num;
		if(A.is(K.VIBRATE_PICKUP)) PickupService.start();
	}

	private void alert(final String num, String name)
	{
		final String called = (A.empty(name)? "" : A.s(R.string.contact)+SEP+name+"\n\n") + A.s(R.string.phone_number)+SEP+num;
		final OnDismissListener dismiss = this;
		final OnCancelListener  cancel  = this;
		BlankActivity.postSingleton(new Runnable(){ public void run(){
			AlertDialog dlg = Alert.msg(
				A.s(R.string.ask_anonym_title),
				called + "\n\n" + A.s(R.string.ask_anonym_msg),
				new Alert.Click(){ public void on(){
					confirmed = anonym = true;
					Dev.dial(num);
				}},
				new Alert.Click(){ public void on(){
					confirmed = true;
					anonym    = false;
					Dev.dial(num);
				}},
				null,
				Alert.YESNO,
				true,
				BlankActivity.getInstance()
			);
			dlg.setOnCancelListener(cancel);
			dlg.setOnDismissListener(dismiss);
		}});
		BlankActivity.force = true;
		Intent i = new Intent(A.app(), BlankActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		A.app().startActivity(i);
	}

	private static void notify(Context ctx)
	{
		if(!A.is(K.ANONYM_NOTIFY)) return;
		A.toast(ctx, R.string.msg_anonym_dial);
	}
	
	@Override
	public void onDismiss(DialogInterface dlg)
	{
		BlankActivity act = BlankActivity.getInstance();
		if(act != null) act.postFinish();
	}

	@Override
	public void onCancel(DialogInterface dlg)
	{
		confirmed = anonym = false;
	}

}
