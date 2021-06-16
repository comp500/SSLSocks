package link.infra.sslsocks.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.system.Os;
import android.util.Log;

import androidx.preference.PreferenceManager;

import android.content.pm.PackageManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import link.infra.sslsocks.BuildConfig;
import link.infra.sslsocks.R;
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

	public static void prepareStunnel(Context context) throws RuntimeException{
		ApplicationInfo info = context.getApplicationInfo();

		File filesMappingFile = new File(info.nativeLibraryDir, "libmappings.so");
		if (!filesMappingFile.exists()) {
			Log.e("Fragment", "No file mapping at " +
					filesMappingFile.getAbsolutePath());
			return;
		}

		try{
			BufferedReader reader =
					new BufferedReader(new FileReader(filesMappingFile));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split("←");
				if (parts.length != 2) {
					Log.e("Fragment", "Malformed line " + line + " in " +
							filesMappingFile.getAbsolutePath());
					continue;
				}

				String oldPath = info.nativeLibraryDir + "/" + parts[0];
				String newPath = context.getFilesDir() + "/" + parts[1];

				File directory = new File(newPath).getParentFile();
				if (!directory.isDirectory() && !directory.mkdirs()) {
					throw new RuntimeException("Unable to create directory: " + directory.getAbsolutePath());
				}

				Log.d("Fragment", "About to setup link: " + oldPath + " ← " + newPath);
				new File(newPath).delete();
				Os.symlink(oldPath, newPath);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
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

		prepareStunnel(context);
		setupConfig(context);
		context.clearLog();
		try {
			String[] env = new String[0];
			File workingDirectory = new File(context.getFilesDir().getPath());
			stunnelProcess = Runtime.getRuntime().exec(context.getFilesDir().getPath() + "/" + EXECUTABLE  + " " + CONFIG, env, workingDirectory);
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
						context.appendLog(line);
					}
				} catch (IOException e) {
					if (e instanceof InterruptedIOException) {
						// This is fine, it quit
						return;
					}
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
				Log.d(TAG, "Attempting to kill stunnel, pid = " + pid);
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

	public String checkStunnelVersion(Context context) {
		File pidFile = new File(context.getFilesDir().getPath() + "/" + PID);
		if (stunnelProcess != null || pidFile.exists()) {
			stop(context);
		}
		prepareStunnel(context);
		try {
			String[] env = new String[0];
			File workingDirectory = new File(context.getFilesDir().getPath());
			// Make the process fail, so we can extract just the version from the error stream
			stunnelProcess = Runtime.getRuntime().exec(context.getFilesDir().getPath() + "/" + EXECUTABLE + " THISFILESHOULDNOTEXIST", env, workingDirectory);
			BufferedSource errors = Okio.buffer(Okio.source(stunnelProcess.getErrorStream()));

			Pattern versionPattern = Pattern.compile("stunnel ([\\d.]+)");
			String line;
			String versionString = null;
			while ((line = errors.readUtf8Line()) != null) {
				Matcher matcher = versionPattern.matcher(line);
				if (matcher.find()) {
					versionString = matcher.group(1);
					break;
				}
			}
			errors.close();

			stunnelProcess.waitFor();
			if (versionString != null) {
				return versionString;
			}
		} catch (IOException e) {
			Log.e(TAG, "failure", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		return context.getString(R.string.pref_desc_stunnel_version_failed);
	}
}
