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

import static link.infra.sslsocks.Constants.CONFIG;
import static link.infra.sslsocks.Constants.DEF_CONFIG;
import static link.infra.sslsocks.Constants.EXECUTABLE;
import static link.infra.sslsocks.Constants.HOME;
import static link.infra.sslsocks.Constants.LOG;

public class StunnelProcessManager {

	private static final String TAG = StunnelProcessManager.class.getSimpleName();
	private Process stunnelProcess;

	public static boolean checkAndExtract(Context context) {
		if (new File(HOME + EXECUTABLE).exists()) {
			return true; // already extracted
		}

		new File(HOME).mkdir();

		// Extract stunnel exectuable
		AssetManager am = context.getAssets();
		try {
			InputStream in = am.open(EXECUTABLE);
			OutputStream out = new FileOutputStream(HOME + EXECUTABLE);

			byte[] buf = new byte[512];
			int len;

			while ((len = in.read(buf)) > -1) {
				out.write(buf, 0, len);
			}

			in.close();
			out.flush();
			out.close();

			Runtime.getRuntime().exec("chmod 777 " + HOME + EXECUTABLE);

			Log.d(TAG, "Extracted stunnel binary successfully");
		} catch (Exception e) {
			Log.e(TAG, "Failed stunnel extraction: ", e);
			return false; // extraction failed
		}
		return true; // extraction succeeded
	}

	public static boolean setupConfig() {
		if (new File(HOME + CONFIG).exists()) {
			return true; // already extracted
		}

		new File(HOME).mkdir();

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(HOME + CONFIG);
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
		checkAndExtract(context);
		setupConfig();
		try {
			stunnelProcess = Runtime.getRuntime().exec(HOME + EXECUTABLE + " " + HOME + CONFIG);
			stunnelProcess.waitFor();
			Log.d(TAG, new java.util.Scanner(stunnelProcess.getErrorStream()).useDelimiter("\\A").next());
			Log.d(TAG, new java.util.Scanner(stunnelProcess.getInputStream()).useDelimiter("\\A").next());

			File file = new File(HOME + LOG);
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

			Log.d(TAG, text.toString());
		} catch (IOException e) {
			Log.e(TAG, "failure", e);
		} catch (NoSuchElementException e) {
			Log.e(TAG, "failure", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void stop() {
		if (stunnelProcess != null) {
			stunnelProcess.destroy();
		}
	}
}
