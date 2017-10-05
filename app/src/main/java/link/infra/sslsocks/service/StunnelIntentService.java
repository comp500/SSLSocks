package link.infra.sslsocks.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class StunnelIntentService extends IntentService {
	private static final String ACTION_STARTNOVPN = "link.infra.sslsocks.service.action.STARTNOVPN";

	private StunnelProcessManager processManager = new StunnelProcessManager();

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
		context.startService(intent);
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
		ServiceUtils.showNotification(this);
		processManager.start(this);
	}

	public void onDestroy() {
		processManager.stop();
		ServiceUtils.removeNotification(this);
		super.onDestroy();
	}
}
