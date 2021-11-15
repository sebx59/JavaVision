package fr.sebx.vision.consumer;

import org.bytedeco.javacv.Frame;

public interface FrameConsumer {

	public void newFrame(Frame frame);
	
	public boolean shouldBeDisposed();
}
