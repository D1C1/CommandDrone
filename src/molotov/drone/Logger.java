package molotov.drone;

/*
 * Incredibly baisc logger. Literally a wrapper for sysout. No jokes dude...
 * @author pol
 */

public class Logger {
	long time;
	
	// Color codes
	final int NORMAL = 0;
	final int INFO = 2;
	final int SUCCESS = 1;
	final int ERROR = 3;
	final int WATCHDOG = 4;
	final int PACKET = 5;
	final int FLYING = 6;
	final int WARNING = 7;

	public Logger() {
		time = System.currentTimeMillis();
	}

	public void log(String s) {
		long j = (System.currentTimeMillis() - time)/1000;
		System.out.println("[" + (j) + "] " + s);
	}

	public void log(String s, int c) {
		long j = (System.currentTimeMillis() - time)/1000;
		System.out.println("[" + (j) + "] " + s);
	}

	public void log_info(String s, int c) {
		System.out.println("===> [" + s + "] <===");
	}
}
