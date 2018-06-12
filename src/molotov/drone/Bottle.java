package molotov.drone;

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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Timer;

import javax.swing.JFrame;

import org.opencv.core.Core;

import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStreamCoder;

public class Bottle {
	// Communcation stuff
	final static int ARDRONE_NAVDATA_PORT = 5554;
	final static int ARDRONE_VIDEO_PORT = 5555;
	final static int ARDRONE_COMMAND_PORT = 5556;
	final static String ARDRONE_IP = "192.168.1.1";
	final static int time_delay = 30;

	// Threads, timing
	Timer watchdog, sender;
	public int seq_nr;

	// Socket stuff
	InetAddress ip;
	DatagramSocket server;
	DatagramSocket nav;
	Socket video;
	InputStreamReader vid_in;
	OutputStreamWriter vid_out;

	DatagramPacket nav_data;

	byte[] buf_snd = { 0x01, 0x00, 0x00, 0x00 };

	// Logging
	Logger g = new Logger();
	private JFrame frame;
	private Panel my_panel;
	protected IContainer container;
	protected IStreamCoder videoCoder = null;
	protected DroneVideo dv;
	public boolean vid_on = false;

	public Bottle() {
		try {
			seq_nr = 1;
			ip = InetAddress.getByName(ARDRONE_IP);

			g.log("Connecting to drone command...", g.NORMAL);
			this.server = new DatagramSocket(ARDRONE_COMMAND_PORT);

			beginFilming();
			startNav();

			g.log("All streams connected.", g.NORMAL);
			sender = new Timer();
		} catch (IOException e) {
			g.log("Something's fucked up....");
		}
	}

	/*
	 * ################################################## ## Low level AT
	 * Commands ############ #####################################
	 */

	private void startNav() {
		try {
			g.log("Connecting to drone navdata...", g.NORMAL);
			this.nav = new DatagramSocket(ARDRONE_NAVDATA_PORT);
			nav_data = new DatagramPacket(buf_snd, buf_snd.length, ip,
					ARDRONE_NAVDATA_PORT);
			nav_ping();
			g.log_info("BEGIN NAVDATA FEED", g.INFO);
		} catch (SocketException e) {
			g.log("Failed.", g.ERROR);
		}
	}

	private void beginFilming() throws IOException {
		g.log("Connecting to drone video...");
		this.video = new Socket(ip, ARDRONE_VIDEO_PORT);

		video.setSoTimeout(3000);
		vid_in = new InputStreamReader(video.getInputStream());
		vid_out = new OutputStreamWriter(video.getOutputStream());

		g.log("Connected.");

		// Load the native library.
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String window_name = "Molotov-Feed";

		frame = new JFrame(window_name);
		my_panel = new Panel();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(680, 420);
		frame.setContentPane(my_panel);
		frame.setVisible(true);
	}

	public void nav_ping() {
		try {
			g.log("*nav_ping*", g.INFO);
			nav.send(nav_data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void at_ref(boolean takeoff, boolean emergency) {
		try {
			/*
			 * Basic behaviour of the drone: take-off/landing, emergency
			 * stop/reset)
			 * 
			 * Parameters: seq -- sequence number takeoff -- True: Takeoff /
			 * False: Land emergency -- True: Turn of the engines
			 */

			int p = 0b10001010101000000000000000000;
			if (takeoff)
				p += 0b1000000000;
			if (emergency)
				p += 0b0100000000;

			String[] params = { String.valueOf(p) };

			at("REF", params);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Makes the drone move (translate/rotate).
	 * 
	 * Parameters: seq -- sequence number progressive -- True: enable
	 * progressive commands, False: disable (i.e. enable hovering mode) lr --
	 * left-right tilt: float [-1..1] negative: left, positive: right rb --
	 * front-back tilt: float [-1..1] negative: forwards, positive: backwards vv
	 * -- vertical speed: float [-1..1] negative: go down, positive: rise va --
	 * angular speed: float [-1..1] negative: spin left, positive: spin right
	 * 
	 * The above float values are a percentage of the maximum speed.
	 */
	public void at_pcmd(boolean progressive, float lr, float fb, float vv,
			float va, long time) throws InterruptedException {
		try {
			int p = progressive ? 1 : 0;
			String[] params = { String.valueOf(p), String.valueOf(f2i(lr)),
					String.valueOf(f2i(fb)), String.valueOf(f2i(vv)),
					String.valueOf(f2i(va)) };

			for (int i = 0; i < time / time_delay; i++) {
				at("PCMD", params);
				Thread.sleep(time_delay);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void at_ftrim() {
		try {
			/*
			 * Tell the drone it's lying horizontally.
			 * 
			 * Parameters: seq -- sequence number
			 */

			String[] params = {};
			g.log("[FTrim]        Ftrimming...", g.INFO);
			at("FTRIM", params);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void at_config(String param, String value, String option) {
		try {
			/*
			 * Set configuration parameters of the drone. TODO: Check for acks
			 */

			String[] params = { '"' + option + ":" + param + '"',
					'"' + value + '"' };
			g.log("[Config] Setting " + param + " [" + option + "] to " + value
					+ "...", g.INFO);
			at("CONFIG", params);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startVideo() throws IOException {
		container = IContainer.make();
		vid_on = true;
		dv = new DroneVideo(this, container, video.getInputStream(), videoCoder,
				my_panel);
		dv.run();
	}

	public void at_comwdg() {
		try {
			/*
			 * Reset communication watchdog.
			 */

			String[] params = {};
			at("COMWDG", params);
			// g.log("*Watchdog*", g.WATCHDOG);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void at(final String command, final String[] params)
			throws IOException {
		/*
		 * Parameters: command -- the command seq -- the sequence number params
		 * -- a list of elements which can be either int, float or string
		 */

		String param_str = "";
		for (String p : params)
			param_str += ',' + p;

		String msg = "AT*"
				+ command
				+ (!command.equals("COMWDG") ? "=" + String.valueOf(seq_nr)
						+ param_str : "");
		// Make actual UDP broadcast
		InetAddress ip = InetAddress.getByName(ARDRONE_IP);
		byte[] _msg = (msg + "\r").getBytes();
		DatagramPacket packet = new DatagramPacket(_msg, _msg.length, ip,
				ARDRONE_COMMAND_PORT);

		server.send(packet);
		g.log("[UDP" + seq_nr + "] " + msg, g.PACKET);

		if (!command.equals("COMWDG"))
			seq_nr++;
	}

	public int f2i(float f) {
		/*
		 * Interpret IEEE-754 floating-point value as signed integer.
		 * 
		 * Arguments: f -- floating point value
		 */

		return Float.floatToIntBits(f);
	}
}
