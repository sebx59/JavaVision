package fr.sebx.vision.handler;

import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder;

import fr.sebx.vision.consumer.FrameConsumer;
import fr.sebx.vision.core.CapturedImage;
import fr.sebx.vision.core.MotionDetectedEvent;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VideoRecorder implements MotionDetectedEventHandler, FrameConsumer {

	protected FrameRecorder recorder;
	
	private boolean recording;
	
	private JLabel icon;
	
	@Setter
	private Path recordingDirectory;
	
	private String filenamePattern = "yyyyMMdd-HHmmssSSS";
	private DateFormat dateFormatter;
	
	@Setter
	private long minimumRecordLength = 2000;
	
	private long recordingStartTimestamp;
	
	public VideoRecorder(JLabel icon) {
		
		super();
		this.icon = icon;
		dateFormatter = new SimpleDateFormat(filenamePattern);
	}
	
	private void startRecording(CapturedImage capturedImage) { 
		
		String filename = dateFormatter.format(new Date(capturedImage.getTimestamp())) + ".mp4";
		Path imagePath = recordingDirectory.resolve(filename);

		recorder = new FFmpegFrameRecorder(imagePath.toString(), capturedImage.getFrame().imageWidth, capturedImage.getFrame().imageHeight, 0);
		
		recording = true;
		icon.setVisible(true);
		recordingStartTimestamp = System.currentTimeMillis();
		
		try {
			recorder.start();
			recorder.record(capturedImage.getFrame());
			
		} catch (Exception e) {
			
			log.error("Error while recording frame : {}", e.getMessage(), e);
		}
	}
	
	private void stopRecording() {
		
		try {
			recorder.close();
			
		} catch (Exception e) {

			log.error("Error while closing recorder : {}", e.getMessage(), e);
			
		} finally {
			
			recorder = null;
		}
		
		recording = false;
		icon.setVisible(false);
	}

	@Override
	public void handleEvent(MotionDetectedEvent event) {
		
		if(!recording) {
			
			log.debug("Starting recording of video flow");
			
			startRecording(event.getFrame());
		
		} else {
			
//			If motion is detected during recording, we set the starting timestamp of recording to current timestamp 
//			so that the record will be [minimumRecordLength] longer than the last motion detected
			recordingStartTimestamp = System.currentTimeMillis();
		}
	}

	@Override
	public void newImage(CapturedImage frame) {
		
		if(recording) {
			
			try {
				recorder.record(frame.getFrame());
				
			} catch (Exception e) {
				
				log.error("Error while recording frame : {}", e.getMessage(), e);
			}
			
			if(System.currentTimeMillis() > (recordingStartTimestamp + minimumRecordLength)) {
				
				stopRecording();
			}
		}
	}

	@Override
	public boolean shouldBeDisposed() {
		
		return false;
	}

}
