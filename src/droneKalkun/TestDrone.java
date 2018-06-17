package droneKalkun;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TestDrone {

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		
		BlockingQueue queueBooleans = new ArrayBlockingQueue(1024);
		BlockingQueue queueBooleans2 = new ArrayBlockingQueue(1024);
		BlockingQueue queueBooleans3 = new ArrayBlockingQueue(1024);
		
		DroneController drone = new DroneController(queueBooleans, queueBooleans2, queueBooleans3);
		
		drone.takeoff();
		
		Thread.sleep(5000);
		
		drone.search();
		
		Thread.sleep(6000);
		
		drone.land();
		
	}

}
