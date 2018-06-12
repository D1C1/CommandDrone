package molotov.drone;

import java.io.IOException;

/*
 * Advanced Flight Controller.
 * @author pol
 */

public class Molotov extends Thread {
	Vodka _agent;
	public Molotov(Vodka drone) {
		_agent = drone;
	}

	public void run() {
		try {
			fly();
			_agent.halt();
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	private void fly() throws InterruptedException, IOException {
		_agent.takeoff();
		_agent.vid_on = true;
		_agent.up(5);
		_agent.setRotorPower(0.1);
		_agent.land();
		_agent.vid_on = false;
	}
}
