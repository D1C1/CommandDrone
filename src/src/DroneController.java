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

import molotov.drone.Alcohol;
import molotov.drone.Bottle;
import molotov.drone.DroneState;
import molotov.drone.Vodka;

public class DroneController {
	
	Vodka drone;
	double x, y, z;
	String data;
	
	public DroneController() {
		drone = new Vodka();
		
		System.out.println(drone.getCoordinates());
		
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
