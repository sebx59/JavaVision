package fr.sebx.vision.handler;

import fr.sebx.vision.core.MotionDetectedEvent;

public interface MotionDetectedEventHandler {

	public void handleEvent(MotionDetectedEvent event);
}
