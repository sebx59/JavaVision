package fr.sebx.vision.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.sebx.vision.consumer.CanvasDisplay;
import fr.sebx.vision.consumer.MotionDetector;
import fr.sebx.vision.consumer.ResolutionDisplay;
import fr.sebx.vision.core.Camera;
import fr.sebx.vision.exception.CameraException;
import fr.sebx.vision.handler.MotionDetectedIconBlinking;
import fr.sebx.vision.utils.CameraUtils;
import fr.sebx.vision.utils.Resolution;
import lombok.extern.slf4j.Slf4j;
import javax.swing.JCheckBox;

@SuppressWarnings("serial")
@Slf4j
public class MainWindow extends JFrame {
	
	private static final Resolution DEFAULT_RESOLUTION = Resolution.VGA;

	private static MainWindow frame;
	
	private JPanel contentPane;
	
	private Canvas canvas;

	private Camera camera;
	
	private CanvasDisplay canvasDisplay;
	private MotionDetector motionDetector;

	private JButton btnStartCamera;
	private JButton btnStartMotiondetection;
	private JButton btnExit;
	
	private JLabel lblDisplayRatePrefix;
	private JLabel lblDisplayRateData;
	private JLabel motionDetectionDelay;
	private JLabel motionDetectionActiveIcon;
	private JLabel motionDetectedIcon;
	private JLabel lblDisplayResolutionData;
	
	private JComboBox<String> cBoxCamSelection;
	private JComboBox<Resolution> cBoxResolutionSelection;
	
	private JSlider motionDetectionDelaySlider;
	
