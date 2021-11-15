package fr.sebx.vision.utils;

import org.bytedeco.videoinput.videoInput;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CameraUtils {
	
	static {
		
		videoInput.setVerbose(false);
	}

	public static String[] getDevicesNames() {
		
		int nbDevices = videoInput.listDevices(true);
		
		String result[] = new String[nbDevices];
		
		for(int i = 0; i < nbDevices; i++) {
			
			String deviceName = videoInput.getDeviceName(i).getString();
			log.info("Adding device : {}", deviceName);
			result[i] = deviceName;
		}		
				
		return result;
	}
	
}
