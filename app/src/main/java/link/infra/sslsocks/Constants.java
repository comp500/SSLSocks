package link.infra.sslsocks;

public class Constants {
	public static final String HOME = "/data/data/link.infra.sslsocks/files/";
	public static final String CERTSFOLDER = "certs/";
	public static final String EXECUTABLE = "stunnel";
	public static final String CONFIG = "config.conf";
	public static final String LOG = "log.txt";
	public static final String PID = "pid";

	public static final String DEF_CONFIG =
			"foreground = yes\n" +
					"client = yes\n" +
					"log = overwrite\n" +
					"output = " + HOME + LOG + "\n" +
					"pid = " + HOME + PID;
}
