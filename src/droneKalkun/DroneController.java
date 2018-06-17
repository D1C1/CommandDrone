package droneKalkun;

import java.util.concurrent.BlockingQueue;

import org.opencv.core.Point;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import de.yadrone.base.navdata.Altitude;
import de.yadrone.base.navdata.AltitudeListener;

public class DroneController {

	double x, y, z;
	String data;
	int speed = 5;
	IARDrone drone = null;
	int alt = 0, cases = 0;
	long timeNow, timeNow2; //Gør at dronen ikke spammer altitude ud, men kun 1 gang hvert sekund.
	private boolean ready;
	private int direction = 0;
	private BlockingQueue queueDrone;
	private BlockingQueue queueBooleans;
	private BlockingQueue queuePoint;

	public void isReady() {
		try {
			queueDrone.put(true);
		} catch (InterruptedException e) {
			System.err.println("Fejl i Drone - Dronen kan ikke blive klar");
			e.printStackTrace();
		}
	}

	public DroneController(BlockingQueue queueDrone, BlockingQueue queueBooleans, BlockingQueue queuePoint) {
		timeNow = System.currentTimeMillis();
		timeNow2 = System.currentTimeMillis();
		try {
			drone = new ARDrone();
			drone.start();
			//drone.getCommandManager().setVideoBitrate(4000); 
			this.queueDrone = queueDrone;
			this.queueBooleans = queueBooleans;
			this.queuePoint = queuePoint;
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		drone.getNavDataManager().addAltitudeListener(new AltitudeListener() {
			@Override
			public void receivedAltitude(int altitude) {
				alt = altitude;
				if (System.currentTimeMillis() - timeNow > 1000) {
					System.out.println("alti is " + altitude);
					timeNow = System.currentTimeMillis();
				}
				else if (altitude < 0)
					System.out.println("alti is negative: " + altitude);
				if (alt > 1600) 
					drone.getCommandManager().landing();
			}
			@Override
			public void receivedExtendedAltitude(Altitude arg0) {/*Empty*/}
		});
		isReady();
	}

	public void takeoff() {
		long idle = System.currentTimeMillis();
		System.err.println("takeoff");
	//	while (System.currentTimeMillis() - idle < 2500) {
			drone.getCommandManager().takeOff().doFor(2500);
		//}
		idle = System.currentTimeMillis();
		System.err.println("hover");
		//while (System.currentTimeMillis() - idle < 1000) {
			drone.getCommandManager().hover().doFor(500);
		//}		

		while (alt < 1180) { //1280 tidligere
			drone.getCommandManager().up(speed*2).doFor(250); //HUSK SPEED GANGE 2
		}
		while (alt > 1420) {
			drone.getCommandManager().down(speed).doFor(250);
		}
		drone.getCommandManager().hover();
		isReady();
	}

	public void searchForQR() throws InterruptedException {
		drone.getCommandManager().down(speed*2).doFor(2000)
		.forward(speed).doFor(1000)
		.hover();
		queueBooleans.put(true);
		isReady();
	}

	public void correctQR() {
		drone.getCommandManager().backward(speed).doFor(1000)
		.up(speed*2).doFor(2000)
		.hover();
		isReady();
	}

	public void search() {
		long idle = System.currentTimeMillis();
		switch(direction) {
		case 0:
			System.err.println("Forward.....................................");
			while (System.currentTimeMillis() - idle < 1500) {
				drone.getCommandManager().forward(speed*2).doFor(500);
			}
			direction = 1;
			break;
		case 1:
			System.err.println("Spinning right..............................");
			while (System.currentTimeMillis() - idle < 1500) {
				drone.getCommandManager().spinRight(speed*3).doFor(500);
			}
			direction = 0;
			break;
		default:
			System.err.println("Fuck im fucked");
			break;
		}
		//drone.getCommandManager().hover();
		isReady();
	}

	public void standStill() {
		drone.getCommandManager().hover();
		isReady();
	}

	public void flyThroughRing() {
		drone.getCommandManager().forward(speed*4);
		drone.getCommandManager().hover();
		isReady();
	}

	public void land() {
		drone.getCommandManager().landing();
	}

	public boolean center(int lowerX, int upperX, int lowerY, int upperY) throws InterruptedException {

		Point cp = null;
		if(!queuePoint.isEmpty())
			cp = (Point) queuePoint.take();

		if (cp != null) {
			System.err.println("Prøver at centrere dronen");
			while (cp.x < lowerX) {
				System.err.println("Left");
				drone.getCommandManager().goLeft(speed/2).doFor(50);
				if(!queuePoint.isEmpty())
					cp = (Point) queuePoint.take();
			} 
			while (cp.x > upperX) {
				System.err.println("Right");
				drone.getCommandManager().goRight(speed/2).doFor(50);
				if(!queuePoint.isEmpty())
					cp = (Point) queuePoint.take();
			}
			while(cp.y < lowerY) {
				System.err.println("Down");
				drone.getCommandManager().down(speed/2).doFor(50);
				if(!queuePoint.isEmpty())
					cp = (Point) queuePoint.take();
			}
			while(cp.y > upperY) {
				System.err.println("Up");
				drone.getCommandManager().up(speed/2).doFor(50);
				if(!queuePoint.isEmpty())
					cp = (Point) queuePoint.take();
			}
			if (cp.y > lowerY && cp.y < upperY && cp.x > lowerX && cp.x < upperX) {
				System.out.println("Spot on");
				isReady();
				drone.getCommandManager().hover();
				return true;
			}
		}
		isReady();
		drone.getCommandManager().hover();
		return false;
	}

	public boolean getReady() {
		// TODO Auto-generated method stub
		return false;
	}
	public void setReady(boolean b) {
		ready = b;
		// TODO Auto-generated method stub
	}

	private void wait(int millis) {
		long idle = System.currentTimeMillis();
		while (System.currentTimeMillis() - idle < millis) {
			//Wait ffs
		}
	}
}
