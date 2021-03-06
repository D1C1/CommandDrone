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

	private JFrame frame;
	//private Image img;
	Mat m;
	private ImageIcon icon;
	private JLabel lbl;
	BlockingQueue queue;
	boolean running;

	public VideoStreamController(BlockingQueue queue) {
		this.frame = new JFrame();
		this.frame.setLayout(new FlowLayout());
		this.frame.setSize(0,0);
		this.frame.setVisible(true);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		lbl = new JLabel();
		icon = new ImageIcon();
		this.queue = queue;
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

	public void displayImage(Image img) {

		icon = new ImageIcon(img);

		if(this.frame.getSize().getWidth() == 0 && this.frame.getSize().getHeight() == 0) {
			frame.setSize(img.getWidth(null) + 50, img.getHeight(null) + 50);
		}
		lbl.setIcon(icon);
		frame.add(lbl);
	}

	public void updateImage(Image img) {

		icon = new ImageIcon(img);
		lbl.setIcon(icon);
		frame.setSize(img.getWidth(null) + 50, img.getHeight(null) + 50);
		frame.add(lbl);
		//System.out.println("frame width: " + frame.getWidth() + " frame height: " + frame.getHeight());
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		//Ini frame
		try {
			this.displayImage(this.Mat2BufferedImage(queue.take()));
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			System.err.println("Fejl ved at få objekt fra queue!");
			e1.printStackTrace();
		}
		
		while (running) {
			try {
				this.updateImage(this.Mat2BufferedImage(queue.take()));
			} catch (InterruptedException e) {
				System.err.println("Fejl ved at få fat i objekt i Blockingqueue!");
				e.printStackTrace();
			}
		}
	}

}
