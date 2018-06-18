package droneKalkun;

import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import QR.QRReader;

public class OCVController implements Runnable {

	VideoCapture cam;
	Mat frame = new Mat();
	BlockingQueue queueFrame, queueBooleans, queueQR, queuePoint;
	boolean running, searchForCenter, beginSearch;
	BufferedImage Bimg;
	Point center;
	List<Point> circleslist;
	String qr;
	private boolean b;
	private BlockingQueue queueEdge;

	public OCVController(BlockingQueue queueFrame, BlockingQueue queueBooleans, BlockingQueue queueQR, BlockingQueue queuePoint, BlockingQueue queueEdge) {
		cam = new VideoCapture("tcp://192.168.1.1:5555");
		frame = new Mat();
		this.queueFrame = queueFrame;
		this.queueBooleans = queueBooleans;
		this.queueQR = queueQR;
		this.queuePoint = queuePoint;
		this.queueEdge = queueEdge;
		running = true;
		searchForCenter = false;
		beginSearch = false;
		//cam.set(3, 320); //Resolution på bredden - 3: bredde, 320 er reso
		//cam.set(4, 180); //Samme som ovenover - 4: højde
		center = new Point(0,0);
		circleslist = new ArrayList<Point>();
		qr = "default value"; //Undgår null pointer
	}

	private BufferedImage Mat2Bimg(Mat m) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}

		int bufferSize = m.channels()*m.cols()*m.rows();
		byte [] b = new byte[bufferSize];
		m.get(0, 0, b);
		BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return image;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		b = false;
		while (running) {
			cam.read(frame); //640 x 360

			Mat bilateral_filtered_img = new Mat();
			Mat edge = new Mat();
			Imgproc.bilateralFilter(frame, bilateral_filtered_img, 5, 175, 175);
			Imgproc.Canny(bilateral_filtered_img, edge, 75, 200);

			List<MatOfPoint> contours = new ArrayList<>();
			List<Point> hierarchy = new ArrayList<>();
			
			Mat edgeContours = new Mat();

			Imgproc.findContours(edge, contours, edgeContours, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

			List<MatOfPoint> rectContours = new ArrayList<>();

			double largest_area = 0;
			int largest_countour_index = 0;
			
			for (int i = 0; i < contours.size(); i++) {

				double a = Imgproc.contourArea(contours.get(i), false);

				if (a > largest_area) {
					largest_area = a;
					largest_countour_index = i;
				}
				for (int j = 0; j <= i; j++) {
					if (edgeContours.get(j, 2, edgeContours.get(j, 2)) != -1) {
						Imgproc.drawContours(edge, contours, j, new Scalar(0,255,0));
					}
					else {
						Imgproc.drawContours(edge, contours, j, new Scalar(255,0,0));
					}
				}
				Imgproc.drawContours(edge, contours, largest_countour_index, new Scalar(0,0,255), 1, 8, edgeContours, 0, new Point());

			}


			//			MatOfPoint2f magic = new MatOfPoint2f();
			//			
			//				for (int i = 0; i<contours.size(); i++) {
			//					
			//					contours.get(i).convertTo(magic, CvType.CV_32FC2);
			//					
			//					Imgproc.approxPolyDP(magic, magic, 0.01*Imgproc.arcLength(magic, true), true);
			//					int approx = magic.channels();
			//					magic.convertTo(contours.get(i), CvType.CV_32S);
			//					
			//					//double area = Imgproc.contourArea(contours.get(i));
			//					//if (approx < 8) {
			//						rectContours.add(contours.get(i));
			//						System.out.println(rectContours.get(i).);
			//					//}
			//				}
			//				
			//				Imgproc.drawContours(edge, rectContours, -1, new Scalar(255,0,0));
			//				//Imgproc.cvtColor(edge, edge, Imgproc.COLOR_BGR2GRAY);

			try {
				queueFrame.put(frame);
				queueEdge.put(edge);
			} catch (InterruptedException e) {
				System.err.println("Fejl ved at putte frame i queue!");
				e.printStackTrace();
			}
		}
	}

	//	Mat dest = new Mat();
	//	
	//	Core.inRange(frame, new Scalar(58,125,0), new Scalar(256,256,256), dest);
	//	
	//	Mat erode = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3));
	//	Mat dilate = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5));
	//	
	//	Imgproc.erode(dest, dest, erode);
	//	Imgproc.erode(dest, dest, erode);
	//	
	//	Imgproc.dilate(dest, dest, dilate);
	//	Imgproc.dilate(dest, dest, dilate);
	//	
	//	List<MatOfPoint> contours = new ArrayList<>();
	//	
	//	Imgproc.findContours(dest, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
	//	Imgproc.drawContours(dest, contours, -1, new Scalar(255,0,0));
}