package fr.sebx.vision.handler;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.Timer;

import fr.sebx.vision.core.MotionDetectedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MotionDetectedIconBlinking implements MotionDetectedEventHandler {

	private final int BLINKING_DURATION = 100;
	
	private JLabel icon;
		
	public MotionDetectedIconBlinking(JLabel label) {
		
		super();
		this.icon = label;
	}

	@Override
	public void handleEvent(MotionDetectedEvent event) {

		icon.setVisible(true);		
		
		Timer timer = new Timer(BLINKING_DURATION, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				icon.setVisible(false);
			}
		});
		
		timer.setRepeats(false);
		timer.start();
	}
}
