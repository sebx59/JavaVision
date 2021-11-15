package fr.sebx.vision.consumer;

import javax.swing.JLabel;

import org.bytedeco.javacv.Frame;

import fr.sebx.vision.utils.Resolution;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResolutionDisplay implements FrameConsumer {

	protected JLabel resolutionDataLabel;
		
	public ResolutionDisplay(JLabel resDataLabel) {	
		
		this.resolutionDataLabel = resDataLabel;
	}

	@Override
	public void newFrame(Frame frame) {
		
		int width = frame.imageWidth;
		int height = frame.imageHeight;
		
		Resolution foundResolution = null;
		
		for(Resolution r : Resolution.values()) {
			
			if(r.getHeight() == height && r.getWidth() == width) {
				
				foundResolution = r;
				break;
			}
		}
		
		if(foundResolution != null) {
			
			resolutionDataLabel.setText(foundResolution.toString());
			
		} else {
			
			resolutionDataLabel.setText(width + "x" + height);
		}
		
	}
	
	@Override
	public boolean shouldBeDisposed() {
		
		return false;
	}
}
