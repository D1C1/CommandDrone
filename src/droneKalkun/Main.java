package droneKalkun;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import QR.QRReader;

public class Main {

	static final int imgX = 640;
	static final int imgY = 320;
	// variance
	static final int lowerX = imgX/2 - 20;
	static final int lowerY = imgY/2 - 20;
	static final int upperX = imgX/2 + 20;
	static final int upperY = imgX/2 + 20;
	static long tempTime = 0;

	static List<String> qrNr;
	static BlockingQueue queueDrone;


	public static void main(String[] args) throws IOException, InterruptedException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.loadLibrary("opencv_ffmpeg341_64");
		// TODO Auto-generated method stub

		tempTime = System.currentTimeMillis();

		BlockingQueue queueFrame = new ArrayBlockingQueue(1024);
		BlockingQueue queueBooleans = new ArrayBlockingQueue(1024);
		BlockingQueue queueQR = new ArrayBlockingQueue(1024);
		BlockingQueue queuePoint = new ArrayBlockingQueue(1024);
		queueDrone = new ArrayBlockingQueue(1024);

		Thread OCV = new Thread(new OCVController(queueFrame, queueBooleans, queueQR, queuePoint)); //OpenCV video capture controller
		OCV.start();


		Thread VCS = new Thread(new VideoStreamController(queueFrame));
		VCS.start();

		DroneController drone = new DroneController(queueDrone, queueBooleans, queuePoint);
		boolean swt = true;
		boolean isCentered = false;

		qrNr = new ArrayList<String>();

		for (int i = 0; i < 8; i++) {
			qrNr.add(Integer.toString(i));
		}

		System.out.println("takeoff nu");
		drone.takeoff();
		waitDrone();
		System.out.println("takeoff færdig ");


		//Brug navdata i stedet

		while(swt) {

			if (!queuePoint.isEmpty()) {

//				boolean b = (boolean) queueBooleans.take();
//				Point center = (Point) queuePoint.take();
//				if (b)
//					queueBooleans.put(false);

				System.out.println("Ring fundet !!! centrere");
				isCentered = drone.center( lowerX,  upperX,  lowerY,  upperY);
				waitDrone();
				if(isCentered) {
					System.out.println("Færdig med at centrere, tjekker QR");
					drone.standStill();
					wait(2000);
					waitDrone();
					drone.searchForQR();
					waitDrone();
				}
				if (!queueQR.isEmpty()) {
					String qr = (String) queueQR.take();
					System.err.println("QR kode fundet, der står: " + qr);
					if (qr.contains("0")) {
						System.err.println("Rigtige QR! Justerer højde og flyver igennem ringen!");
						drone.correctQR();
						waitDrone();
						drone.flyThroughRing();
						waitDrone();
					}
					else {
						System.err.println("Ikke korrekte QR...");
					}
					drone.land();
					//wait(drone);
					System.exit(0);
				}

			}
			else {
				//søgnings algoritme
				drone.standStill();
				waitDrone();
				drone.search();
				waitDrone();
			}
		}
	}
	private static void wait(int millis) {
		long idle = System.currentTimeMillis();
		while (System.currentTimeMillis() - idle < millis) {
			//Do nothing
		}
	}

	//	private static void waitDrone(DroneController drone) {
	//		long idle = System.currentTimeMillis();
	//		int counter = 0;
	//		while (!drone.getReady() && counter < 15) {
	//			if (System.currentTimeMillis() - idle > 1000) {
	//				System.err.println("dronen er ikke klar endnu");
	//				idle = System.currentTimeMillis();
	//				counter++;
	//			}
	//		}
	//	}
	//	

	private static void waitDrone() {
		while(queueDrone.isEmpty()) {
			System.err.println("Dronen er ikke klar!");
		}
		try {
			boolean b = (boolean) queueDrone.take();
			System.err.println("Er dronen klar? : " + b);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private static boolean correctQR(String qr) {
		if (qr.contains("0"+qrNr.get(0)))
			return true;
		else
			return false;
	}

}
