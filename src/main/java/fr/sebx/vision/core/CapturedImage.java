package fr.sebx.vision.core;

import java.awt.image.BufferedImage;

import org.bytedeco.javacv.Frame;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CapturedImage {
	
	@NonNull @Getter
	private Frame frame;
	
	@NonNull @Getter
	private BufferedImage image;
	
	@Getter
	private final long timestamp = System.currentTimeMillis();
}
