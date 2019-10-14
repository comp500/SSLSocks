package link.infra.sslsocks.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import link.infra.sslsocks.gui.OpenVPNIntegrationHandler;

public class ServiceStopReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String openVpnProfile = PreferenceManager.getDefaultSharedPreferences(context).getString("open_vpn_profile", "");
		if (openVpnProfile != null && openVpnProfile.trim().length() > 0) {
			Log.d("a", "Starting service");
			Intent intentDisconnect = new Intent(context, OpenVPNIntentService.class);
			context.startService(intentDisconnect);
		}

		Intent intentStop = new Intent(context, StunnelIntentService.class);
		context.stopService(intentStop);
	}

	// IntentService is used because a BroadcastReceiver can't bind to services
	public static class OpenVPNIntentService extends IntentService {
		private OpenVPNIntegrationHandler handler;
		public OpenVPNIntentService() {
			super("OpenVPNIntentService");
			Log.d("a", "hmm");
		}
		@Override
		protected void onHandleIntent(@Nullable Intent intent) {
			Log.d("a", "Creating handler");
			handler = new OpenVPNIntegrationHandler(this, new Runnable() {
				@Override
				public void run() {
					handler.unbind();
				}
			}, "", true);
			handler.bind();
		}
	}
}
