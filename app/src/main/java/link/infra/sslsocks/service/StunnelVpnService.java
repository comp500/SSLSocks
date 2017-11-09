package link.infra.sslsocks.service;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

public class StunnelVpnService extends VpnService {
	private VpnService.Builder builder = new VpnService.Builder();
	private Thread vpnThread;
	private Thread stunnelThread;
	private ParcelFileDescriptor vpnInterface;

	public static final String ACTION_STARTVPN = "link.infra.sslsocks.service.action.STARTVPN";

	private StunnelProcessManager processManager = new StunnelProcessManager();

	public StunnelVpnService() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		ServiceUtils.showNotification(this);

		final Context ctx = this;

		stunnelThread = new Thread(new Runnable() {
			@Override
			public void run() {
				processManager.start(ctx);
			}
		});

		stunnelThread.start();

		// Start a new session by creating a new thread.
		vpnThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					//a. Configure the TUN and get the interface.
					builder.setSession("MyVPNService");
					builder.addAddress("192.168.0.1", 24);
					builder.addRoute("0.0.0.0", 0);
					vpnInterface = builder.establish();
					//b. Packets to be sent are queued in this input stream.
					FileInputStream in = new FileInputStream(
							vpnInterface.getFileDescriptor());
					//b. Packets received need to be written to this output stream.
					FileOutputStream out = new FileOutputStream(
							vpnInterface.getFileDescriptor());
					//c. The UDP channel can be used to pass/get ip package to/from server
					DatagramChannel tunnel = DatagramChannel.open();
					// Connect to the server, localhost is used for demonstration only.
					tunnel.connect(new InetSocketAddress("127.0.0.1", 9050));
					//d. Protect this socket, so package send by it will not be feedback to the vpn service.
					protect(tunnel.socket());
					//e. Use a loop to pass packets.
					char[] myBuffer = new char[1024];
					int bytesRead = 0;

					while (true) {
						while (in.available() > 0) {
							int a = in.read();

							//tunnel.write(a);
						}
						//get packet with in
						//put packet to tunnel
						//get packet form tunnel
						//return packet with out
						//sleep is a must
						Thread.sleep(100);
					}

				} catch (Exception e) {
					// Catch any exception
					e.printStackTrace();
				} finally {
					try {
						if (vpnInterface != null) {
							vpnInterface.close();
							vpnInterface = null;
						}
					} catch (Exception e) {

					}
				}
			}

		}, "MyVpnRunnable");

		//start the service
		vpnThread.start();

		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		vpnThread.interrupt();
		stunnelThread.interrupt();
		processManager.stop(this);
		ServiceUtils.removeNotification(this);
		super.onDestroy();
	}
}
