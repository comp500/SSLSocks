package link.infra.sslsocks.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import link.infra.sslsocks.BuildConfig;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import static link.infra.sslsocks.Constants.CONFIG;
import static link.infra.sslsocks.Constants.DEF_CONFIG;
import static link.infra.sslsocks.Constants.EXECUTABLE;
import static link.infra.sslsocks.Constants.PID;

public class StunnelProcessManager {

	private static final String TAG = StunnelProcessManager.class.getSimpleName();
	private Process stunnelProcess;

	private static boolean hasBeenUpdated(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		int versionCode = sharedPreferences.getInt("VERSION_CODE", 0);

		if (versionCode != BuildConfig.VERSION_CODE) {
			sharedPreferences.edit().putInt("VERSION_CODE", BuildConfig.VERSION_CODE).apply();
			return true;
		}
		return false;
	}

	public static void checkAndExtract(Context context) {
		File execFile = new File(context.getFilesDir().getPath() + "/" + EXECUTABLE);
		if (execFile.exists() && !hasBeenUpdated(context)) {
			return; // already extracted
		}

		//noinspection ResultOfMethodCallIgnored
		execFile.getParentFile().mkdir();

		// Extract stunnel exectuable
		AssetManager am = context.getAssets();
		try (BufferedSource in = Okio.buffer(Okio.source(am.open(EXECUTABLE)));
		     BufferedSink out = Okio.buffer(Okio.sink(execFile))) {
			out.writeAll(in);

			//noinspection ResultOfMethodCallIgnored
			execFile.setExecutable(true);

			Log.d(TAG, "Extracted stunnel binary successfully");
		} catch (Exception e) {
			Log.e(TAG, "Failed stunnel extraction: ", e);
		}
	}

	public static boolean setupConfig(Context context) {
		File configFile = new File(context.getFilesDir().getPath() + "/" + CONFIG);
		if (configFile.exists()) {
			return true; // already created
		}

		//noinspection ResultOfMethodCallIgnored
		configFile.getParentFile().mkdir();

		try (BufferedSink out = Okio.buffer(Okio.sink(configFile))) {
			out.writeUtf8(DEF_CONFIG);
			out.writeUtf8(context.getFilesDir().getPath());
			out.writeUtf8("/");
			out.writeUtf8(PID);
			return true;
		} catch (IOException e) {
			Log.e(TAG, "Failed config file creation: ", e);
			return false;
		}
	}

	void start(StunnelIntentService context) {
		File pidFile = new File(context.getFilesDir().getPath() + "/" + PID);
		if (stunnelProcess != null || pidFile.exists()) {
			stop(context);
		}
		checkAndExtract(context);
		setupConfig(context);
		ServiceUtils.clearLog(context);
		try {
			String[] env = new String[0];
			File workingDirectory = new File(context.getFilesDir().getPath());
			stunnelProcess = Runtime.getRuntime().exec(context.getFilesDir().getPath() + "/" + EXECUTABLE + " " + CONFIG, env, workingDirectory);
			readInputStream(context, Okio.buffer(Okio.source(stunnelProcess.getErrorStream())));
			readInputStream(context, Okio.buffer(Okio.source(stunnelProcess.getInputStream())));
			stunnelProcess.waitFor();
		} catch (IOException e) {
			Log.e(TAG, "failure", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private static void readInputStream(final StunnelIntentService context, final BufferedSource in) {
		Thread streamReader = new Thread(){
			public void run() {
				String line;
				try {
					while ((line = in.readUtf8Line()) != null) {
						ServiceUtils.broadcastLog(context, line);
					}
				} catch (IOException e) {
					Log.e(TAG, "Error reading stunnel stream: ", e);
				}
			}
		};
		streamReader.start();
	}

	void stop(Context context) {
		if (stunnelProcess != null) {
			stunnelProcess.destroy();
		}
		File pidFile = new File(context.getFilesDir().getPath() + "/" + PID);
		if (pidFile.exists()) { // still alive!
			String pid = null;
			try (BufferedSource in = Okio.buffer(Okio.source(pidFile))){
				pid = in.readUtf8Line();
			} catch (IOException e) {
				Log.e(TAG, "Failed to read PID file", e);
			}

			if (pid == null || !pid.trim().equals("")) {
				Log.d(TAG, "Attmepting to kill stunnel, pid = " + pid);
				try {
					Runtime.getRuntime().exec("kill " + pid).waitFor();
				} catch (Exception e) {
					Log.e(TAG, "Failed to kill stunnel", e);
				}

				if (pidFile.exists()) {
					// presumed dead, remove pid
					//noinspection ResultOfMethodCallIgnored
					pidFile.delete();
				}
			}
		}
	}
}
