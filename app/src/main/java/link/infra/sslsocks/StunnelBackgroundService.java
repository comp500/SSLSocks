package link.infra.sslsocks;

import android.app.Service;
import android.content.Intent;
import android.net.VpnService;
import android.os.IBinder;
import android.util.Log;

public class StunnelBackgroundService extends VpnService {
	private VpnService.Builder builder;

	public StunnelBackgroundService() {

	}

	public void onCreate() {
		builder = new VpnService.Builder();
		builder.establish();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("Service", "HI");
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
