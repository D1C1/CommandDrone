package droneKalkun;

import org.opencv.core.Point;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import de.yadrone.base.configuration.ConfigurationListener;
import de.yadrone.base.navdata.Altitude;
import de.yadrone.base.navdata.AltitudeListener;
import de.yadrone.base.navdata.ControlState;
import de.yadrone.base.navdata.DroneState;
import de.yadrone.base.navdata.GyroListener;
import de.yadrone.base.navdata.GyroPhysData;
import de.yadrone.base.navdata.GyroRawData;
import de.yadrone.base.navdata.StateListener;
import de.yadrone.base.navdata.TrackerData;
import de.yadrone.base.navdata.VelocityListener;
import de.yadrone.base.navdata.VisionData;
import de.yadrone.base.navdata.VisionListener;
import de.yadrone.base.navdata.VisionPerformance;
import de.yadrone.base.navdata.VisionTag;

public class DroneController {

	double x, y, z;
	String data;
	int speed = 5;
	IARDrone drone = null;
	int alt = 0, cases = 0;
	long timeNow, timeNow2; //Gør at dronen ikke spammer altitude ud, men kun 1 gang hvert sekund.

	public DroneController() {
		timeNow = System.currentTimeMillis();
		timeNow2 = System.currentTimeMillis();
		try {
			drone = new ARDrone();
			drone.start();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		drone.getNavDataManager().addVelocityListener(new VelocityListener() {

			@Override
			public void velocityChanged(float arg0, float arg1, float arg2) {
				// TODO Auto-generated method stub
				System.out.println("arg0: " + arg0 + " arg1: " + arg1 + " arg2: " + arg2);
			}
			
		});
		
		drone.getNavDataManager().addAltitudeListener(new AltitudeListener() {

			@Override
			public void receivedAltitude(int altitude) {

				if (System.currentTimeMillis() - timeNow > 2000) {
					System.out.println("alti is " + altitude);
					timeNow = System.currentTimeMillis();
				}
				else if (altitude < 0)
					System.out.println("alti is " + altitude);
				alt = Math.abs(altitude);
				if (alt > 1700) 
					drone.getCommandManager().landing();
			}

			@Override
			public void receivedExtendedAltitude(Altitude arg0) {
				// TODO Auto-generated method stub
				//System.out.println("receivedExtendedAltitude - ObsX: " + arg0.getObsX());
			}

		});
	}

	public void flightLogic() {
		boolean swt = true;
		while (swt = true) {

		}
	}



	public void takeoff() {
		drone.getCommandManager().takeOff().doFor(2500);
		drone.getCommandManager().hover().doFor(5000);
		drone.getCommandManager().goLeft(5).doFor(100);
		while (alt < 1280) {
			drone.getCommandManager().up(speed*2).doFor(250); //HUSK SPEED GANGE 2
		}
		while (alt > 1420) {
			drone.getCommandManager().down(speed).doFor(250);
		}
		drone.getCommandManager().hover().doFor(2000);
		//1drone.getCommandManager().landing();

		System.out.println("Takeoff!");
	}

	public void search() {

		//if (System.currentTimeMillis() - timeNow2 > 500) {
		//System.out.println("Søgnings algoritme");
		switch(cases) {
		case 0:
			drone.getCommandManager().spinLeft(speed*3).doFor(250);
			cases++;
			//timeNow2 = System.currentTimeMillis();
			break;
		case 1:
			drone.getCommandManager().spinRight(speed*3).doFor(500);
			cases++;
			//timeNow2 = System.currentTimeMillis();
			break;
		case 2:
			drone.getCommandManager().spinLeft(speed*3).doFor(250);
			cases++;
			//timeNow2 = System.currentTimeMillis();
			break;
		case 3:
			/*if (this.alt > 1700) 
					drone.getCommandManager().landing();
				else*/
			drone.getCommandManager().up(speed).doFor(200);
			cases = 0;
			//timeNow2 = System.currentTimeMillis();
			break;
		default:
			cases = 0;
			//timeNow2 = System.currentTimeMillis();
			break;
		}
		//	}

	}

	public void test() {
		drone.getCommandManager().forward(speed).doFor(2000);
		drone.getCommandManager().landing();

	}

	public void standStill() {
		drone.getCommandManager().hover().doFor(2000);
	}

	public void flyThroughRing() {
		// calculate distance
		// fly the distance
		drone.getCommandManager().forward(speed*4);
		//drone.getCommandManager().forward(speed*6).doFor(5000);
		//drone.getCommandManager().forward(speed*6).doFor(5000);
		//drone.getCommandManager().hover().doFor(2000);
		// TODO lav flyvnings sekvens gennem ring
		System.out.println("Flyver igennem ring");
	}

	public void land() {
		System.out.println("lander");
		drone.getCommandManager().landing();
	}

	public boolean center(int lowerX, int upperX, int lowerY, int upperY, Point center) {

		System.out.println("Prøver at centrere dronen");

		if (center.x < lowerX) {
			System.out.println("left");
			drone.getCommandManager().goLeft(speed).doFor(150);
		} else if (center.x > upperX) {
			System.out.println("right");
			drone.getCommandManager().goRight(speed).doFor(150);
		}

		if (center.y < lowerY) {
			System.out.println("Down");
			//drone.getCommandManager().hover().doFor(200);
			drone.getCommandManager().down(speed).doFor(150);
		} else if (center.y > upperY) {
			// maybe compare to altitude
			System.out.println("up");
			//drone.getCommandManager().hover().doFor(200);
			drone.getCommandManager().up(speed).doFor(150);
		}
		if (center.y > lowerY && center.y < upperY && center.x > lowerX && center.x < upperX) {
			System.out.println("Spot on");
			//this.flyThroughRing();
			return true;
		}
		return false;

		//System.out.println("centrer drone");

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
