package molotov.drone;

import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Audio {
	private static AudioFormat format = getAudioFormat();
	private TargetDataLine mic;
	private ByteArrayOutputStream out;
	private int bytesRead;
	private byte[] data;

	public Audio() throws LineUnavailableException {
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		if (!AudioSystem.isLineSupported(info)) {
			System.out.println("Line not suppported.");
		}

		try {
			mic = (TargetDataLine) AudioSystem.getLine(info);
		} catch (LineUnavailableException e) {
			System.out.println("Line unavailable.");
		}
	}

	public void openAudio() {
		try {
			mic.open(format);
			out = new ByteArrayOutputStream();
			data = new byte[mic.getBufferSize() / 5];
			mic.start();
		} catch (LineUnavailableException e) {
			System.out.println("Line unavailable.");
		}
	}

	private static AudioFormat getAudioFormat() {
		float sampleRate = 44100.0F; // 11025, 16000, 22050, 44100
		int sampleSizeInBits = 16;
		int channels = 2;
		boolean signed = true;
		boolean bigEndian = false;

		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,
				bigEndian);
	}

	public byte[] get() {
		bytesRead = mic.read(data, 0, data.length);
		out.write(data, 0, bytesRead);
		return data;
	}

	public AudioFormat format() {
		return format;
	}
	public void closeAudio() {
		mic.stop();
		mic.close();
		mic = null;
	}
}
