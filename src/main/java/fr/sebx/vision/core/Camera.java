package fr.sebx.vision.core;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.VideoInputFrameGrabber;

import fr.sebx.vision.consumer.FrameConsumer;
import fr.sebx.vision.exception.CameraException;
import fr.sebx.vision.utils.Resolution;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Camera extends SwingWorker<Void, Frame> {
	
	@Getter @Setter
	protected int fps = 25;
    
    private FrameGrabber grabber;
        
    private List<FrameConsumer> consumers;
            
	public Camera(int webcamDeviceIndex, Resolution resolution) throws CameraException {
		
		consumers = new ArrayList<>();
		
		try {
			grabber = VideoInputFrameGrabber.createDefault(webcamDeviceIndex);
			grabber.setImageHeight(resolution.getHeight());
			grabber.setImageWidth(resolution.getWidth());
			
		} catch (Exception e) {
			
			throw new CameraException("Error during camera initialization : " + e.getMessage(), e);
		}
	}
		
	@Override
	protected Void doInBackground() throws java.lang.Exception {
		
		log.info("Camera is starting");
		                
        try {
        	
			grabber.start();
			log.info("Camera started");
			
		} catch (Exception e) {

			log.error("Erreur from camera : {}", e.getMessage(), e);
		}
        
        Frame capturedFrame = null;
        
		while (!isCancelled()) {
			
			try {
				capturedFrame = grabber.grabFrame();
				capturedFrame.timestamp = System.currentTimeMillis();
				
			} catch (Exception e) {

				log.error("Error while image grabbing : {}", e.getMessage(), e);
			}

			if(capturedFrame != null) {
			
				publish(capturedFrame);					
			}
		}			
        
        try {
			grabber.close();
			grabber.release();
			
		} catch (Exception e) {
			
			log.error("Erreur during camera shutdown : {}", e.getMessage(), e);
		}
        
        return null;
	}

	@Override
	protected void process(List<Frame> chunks) {
		
		if(chunks.size() > 1) {
			log.debug("Skipping {} frames", chunks.size() - 1);
		}

		Frame frame = chunks.get(chunks.size()-1);
		
//		On propage la frame
		consumers.stream().filter(consumer -> !consumer.shouldBeDisposed()).forEach(consumer -> consumer.newFrame(frame));		
	}
	
	public boolean addConsumer(FrameConsumer e) {
		return consumers.add(e);
	}

}
