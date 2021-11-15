package fr.sebx.vision.consumer;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.CvContour;
import org.bytedeco.opencv.opencv_core.CvMemStorage;
import org.bytedeco.opencv.opencv_core.CvSeq;
import org.bytedeco.opencv.opencv_core.IplImage;

import fr.sebx.vision.core.MotionDetectedEvent;
import fr.sebx.vision.handler.MotionDetectedEventHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MotionDetector implements FrameConsumer {
	
    private OpenCVFrameConverter.ToIplImage converter;
    private CvMemStorage storage;
    private IplImage previousImage;
    
    private long delayBetweenDetection;
    
    private long nextAllowedDetectionTimestamp;
    
    private List<MotionDetectedEventHandler> eventHandlers;
    
	public MotionDetector() {
		
		super();		
		converter = new OpenCVFrameConverter.ToIplImage();
		storage = CvMemStorage.create();
		nextAllowedDetectionTimestamp = System.currentTimeMillis();
		eventHandlers = new ArrayList<>();
	}

	@Override
	public void newFrame(Frame frame) {	
		
		if(System.currentTimeMillis() < nextAllowedDetectionTimestamp) {
			
			log.debug("Skipping frame as delay is not reached");
			return;
		}
		
		log.debug("Frame received");
		IplImage frameImage = converter.convert(frame);
		log.debug("Frame converted");
		
		opencv_core.cvClearMemStorage(storage);
    	
    	opencv_imgproc.cvSmooth(frameImage, frameImage, opencv_imgproc.CV_GAUSSIAN, 9, 9, 2, 2);
    	log.debug("Frame smoothed");
    	    	
//		Used for greyscale conversion
    	IplImage greyScaleImage = IplImage.create(frameImage.width(), frameImage.height(), opencv_core.IPL_DEPTH_8U, 1);
        opencv_imgproc.cvCvtColor(frameImage, greyScaleImage, opencv_imgproc.CV_RGB2GRAY);
        log.debug("Frame converted to greyscale");
        
//      To avoid calculating difference on the first frame reveived
    	if(previousImage != null) {

            IplImage diff = IplImage.create(frameImage.width(), frameImage.height(), opencv_core.IPL_DEPTH_8U, 1);
            
            opencv_core.cvAbsDiff(greyScaleImage, previousImage, diff);
                        
    		log.debug("Difference calculated");
    		opencv_imgproc.cvThreshold(diff, diff, 64, 255, opencv_imgproc.CV_THRESH_BINARY);
    		log.debug("Threshold applied");
    		
            // recognize contours
            CvSeq contour = new CvSeq(null);
            opencv_imgproc.cvFindContours(diff, storage, contour, Loader.sizeof(CvContour.class), opencv_imgproc.CV_RETR_LIST, opencv_imgproc.CV_CHAIN_APPROX_SIMPLE);

            if (!contour.isNull()) {

//            	We detected a motion
            	notifyHandlers();
            }
            
    		previousImage.deallocate();
    		previousImage.close();
    		
    		diff.deallocate();
    		diff.close();    		
    	}
        
    	previousImage = greyScaleImage.clone();
    	log.debug("Frame stored for future motion detection");
    	
    	greyScaleImage.deallocate();
    	greyScaleImage.close();
    	
    	nextAllowedDetectionTimestamp = System.currentTimeMillis() + delayBetweenDetection;
	}
	
	private void notifyHandlers() {
		
		MotionDetectedEvent event = new MotionDetectedEvent();
		
		eventHandlers.forEach(handler -> handler.handleEvent(event));
	}

	@Override
	public boolean shouldBeDisposed() {
		
		return false;
	}

	public void setDelayBetweenDetection(long delayBetweenDetection) {
		this.delayBetweenDetection = delayBetweenDetection;
	}

	public void addEventHandler(MotionDetectedEventHandler handler) {
		
		eventHandlers.add(handler);
	}
}
