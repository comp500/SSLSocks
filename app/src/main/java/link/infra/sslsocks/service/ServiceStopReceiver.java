/*
 * Copyright (C) 2017-2021 comp500
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify this Program, or any covered work, by linking or combining
 * it with OpenSSL (or a modified version of that library), containing parts
 * covered by the terms of the OpenSSL License, the licensors of this Program
 * grant you additional permission to convey the resulting work.
 */

package link.infra.sslsocks.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import link.infra.sslsocks.gui.OpenVPNIntegrationHandler;

public class ServiceStopReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String openVpnProfile = PreferenceManager.getDefaultSharedPreferences(context).getString("open_vpn_profile", "");
		if (openVpnProfile.trim().length() > 0) {
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
		}
		@Override
		protected void onHandleIntent(@Nullable Intent intent) {
			handler = new OpenVPNIntegrationHandler(this, () -> handler.unbind(), "", true);
			handler.bind();
		}
	}
}
