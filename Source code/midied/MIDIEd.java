package midied;

import javax.sound.midi.*;

import java.io.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.*;

/**
 * The MIDIEd class describes the window in which the PianoRollPanel is
 * presented to the used. Additionally, it gives the user a number of options
 * regarding which channels' events should be displayed and, the note-channel,
 * note-velocity and note-length of new notes. It also contains two menus, the
 * File- and Edit-menu. The file-menu allows the user to create new files, save
 * and load files and quit the program, while the edit-menu allows the user to
 * change which instruments are represented by each of the channels and how
 * quantisation should be done.
 * 
 * @author 090010514
 */
public final class MIDIEd extends JFrame implements WindowListener {
	private static final long serialVersionUID = 1L;

	// String constants for action commands
	private static final String NEW = "New";
	private static final String OPEN = "Open";
	private static final String SAVE = "Save";
	private static final String PLAY = "Play";
	private static final String STOP = "Stop";
	private JButton playButton;
	private JButton stopButton;
	private static final String QUIT = "Quit";
	private static final String QUANTISATION = "Quantisation";
	private static final String INSTRUMENT = "Instrument";
	private static final String DISPLAY_BARS = "Display bars";
	private static final String ADD_BAR = "Add bar";
	private static final String REMOVE_BAR = "Remove bar";
	private static final String V_ZOOM_IN = "VIn";
	private static final String V_ZOOM_OUT = "VOut";
	private static final String H_ZOOM_IN = "HIn";
	private static final String H_ZOOM_OUT = "HOut";

	// The ScrollPane manages a viewport onto the larger piano roll
	private JScrollPane prScrollPane;
	private PianoRollPanel pianoRollPanel;

	// The sequencer plays the sequence
	private Sequencer sequencer;

	// The Sequence object we are editing
	private Sequence sequence;

	private NoteLength quantisationNoteLength = Constants.DEFAULT_QUANTISATION;

	private static final String TITLE = "MIDI Editor";

	/**
	 * Creates a new MIDI editor window with an empty sequence and a single
	 * track.
	 */
	public MIDIEd() {
		super(TITLE + " - Untitled");
		// Initialise the Sequencer.
		try {
			sequencer = MidiSystem.getSequencer();
		} catch (MidiUnavailableException mue) {
			midiUnavailable();
		}
		// Initialise the sequence
		sequence = null;
		// Initialise the GUI
		makeGUI();
		// want to specify close operation in WindowListener.
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		pack();
		// make the window appear in the middle of the screen
		setLocationRelativeTo(null);
		setVisible(true);
	} // end of MIDIEd constructor

	/**
	 * Sets up all the GUI components. This includes menus, buttons (play and
	 * zooms), note-options (channel, velocity and note-length), and
	 * pianoRollPanel.
	 */
	private void makeGUI() {
		initScrollPane();
		JPanel controls = initControls();
		JPanel noteOptions = initNoteOptions();
		JPanel channelOptions = initChannelOptions();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JPanel firstPanel = new JPanel(new BorderLayout());
		firstPanel.add(prScrollPane, BorderLayout.CENTER);
		firstPanel.add(channelOptions, BorderLayout.EAST);
		mainPanel.add(firstPanel);
		mainPanel.add(controls);
		mainPanel.add(noteOptions);
		setContentPane(mainPanel);

		makeMenus();
	} // end of makeGUI()

	/**
	 * Initialises the scrollPane with the PianoRollPanel.
	 * 
	 * @return The scrollPane with the PianoRollPanel.
	 */
	private JScrollPane initScrollPane() {
		pianoRollPanel = new PianoRollPanel(this);
		prScrollPane = new JScrollPane(pianoRollPanel);
		prScrollPane.setPreferredSize(new Dimension(500, 600));
		return prScrollPane;
	}

	/**
	 * Initialises the menus.
	 */
	private void makeMenus() {
		JMenuBar menuBar = new JMenuBar();
		addFileMenu(menuBar);
		addEditMenu(menuBar);
		setJMenuBar(menuBar);
	}

