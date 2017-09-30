package link.infra.sslsocks;

import android.net.VpnService;

public class StunnelBackgroundService extends VpnService {
	private VpnService.Builder builder;
	private static final String VPN_ADDRESS = "10.0.0.2"; // Only IPv4 support for now
	private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything

	public StunnelBackgroundService() {

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
	}
}
