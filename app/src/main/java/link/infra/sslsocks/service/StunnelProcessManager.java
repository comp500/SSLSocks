package link.infra.sslsocks.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import link.infra.sslsocks.BuildConfig;

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
		File currExec = new File(context.getFilesDir().getPath() + "/" + EXECUTABLE);
		if (currExec.exists() && !hasBeenUpdated(context)) {
			return; // already extracted
		}

		//noinspection ResultOfMethodCallIgnored
		new File(context.getFilesDir().getPath()).mkdir();

		// Extract stunnel exectuable
		AssetManager am = context.getAssets();
		try {
			InputStream in = am.open(EXECUTABLE);
			OutputStream out = new FileOutputStream(context.getFilesDir().getPath() + "/" + EXECUTABLE);

			byte[] buf = new byte[512];
			int len;

			while ((len = in.read(buf)) > -1) {
				out.write(buf, 0, len);
			}

			in.close();
			out.flush();
			out.close();

			Runtime.getRuntime().exec("chmod 777 " + context.getFilesDir().getPath() + "/" + EXECUTABLE);

			Log.d(TAG, "Extracted stunnel binary successfully");
		} catch (Exception e) {
			Log.e(TAG, "Failed stunnel extraction: ", e);
		}
	}

	public static boolean setupConfig(Context context) {
		if (new File(context.getFilesDir().getPath() + "/" + CONFIG).exists()) {
			return true; // already created
		}

		//noinspection ResultOfMethodCallIgnored
		new File(context.getFilesDir().getPath()).mkdir();

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(context.getFilesDir().getPath() + "/" + CONFIG);
			try {
				String conf = DEF_CONFIG + context.getFilesDir().getPath() + "/" + PID;
				fileOutputStream.write(conf.getBytes());
				fileOutputStream.close();
				return true;
			} catch (IOException e) {
				Log.e(TAG, "Failed config file creation: ", e);
				try {
					// attempt to finally close the file
					fileOutputStream.close();
				} catch (IOException e1) {
					// ignore exception
				}
				return false;
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Failed config file creation: ", e);
			return false;
		}
	}

	void start(StunnelIntentService context) {
		if (isAlive(context) || stunnelProcess != null) {
			stop(context);
		}
		checkAndExtract(context);
		setupConfig(context);
		ServiceUtils.clearLog(context);
		try {
			String[] env = new String[0];
			File workingDirectory = new File(context.getFilesDir().getPath());
			stunnelProcess = Runtime.getRuntime().exec(context.getFilesDir().getPath() + "/" + EXECUTABLE + " " + CONFIG, env, workingDirectory);
			readInputStream(context, stunnelProcess.getErrorStream());
			readInputStream(context, stunnelProcess.getInputStream());
			ServiceUtils.broadcastStarted(context);
			stunnelProcess.waitFor();
		} catch (IOException e) {
			Log.e(TAG, "failure", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private static void readInputStream(final StunnelIntentService context, final InputStream stream) {
		Thread streamReader = new Thread(){
			public void run() {
				Scanner scanner = new Scanner(stream);
				//scanner.useDelimiter("\\A");
				while (scanner.hasNextLine()) {
					ServiceUtils.broadcastLog(context, scanner.nextLine());
				}
			}
		};
		streamReader.start();
	}

	void stop(Context context) {
		if (stunnelProcess != null) {
			stunnelProcess.destroy();
		}
		if (isAlive(context)) { // still alive!
			String pid = "";

			try {
				BufferedReader br = new BufferedReader(new FileReader(context.getFilesDir().getPath() + "/" + PID));
				pid = br.readLine();
			} catch (IOException e) {
				Log.e(TAG, "Failed to read PID file", e);
			}

			Log.d(TAG, "pid = " + pid);

			if (!pid.trim().equals("")) {
				try {
					Runtime.getRuntime().exec("kill " + pid).waitFor();
				} catch (Exception e) {
					Log.e(TAG, "Failed to kill stunnel", e);
				}

				if (isAlive(context)) {
					// presumed dead, remove pid
					//noinspection ResultOfMethodCallIgnored
					new File(context.getFilesDir().getPath() + "/" + PID).delete();
				}
			}
		}
	}

	private boolean isAlive(Context context) {
		return new File(context.getFilesDir().getPath() + "/" + PID).exists();
	}
}
