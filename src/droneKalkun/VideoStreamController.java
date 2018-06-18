package droneKalkun;

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.concurrent.BlockingQueue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Mat;

//reference : https://stackoverflow.com/questions/26515981/display-image-using-mat-in-opencv-java

public class VideoStreamController implements Runnable {

	private JFrame frame1, frame2;
	//private Image img;
	Mat m;
	private ImageIcon icon;
	private JLabel lbl;
	BlockingQueue queue;
	boolean running;
	private BlockingQueue queueEdge;

	public VideoStreamController(BlockingQueue queue, BlockingQueue queueEdge) {
		this.frame1 = new JFrame();
		this.frame1.setLayout(new FlowLayout());
		this.frame1.setSize(0,0);
		this.frame1.setVisible(true);
		this.frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		
		this.frame2 = new JFrame();
		this.frame2.setLayout(new FlowLayout());
		this.frame2.setSize(0,0);
		this.frame2.setVisible(true);
		this.frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		lbl = new JLabel();
		icon = new ImageIcon();
		this.queue = queue;
		this.queueEdge = queueEdge;
		running = true;
	}

	public BufferedImage Mat2BufferedImage(Object o) {

		if (o instanceof Mat) {
			m = (Mat) o;
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
		else {
			return null;
		}

	}

	public void displayImage(Image img, String frame) {

		icon = new ImageIcon(img);

		if (frame.equals("frame1")) {
			if(this.frame1.getSize().getWidth() == 0 && this.frame1.getSize().getHeight() == 0) {
				frame1.setSize(img.getWidth(null) + 50, img.getHeight(null) + 50);
			}
			lbl.setIcon(icon);
			frame1.add(lbl);
		}
		else if (frame.equals("frame2")) {
			if (this.frame2.getSize().getWidth() == 0 && this.frame2.getSize().getHeight() == 0) {
				frame2.setSize(img.getWidth(null) + 50, img.getHeight(null) + 50);
			}
			lbl.setIcon(icon);
			frame2.add(lbl);
		}
	}

	public void updateImage(Image img, String frame) {

		icon = new ImageIcon(img);
		lbl.setIcon(icon);
		if (frame.equals("frame1")) {
			frame1.setSize(img.getWidth(null) + 50, img.getHeight(null) + 50);
			frame1.add(lbl);
		}
		else if (frame.equals("frame2")) {
			frame2.setSize(img.getWidth(null) + 50, img.getHeight(null) + 50);
			frame2.add(lbl);
		}
		else
			System.err.println("Fejl i display af image");
		//System.out.println("frame width: " + frame.getWidth() + " frame height: " + frame.getHeight());
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		//Ini frame
		try {
			//this.displayImage(this.Mat2BufferedImage(queue.take()), "frame1");
			this.displayImage(this.Mat2BufferedImage(queueEdge.take()), "frame2");
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			System.err.println("Fejl ved at få objekt fra queue!");
			e1.printStackTrace();
		}

		while (running) {
			try {
				//this.updateImage(this.Mat2BufferedImage(queue.take()), "frame1");
				this.updateImage(this.Mat2BufferedImage(queueEdge.take()), "frame2");
			} catch (InterruptedException e) {
				System.err.println("Fejl ved at få fat i objekt i Blockingqueue!");
				e.printStackTrace();
			}
		}
	}

}
