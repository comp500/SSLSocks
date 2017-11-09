package link.infra.sslsocks.service;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

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
					Thread.sleep(1000);
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
					//DatagramChannel tunnel = DatagramChannel.open();
					Socket tunnel = new Socket();
					// Connect to the server, localhost is used for demonstration only.
					tunnel.connect(new InetSocketAddress("127.0.0.1", 10000));
					//tunnel.configureBlocking(false);
					//d. Protect this socket, so package send by it will not be feedback to the vpn service.
					protect(tunnel);
					//e. Use a loop to pass packets.
					ByteBuffer packet = ByteBuffer.allocate(Short.MAX_VALUE);
					OutputStream outStream = tunnel.getOutputStream();
					InputStream inStream = tunnel.getInputStream();

					byte[] buffer = new byte[Short.MAX_VALUE];
					int len;

					while (!Thread.interrupted()) {
						Log.d("VPNservice", "5");
						while (in.available() > 0) {
							len = in.read(buffer);
							outStream.write(buffer, 0, len);
							Log.d("VPNservice", "6" + len);
						}
						while (inStream.available() > 0) {
							len = inStream.read(buffer);
							out.write(buffer, 0, len);
							Log.d("VPNservice", "7");
						}
						Log.d("VPNservice", "8");

						Thread.sleep(100);

						/*
						Log.d("VPNservice", "5");
						int length = in.read(packet.array());
						if (length > 0) {
							packet.limit(length);
							outChannel.write(packet);
							// There might be more outgoing packets.
							Log.d("VPNservice", "6");
						}
						Log.d("VPNservice", "7");
						length = inChannel.read(packet);
						Log.d("VPNservice", "8");
						if (length > 0) {
							if (packet.get(0) != 0) {
								out.write(packet.array(), 0, length);
								Log.d("VPNservice", "9");
							}
							packet.clear();
							Log.d("VPNservice", "10");
						}
						Thread.sleep(100);
						Log.d("VPNservice", "11");*/
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
