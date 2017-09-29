package link.infra.sslsocks;

import android.app.Service;
import android.content.Intent;
import android.net.VpnService;
import android.os.IBinder;

public class StunnelBackgroundService extends VpnService {
	public StunnelBackgroundService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
