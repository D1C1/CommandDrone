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


	public static void main(String[] args) throws IOException, InterruptedException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.loadLibrary("opencv_ffmpeg341_64");
		// TODO Auto-generated method stub

		tempTime = System.currentTimeMillis();

		BlockingQueue queueFrame = new ArrayBlockingQueue(1024);
		BlockingQueue queueBooleans = new ArrayBlockingQueue(1024);
		BlockingQueue queueQR = new ArrayBlockingQueue(1024);
		BlockingQueue queuePoint = new ArrayBlockingQueue(1024);

		Thread OCV = new Thread(new OCVController(queueFrame, queueBooleans, queueQR, queuePoint)); //OpenCV video capture controller
		OCV.start();

		wait(1000); //Tid til at få billeder til VideoStreamController (VCS)

		Thread VCS = new Thread(new VideoStreamController(queueFrame));
		VCS.start();

		DroneController drone = new DroneController();
		boolean swt = true;
		boolean isCentered = false;

		System.out.println("takeoff nu");
		drone.takeoff();
		wait(5000);
		System.out.println("takeoff færdig ");


		//Brug navdata i stedet

		while(swt) {

			if ((!queueBooleans.isEmpty()) && (!queuePoint.isEmpty())) {

				boolean b = (boolean) queueBooleans.take();
				Point center = (Point) queuePoint.take();
				if (b)
					queueBooleans.put(false);

				System.out.println("Ring fundet !!! centrere");
				if(isCentered == false) {
					isCentered = drone.center( lowerX,  upperX,  lowerY,  upperY, center);
					long idle = System.currentTimeMillis();
					wait(500);
					//Thread.sleep(250);
				}else if (isCentered == true) {
					drone.standStill();
					wait(2000);
				}
				if (!queueQR.isEmpty()) {
					String qr = (String) queueQR.take();
					System.err.println("QR kode fundet, der står: " + qr);
					// if qr er sand
					//udregn distance
					System.err.println("Flyver igennem ringen!");
					drone.flyThroughRing();
					//long idle = System.currentTimeMillis();
					wait(5000);
					drone.land();
					wait(2000);
					System.exit(0);
				}

			}else {
				//søgnings algoritme
				drone.standStill();
				wait(2000);
				drone.search();
				long idle = System.currentTimeMillis();
				wait(500);
				//swt = false;
			}
		}
	}
	
	private static void wait(int millis) {
		long idle = System.currentTimeMillis();
		while (System.currentTimeMillis() - idle < millis) {
			//Do nothing
		}
	}
}


