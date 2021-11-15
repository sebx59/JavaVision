package fr.sebx.vision.consumer;

import java.awt.Canvas;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CanvasDisplay implements FrameConsumer {

	protected Canvas canvas;
	protected JLabel fpsDataLabel;
	
	private BufferedImage buffer;
	private Java2DFrameConverter converter;
	
	private long nextTimeStampToDisplayFps;
	
	private long framesDisplayed;
	
	private boolean enabled = true;
	
	public CanvasDisplay(@NonNull Canvas canvas, JLabel fpsDataLabel) {
				
		this.canvas = canvas;
		converter = new Java2DFrameConverter();	
		nextTimeStampToDisplayFps = System.currentTimeMillis();
		this.fpsDataLabel = fpsDataLabel;
	}
	
	

	@Override
	public void newFrame(Frame frame) {
		
		if(!enabled) {
			
			return;
		}
		
		buffer = converter.getBufferedImage(frame, 1.0, false, null);
		canvas.getGraphics().drawImage(buffer, 0, 0, canvas.getWidth(), canvas.getHeight(), null);		
		
		framesDisplayed++;
		printStats();
	}
	
	private void printStats() {
		
		if(System.currentTimeMillis() >= nextTimeStampToDisplayFps) {
			
			log.debug("Currently displaying at {}fps", framesDisplayed);
			
			if(fpsDataLabel != null) {
				
				fpsDataLabel.setText(framesDisplayed + " fps");				
			}
			
			framesDisplayed = 0;
			nextTimeStampToDisplayFps += 1000;
		}
	}

	@Override
	public boolean shouldBeDisposed() { return false; }

	public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
