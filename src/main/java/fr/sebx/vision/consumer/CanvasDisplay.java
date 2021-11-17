package fr.sebx.vision.consumer;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;

import javax.swing.JLabel;

import fr.sebx.vision.core.CapturedImage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CanvasDisplay implements FrameConsumer {

	protected Canvas canvas;
	protected JLabel fpsDataLabel;
		
	private long nextTimeStampToDisplayFps;
	
	private long framesDisplayed;
	
	private boolean enabled = true;
	
	public CanvasDisplay(@NonNull Canvas canvas, JLabel fpsDataLabel) {
				
		this.canvas = canvas;
		nextTimeStampToDisplayFps = System.currentTimeMillis();
		this.fpsDataLabel = fpsDataLabel;
	}	

	@Override
	public void newImage(CapturedImage frame) {
		
		if(!enabled) {
			
			return;
		}				
		
		BufferStrategy bufferStrategy = canvas.getBufferStrategy();
		Graphics graphics = bufferStrategy.getDrawGraphics();
		graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		Dimension imageDim = new Dimension(frame.getImage().getWidth(), frame.getImage().getHeight());
		Dimension canvasDim = new Dimension(canvas.getWidth(), canvas.getHeight());
		
		Dimension resizedImageDim = getScaledDimension(imageDim, canvasDim);
		
		int canvasOffset = resizedImageDim.getHeight() < canvas.getHeight() ? ((int)((canvas.getHeight() - resizedImageDim.getHeight()) / 2)) : 0;
		
		graphics.drawImage(frame.getImage(), 0, canvasOffset, (int)Math.round(resizedImageDim.getWidth()), (int)Math.round(resizedImageDim.getHeight()), null);		
		
		graphics.dispose();
		bufferStrategy.show();
		
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
	
	public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {

	    int original_width = imgSize.width;
	    int original_height = imgSize.height;
	    int bound_width = boundary.width;
	    int bound_height = boundary.height;
	    int new_width = original_width;
	    int new_height = original_height;

	    // first check if we need to scale width
	    if (original_width > bound_width) {
	        //scale width to fit
	        new_width = bound_width;
	        //scale height to maintain aspect ratio
	        new_height = (new_width * original_height) / original_width;
	    }

	    // then check if we need to scale even with the new height
	    if (new_height > bound_height) {
	        //scale height to fit instead
	        new_height = bound_height;
	        //scale width to maintain aspect ratio
	        new_width = (new_height * original_width) / original_height;
	    }

	    return new Dimension(new_width, new_height);
	}
}
