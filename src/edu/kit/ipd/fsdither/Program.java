package edu.kit.ipd.fsdither;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Main class of the program.
 * 
 */
public final class Program extends JFrame {
	private static final long serialVersionUID = 1L;
	// The 3x3 distribution matrix of the algorithm
	private static final double[][] DISTRIBUTION = { { 0, 0, 0 },
			{ 0, 0, 7. / 16 }, { 3. / 16, 5. / 16, 1. / 16 } };

	private BufferedImage source;
	private final BufferedImage reviewImage = new BufferedImage(150, 150,
			BufferedImage.TYPE_INT_RGB);
	private final BufferedImage previewImage = new BufferedImage(150, 150,
			BufferedImage.TYPE_INT_RGB);
	private File target;

	private final JButton floydButton;

	private Program() {
		super("Floyd-Steinberg");

		JPanel centerPanel = createCenterPanel();

		JPanel bottomPanel = new JPanel(new BorderLayout());

		floydButton = new JButton("Bild auf 3 Bit reduzieren");
		floydButton.setEnabled(false);

		JSlider spaceSlider = new JSlider(0, 6, 0);

		bottomPanel.add(spaceSlider, BorderLayout.LINE_START);
		bottomPanel.add(floydButton);

		add(centerPanel);
		add(bottomPanel, BorderLayout.PAGE_END);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
	}

	private JPanel createCenterPanel() {
		JPanel centerPanel = new JPanel(new GridLayout(2, 2));

		final JButton sourceButton = new JButton("Quelldatei wählen");
		sourceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new FileNameExtensionFilter("PNG", "png"));
				if (chooser.showOpenDialog(Program.this) == JFileChooser.APPROVE_OPTION) {
					try {
						source = ImageIO.read(chooser.getSelectedFile());
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(Program.this,
								"Quelldatei konnte nicht gelesen werden",
								"Fehler", JOptionPane.ERROR_MESSAGE);
					}
					sourceButton.setText("Quelldatei: " + source);

					if (source != null && target != null) {
						floydButton.setEnabled(true);
					}
					updateViews();
				}
			}
		});

		JButton targetButton = new JButton("Zieldatei wählen");
		targetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new FileNameExtensionFilter("PNG", "png"));
				if (chooser.showSaveDialog(Program.this) == JFileChooser.APPROVE_OPTION) {
					target = chooser.getSelectedFile();
					sourceButton.setText("Zieldatei: " + target);

					if (source != null && target != null) {
						floydButton.setEnabled(true);
					}
				}
			}
		});

		ImageIcon reviewIcon = new ImageIcon(reviewImage);
		ImageIcon previewIcon = new ImageIcon(previewImage);

		centerPanel.add(sourceButton);
		centerPanel.add(targetButton);
		centerPanel.add(new JLabel(reviewIcon));
		centerPanel.add(new JLabel(previewIcon));
		return centerPanel;
	}

	private void updateViews() {
		BufferedImage result = new BufferedImage(source.getWidth(),
				source.getHeight(), source.getType());
		result.setData(source.getData());
		floydSteinbergDither(result);

		reviewImage.setData(source.getData(new Rectangle(0, 0, 150, 150)));
		previewImage.setData(result.getData());
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
	private static void floydSteinbergDither(BufferedImage image) {
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int[] rgb = colorToRGB(image.getRGB(x, y));
				int[] newPixel = new int[3];

				// Reduce each channel to 1 bit
				for (int channel = 0; channel < 3; channel++) {
					newPixel[channel] = (rgb[channel] + 128) / 256 * 255;
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
