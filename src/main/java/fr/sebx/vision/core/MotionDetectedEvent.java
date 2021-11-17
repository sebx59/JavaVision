package fr.sebx.vision.core;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MotionDetectedEvent {

	@NonNull @Getter
	private final CapturedImage frame;
}
