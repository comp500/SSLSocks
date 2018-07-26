package link.infra.sslsocks.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			boolean start = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("start_on_boot", false);
			if (start) {
				StunnelIntentService.start(context);
			}
		}
	}
}
