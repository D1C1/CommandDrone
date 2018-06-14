package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.LEDAnimation;
import de.yadrone.base.exception.ARDroneException;
import de.yadrone.base.exception.IExceptionListener;
import de.yadrone.base.navdata.AttitudeListener;

import molotov.drone.Alcohol;
import molotov.drone.Bottle;
import molotov.drone.DroneState;
import molotov.drone.Vodka;

public class DroneController {
	
	double x, y, z;
	String data;
	int speed = 30;
	IARDrone drone = null;


	
	public DroneController() {
		//drone = new Vodka();
	    try
	    {
	        drone = new ARDrone();
	        drone.start();
	    }
	    catch (Exception exc)
		{
			exc.printStackTrace();
		}
		finally
		{
			if (drone != null)
				drone.stop();
			System.exit(0);
		}

	    
		drone.getNavDataManager().addAttitudeListener(new AttitudeListener() {
			
		    public void attitudeUpdated(float pitch, float roll, float yaw)
		    {
		        System.out.println("Pitch: " + pitch + " Roll: " + roll + " Yaw: " + yaw);
		    }

		    public void attitudeUpdated(float pitch, float roll) { }
		    public void windCompensation(float pitch, float roll) { }
		});
		
		drone.addExceptionListener(new IExceptionListener() {
		    public void exeptionOccurred(ARDroneException exc)
		    {
		        exc.printStackTrace();
		    }
		});
		
		
		//System.out.println(drone.getCoordinates());
		
		//https://stackoverflow.com/questions/29862870/java-udp-connection?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
		/*BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			DatagramSocket socket = new DatagramSocket(5554);
			byte[] inData = new byte[1024];
			
			InetAddress IP = InetAddress.getByName("192.168.1.1");
			DatagramPacket recPackage = new DatagramPacket(inData, inData.length);
			socket.receive(recPackage);
			data = recPackage.toString();
			System.out.println("Data got: " + data);
			
		} catch (IOException e) {
			System.err.println("Error in UDP connection!");
			e.printStackTrace();
		}
		*/
		//Ini x, y and z variables.
	}
		
	
	public void takeoff() {
		//drone.takeoff();
		drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_ORANGE, 3, 10);
		//drone.getCommandManager().takeOff();
		//drone.getCommandManager().waitFor(5000);
		//drone.getCommandManager().landing();
		System.out.println("Takeoff!");
	}
	
	public void search() {
		
		//TODO lav søgnings algoritme
		System.out.println("Søgnings algoritme");
		
	}
	
	public void flyThroughRing() {
		
		//TODO lav flyvnings sekvens gennem ring
		System.out.println("Flyver igennem ring");
	}
	

	//Getters and setters
	
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
