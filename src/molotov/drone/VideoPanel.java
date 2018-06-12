package molotov.drone;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class VideoPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	BufferedImage im;

	public VideoPanel(BufferedImage image) {
		im = image;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(im, null, 50, 50);
	}
}
