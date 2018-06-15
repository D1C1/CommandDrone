package droneKalkun;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
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

	public OCVController(BlockingQueue queueFrame, BlockingQueue queueBooleans, BlockingQueue queueQR, BlockingQueue queuePoint) {
		cam = new VideoCapture("tcp://192.168.1.1:5555");
		frame = new Mat();
		this.queueFrame = queueFrame;
		this.queueBooleans = queueBooleans;
		this.queueQR = queueQR;
		this.queuePoint = queuePoint;
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
		while (running) {
			cam.read(frame); //640 x 360


			Mat gray = new Mat();
			Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
			Imgproc.medianBlur(gray, gray, 5);

			Mat circles = new Mat();
			Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1.0, (double) gray.rows() / 1, // change this value to detect circles with different distances to each other
					100.0, 60.0, 100, 500);

			for (int x = 0; x < circles.cols(); x++) {
				double[] c = circles.get(0, x);
				center = new Point(Math.round(c[0]), Math.round(c[1]));
				//System.out.println("center x: " + center.x + " center y: " + center.y);
				circleslist.add(center);
				// circle center
				Imgproc.circle(frame, center, 1, new Scalar(0, 100, 100), 3, 8, 0);
				// circle outline
				int radius = (int) Math.round(c[2]);
				Imgproc.circle(frame, center, radius, new Scalar(255, 0, 255), 3, 8, 0);
			}

			if(circleslist.size() > 9) {
				//System.out.println("arraylist size: " + circleslist.size());
				double tempX = circleslist.get(0).x;
				double tempY = circleslist.get(0).y;
				for (int i = 1; i < circleslist.size(); i++) {
					if (!(tempX >= circleslist.get(i).x - 20 || tempX <= circleslist.get(i).x + 20) && (tempY >= circleslist.get(i).y - 20 || tempY <= circleslist.get(i).y + 20)) {
						searchForCenter = false;
						break;
					}
					else
						searchForCenter = true;
				}
				circleslist.clear();
				if (circles.cols()!=0 && searchForCenter) { //Cirklen er god nok

					try {
						qr = QRReader.ReadQR(this.Mat2Bimg(frame));
						if (qr != null && !qr.isEmpty())
							queueQR.put(qr);
					} catch (IOException | InterruptedException e1) {
						// TODO Auto-generated catch block
						System.err.println("Fejl ved læsning af QR kode...");
						e1.printStackTrace();
					}

					if (queueBooleans.isEmpty() && queuePoint.isEmpty()) { //Nullpointer
						try {
							queueBooleans.put(true);
							queuePoint.put(center);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}

			try {
				queueFrame.put(frame);
			} catch (InterruptedException e) {
				System.err.println("Fejl ved at putte frame i queue!");
				e.printStackTrace();
			}
		}
	}

	//	public boolean getSearchForQR() {
	//		return searchForQR;
	//	}
	//	
	//	public boolean getBeginSearch() {
	//		return beginSearch;
	//	}


}
