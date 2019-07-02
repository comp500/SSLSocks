package link.infra.sslsocks.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class StunnelIntentService extends IntentService {
	private static final String ACTION_STARTNOVPN = "link.infra.sslsocks.service.action.STARTNOVPN";
	private static final String ACTION_RESUMEACTIVITY = "link.infra.sslsocks.service.action.RESUMEACTIVITY";

	private static final MutableLiveData<Boolean> privateIsRunning = new MutableLiveData<>();
	public static final LiveData<Boolean> isRunning = privateIsRunning;

	private final StunnelProcessManager processManager = new StunnelProcessManager();
	public String pendingLog;

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
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
		IntentFilter resumeIntentFilter = new IntentFilter(ACTION_RESUMEACTIVITY);
		final StunnelIntentService ctx = this;
		BroadcastReceiver resumeReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				ServiceUtils.broadcastPreviousLog(ctx);
			}
		};
		manager.registerReceiver(resumeReceiver, resumeIntentFilter);

		privateIsRunning.postValue(true);
		ServiceUtils.showNotification(this);
		processManager.start(this);
	}

	public void onDestroy() {
		processManager.stop(this);
		ServiceUtils.removeNotification(this);
		privateIsRunning.postValue(false);
		super.onDestroy();
	}
}
