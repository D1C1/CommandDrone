package droneKalkun;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import de.yadrone.base.navdata.Altitude;
import de.yadrone.base.navdata.AltitudeListener;

public class DroneController {

	double x, y, z;
	String data;
	int speed = 30;
	IARDrone drone = null;
	int alt = 0;

	public DroneController() {
		try {
			drone = new ARDrone();
			drone.start();
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		drone.getNavDataManager().addAltitudeListener(new AltitudeListener() {

			@Override
			public void receivedAltitude(int altitude) {
				System.out.println("alti is " + altitude);
				alt = altitude;
			}

			@Override
			public void receivedExtendedAltitude(Altitude arg0) {
				// TODO Auto-generated method stub

			}

		});
	}

	public void flightLogic() {
		boolean swt = true;
		while (swt = true) {

		}
	}

	public void takeoff() {
		drone.getCommandManager().takeOff().doFor(1500);
		drone.getCommandManager().hover().doFor(5000);
		while (alt < 1280) {
			drone.getCommandManager().up(speed).doFor(250);
		}
		while (alt > 1420) {
			drone.getCommandManager().down(speed).doFor(250);
		}
		drone.getCommandManager().hover().doFor(5000);
		drone.getCommandManager().landing();

		System.out.println("Takeoff!");
	}

	public void search() {

		// TODO lav søgnings algoritme
		// drone.getCommandManager().landing();
		System.out.println("Søgnings algoritme");

	}

	public void test() {
		drone.getCommandManager().forward(speed).doFor(2000);
		drone.getCommandManager().landing();

	}

	public void flyThroughRing() {
		// calculate distance
		// fly the distance
		drone.getCommandManager().forward(speed).doFor(2000);
		drone.getCommandManager().hover().doFor(2000);
		// TODO lav flyvnings sekvens gennem ring
		System.out.println("Flyver igennem ring");
	}

	public void land() {
		System.out.println("lander");
		drone.getCommandManager().landing();
	}

	public void center(int lowerX, int upperX, int lowerY, int upperY) {

		if (x < lowerX) {
			// System.out.println("left");
		} else if (x > upperX) {
			// System.out.println("right");
		}

		if (y < lowerY) {
			// System.out.println("Down");
		} else if (y > upperY) {
			// maybe compare to altitude
			// System.out.println("up");
		}
		if (y > lowerY && y < upperY && x > lowerX && x < upperX) {
			// System.out.println("Spot on");
		}

		System.out.println("centrer drone");

	}

	// Getters and setters

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setZ(double z) {
		this.z = z;
	}
}
