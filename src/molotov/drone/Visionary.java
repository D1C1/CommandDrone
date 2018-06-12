package molotov.drone;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Visionary {
	public static Mat matify(BufferedImage im) {
		// Convert INT to BYTE
		// im = new BufferedImage(im.getWidth(),
		// im.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
		// Convert bufferedimage to byte array
		byte[] pixels = ((DataBufferByte) im.getRaster().getDataBuffer())
				.getData();

		// Create a Matrix the same size of image
		Mat image = new Mat(im.getHeight(), im.getWidth(), CvType.CV_8UC3);
		// Fill Matrix with image values
		image.put(0, 0, pixels);

		return image;
	}

	public static BufferedImage MatToBufferedImage(Mat matBGR) {
		BufferedImage image;
		int width = matBGR.width(), height = matBGR.height(), channels = matBGR
				.channels();
		byte[] sourcePixels = new byte[width * height * channels];
		matBGR.get(0, 0, sourcePixels);
		// create new image and get reference to backing data
		image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster()
				.getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

		return image;
	}

	public static Mat detect(Mat inputframe, double thresh, double thresh2) {
		Mat mRgba = new Mat();
		inputframe.copyTo(mRgba);

		Mat mYuv = new Mat();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.threshold(mRgba, mYuv, thresh, thresh2, Imgproc.THRESH_BINARY);
		// Imgproc.Canny(mYuv, mYuv, 100, 100);
		Imgproc.cvtColor(mYuv, mYuv, Imgproc.COLOR_RGB2GRAY, 4);

		Imgproc.findContours(mYuv, contours, hierarchy, Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_SIMPLE);
		Rect rect = null;

		if (!contours.isEmpty()) {
			rect = Imgproc.boundingRect(contours.get(0));
			for (int i = 1; i < contours.size(); i++) {
				if (contourIsArrow(contours.get(i), mYuv)) {
					rect = Imgproc.boundingRect(contours.get(i));
					Imgproc.drawContours(mRgba, contours, i,
							new Scalar(0, 0, 0), 1);

					if (rect.height > 28) {
						Imgproc.rectangle(mRgba, new Point(rect.x, rect.y),
								new Point(rect.x + rect.width, rect.y
										+ rect.height), new Scalar(0, 255, 0),
								1);
						Imgproc.line(
								mRgba,
								new Point(rect.width / 2 + rect.x, rect.height
										/ 2 + rect.y),
								new Point(mRgba.width() / 2, mRgba.height() / 2),
								new Scalar(255, 255, 3), 3);
					}
				}
			}
		}

		Imgproc.line(mRgba, new Point(mRgba.width() / 2, 0),
				new Point(mRgba.width() / 2, mRgba.height()), new Scalar(255,
						255, 255), 3);
		Imgproc.line(mRgba, new Point(0, mRgba.height() / 2),
				new Point(mRgba.width(), mRgba.height() / 2), new Scalar(255,
						255, 255), 3);
		return mRgba;
	}

	// Check if contour is an arrow
	private static boolean contourIsArrow(MatOfPoint c, Mat m) {
		if (Imgproc.contourArea(c) > 50 && c.isContinuous()) {
			MatOfPoint2f approx = new MatOfPoint2f();
			MatOfPoint2f mMOP2f1 = new MatOfPoint2f();

			c.convertTo(mMOP2f1, CvType.CV_32FC2);
			Imgproc.approxPolyDP(mMOP2f1, approx, 10, true);

			mMOP2f1.convertTo(c, CvType.CV_32S);

			if (approx.size().height == 7) {
				do_stuff(m, approx);
				return true;
			}
		}

		return false;
	}

	private static void do_stuff(Mat mRgba, MatOfPoint2f hept) {
		double minAgle = 360;
		int vertex = 0;
		double[] tip = null;

		for (int t = 1; t < 7; t++) {
			Imgproc.line(mRgba, new Point(hept.get(t - 1,0)), new Point(hept.get(t, 0)),
					new Scalar(255, 255, 255), 1);
			//float grad = (float) ((float) (hept.get(0,t - 1)[1] -hept.get(0,t)[1]) / (hept.get(0,t - 1)[0] - hept.get(0,t][0]));
			//double[] pos = { (hept.get(0,t - 1][1] - hept.get(0,t][1]) / 2,
				//	(hept.get(0,t - 1][0] - hept.get(0,t][0]) / 2 };

			if (t < 6) {
				// double grad = (float) ((float)
				// (hept.get(0,t-1][1]-hept.get(0,t][1])/(hept.get(0,t-1][0]-hept.get(0,t][0]))
				double angle = calcAngle(hept.get(t - 1,0), hept.get(t,0), hept.get(t + 1,0));
				//System.out.println(angle);
				if (angle <= minAgle) {
					minAgle = angle;
					tip = hept.get(t,0);
					vertex = t;
				}
				// Core.circle(mRgba, new Point(hept.get(t-1,0)), 3, new Scalar(255,
				// 0, 0), -1);
				//System.out.println(hept.get(t,0));
				Imgproc.circle(mRgba, new Point(hept.get(t,0)), 3, new Scalar(0, 255,
				 0), -1);
				// Core.circle(mRgba, new Point(hept.get(t+1,0)), 3, new Scalar(0, 0,
				// 255), -1);
			}
		}

		Imgproc.line(mRgba, new Point(hept.get(6,0)), new Point(hept.get(0,0)), new Scalar(
				255, 255, 255), 1);
		double angle = calcAngle(hept.get(5,0), hept.get(6,0), hept.get(0,0));
		if (angle <= minAgle) {
			minAgle = angle;
			tip = hept.get(0,6);
			vertex = 6;
		}

		//System.out.println(angle);

		angle = calcAngle(hept.get(6,0), hept.get(0,0), hept.get(1,0));
		if (angle <= minAgle) {
			minAgle = angle;
			tip = hept.get(0,0);
			vertex = 0;
		}
		//System.out.println(angle);

		// Check furthest point from t
		double[] one;
		double[] two;

		if (vertex == 0) {
			one = hept.get(6,0);
			two = hept.get(1,0);
		} else if (vertex == 6) {
			one = hept.get(5,0);
			two = hept.get(0,0);
		} else {
			one = hept.get(vertex - 1,0);
			two = hept.get(vertex + 1,0);
		}

		int tip_index = 0, other_index = 0;
		if (Math.sqrt(Math.pow(one[0] - hept.get(vertex,0)[0], 2)
				+ Math.pow(one[1] - hept.get(vertex,0)[1], 2)) < Math.sqrt(Math.pow(
				two[0] - hept.get(vertex,0)[0], 2)
				+ Math.pow(two[1] - hept.get(vertex,0)[1], 2))) {
			tip = two;
			if (vertex == 0) {
				tip_index = 1;
			} else if (vertex == 6) {
				tip_index = 0;
			} else {
				tip_index = vertex + 1;
			}
		} else {
			if (vertex == 0) {
				tip_index = 6;
			} else if (vertex == 6) {
				tip_index = 5;
			} else {
				tip_index = vertex - 1;
			}
			tip = one;
		}

		// Last trident point
		if (tip_index == 0)
			other_index = vertex == 6 ? 1 : 6;
		else if (tip_index == 6)
			other_index = vertex == 5 ? 0 : 5;
		else
			other_index = tip_index - 1 == vertex ? tip_index + 1
					: tip_index - 1;

		// Midpoint finding:
		double[] midpoint = {(hept.get(other_index,0)[0]+hept.get(vertex,0)[0])/2, (hept.get(other_index,0)[1]+hept.get(vertex,0)[1])/2};

		// Draw arrow points
		Imgproc.circle(mRgba, new Point(tip), 4, new Scalar(255, 0, 0), -1);
		Imgproc.circle(mRgba, new Point(hept.get(vertex,0)), 3, new Scalar(0, 255, 0),
				-1);
		Imgproc.circle(mRgba, new Point(hept.get(other_index,0)), 3, new Scalar(0, 255,
				0), -1);
		Imgproc.circle(mRgba, new Point(midpoint), 3, new Scalar(0, 0,
				255), -1);

		// Draw arrow vector line
		Imgproc.line(mRgba, new Point(tip), new Point(midpoint), new Scalar(255, 255, 255), 1);

		// axes
		Imgproc.line(mRgba, new Point(mRgba.width() / 2, 0),
				new Point(mRgba.width() / 2, mRgba.height()), new Scalar(0, 0,
						0), 2);
		Imgproc.line(mRgba, new Point(0, mRgba.height() / 2),
				new Point(mRgba.width(), mRgba.height() / 2), new Scalar(0, 0,
						0), 2);
	}

	public static double calcAngle(double[] l1, double[] l2, double[] l3) {
		double[] x = { l2[0] - l1[0], l2[1] - l1[1] };
		double[] y = { l2[0] - l3[0], l2[1] - l3[1] };

		double angle = Math.acos((x[0] * y[0] + x[1] * y[1])
				/ (Math.sqrt(Math.pow(x[0], 2) + Math.pow(x[1], 2)) * (Math
						.sqrt(Math.pow(y[0], 2) + Math.pow(y[1], 2)))));

		return angle * 180 / Math.PI;
	}
}
