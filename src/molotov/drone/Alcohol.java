package molotov.drone;

import java.net.DatagramPacket;
import java.util.Timer;
import java.util.TimerTask;

/*
 * Copyright (c) 2014 Pol Osei
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class Alcohol extends Bottle {
	// video stuff
	public final static int BOTTOM = 1;
	public final static int FRONT = 2;
	// some stuff
	private double power;
	private boolean flying = false;

	public Alcohol() {
		this.power = 1;
		this.watchdog = new Timer();

		watchdog.schedule(new TimerTask() {
			@Override
			public void run() {
				commwdg();
			}
		}, 0, 1000);

		chooseVideoInput(Alcohol.FRONT);
	}

	public void set_power(float power) {
		/*
		 * Set the drone's power.
		 * 
		 * Valid values are floats from [0..1]
		 */

		g.log("[power]        Changing from " + this.power + " to " + power
				+ "...", g.INFO);
		this.power = power;
	}

	public void ftrim() {
		/*
		 * Tell the drone it's on a flat surface.
		 */

		if (!flying)
			this.at_ftrim();
		else
			g.log("[Warning]      Cannot ftrim while flying!", g.WARNING);
	}

	public void takeoff() throws InterruptedException {
		/*
		 * Make the drone takeoff.
		 */

		ftrim();
		// g.log("[State Change] Disable Emergency...", g.INFO);
		// this.at_ref(false, true);
		g.log("[State Change] Takeoff...", g.INFO);
		this.at_ref(true, false);
		Thread.sleep(5000);
		this.flying = true;
		g.log("[State Change] Flying...", g.FLYING);
	}

	public void land() {
		/*
		 * Make the drone land.
		 */

		try {
			g.log("[State Change] Landing...", g.INFO);
			this.at_ref(false, false);
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void hover(long time) {
		/*
		 * Make the drone hover.
		 */

		g.log("[State Change] Hovering...", g.INFO);
		double[] params = { 0.0, 0.0, 0.0, 0.0 };

		this.move(params, time * 1000);
	}

	public void move_left(long time) {
		/*
		 * Make the drone move left.
		 */

		g.log("[State Change] Moving left...", g.INFO);
		double[] params = { -this.power, 0.0, 0.0, 0.0 };
		this.move(params, time);
	}

	public void move_right(long time) {
		/*
		 * Make the drone move right.
		 */

		g.log("[State Change] Moving right...", g.INFO);
		double[] params = { this.power, 0.0, 0.0, 0.0 };
		this.move(params, time);
	}

	public void move_up(long time) {
		/*
		 * Make the drone rise upwards.
		 */

		g.log("[State Change] Moving up...", g.INFO);
		double[] params = { 0.0, 0.0, this.power, 0.0 };
		this.move(params, time);
	}

	public void move_down(long time) {
		/*
		 * Make the drone decent downwards.
		 */

		g.log("[State Change] Moving down...", g.INFO);
		double[] params = { 0.0, 0.0, -this.power, 0.0 };
		this.move(params, time);
	}

	public void move_forward(long time) {
		/*
		 * Make the drone move forward.
		 */

		g.log("[State Change] Moving forward...", g.INFO);
		double[] params = { 0.0, -this.power, 0.0, 0.0 };
		this.move(params, time);
	}

	public void move_backward(long time) {
		/*
		 * Make the drone move backwards.
		 */

		g.log("[State Change] Reversing...", g.INFO);
		double[] params = { 0.0, this.power, 0.0, 0.0 };
		this.move(params, time);
	}

	public void turn_left(long time) {
		/*
		 * Make the drone rotate left.
		 */

		g.log("[State Change] Turning left...", g.INFO);
		double[] params = { 0.0, 0.0, 0.0, -this.power };
		this.move(params, time);
	}

	public void turn_right(long time) {
		/*
		 * Make the drone rotate right.
		 */

		g.log("[State Change] Turning right...", g.INFO);
		double[] params = { 0.0, 0.0, 0.0, this.power };
		this.move(params, time);
	}

	/*
	 * Makes the drone move (translate/rotate).
	 * 
	 * Parameters: l[0] -- left-right tilt: float [-1..1] negative: left,
	 * positive: right l[1] -- front-back tilt: float [-1..1] negative:
	 * forwards, positive: backwards l[2] -- vertical speed: float [-1..1]
	 * negative: go down, positive: rise l[3] -- angular speed: float [-1..1]
	 * negative: spin left, positive: spin right
	 */
	public void move(double[] l, long time) {
		try {
			this.at_pcmd(true, l[0], l[1], l[2], l[3], time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void at_pcmd(boolean b, double lr, double e, double f, double g,
			long time) throws InterruptedException {
		super.at_pcmd(b, (float) lr, (float) e, (float) f, (float) g, time);
	}

	/*
	 * Communication watchdog signal.
	 * 
	 * This needs to be send regulary to keep the communication with the drone
	 * alive.
	 */
	public void commwdg() {
		this.at_comwdg();
	}

	/*
	 * Set the power of the rotors to use in calculations.
	 */
	public void setRotorPower(double power) {
		this.power = power;
	}

	/*
	 * Set the maximum flight altitude.
	 */
	public void setMaxAltitude(double v) {
		this.at_config("altitude_max", String.valueOf(v), "config");
	}

	/*
	 * Set the minimum flight altitude.
	 */
	public void setMinAltitude(double v) {
		this.at_config("altitude_min", String.valueOf(v), "config");
	}

	/*
	 * Set the maximum vertical speed.
	 */
	public void setMaxVZ(double v) {
		this.at_config("control_vz_max", String.valueOf(v), "config");
	}

	/*
	 * Set the maximum yaw speed.
	 */
	public void setMaxYawSpeed(double v) {
		this.at_config("control_yaw", String.valueOf(v), "config");
	}

	/*
	 * Set the maximum euler angle.
	 */
	public void setMaxEulerAngle(double v) {
		this.at_config("euler_angle_max", String.valueOf(v), "config");
	}

	/*
	 * Set whether or not the video is enabled.
	 */
	public void setVideoEnable(boolean b) {
		this.at_config("video_enable", String.valueOf(b).toUpperCase(),
				"general");
	}

	public void chooseVideoInput(int choice) {
		this.at_config("video_channel", String.valueOf(choice), "video");
	}

	/*
	 * Set navadata to demo.
	 */
	public void setNavdataOn(boolean b) {
		this.at_config("navdata_demo", String.valueOf(b).toUpperCase(),
				"general");
	}

	/*
	 * get the rotor power;
	 */
	public double getRotorPower() {
		return this.power;
	}

	/*
	 * Get navdata feed
	 */

	public void getNavFeed() {
		// try {
		byte[] buf_rcv = new byte[292];
		DatagramPacket packet_rcv = new DatagramPacket(buf_rcv, 292);
		// nav.receive(packet_rcv);
		g.log("Nav Received: " + packet_rcv.getLength() + " bytes", g.INFO);
		nav_ping();
		// }// catch (IOException ex) {
		// g.log("[!] Couldn't get nav feed!", g.ERROR);
		// }
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
		try {
			g.log_info("Halt", g.INFO);
			vid_on = false;
			if (videoCoder != null) {
				videoCoder.close();
				videoCoder = null;
			}
			if (container != null) {
				container.close();
				container = null;
			}

			if (watchdog != null) {
				watchdog.cancel();
				watchdog.purge();
			}
			if (sender != null) {
				sender.cancel();
				sender.purge();
			}
			Thread.sleep(1000);

			this.server.disconnect();
			this.server.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