	private JCheckBox chckbxDisplayCamera;
	
	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			
			log.warn("Error while setting look & feel : {}", e1.getMessage(), e1);
		}
		
		EventQueue.invokeLater(new Runnable() {
			
			public void run() {
				
				try {
					
					log.info("Démarrage...");
					frame = new MainWindow();
					frame.setVisible(true);	
					frame.init();
					
				} catch (Exception e) {
					log.error("Erreur lors de l'initialisation de la fenêtre : {}", e.getMessage(), e);
				}
			}
		});
	}
		
	public MainWindow() {
		setTitle("Java Vision");
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);		
		setSize(800, 363);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	    setLocation(dim.width/2 - getWidth()/2, dim.height/2 - getHeight()/2);
	    
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		canvas = new Canvas();
		canvas.setBackground(Color.GRAY);
		canvas.setBounds(10, 10, 320, 240);
		contentPane.add(canvas);
		
		btnExit = new JButton("Exit");
		
		btnExit.setBounds(685, 252, 89, 23);
		contentPane.add(btnExit);
		
		JPanel cameraSetupPanel = new JPanel();
		cameraSetupPanel.setBorder(new TitledBorder(null, "Camera Setup", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		cameraSetupPanel.setBounds(336, 10, 216, 165);
		contentPane.add(cameraSetupPanel);
		cameraSetupPanel.setLayout(null);
		
		btnStartCamera = new JButton("Start Camera");
		btnStartCamera.setBounds(10, 131, 97, 23);
		cameraSetupPanel.add(btnStartCamera);
		
		cBoxCamSelection = new JComboBox<String>();
		cBoxCamSelection.setBounds(10, 47, 196, 23);
		cameraSetupPanel.add(cBoxCamSelection);
		
		JLabel lblNewLabel = new JLabel("Device Selection");
		lblNewLabel.setBounds(10, 29, 88, 14);
		cameraSetupPanel.add(lblNewLabel);
		
		JLabel lblNewLabel_2 = new JLabel("Capture Resolution");
		lblNewLabel_2.setBounds(10, 81, 97, 14);
		cameraSetupPanel.add(lblNewLabel_2);
		
		cBoxResolutionSelection = new JComboBox<>();
		cBoxResolutionSelection.setBounds(10, 95, 196, 23);
		cameraSetupPanel.add(cBoxResolutionSelection);
		
		JPanel motionDetectionPanel = new JPanel();
		motionDetectionPanel.setLayout(null);
		motionDetectionPanel.setBorder(new TitledBorder(null, "Motion Detection", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		motionDetectionPanel.setBounds(558, 10, 216, 116);
		contentPane.add(motionDetectionPanel);
		
		btnStartMotiondetection = new JButton("Start Motion Detection");
		btnStartMotiondetection.setEnabled(false);
		btnStartMotiondetection.setBounds(10, 81, 141, 23);
		motionDetectionPanel.add(btnStartMotiondetection);
		
		motionDetectionDelaySlider = new JSlider();
		motionDetectionDelaySlider.setValue(500);
		motionDetectionDelaySlider.setSnapToTicks(true);
		motionDetectionDelaySlider.setPaintTicks(true);
		motionDetectionDelaySlider.setMajorTickSpacing(500);
		motionDetectionDelaySlider.setMaximum(10000);
		motionDetectionDelaySlider.setMinimum(500);
		motionDetectionDelaySlider.setBounds(10, 34, 196, 23);
		motionDetectionPanel.add(motionDetectionDelaySlider);
		
		JLabel lblNewLabel_1 = new JLabel("Delay between detection");
		lblNewLabel_1.setBounds(10, 20, 130, 14);
		motionDetectionPanel.add(lblNewLabel_1);
		
		motionDetectionDelay = new JLabel("0 ms");
		motionDetectionDelay.setBounds(150, 20, 46, 14);
		motionDetectionPanel.add(motionDetectionDelay);		
				
		motionDetectionActiveIcon = new JLabel();
		motionDetectionActiveIcon.setBounds(250, 258, 32, 32);
		motionDetectionActiveIcon.setVisible(false);
		
		contentPane.add(motionDetectionActiveIcon);
		
		motionDetectedIcon = new JLabel();
		motionDetectedIcon.setBounds(287, 258, 32, 32);
		motionDetectedIcon.setVisible(false);
		
		contentPane.add(motionDetectedIcon);
		
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		horizontalBox.setBounds(0, 301, 784, 23);
		contentPane.add(horizontalBox);
		
		lblDisplayRatePrefix = new JLabel(" Display rate : ");
		horizontalBox.add(lblDisplayRatePrefix);
		
		lblDisplayRateData = new JLabel("0 fps");
		horizontalBox.add(lblDisplayRateData);
		
		Component verticalStrut = Box.createVerticalStrut(20);
		horizontalBox.add(verticalStrut);
		
		JLabel lblNewLabel_3 = new JLabel("Real capture resolution : ");
		horizontalBox.add(lblNewLabel_3);
		
		lblDisplayResolutionData = new JLabel("");
		horizontalBox.add(lblDisplayResolutionData);
		
		Component verticalStrut_1 = Box.createVerticalStrut(20);
		horizontalBox.add(verticalStrut_1);
		
		chckbxDisplayCamera = new JCheckBox("Display camera output");
		chckbxDisplayCamera.setEnabled(false);
		chckbxDisplayCamera.setSelected(true);
		chckbxDisplayCamera.setBounds(6, 256, 133, 23);
		contentPane.add(chckbxDisplayCamera);
	}
	
	protected void init() {

		try {
			
			String[] cameraDevices = CameraUtils.getDevicesNames();
			
			for(String device : cameraDevices) {
				
				cBoxCamSelection.addItem(device);
			}
			
		} catch (Exception e) {
			
			log.error("Error while retrieving available devices : {}", e.getMessage(), e);
		}
		
		
		for(Resolution res : Resolution.values()) {
			
			cBoxResolutionSelection.addItem(res);
		}
		
		cBoxResolutionSelection.setSelectedItem(DEFAULT_RESOLUTION);
		
		InputStream motionDetectedImageInputStream = ClassLoader.class.getResourceAsStream("/img/motiondetection.png");
		Image motionDetectedImage = null;

		InputStream motionDetectionActiveImageInputStream = ClassLoader.class.getResourceAsStream("/img/eye.png");
		Image motionDetectionActiveImage = null;

		try {
			motionDetectedImage = ImageIO.read(motionDetectedImageInputStream);
			motionDetectionActiveImage = ImageIO.read(motionDetectionActiveImageInputStream);
			
		} catch (IOException e1) {

			log.error("Error while loading resources : {}", e1.getMessage(), e1);
		}
		
		ImageIcon motionDetectionActiveImageIcon = new ImageIcon(motionDetectionActiveImage);
		motionDetectionActiveIcon.setIcon(motionDetectionActiveImageIcon);
		
		ImageIcon motionDetectedImageIcon = new ImageIcon(motionDetectedImage);
		motionDetectedIcon.setIcon(motionDetectedImageIcon);
		
		motionDetectionDelay.setText(motionDetectionDelaySlider.getValue() + " ms");
				
		btnExit.addActionListener(new ExitApplicationActionListener());
		btnStartCamera.addActionListener(new StartCameraActionListener());
		btnStartMotiondetection.addActionListener(new StartMotionDetectionActionListener());
		
		motionDetectionDelaySlider.addChangeListener(new MotionDetectionDelaySliderChangeListener());	
	}	
	
	private class MotionDetectionDelaySliderChangeListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			
			motionDetectionDelay.setText(motionDetectionDelaySlider.getValue() + " ms");
			
			if(motionDetector != null) {
				motionDetector.setDelayBetweenDetection(motionDetectionDelaySlider.getValue());
			}
		}		
	}
	
	private class ExitApplicationActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			log.info("Exitting...");
			
			if(camera != null) {
				
				camera.cancel(false);							
			}
			
			System.exit(0);
		}		
	}
	
	private class StartCameraActionListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			int deviceIndex = cBoxCamSelection.getSelectedIndex();
			
			try {
				camera = new Camera(deviceIndex, (Resolution) cBoxResolutionSelection.getSelectedItem());
								
			} catch (CameraException ex) {

				JOptionPane.showMessageDialog(frame, 
						"Error during camera startup : " + ex.getMessage(), 
						"Unable to start capture device", 
						JOptionPane.ERROR_MESSAGE);
				
				log.error("Error during camera startup : {}", ex.getMessage(), ex);			
			}
			
			ResolutionDisplay resDisplay = new ResolutionDisplay(lblDisplayResolutionData);
			camera.addConsumer(resDisplay);
			
			canvasDisplay = new CanvasDisplay(canvas, lblDisplayRateData);
			camera.addConsumer(canvasDisplay);
			camera.execute();
			btnStartCamera.setEnabled(false);
			cBoxCamSelection.setEnabled(false);
			cBoxResolutionSelection.setEnabled(false);
			btnStartMotiondetection.setEnabled(true);
			chckbxDisplayCamera.setEnabled(true);
			chckbxDisplayCamera.addChangeListener(new DisplayCameraChangeListener());
		}
	}
	
	private class DisplayCameraChangeListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			
			JCheckBox component = (JCheckBox)e.getSource();
			canvasDisplay.setEnabled(component.isSelected());
			
		}		
	}
	
	private class StartMotionDetectionActionListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
						
			motionDetector = new MotionDetector();
			motionDetector.setDelayBetweenDetection(motionDetectionDelaySlider.getValue());
			camera.addConsumer(motionDetector);	
			
			MotionDetectedIconBlinking iconBlinking = new MotionDetectedIconBlinking(motionDetectedIcon);
			motionDetector.addEventHandler(iconBlinking);
			
			motionDetectionActiveIcon.setVisible(true);
			
			btnStartMotiondetection.setEnabled(false);
		}
	}
}
