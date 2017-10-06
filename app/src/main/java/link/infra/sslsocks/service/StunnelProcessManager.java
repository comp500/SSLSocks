package link.infra.sslsocks.service;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static link.infra.sslsocks.Constants.CONFIG;
import static link.infra.sslsocks.Constants.DEF_CONFIG;
import static link.infra.sslsocks.Constants.EXECUTABLE;
import static link.infra.sslsocks.Constants.LOG;
import static link.infra.sslsocks.Constants.PID;

public class StunnelProcessManager {

	private static final String TAG = StunnelProcessManager.class.getSimpleName();
	private Process stunnelProcess;

	public static boolean checkAndExtract(Context context) {
		if (new File(context.getFilesDir().getPath() + EXECUTABLE).exists()) {
			return true; // already extracted
		}

		new File(context.getFilesDir().getPath()).mkdir();

		// Extract stunnel exectuable
		AssetManager am = context.getAssets();
		try {
			InputStream in = am.open(EXECUTABLE);
			OutputStream out = new FileOutputStream(context.getFilesDir().getPath() + EXECUTABLE);

			byte[] buf = new byte[512];
			int len;

			while ((len = in.read(buf)) > -1) {
				out.write(buf, 0, len);
			}

			in.close();
			out.flush();
			out.close();

			Runtime.getRuntime().exec("chmod 777 " + context.getFilesDir().getPath() + EXECUTABLE);

			Log.d(TAG, "Extracted stunnel binary successfully");
		} catch (Exception e) {
			Log.e(TAG, "Failed stunnel extraction: ", e);
			return false; // extraction failed
		}
		return true; // extraction succeeded
	}

	public static boolean setupConfig(Context context) {
		if (new File(context.getFilesDir().getPath() + CONFIG).exists()) {
			return true; // already extracted
		}

		new File(context.getFilesDir().getPath()).mkdir();

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(context.getFilesDir().getPath() + CONFIG);
			try {
				fileOutputStream.write(DEF_CONFIG.getBytes());
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

	public void start(Context context) {
		if (isAlive(context) || stunnelProcess != null) {
			stop(context);
		}
		checkAndExtract(context);
		setupConfig(context);
		try {
			stunnelProcess = Runtime.getRuntime().exec(context.getFilesDir().getPath() + EXECUTABLE + " " + context.getFilesDir().getPath() + CONFIG);
			stunnelProcess.waitFor();
			ServiceUtils.broadcastLog(context, readInputStream(stunnelProcess.getErrorStream()));
			ServiceUtils.broadcastLog(context, readInputStream(stunnelProcess.getInputStream()));

			File file = new File(context.getFilesDir().getPath() + LOG);
			StringBuilder text = new StringBuilder();

			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;

				while ((line = br.readLine()) != null) {
					text.append(line);
					text.append('\n');
				}
				br.close();
			} catch (IOException e) {
				Log.e(TAG, "Failed to read config file", e);
			}

			ServiceUtils.broadcastLog(context, text.toString());
		} catch (IOException e) {
			Log.e(TAG, "failure", e);
		} catch (NoSuchElementException e) {
			Log.e(TAG, "failure", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public static String readInputStream(InputStream stream) {
		Scanner scanner = new Scanner(stream);
		scanner.useDelimiter("\\A");
		if (scanner.hasNext()) {
			return scanner.next();
		} else {
			return "";
		}
	}

	public void stop(Context context) {
		if (stunnelProcess != null) {
			stunnelProcess.destroy();
		}
		if (isAlive(context)) { // still alive!
			String pid = "";

			try {
				BufferedReader br = new BufferedReader(new FileReader(context.getFilesDir().getPath() + PID));
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
					new File(context.getFilesDir().getPath() + PID).delete();
				}
			}
		}
	}

	public boolean isAlive(Context context) {
		return new File(context.getFilesDir().getPath() + PID).exists();
	}
}
