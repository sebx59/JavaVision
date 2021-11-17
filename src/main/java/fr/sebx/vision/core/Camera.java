package fr.sebx.vision.core;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.VideoInputFrameGrabber;

import fr.sebx.vision.consumer.FrameConsumer;
import fr.sebx.vision.exception.CameraException;
import fr.sebx.vision.utils.Resolution;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Camera extends SwingWorker<Void, CapturedImage> {
	    
    private FrameGrabber grabber;
    private FrameFilter filter = null;

	private final Java2DFrameConverter converter;
        
    private List<FrameConsumer> consumers;
    
    private boolean useFilter = true;
    
    private static final String ffmpegString = "yadif=mode=0:parity=-1:deint=0,format=bgr24";

    private static final int PIXEL_FORMAT = avutil.AV_PIX_FMT_BGR24;
            
	public Camera(int webcamDeviceIndex, Resolution resolution) throws CameraException {
		
		consumers = new ArrayList<>();
		
		try {
			grabber = VideoInputFrameGrabber.createDefault(webcamDeviceIndex);
			grabber.setImageHeight(resolution.getHeight());
			grabber.setImageWidth(resolution.getWidth());
			grabber.setPixelFormat(PIXEL_FORMAT);
			
			converter = new Java2DFrameConverter();	
						
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
				continue;
			}

			if(useFilter && filter == null) {
				
				filter = new FFmpegFrameFilter(ffmpegString, capturedFrame.imageWidth, capturedFrame.imageHeight);
				filter.setPixelFormat(PIXEL_FORMAT);
				filter.start();
			}
			
			if(useFilter) {
				
				filter.push(capturedFrame);
				capturedFrame = filter.pull();
			}	
			
//			We convert the frame to BufferedImage here so many consumers will not have to do it
			BufferedImage image = converter.convert(capturedFrame);
			
			if(image == null) {
				log.debug("A problem occured while converting frame to BufferedImage");
				continue;
			}
			
			CapturedImage capturedImage = new CapturedImage(capturedFrame.clone(), image);

			publish(capturedImage);	
			
			capturedFrame.close();
			System.gc();
		}
        
        try {
			grabber.close();
			grabber.release();
			
			if(filter != null) {
				
				filter.close();
			}
			
		} catch (Exception e) {
			
			log.error("Error during camera shutdown : {}", e.getMessage(), e);
		}
        
        return null;
	}

	@Override
	protected void process(List<CapturedImage> chunks) {
		
		if(chunks.size() > 1) {
			log.debug("Skipping {} frames", chunks.size() - 1);
		}

		CapturedImage frame = chunks.get(chunks.size()-1);
		
//		propagate the frame across consumers		
		consumers.stream().filter(consumer -> !consumer.shouldBeDisposed()).forEach(consumer -> consumer.newImage(frame));
		
		frame.getFrame().close();
		System.gc();
	}
	
	public boolean addConsumer(FrameConsumer e) { return consumers.add(e); }

	public void setUseFilter(boolean useFilter) { this.useFilter = useFilter; }
}
