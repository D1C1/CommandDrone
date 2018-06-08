package src;

import java.awt.image.BufferedImage;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class Main {

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.loadLibrary("opencv_ffmpeg341_64");
		// TODO Auto-generated method stub
		
		VideoCapture cam = new VideoCapture("tcp://192.168.1.1:5555");
		Mat frame = new Mat();
		MatTransformer MT = new MatTransformer();
		BufferedImage Bimg = null;
		
		if (cam.isOpened()) {
			cam.read(frame);
			
			Bimg = MT.Mat2BufferedImage(frame);
			MT.displayImage(Bimg);
			while(true ) {
				cam.read(frame);
				
				
				Mat gray = new Mat();
				Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
				Imgproc.medianBlur(gray, gray, 5);
				//Imgproc.GaussianBlur(gray, gray, new Size(9,9), 0);
				Mat circles = new Mat();
				Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1.0, (double) gray.rows() / 1, // change this value to detect circles with different distances to each other
				100.0, 60.0, 100, 500); // change the last two parameters
				// (min_radius & max_radius) to detect larger circles
				for (int x = 0; x < circles.cols(); x++) {
					double[] c = circles.get(0, x);
					Point center = new Point(Math.round(c[0]), Math.round(c[1]));
					// circle center
					Imgproc.circle(frame, center, 1, new Scalar(0, 100, 100), 3, 8, 0);
					// circle outline
					int radius = (int) Math.round(c[2]);
					System.out.println("radius = "+radius);
					System.out.println(gray.rows());
					Imgproc.circle(frame, center, radius, new Scalar(255, 0, 255), 3, 8, 0);
				}
				
				
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
	}

}
