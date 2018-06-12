package molotov.drone;

/*
 * Basic Flight Engine.
 * @author pol
 */

public class Vodka extends Alcohol {
	/*
	 * Default constants
	 * =======================================================================
	 * 
	 * Maximum vertical in milimeters per second. Recommended values go from 200
	 * to 2000. Others values may cause instability. This value will be saved to
	 * indoor/outdoor_control_vz_max, according to the CONFIG:outdoor setting.
	 */
	private final int vz_max = 1000;

	/*
	 * Drone's mass, in kilograms. There are only two values really, 420g and
	 * 380g. Which map to the outdoor hull weight and indoor hull masses
	 * respectively.
	 */
	private final double mass = 0.420;

	/*
	 * Maximum yaw speed, in radians per second. Recommended values go from 40
	 * deg/s to 350 deg/s (approx 0.7rad/s to 6.11rad/s). Others values may
	 * cause instability. This value will be saved to
	 * indoor/outdoor_control_yaw, according to the CONFIG:outdoor setting.
	 */
	private final double yaw_max = Math.PI / 2;

	/*
	 * Maximum drone altitude in millimeters.
	 * 
	 * Any value will be set as a maximum altitude, as the pressure sensor
	 * allows altitude measurement at any height. Typical value for "unlimited"
	 * altitude will be 100000.
	 */
	private final int max_altitude = 100000;
	private final int min_altitude = 100;

	/*
	 * Maximum bending angle in radians, for both pitch and roll. The
	 * progressive command function refers to a percentage of this value.
	 * 
	 * This parameter is a positive floating-point value between 0 and 0.52 (ie.
	 * 30 deg). This value will be saved to indoor/outdoor_euler_angle_max,
	 * according to the CONFIG:outdoor setting.
	 */

	private final double euler_max = Math.PI / 6;

	// Physics variables
	double altitude;
	double euler;
	double yaw_speed;
	double vertical_speed;
	double rotor_power;
	double speed;
	double momentum = mass*speed;

	public Vodka() {
		start();
		g.log_info("Engine Started", g.INFO);
	}

	public void start() {
		config();
		altitude = 0.0;
		yaw_speed = rotorpower(yaw_max);
		euler = rotorpower(euler_max);
		vertical_speed = rotorpower(vz_max);
	}

	public void config() {
		setRotorPower(1);
		setNavdataOn(true);
		setMaxAltitude(max_altitude);
		setMaxEulerAngle(euler_max);
		setMaxVZ(vz_max);
		setMaxYawSpeed(yaw_max);
		setMinAltitude(min_altitude);
		setVideoEnable(true);
	}

	/*
	 * Move up a certain distance in meters.
	 */
	public void up(double distance) {
		// time needed to gain
		distance *= 1000;
		long time = (long) Math
				.round(Math.abs(distance / vertical_speed) * 1000);
		altitude += distance;
		move_up(time);
	}

	/*
	 * Move down a certain distance in meters.
	 */
	public void down(double distance) {
		// time needed to descend
		distance *= 1000;
		long time = (long) Math
				.round(Math.abs(distance / vertical_speed) * 1000);
		altitude += distance;
		move_down(time);
	}

	/*
	 * Move forward a certain distance in meters.
	 */
	public void forward(double distance) {
		double ratio = Math.cos(euler);
		distance *= 1000;
		long time = (long) Math.round(Math.abs(distance / vertical_speed
				* ratio) * 1000);
		g.log("[FORWARD] => " + time + "ms");
		move_forward(time);
	}

	/*
	 * Reverse a certain distance in meters.
	 */
	public void reverse(double distance) {
		double ratio = Math.cos(euler);
		distance *= 1000;
		long time = (long) Math.round(Math.abs(distance / vertical_speed
				* ratio) * 1000);
		move_backward(time);
	}

	/*
	 * Move left a certain distance in meters.
	 */
	public void left(double distance) {
		double ratio = Math.cos(euler);
		distance *= 1000;
		long time = (long) Math.round(Math.abs(distance / vertical_speed
				* ratio) * 1000);
		move_left(time);
	}

	/*
	 * Move right a certain distance in meters.
	 */
	public void right(double distance) {
		double ratio = Math.cos(euler);
		distance *= 1000;
		long time = (long) Math.round(Math.abs(distance / vertical_speed
				* ratio) * 1000);
		move_right(time);
	}

	/*
	 * Rotate a certain amount of degrees.
	 */
	public void turn(double phi) {
		phi *= (Math.PI / 180); // angle in radians
		long time = (long) Math.round(Math.abs(phi / yaw_speed) * 1000); // time
																			// needed
																			// to
																			// turn
		if (phi < 0)
			turn_left(time);
		else
			turn_right(time);
	}

	public double rotorpower(double val) {
		return val * getRotorPower();
	}

	/*
	 * Shutdown the drone.
	 * 
	 * This method does not land or halt the actual drone, but ends the
	 * communication with the drone. You should call it at the end of your
	 * application to close all sockets, pipes, processes and threads related
	 * with this object.
	 */
	public void halt() {
		g.log("Halting...");
		super.halt();
		g.log_info("Engine Stopped", g.INFO);
	}
}
