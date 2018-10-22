package link.infra.sslsocks.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;

import link.infra.sslsocks.R;
import link.infra.sslsocks.gui.main.MainActivity;

public class ServiceUtils {
	private static final int NOTIFICATION_ID = 1;
	public static final String ACTION_LOGBROADCAST = "link.infra.sslsocks.service.action.LOGBROADCAST";
	public static final String EXTENDED_DATA_LOG = "link.infra.sslsocks.service.action.LOGDATA";
	public static final String ACTION_CLEARLOG = "link.infra.sslsocks.service.action.CLEARLOG";
	public static final String ACTION_STARTED = "link.infra.sslsocks.service.action.STARTED";
	public static final String ACTION_STOPPED = "link.infra.sslsocks.service.action.STOPPED";
	public static final String SHOULD_CLEAR_LOG = "link.infra.sslsocks.service.action.SHOULDCLEAR";

	static void showNotification(StunnelIntentService ctx) {
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(ctx, MainActivity.CHANNEL_ID)
						.setSmallIcon(R.drawable.ic_info_black_24dp)
						.setContentTitle(ctx.getString(R.string.app_name))
						.setContentText(ctx.getString(R.string.notification_desc))
						.setCategory(NotificationCompat.CATEGORY_SERVICE)
						.setOngoing(true);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(ctx, MainActivity.class);
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
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

		// Ensure that the service is a foreground service
		ctx.startForeground(NOTIFICATION_ID, mBuilder.build());
	}

	static void removeNotification(Context ctx) {
		NotificationManager mNotificationManager =
				(NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		if (mNotificationManager != null) {
			mNotificationManager.cancel(NOTIFICATION_ID);
		}
	}

	static void broadcastLog(StunnelIntentService ctx, String status) {
		Intent localIntent =
				new Intent(ACTION_LOGBROADCAST)
						// Puts the status into the Intent
						.putExtra(EXTENDED_DATA_LOG, status);
		// Broadcasts the Intent to receivers in this app.
		LocalBroadcastManager.getInstance(ctx).sendBroadcast(localIntent);
		if (ctx.pendingLog == null) {
			ctx.pendingLog = status + "\n";
		} else {
			ctx.pendingLog = ctx.pendingLog + status + "\n";
		}
	}

	static void broadcastPreviousLog(StunnelIntentService ctx) {
		if (ctx.pendingLog == null) return;

		Intent localIntent =
				new Intent(ACTION_LOGBROADCAST)
						// Puts the status into the Intent
						.putExtra(EXTENDED_DATA_LOG, ctx.pendingLog)
						.putExtra(SHOULD_CLEAR_LOG, "");
		// Broadcasts the Intent to receivers in this app.
		LocalBroadcastManager.getInstance(ctx).sendBroadcast(localIntent);
	}

	static void broadcastStarted(Context ctx) {
		Intent localIntent = new Intent(ACTION_STARTED);
		// Broadcasts the Intent to receivers in this app.
		LocalBroadcastManager.getInstance(ctx).sendBroadcast(localIntent);
	}

	static void broadcastStopped(Context ctx) {
		Intent localIntent = new Intent(ACTION_STOPPED);
		// Broadcasts the Intent to receivers in this app.
		LocalBroadcastManager.getInstance(ctx).sendBroadcast(localIntent);
	}

	static void clearLog(StunnelIntentService ctx) {
		Intent localIntent = new Intent(ACTION_CLEARLOG);
		// Broadcasts the Intent to receivers in this app.
		LocalBroadcastManager.getInstance(ctx).sendBroadcast(localIntent);
		ctx.pendingLog = null;
	}
}
