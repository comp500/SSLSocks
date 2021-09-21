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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import link.infra.sslsocks.R;
import link.infra.sslsocks.gui.main.MainActivity;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class StunnelIntentService extends IntentService {
	private static final String ACTION_STARTNOVPN = "link.infra.sslsocks.service.action.STARTNOVPN";
	private static final String ACTION_RESUMEACTIVITY = "link.infra.sslsocks.service.action.RESUMEACTIVITY";

	private static final int NOTIFICATION_ID = 1;
	private static final String ACTION_STOP = "link.infra.sslsocks.service.action.STOP";

	private static final MutableLiveData<Boolean> privateIsRunning = new MutableLiveData<>();
	public static final LiveData<Boolean> isRunning = privateIsRunning;

	private static final MutableLiveData<String> logDataPrivate = new MutableLiveData<>();
	public static final LiveData<String> logData = logDataPrivate;
	private String currLogValue = "";

	private final StunnelProcessManager processManager = new StunnelProcessManager();

	public StunnelIntentService() {
		super("StunnelIntentService");
	}

	/**
	 * Starts this service to perform action Foo with the given parameters. If
	 * the service is already performing a task this action will be queued.
	 *
	 * @see IntentService
	 */
	public static void start(Context context) {
		Intent intent = new Intent(context, StunnelIntentService.class);
		intent.setAction(ACTION_STARTNOVPN);
		if (android.os.Build.VERSION.SDK_INT >= 26) {
			context.startForegroundService(intent);
		} else {
			context.startService(intent);
		}
	}

	public static void checkStatus(Context context) {
		Intent localIntent = new Intent(ACTION_RESUMEACTIVITY);
		// Broadcasts the Intent to receivers in this app.
		LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			final String action = intent.getAction();
			if (ACTION_STARTNOVPN.equals(action)) {
				handleStart();
			}
		}
	}

	/**
	 * Handle start action in the provided background thread with the provided
	 * parameters.
	 */
	private void handleStart() {
		privateIsRunning.postValue(true);
		showNotification();
		processManager.start(this);
	}

	public void onDestroy() {
		processManager.stop(this);
		removeNotification();
		privateIsRunning.postValue(false);
		super.onDestroy();
	}

	public void appendLog(String value) {
		currLogValue += value + "\n";
		logDataPrivate.postValue(currLogValue);
	}

	public void clearLog() {
		currLogValue = "";
		logDataPrivate.postValue(currLogValue);
	}

	private void showNotification() {
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
						.setSmallIcon(R.drawable.ic_service_running)
						.setContentTitle(getString(R.string.app_name_full))
						.setContentText(getString(R.string.notification_desc))
						.setCategory(NotificationCompat.CATEGORY_SERVICE)
						.setPriority(NotificationCompat.PRIORITY_DEFAULT)
						.setOngoing(true);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
				stackBuilder.getPendingIntent(
						0,
						PendingIntent.FLAG_UPDATE_CURRENT
				);
		mBuilder.setContentIntent(resultPendingIntent);

		Intent serviceStopIntent = new Intent(this, ServiceStopReceiver.class);
		serviceStopIntent.setAction(ACTION_STOP);
		PendingIntent serviceStopIntentPending = PendingIntent.getBroadcast(this, 1, serviceStopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.addAction(R.drawable.ic_stop, "Stop", serviceStopIntentPending);

		// Ensure that the service is a foreground service
		startForeground(NOTIFICATION_ID, mBuilder.build());
	}

	private void removeNotification() {
		NotificationManager mNotificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (mNotificationManager != null) {
			mNotificationManager.cancel(NOTIFICATION_ID);
		}
	}
}
