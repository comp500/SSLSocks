package link.infra.sslsocks;

public class Constants {
	public static final String HOME = "/data/data/link.infra.sslsocks/files/";
	public static final String EXECUTABLE = "stunnel";
	public static final String CONFIG = "config";
	public static final String PID = "pid";
	public static final String LOG = "log";

	public static final String DEF_CONFIG = "# Do not change the following lines\n" +
			"log = overwrite\noutput = " + HOME + LOG + "\n" +
			"pid = " + HOME + PID + "\n# Less verbosity, prevent log from getting too long\n# Set debug = 7 if you want more verbosity.\ndebug = 4" +
			"\n\n# Now add your configs\n";
}
