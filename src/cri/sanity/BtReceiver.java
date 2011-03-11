package cri.sanity;

import cri.sanity.util.Dev;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BtReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context ctx, Intent i)
	{
		final PhoneListener pl = PhoneListener.getActiveInstance();
		final String  act      = i.getAction();
		/*
		// media button
		if(Intent.ACTION_MEDIA_BUTTON.equals(act)) {
			if(pl!=null && Dev.isBtOn()) pl.updateHeadsetBt(true);
			return;
		}
		*/
		// bluetooth connection/disconnection
		final boolean conn = BluetoothDevice.ACTION_ACL_CONNECTED.equals(act);
		final int oldCount = pl==null? A.geti(K.BT_COUNT) : pl.btCount;
		final int newCount = conn? Math.max(oldCount+1,1) : (Dev.isBtOn()? Math.max(oldCount-1,0) : 0);
		if(oldCount == newCount) return;
		A.putc(K.BT_COUNT, newCount);
		//A.logd("BtReceiver: "+newCount+" connected devices; last one is connected = "+conn);
		if(pl == null) return;
		pl.btCount = newCount;
		// assume: if a bt device is connected during call, that bt device is an audio one (like headset)
		// FIX: use a stronger assumption by checking if bt device is an audio one???
		/*
		final BluetoothDevice btDev = i.getExtras().getParcelable(BluetoothDevice.EXTRA_DEVICE);
		if(btDev == null) {
			A.logd("BtReceiver skip: null btDev!");
			return;
		}
		final BluetoothClass btClass = btDev.getBluetoothClass();
		if(btClass == null) {
			A.logd("BtReceiver skip: null btClass!");
			return;
		}
		if(!btClass.hasService(BluetoothClass.Service.AUDIO)) {
			A.logd("BtReceiver skip: no audio service");
			return;
		}
//		if(btClass.getMajorDeviceClass() != BluetoothClass.Device.Major.AUDIO_VIDEO) {
//			A.logd("BtReceiver skip: bt device is not a major audio/video one");
//			return;
//		}
//		final int dc = btClass.getDeviceClass();
//		if(dc != BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE &&
//       dc != BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES &&
//       dc != BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO &&
//       dc != BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET &&
//       dc != BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO &&
//       dc != BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO &&
//       dc != BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER &&
//       dc != BluetoothClass.Device.AUDIO_VIDEO_MICROPHONE) {
//			A.logd("BtReceiver skip: bt device "+dc+" is not audio one");
//			return;
//		}
		*/
		pl.updateHeadsetBt(conn);
	}

}
