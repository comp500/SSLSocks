package link.infra.sslsocks;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class StunnelBackgroundService extends VpnService {
	private VpnService.Builder builder;
	private static final String VPN_ADDRESS = "10.0.0.2"; // Only IPv4 support for now
	private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything
	private static final int NOTIFICATION_ID = 0;

	public StunnelBackgroundService() {

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		showNotification();
		return START_STICKY;
	}

	private void showNotification() {
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
						.setSmallIcon(android.R.color.transparent)
						.setContentTitle(getString(R.string.app_name))
						.setContentText(getString(R.string.notification_desc))
						.setCategory(NotificationCompat.CATEGORY_SERVICE)
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
		NotificationManager mNotificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	@Override
	public void onCreate() {
		super.onCreate();
		builder = new VpnService.Builder();
		builder.addAddress(VPN_ADDRESS, 32);
		builder.addRoute(VPN_ROUTE, 0);
		builder.establish();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		NotificationManager mNotificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTIFICATION_ID);
	}
}
