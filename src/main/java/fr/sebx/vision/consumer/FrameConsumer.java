package fr.sebx.vision.consumer;

import fr.sebx.vision.core.CapturedImage;

public interface FrameConsumer {

	public void newImage(CapturedImage frame);
	
	public boolean shouldBeDisposed();
}
