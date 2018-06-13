package src;

import molotov.drone.Vodka;

public class DroneController {
	
	Vodka drone;
	double x, y, z;
	
	public DroneController() {
		//drone = new Vodka();
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
