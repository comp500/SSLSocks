package link.infra.sslsocks.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class StunnelIntentService extends IntentService {
	private static final String ACTION_STARTNOVPN = "link.infra.sslsocks.service.action.STARTNOVPN";

	// TODO: Rename parameters
	private static final String EXTRA_PARAM1 = "link.infra.sslsocks.service.extra.PARAM1";
	private static final String EXTRA_PARAM2 = "link.infra.sslsocks.service.extra.PARAM2";

	public StunnelIntentService() {
		super("StunnelIntentService");
	}

	/**
	 * Starts this service to perform action Foo with the given parameters. If
	 * the service is already performing a task this action will be queued.
	 *
	 * @see IntentService
	 */
	// TODO: Customize helper method
	public static void start(Context context, String param1, String param2) {
		Intent intent = new Intent(context, StunnelIntentService.class);
		intent.setAction(ACTION_STARTNOVPN);
		intent.putExtra(EXTRA_PARAM1, param1);
		intent.putExtra(EXTRA_PARAM2, param2);
		context.startService(intent);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			final String action = intent.getAction();
			if (ACTION_STARTNOVPN.equals(action)) {
				final String param1 = intent.getStringExtra(EXTRA_PARAM1);
				final String param2 = intent.getStringExtra(EXTRA_PARAM2);
				handleStart(param1, param2);
			}
		}
	}

	/**
	 * Handle action Foo in the provided background thread with the provided
	 * parameters.
	 */
	private void handleStart(String param1, String param2) {
		// TODO: Make this better
		ServiceUtils.showNotification(this);
	}

	public void onDestroy() {
		ServiceUtils.removeNotification(this);
		super.onDestroy();
	}
}
