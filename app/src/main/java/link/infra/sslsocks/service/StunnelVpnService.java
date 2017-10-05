package link.infra.sslsocks.service;

import android.content.Intent;
import android.net.VpnService;

public class StunnelVpnService extends VpnService {
	private VpnService.Builder builder;
	private static final String VPN_ADDRESS = "10.0.0.2"; // Only IPv4 support for now
	private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything

	public StunnelVpnService() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		ServiceUtils.showNotification(this);
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		builder = new VpnService.Builder();
		builder.addAddress(VPN_ADDRESS, 32);
		builder.addRoute(VPN_ROUTE, 0);
		builder.establish();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ServiceUtils.removeNotification(this);
	}
}
