package src;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import QR.QRReader;

public class Main {

	static final int imgX = 640;
	static final int imgY = 320;
	// variance
	static final int lowerX = imgX/2 - 15;
	static final int lowerY = imgY/2 - 15;
	static final int upperX = imgX/2 + 15;
	static final int upperY = imgX/2 + 15;





	public static void main(String[] args) throws IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.loadLibrary("opencv_ffmpeg341_64");
		// TODO Auto-generated method stub

		VideoCapture cam = new VideoCapture("tcp://192.168.1.1:5555");
		Mat frame = new Mat();
		MatTransformer MT = new MatTransformer();
		String qr;
		BufferedImage Bimg = null;
		BufferedImage Bimgc = null;
		//Vodka drone = new Vodka();
		boolean swt = true;
		Point center = new Point(0,0);
		double angle = 0;
		double posX = 0,posY = 0,posZ = 0;
		List<Point> circleslist = new ArrayList<Point>();
		/*
		drone.setRotorPower(0.25);

		try {
			drone.takeoff();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 */

		posX = 0;
		posY = 0;
		posZ = 0;

		if (cam.isOpened()) {
			cam.read(frame);

			Bimg = MT.Mat2BufferedImage(frame);
			MT.displayImage(Bimg);

			while(swt == true ) {
				cam.read(frame);

				qr = QRReader.ReadQR(Bimg);

				System.out.println(qr);

				Mat gray = new Mat();
				Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
				Imgproc.medianBlur(gray, gray, 5);
				//Imgproc.GaussianBlur(gray, gray, new Size(9,9), 0);
				Mat circles = new Mat();
				Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1.0, (double) gray.rows() / 1, // change this value to detect circles with different distances to each other
						100.0, 60.0, 100, 500); // change the last two parameters
				// (min_radius & max_radius) to detect larger circles

				Mat tempCircles = circles.clone();

				for (int x = 0; x < circles.cols(); x++) {
					double[] c = circles.get(0, x);
					center = new Point(Math.round(c[0]), Math.round(c[1]));
					System.out.println("center x: " + center.x + " center y: " + center.y);
					circleslist.add(center);
					// circle center
					Imgproc.circle(frame, center, 1, new Scalar(0, 100, 100), 3, 8, 0);
					// circle outline
					int radius = (int) Math.round(c[2]);
					System.out.println("radius = "+radius);
					System.out.println(gray.rows());
					System.out.println("col = " + gray.cols());
					Imgproc.circle(frame, center, radius, new Scalar(255, 0, 255), 3, 8, 0);

				}

				if(circleslist.size() > 9) {
					System.out.println("arraylist size: " + circleslist.size());
					double tempX = circleslist.get(0).x;
					double tempY = circleslist.get(0).y;
					boolean searchQR = false;
					for (int i = 1; i < circleslist.size(); i++) {
						if (!(tempX >= circleslist.get(i).x - 15 || tempX <= circleslist.get(i).x + 15) && (tempY >= circleslist.get(i).y - 15 || tempY <= circleslist.get(i).y + 15))
							searchQR = false;
						else
							searchQR = true;
					}
					
					if (searchQR == true) {
						//Scan QR, fly through circle...
					}
				}

				posX = center.x;
				posY = center.y;
				if(posX < lowerX) {
					System.out.println("left");
				}else if (posX > upperX) {
					System.out.println("right");
				}

				if(posY < lowerY) {
					System.out.println("Down");
				}else if (posY > upperY) {
					System.out.println("up");
				}
				if (posY > lowerY && posY < upperY && posX > lowerX && posX < upperX) {
					System.out.println("Spot on");
				}


				// Centrere dronen i forhold til center (cirklen den finder på kameraet)


				/*
				if (circles.cols()!=0) {
					// scan qr
					// if qr er sand
					//udregn distance 

					drone.forward(1);
					//TODO set x += parameteren
					posX += 1;
					drone.land();
					swt = false;
				}else {
					//søgnings algoritme
					drone.land();
					swt = false;

					System.out.println("turning");
					angle=+5;
					if(angle == 360) {
						angle = 0;
						drone.up(0.5);
						//TODO samme med z som x
						posZ += 0.5;
					}

				}
				 */
				Bimg = MT.Mat2BufferedImage(frame);
				MT.updateImage(Bimg);
				//HighGui.imshow("detected circles", frame);
			}


		}
		else {
			System.out.println("Error...");
		}

		/*
		VideoCapture cam = new VideoCapture();
		cam.open("tcp://192.168.1.1:5555");

		if (cam.isOpened()) {

			System.out.println(cam.grab());

		}
		else {
			System.out.println("Error...");
		}
		 */
		//drone.land();
	}

}
