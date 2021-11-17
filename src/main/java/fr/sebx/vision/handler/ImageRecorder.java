package fr.sebx.vision.handler;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import fr.sebx.vision.core.MotionDetectedEvent;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageRecorder implements MotionDetectedEventHandler {
	
	@Setter
	private Path imageSavePath;
		
	private String filenamePattern = "yyyyMMdd-HHmmssSSS";
	private DateFormat dateFormatter;

	public ImageRecorder() {
		
		super();
		dateFormatter = new SimpleDateFormat(filenamePattern);
	}

	@Override
	public void handleEvent(MotionDetectedEvent event) {
				
		String filename = dateFormatter.format(new Date()) + ".jpg";
		Path imagePath = imageSavePath.resolve(filename);
		
		try {
			ImageIO.write(event.getFrame().getImage(), "jpg", imagePath.toFile());
			log.info("Image saved to {}", imagePath.toString());
			
		} catch (IOException e) {
			
			log.error("Error while saving frame to {} : {}", imagePath.toString(), e.getMessage(), e);
		}
	}
}
