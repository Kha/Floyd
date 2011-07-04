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

	// Models a panel consisting of a file selector (non-editable path textbox
	// and
	// button to invoke a FileChooser) and a preview of the image loaded/to be
	// saved.
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
	private BufferedImage result;

	private JButton floydButton;
	private JSlider bitSlider;
	private FilePanel sourcePanel;
	private FilePanel targetPanel;

	private Program() {
		super("Floyd-Steinberg");

		createContent();
	}

	// Creates source and target panels, bit depth slider and execution button.
	private void createContent() {
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
		floydButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ImageIO.write(result, "PNG", target);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(Program.this,
							"Zieldatei konnte nicht geschrieben werden",
							"Fehler", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		add(imagesPanel);
		add(optionsPanel);
		add(floydButton);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		pack();
	}

	// Creates source and target panels.
	private JPanel createImagesPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		sourcePanel = new FilePanel("Quelldatei:");

		sourcePanel.chooseButton.addActionListener(new ActionListener() {
			// Opens chosen file, fills path textbox and updates previews if
			// ready.
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

	// Updates both preview labels using source.
	private void updateViews() {
		result = cloneImage(source);
		new FloydSteinberg(result).dither(bitSlider.getValue() / 3, 1);

		sourcePanel.imageLabel.setIcon(new ImageIcon(source.getSubimage(0, 0, 150, 150)));
		targetPanel.imageLabel.setIcon(new ImageIcon(result.getSubimage(0, 0, 150, 150)));
	}

	// Creates a copy of image.
	private static BufferedImage cloneImage(BufferedImage image) {
		return new BufferedImage(image.getColorModel(), image.copyData(null), image.isAlphaPremultiplied(), null);
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
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {
		if (args.length > 0) {
			Configuration config = new Configuration(args);
			if (!config.isErrorFree()) {
				return;
			}

			BufferedImage input;
			try {
				input = ImageIO.read(config.getSource());
			} catch (IOException e) {
				System.out.println("Input file couldn't be read: " + e.getMessage());
				return;
			}

			new FloydSteinberg(input).dither(config.getTargetColorDepth() / 3, config.getNumThreads());

			try {
				ImageIO.write(input, "PNG", config.getTarget());
			} catch (IOException e) {
				System.out.println("Output file couldn't be written: " + e.getMessage());
				return;
			}
		} else {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			new Program().setVisible(true);
		}
	}
}