	/**
	 * Add a file menu to the given menu-bar.
	 * 
	 * @param menuBar
	 *            A JMenuBar.
	 */
	private void addFileMenu(JMenuBar menuBar) {
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);
		JMenuItem newItem = new JMenuItem("New", KeyEvent.VK_N);
		KeyStroke ctrlN = KeyStroke.getKeyStroke(KeyEvent.VK_N,
				KeyEvent.CTRL_DOWN_MASK);
		newItem.setAccelerator(ctrlN);
		newItem.setActionCommand(NEW);
		newItem.addActionListener(new MidiMenuListener());
		JMenuItem openItem = new JMenuItem("Open", KeyEvent.VK_O);
		KeyStroke ctrlO = KeyStroke.getKeyStroke(KeyEvent.VK_O,
				KeyEvent.CTRL_DOWN_MASK);
		openItem.setAccelerator(ctrlO);
		openItem.setActionCommand(OPEN);
		openItem.addActionListener(new MidiMenuListener());
		JMenuItem saveItem = new JMenuItem("Save", KeyEvent.VK_S);
		KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S,
				KeyEvent.CTRL_DOWN_MASK);
		saveItem.setAccelerator(ctrlS);
		saveItem.setActionCommand(SAVE);
		saveItem.addActionListener(new MidiMenuListener());
		JMenuItem quitItem = new JMenuItem("Quit", KeyEvent.VK_Q);
		KeyStroke ctrlQ = KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				KeyEvent.CTRL_DOWN_MASK);
		quitItem.setAccelerator(ctrlQ);
		quitItem.setActionCommand(QUIT);
		quitItem.addActionListener(new MidiMenuListener());
		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(quitItem);
	}

	/**
	 * Add an edit menu to the given menu-bar.
	 * 
	 * @param menuBar
	 *            A JMenuBar.
	 */
	private void addEditMenu(JMenuBar menuBar) {
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		menuBar.add(editMenu);
		JMenuItem quantisationItem = new JMenuItem("Set quantisation",
				KeyEvent.VK_Q);
		quantisationItem.setActionCommand(QUANTISATION);
		quantisationItem.addActionListener(new MidiMenuListener());
		JMenuItem instrumentItem = new JMenuItem("Choose instruments",
				KeyEvent.VK_I);
		instrumentItem.setActionCommand(INSTRUMENT);
		instrumentItem.addActionListener(new MidiMenuListener());
		JMenuItem barItem = new JMenuItem("Set bars", KeyEvent.VK_S);
		barItem.setActionCommand(DISPLAY_BARS);
		barItem.addActionListener(new MidiMenuListener());
		JMenuItem addItem = new JMenuItem("Add bar", KeyEvent.VK_A);
		KeyStroke ctrlB = KeyStroke.getKeyStroke(KeyEvent.VK_B,
				KeyEvent.CTRL_DOWN_MASK);
		addItem.setAccelerator(ctrlB);
		addItem.setActionCommand(ADD_BAR);
		addItem.addActionListener(new MidiMenuListener());
		JMenuItem removeItem = new JMenuItem("Remove bar", KeyEvent.VK_R);
		removeItem.setActionCommand(REMOVE_BAR);
		removeItem.addActionListener(new MidiMenuListener());

		editMenu.add(instrumentItem);
		editMenu.add(quantisationItem);
		editMenu.add(barItem);
		editMenu.add(addItem);
		editMenu.add(removeItem);
	}

	/**
	 * Initialises the control buttons: Play, vertical and horizontal zoom in
	 * and out.
	 * 
	 * @return A JPanel containing the controls.
	 */
	private JPanel initControls() {
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));

		// Add the play button
		playButton = new JButton("Play");
		playButton.setActionCommand(PLAY);
		playButton.addActionListener(new MidiButtonListener());
		controls.add(playButton);

		stopButton = new JButton("Stop");
		stopButton.setActionCommand(STOP);
		stopButton.addActionListener(new MidiButtonListener());
		stopButton.setEnabled(false);
		controls.add(stopButton);

		// Add the vertical zoom in button
		JButton vZoomInButton = new JButton("Zoom in (V)");
		vZoomInButton.setActionCommand(V_ZOOM_IN);
		vZoomInButton.addActionListener(new MidiButtonListener());
		controls.add(vZoomInButton);

		// Add the vertical zoom out button
		JButton vZoomOutButton = new JButton("Zoom out (V)");
		vZoomOutButton.setActionCommand(V_ZOOM_OUT);
		vZoomOutButton.addActionListener(new MidiButtonListener());
		controls.add(vZoomOutButton);

		// Add the horizontal zoom in button
		JButton hZoomInButton = new JButton("Zoom in (H)");
		hZoomInButton.setActionCommand(H_ZOOM_IN);
		hZoomInButton.addActionListener(new MidiButtonListener());
		controls.add(hZoomInButton);

		// Add the horizontal zoom out button
		JButton hZoomOutButton = new JButton("Zoom out (H)");
		hZoomOutButton.setActionCommand(H_ZOOM_OUT);
		hZoomOutButton.addActionListener(new MidiButtonListener());
		controls.add(hZoomOutButton);
		return controls;
	}

	/**
	 * Initialises the note options. This includes choice of channel, velocity
	 * and note length for new notes.
	 * 
	 * @return A JPanel with all the note options.
	 */
	private JPanel initNoteOptions() {
		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.X_AXIS));

		final JComboBox channelBox = initChannelBox();
		optionPanel.add(new JLabel("Channel:"));
		optionPanel.add(channelBox);

		Dimension fillerDim = new Dimension(3, 0);
		optionPanel.add(new Box.Filler(fillerDim, fillerDim, fillerDim));
		addVelocitySlider(optionPanel);
		optionPanel.add(new Box.Filler(fillerDim, fillerDim, fillerDim));

		addNoteLengthOption(optionPanel);
		return optionPanel;
	}

	/**
	 * Initialises the channel visibility check-boxes. These check-boxes can be
	 * used to make each of the channels on the track visible in the
	 * pianoRollPanel.
	 * 
	 * @return A JPanel with all the channel visibility check-boxes.
	 */
	private JPanel initChannelOptions() {
		JPanel channelPanel = new JPanel();
		channelPanel.setLayout(new GridLayout(17, 1));
		channelPanel.add(new JLabel("Channels:"));
		for (int i = 0; i < 16; i++) {
			final int channel = i;
			final JCheckBox cb = new JCheckBox(String.valueOf(i), true);
			cb.setForeground(Constants.CHANNEL_COLORS[channel]);
			cb.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					boolean selected = cb.isSelected();
					pianoRollPanel.setChannelDisplayed(channel, selected);
				}
			});
			channelPanel.add(cb);
		}
		return channelPanel;
	}

	/**
	 * Adds a velocity slider and the corresponding label to a JPanel.
	 * 
	 * @param optionPanel
	 *            The JPanel onto which noteOptions are added.
	 */
	private void addVelocitySlider(JPanel optionPanel) {
		optionPanel.add(new JLabel("Velocity:"));
		final JSlider velocitySlider = new JSlider(1, 127,
				Constants.DEFAULT_NOTE_VELOCITY);
		final JTextField velocityDisplay = new JTextField(Integer
				.toString(Constants.DEFAULT_NOTE_VELOCITY), 2);
		velocityDisplay.setMaximumSize(new Dimension(30, 20));
		velocityDisplay.setEditable(false);
		velocityDisplay.setFocusable(false);
		velocitySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				int velocity = velocitySlider.getValue();
				pianoRollPanel.setVelocity(velocity);
				velocityDisplay.setText(Integer.toString(velocity));
			}
		});
		optionPanel.add(velocitySlider);
		optionPanel.add(velocityDisplay);
	}

	/**
	 * Initialises the channel selection combo-box.
	 * 
	 * @return The channel selection combo-box.
	 */
	private JComboBox initChannelBox() {
		Integer[] channels = new Integer[16];
		for (int i = 0; i < 16; i++) {
			channels[i] = i;
		}
		// Add a combobox to select channel
		final JComboBox channelBox = new JComboBox(channels);
		channelBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				int selected = (Integer) evt.getItem();
				pianoRollPanel.setChannel(selected);
			}
		});
		channelBox.setSelectedIndex(Constants.DEFAULT_NOTE_CHANNEL);
		channelBox.setMaximumSize(new Dimension(15, 30));
		return channelBox;
	}

	/***
	 * Adds a combo-box from which the note-length of new notes can be chosen,
	 * and a corresponding label.
	 * 
	 * @param optionPanel
	 *            The JPanel onto which noteOptions are added.
	 */
	private void addNoteLengthOption(JPanel optionPanel) {
		final JComboBox noteLengthBox = new JComboBox(NoteLength.values());
		noteLengthBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				NoteLength selected = (NoteLength) evt.getItem();
				pianoRollPanel.setNoteLength(selected.value());
			}
		});
		noteLengthBox.setSelectedItem(NoteLength.QUARTER);
		noteLengthBox.setMaximumSize(new Dimension(15, 30));
		optionPanel.add(new JLabel("Note length:"));
		optionPanel.add(noteLengthBox);
	}

	/**
	 * Listens for button clicks and makes the buttons do what they are supposed
	 * to do.
	 */
	private class MidiButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			String command = evt.getActionCommand();
			if (command == PLAY)
				play();
			else if (command == STOP)
				stop();
			else if (command == V_ZOOM_IN)
				pianoRollPanel.modifyVerticalZoom(true);
			else if (command == V_ZOOM_OUT)
				pianoRollPanel.modifyVerticalZoom(false);
			else if (command == H_ZOOM_IN)
				pianoRollPanel.modifyHorizontalZoom(true);
			else if (command == H_ZOOM_OUT)
				pianoRollPanel.modifyHorizontalZoom(false);
		}
	}

	/**
	 * Listens for menu items to be clicked and makes them do what they are
	 * supposed to do.
	 */
	private class MidiMenuListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			String command = evt.getActionCommand();
			if (command == NEW) {
				newFile();
			} else if (command == OPEN) {
				openFile();
			} else if (command == SAVE) {
				saveFile();
			} else if (command == QUIT) {
				exit();
			} else if (command == QUANTISATION) {
				setQuantisation();
			} else if (command == INSTRUMENT) {
				showInstrumentChooser();
			} else if (command == DISPLAY_BARS) {
				setDisplayBars();
			} else if (command == ADD_BAR) {
				pianoRollPanel.addBar();
			} else if (command == REMOVE_BAR) {
				pianoRollPanel.removeBar();
			}
		}
	}

	/**
	 * Opens up a window from which instruments can be choosen for each of the
	 * channels.
	 */
	private void showInstrumentChooser() {
		InstrumentChooser.show(this, pianoRollPanel);
	}

	/**
	 * Set the number of
	 */
	public void setDisplayBars() {
		int current = pianoRollPanel.getDisplayBars();
		final JTextField textfield = new JTextField(String.valueOf(current));
		textfield.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent evt) {
				char c = evt.getKeyChar();
				if (c >= '0' && c <= '9') {
					int start = textfield.getSelectionStart();
					int end = textfield.getSelectionEnd();
					String t = textfield.getText();
					String before = t.substring(0, start);
					String after = t.substring(end);
					// add input behind the cursor
					textfield.setText(before + c + after);
					textfield.setSelectionStart(start + 1);
					textfield.setSelectionEnd(start + 1);
				}
				// consume the KeyEvent
				evt.consume();
			}
		});
		Object[] message = { "Enter number of bars", textfield };
		int opt = JOptionPane.showConfirmDialog(this, message, "Set bars",
				JOptionPane.OK_CANCEL_OPTION);
		if (opt == JOptionPane.OK_OPTION) {
			int bars = Integer.parseInt(textfield.getText());
			pianoRollPanel.setDisplayBars(bars);
		}
	}

	/**
	 * Prompt the user to save changes, then create a new file.
	 */
	private void newFile() {
		if (!pianoRollPanel.isChangeMade()) {
			// even if no change has been made, it may not be a new file.
			resetSequence();
			return;
		}
		int opt = JOptionPane.showConfirmDialog(this,
				"Do you want to save first?", "Save?",
				JOptionPane.YES_NO_CANCEL_OPTION);
		if (opt == JOptionPane.YES_OPTION && saveFile()) {
			resetSequence();
		} else if (opt == JOptionPane.NO_OPTION) {
			resetSequence();
		}

	}

	/**
	 * Reset the sequence, i.e. remove the NoteButtons and create a blank
	 * sequence.
	 */
	private void resetSequence() {
		createBlankSequence();
		pianoRollPanel.clear();
		pianoRollPanel.setChangeMade(false);
		setTitle(TITLE + " - Untitled");
	}

	/**
	 * Open a midi file and read it into the sequence field.
	 */
	public void openFile() {
		FileChooser fc = new FileChooser(".mid", "MIDI file (.mid)");
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == FileChooser.APPROVE_OPTION) {
			pianoRollPanel.clear();
			File file = fc.getSelectedFile();
			try {
				sequence = MidiSystem.getSequence(file);
				pianoRollPanel.setSequence(sequence);
				setTitle(TITLE + " - " + file.getName());
			} catch (IOException ioe) {
				reportError("Error reading file: " + ioe.toString(), "Error!");
			} catch (InvalidMidiDataException imde) {
				reportCriticalError(imde);
			}
		}
	}

	/**
	 * Save the current sequence as a type 0 midi file.
	 * 
	 * @return Whether or not the file was saved.
	 */
	public boolean saveFile() {
		FileChooser fc = new FileChooser(".mid", "MIDI file (.mid)");
		int returnVal = fc.showSaveDialog(this);
		boolean approved = (returnVal == FileChooser.APPROVE_OPTION);
		if (approved) {
			File file = fc.getSelectedFile();
			try {
				MidiSystem.write(sequence, 0, file);
				pianoRollPanel.setChangeMade(false);
				setTitle(TITLE + " - " + file.getName());
			} catch (IOException ioe) {
				reportCriticalError(ioe);
			}
		}
		return approved;
	}

	/**
	 * Creates a blank sequence.
	 */
	public void createBlankSequence() {
		try {
			sequence = new Sequence(Sequence.PPQ, Constants.DEFAULT_RESOLUTION);
		} catch (InvalidMidiDataException e) {
			reportCriticalError(e);
		}
		sequence.createTrack();
		pianoRollPanel.setSequence(sequence);
	}

	/**
	 * This deals with waiting for a sequence to finish in an ugly way - you can
	 * do better. Code from: http://blog.taragana.com/index.php/archive/how
	 * -to-play-a-midi-file-from-a-java-application/
	 */
	public void play() {
		playButton.setEnabled(false);
		stopButton.setEnabled(true);
		Thread play = new Thread() {
			public void run() {

				try {
					sequencer.setSequence(sequence);
					sequencer.open();
				} catch (MidiUnavailableException mue) {
					midiUnavailable();
				} catch (InvalidMidiDataException imde) {
					reportCriticalError(imde);
				}
				sequencer.start();
				while (sequencer.isRunning()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException ignore) {
						break;
					}
				}
				MIDIEd.this.stop();
			}
		};
		play.start();
	}

	/**
	 * Stop playing. Closes the MidiDevice and frees up resources.
	 */
	public void stop() {
		if (sequencer.isOpen()) {
			if (sequencer.isRunning())
				sequencer.stop();
			sequencer.close();
		}
		stopButton.setEnabled(false);
		playButton.setEnabled(true);
	}

	/**
	 * Change program of a given channel to the given program id number. Assumes
	 * that the correct sound bank is selected.
	 * 
	 * @param channel
	 *            A channel number.
	 * @param programID
	 *            A program ID number.
	 */
	public void changeProgram(int channel, int programID) {
		pianoRollPanel.changeProgram(channel, programID);
	}

	/**
	 * Set how quantisation is performed using a ComboBox in a pop-up dialog.
	 */
	private void setQuantisation() {
		JComboBox quantisationBox = new JComboBox(
				Constants.QUANTISATION_OPTIONS);
		quantisationBox.setSelectedItem(quantisationNoteLength);
		int opt = JOptionPane.showConfirmDialog(null, quantisationBox,
				"Set quantisation", JOptionPane.OK_CANCEL_OPTION);
		if (opt == JOptionPane.OK_OPTION) {
			Object selected = quantisationBox.getSelectedItem();
			quantisationNoteLength = (NoteLength) selected;
			int value = quantisationNoteLength.value();
			pianoRollPanel.setQuantisation(value);
		}
	}

	// --- Window listener methods ---
	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	/**
	 * If the window is closing this method is called. This happens in Windows
	 * if the X in the corner of the window or Alt+F4 is pressed.
	 */
	public void windowClosing(WindowEvent e) {
		exit();
	}

	/**
	 * Exits the program if no changes have been made to the sequence, the user
	 * does not wish to save the changes, or saves the file to the harddrive.
	 * Otherwise leaves the program open.
	 */
	public void exit() {
		if (!pianoRollPanel.isChangeMade()) {
			System.exit(0);
		}
		int opt = JOptionPane.showConfirmDialog(this,
				"Do you want to save before exiting?", "Save?",
				JOptionPane.YES_NO_CANCEL_OPTION);
		if (opt == JOptionPane.YES_OPTION) {
			if (saveFile())
				System.exit(0);
		} else if (opt == JOptionPane.NO_OPTION) {
			System.exit(0);
		}
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	/**
	 * Report an error to the user.
	 * 
	 * @param message
	 *            An error message.
	 * @param title
	 *            The title of the error message window.
	 */
	public void reportError(String message, String title) {
		JOptionPane.showConfirmDialog(this, message, title,
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Main method. Creates a new MIDI editor window and creates a blank
	 * sequence.
	 * 
	 * @param args
	 *            Unused.
	 */
	public static void main(String[] args) {
		MIDIEd midiEd = new MIDIEd();
		midiEd.createBlankSequence();
	} // end of main

	/**
	 * Report a critical error based on the given exception. Then exits.
	 * 
	 * @param e
	 *            An exception.
	 */
	public void reportCriticalError(Exception e) {
		reportError("A critical error occured:\n" + e, "Error!");
		System.exit(1);
	}

	/**
	 * Reports that the Midi system is unavailable, then exits.
	 */
	public void midiUnavailable() {
		reportError(
				"Midi System Unavailable: This system cannot play MIDI files.",
				"Error!");
		System.exit(1);
	}
}