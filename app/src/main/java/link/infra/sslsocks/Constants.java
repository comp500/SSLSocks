package link.infra.sslsocks;

public class Constants {
	public static final String EXECUTABLE = "stunnel";
	public static final String CONFIG = "config.conf";
	public static final String OPENSSL_CONF = "openssl.cnf";
	public static final String PSKSECRETS = "psksecrets.txt";
	public static final String PID = "pid";

	public static final String DEF_CONFIG =
			"foreground = yes\n" +
					"client = yes\n" +
					"pid = ";
}
