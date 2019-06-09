package link.infra.sslsocks.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceStopReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent intentStop = new Intent(context, StunnelIntentService.class);
		context.stopService(intentStop);
	}
}
