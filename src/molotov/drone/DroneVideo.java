package molotov.drone;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.core.Mat;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.Utils;

public class DroneVideo extends Thread {
	private long systemClockStartTime;
	private long firstTimestampInStream;
	private IStreamCoder videoCoder;
	private IVideoResampler resampler = null;
	private int vID;
	private IContainer container;
	private Panel panel;
	//private boolean running;
	public Bottle drone;

	public DroneVideo(Bottle d, IContainer c, InputStream video,
			IStreamCoder vc, Panel p) throws IOException {
		drone = d;
		panel = p;
		container = c;
		videoCoder = vc;
		container.open(video, null);
		int numStreams = container.getNumStreams();

		vID = -1;
		for (int i = 0; i < numStreams; i++) {
			IStream stream = container.getStream(i);
			IStreamCoder coder = stream.getStreamCoder();

			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
				vID = i;
				videoCoder = coder;
				break;
			}
		}
		if (vID == -1)
			throw new RuntimeException("could not find video stream");

		videoCoder.open();
		if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {
			resampler = IVideoResampler.make(videoCoder.getWidth(),
					videoCoder.getHeight(), IPixelFormat.Type.BGR24,
					videoCoder.getWidth(), videoCoder.getHeight(),
					videoCoder.getPixelType());
		}

		firstTimestampInStream = Global.NO_PTS;
		systemClockStartTime = 0;
	}

	public BufferedImage getImageFromFeed() {
		IPacket packet = IPacket.make();
		while (container.readNextPacket(packet) >= 0) {
			if (packet.getStreamIndex() == vID) {
				IVideoPicture picture = IVideoPicture.make(
						videoCoder.getPixelType(), videoCoder.getWidth(),
						videoCoder.getHeight());

				try {
					int offset = 0;
					while (offset < packet.getSize()) {
						int bytesDecoded = videoCoder.decodeVideo(picture,
								packet, offset);
						if (bytesDecoded < 0)
							System.out.println("*video error*");
						offset += bytesDecoded;

						if (picture.isComplete()) {
							IVideoPicture newPic = picture;
							if (resampler != null) {
								newPic = IVideoPicture
										.make(resampler.getOutputPixelFormat(),
												picture.getWidth(),
												picture.getHeight());
								if (resampler.resample(newPic, picture) < 0)
									throw new RuntimeException(
											"could not resample video");
							}
							if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
								throw new RuntimeException(
										"could not decode video as BGR 24 bit data");

							if (firstTimestampInStream == Global.NO_PTS) {
								firstTimestampInStream = picture.getTimeStamp();
								systemClockStartTime = System
										.currentTimeMillis();
							} else {
								long systemClockCurrentTime = System
										.currentTimeMillis();
								long millisecondsClockTimeSinceStartofVideo = systemClockCurrentTime
										- systemClockStartTime;

								long millisecondsStreamTimeSinceStartOfVideo = (picture
										.getTimeStamp() - firstTimestampInStream) / 1000;
								final long millisecondsTolerance = 50;
								final long millisecondsToSleep = (millisecondsStreamTimeSinceStartOfVideo - (millisecondsClockTimeSinceStartofVideo + millisecondsTolerance));
								if (millisecondsToSleep > 0) {
									try {
										Thread.sleep(millisecondsToSleep);
									} catch (InterruptedException e) {
										return null;
									}
								}
							}

							return processImage(newPic);
						}
					} // end of while
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			} else {
				/*
				 * This packet isn't part of our video stream, so we just
				 * silently drop it.
				 */
				do {
				} while (false);
			}
		}

		return null;
	}

	private BufferedImage processImage(IVideoPicture pic) {
		BufferedImage im = Utils.videoPictureToImage(pic);
		Mat source = Visionary.matify(im);
		Mat destination = new Mat(source.rows(), source.cols(), source.type());
		destination = source;
		destination = Visionary.detect(destination, 60, 255);
		return Visionary.MatToBufferedImage(destination);
	}

	public void feedVideo() throws InterruptedException {
		panel.image = getImageFromFeed();
		panel.repaint();
	}

	public void run() {
		while (drone.vid_on) {
			try {
				feedVideo();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
