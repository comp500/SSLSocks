package link.infra.sslsocks.service;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static link.infra.sslsocks.Constants.EXECUTABLE;
import static link.infra.sslsocks.Constants.HOME;

public class StunnelProcessManager {

	private static final String TAG = StunnelProcessManager.class.getSimpleName();

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

	public void start() {

	}

	public void stop() {

	}

	public boolean isAlive() {
		return false;
	}
}
