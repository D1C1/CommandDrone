package src;

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Mat;

//reference : https://stackoverflow.com/questions/26515981/display-image-using-mat-in-opencv-java

public class MatTransformer {

	private JFrame frame;
	//private Image img;
	private ImageIcon icon;
	private JLabel lbl;
	
	public MatTransformer() {
		this.frame = new JFrame();
		this.frame.setLayout(new FlowLayout());
		this.frame.setSize(0,0);
		this.frame.setVisible(true);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		lbl = new JLabel();
		icon = new ImageIcon();
	}
	
	public BufferedImage Mat2BufferedImage(Mat m) {
		
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
		System.out.println("frame width: " + frame.getWidth() + " frame height: " + frame.getHeight());
	}
	
}
