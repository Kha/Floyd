package edu.kit.ipd.fsdither;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Main class of the program.
 * 
 */
public final class Program extends JFrame {
	private static final long serialVersionUID = -7816979531719395250L;

	// The 3x3 distribution matrix of the algorithm
	private static final double[][] DISTRIBUTION = {
			{ 0, 0, 0 },
			{ 0, 0, 7. / 16 },
			{ 3. / 16, 5. / 16, 1. / 16 }
	};

	private static final class FilePanel extends JPanel {
		private static final long serialVersionUID = 3673273071068438357L;

		JTextField pathField = new JTextField();
		JButton chooseButton = new JButton("...");
		JLabel imageLabel = new JLabel();

		private FilePanel(String title) {
			setBorder(BorderFactory.createTitledBorder(title));
			setLayout(new BorderLayout());

			pathField.setEditable(false);

			JPanel pathPanel = new JPanel();
			pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.LINE_AXIS));
			pathPanel.add(pathField);
			pathPanel.add(chooseButton);

			imageLabel.setPreferredSize(new Dimension(150, 150));

			add(pathPanel, BorderLayout.PAGE_START);
			add(imageLabel, BorderLayout.CENTER);
		}
	}


	private BufferedImage source;
	private File target;

	private final JButton floydButton;
	private JSlider bitSlider;
	private FilePanel sourcePanel;
	private FilePanel targetPanel;

	private Program() {
		super("Floyd-Steinberg");

		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

		JPanel imagesPanel = createImagesPanel();


		JPanel optionsPanel = new JPanel(new BorderLayout());
		optionsPanel.setBorder(BorderFactory
				.createTitledBorder("Farbtiefe wählen:"));

		bitSlider = new JSlider(3, 24, 3);
		bitSlider.setMajorTickSpacing(3);
		bitSlider.setMinorTickSpacing(3);
		bitSlider.setSnapToTicks(true);
		bitSlider.setPaintTicks(true);
		bitSlider.setPaintLabels(true);

		bitSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (source != null && target != null) {
					updateViews();
				}
			}
		});

		optionsPanel.add(bitSlider);


		floydButton = new JButton("Starte Reduzierung");
		floydButton.setEnabled(false);


		add(imagesPanel);
		add(optionsPanel);
		add(floydButton);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		pack();
	}

	private JPanel createImagesPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		sourcePanel = new FilePanel("Quelldatei:");

		sourcePanel.chooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new FileNameExtensionFilter("PNG", "png"));
				if (chooser.showOpenDialog(Program.this) == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					try {
						source = ImageIO.read(file);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(Program.this,
								"Quelldatei konnte nicht gelesen werden",
								"Fehler", JOptionPane.ERROR_MESSAGE);
						return;
					}

					sourcePanel.pathField.setText(file.getPath());
					if (source != null && target != null) {
						floydButton.setEnabled(true);
					}
					updateViews();
				}
			}
		});

		targetPanel = new FilePanel("Zieldatei:");

		targetPanel.chooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new FileNameExtensionFilter("PNG", "png"));
				if (chooser.showSaveDialog(Program.this) == JFileChooser.APPROVE_OPTION) {
					target = chooser.getSelectedFile();

					targetPanel.pathField.setText(target.getPath());
					if (source != null && target != null) {
						floydButton.setEnabled(true);
					}
				}
			}
		});

		panel.add(sourcePanel);
		panel.add(targetPanel);
		return panel;
	}

	private void updateViews() {
		BufferedImage result = cloneImage(source);
		floydSteinbergDither(result, bitSlider.getValue() / 3);

		sourcePanel.imageLabel.setIcon(new ImageIcon(cloneImage(source, 150, 150)));
		targetPanel.imageLabel.setIcon(new ImageIcon(cloneImage(result, 150, 150)));
	}

	private static BufferedImage cloneImage(BufferedImage image, int width,
			int height) {
		BufferedImage result = new BufferedImage(width, height, image.getType());
		result.setData(image.getData());
		return result;
	}

	private static BufferedImage cloneImage(BufferedImage image) {
		return cloneImage(image, image.getWidth(), image.getHeight());
	}

	/**
	 * Entry point of the program.
	 * 
	 * @param args
	 *            command line arguments - see Configuration.java
	 * @throws UnsupportedLookAndFeelException
	 *             because
	 * @throws IllegalAccessException
	 *             I don't
	 * @throws InstantiationException
	 *             wanna
	 * @throws ClassNotFoundException
	 *             catch those
	 */
	public static void main(String[] args) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new Program().setVisible(true);
	}

	// See http://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering
	private static void floydSteinbergDither(BufferedImage image,
			int bitsPerChan) {
		int chanValues = 1 << bitsPerChan;
		double colorsPerChanValue = 256.0 / chanValues;

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int[] rgb = colorToRGB(image.getRGB(x, y));
				int[] newPixel = new int[3];

				// Reduce each channel
				for (int channel = 0; channel < 3; channel++) {
					newPixel[channel] = (int) (Math.floor(rgb[channel]
							/ colorsPerChanValue) * 255 / (chanValues - 1));
				}
				image.setRGB(x, y, rgbToColor(newPixel));

				// propagate reduction error
				for (int dy = -1; dy <= 1; dy++) {
					for (int dx = -1; dx <= 1; dx++) {
						int y2 = y + dy;
						int x2 = x + dx;
						if (y2 >= 0 && y2 < image.getHeight() && x2 >= 0
								&& x2 < image.getWidth()) {
							int[] rgb2 = colorToRGB(image.getRGB(x2, y2));
							for (int channel = 0; channel < 3; channel++) {
								rgb2[channel] = clampToByte(rgb2[channel]
										+ (int) ((rgb[channel] - newPixel[channel]) * DISTRIBUTION[dy + 1][dx + 1]));
							}
							image.setRGB(x2, y2, rgbToColor(rgb2));
						}
					}
				}
			}
		}
	}

	// Extracts RGB channel values from color.
	private static int[] colorToRGB(int color) {
		return new int[] { (color >>> 16) & 0xFF, (color >>> 8) & 0xFF,
				color & 0xFF };
	}

	// Combines RGB channel values into color value.
	private static int rgbToColor(int[] rgb) {
		return rgb[0] << 16 | rgb[1] << 8 | rgb[2];
	}

	// Fits value into the interval [0..255]
	private static int clampToByte(int value) {
		return Math.max(0, Math.min(255, value));
	}
}
