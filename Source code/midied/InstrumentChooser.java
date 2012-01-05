package midied;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Synthesizer;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Lets the user choose which instruments each of the channels represent.
 * 
 * @author 090010514
 */
public class InstrumentChooser extends JDialog implements ActionListener {
	private static final Dimension LABEL_DIMENSION = new Dimension(63 + 7, 16);
	private static final long serialVersionUID = 1L;
	private static final String OK = "OK";
	private static final String CANCEL = "Cancel";

	private JComboBox[] comboBoxes;
	private Synthesizer synth;
	private PianoRollPanel pianoRollPanel;
	private MIDIEd theFrame;

	/**
	 * Create a new InstrumentChooser.
	 * 
	 * @param theFrame
	 *            The MIDIEd-frame which is this dialog's owner, i.e. input will
	 *            not be accepted into the frame while this dialog is open.
	 * @param pianoRollPanel
	 *            The pianoRollPanel whose instruments are to be choosen.
	 */
	public InstrumentChooser(MIDIEd theFrame, PianoRollPanel pianoRollPanel) {
		super(theFrame, "Instrument Chooser", true);
		this.pianoRollPanel = pianoRollPanel;
		this.theFrame = theFrame;
		try {
			synth = MidiSystem.getSynthesizer();
			if (!synth.isOpen())
				synth.open();
		} catch (MidiUnavailableException e) {
			theFrame.midiUnavailable();
		}
		makeGUI();
		pack();
		setLocationRelativeTo(theFrame);
	}

	/**
	 * Creates a graphical user interface which allows the user to choose which
	 * instruments are played on each of the 16 channels. This includes a label
	 * (channel number) and a combo-box (to choose instruments) for each of the
	 * channels. Additionally, there are two buttons: OK and Cancel.
	 */
	private void makeGUI() {
		Instrument[] instruments = getAvailableInstruments();

		JPanel channels = new JPanel();
		channels.setLayout(new BoxLayout(channels, BoxLayout.Y_AXIS));

		comboBoxes = new JComboBox[16];
		for (int chan = 0; chan < 16; chan++) {
			JPanel channelPanel = new JPanel();
			channelPanel
					.setLayout(new BoxLayout(channelPanel, BoxLayout.X_AXIS));
			JLabel label = new JLabel("Channel " + chan);
			label.setPreferredSize(LABEL_DIMENSION);
			channelPanel.add(label);
			JComboBox cb = new JComboBox(instruments);
			cb.setMaximumRowCount(25);
			int program = pianoRollPanel.getProgram(chan);
			cb.setSelectedIndex(program);
			channelPanel.add(cb);
			comboBoxes[chan] = cb;
			channels.add(channelPanel);
		}
		add(channels, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton(OK);
		okButton.setActionCommand(OK);
		okButton.setMnemonic('O');
		okButton.addActionListener(this);
		JButton cancelButton = new JButton(CANCEL);
		cancelButton.setActionCommand(CANCEL);
		cancelButton.setMnemonic('C');
		cancelButton.addActionListener(this);
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	/**
	 * Returns the 128 first instruments returned by the
	 * Synthesizer.getAvailableInstruments() method. Assumes that there are 128
	 * instruments in the first soundbank and that it is the currently selected
	 * one.
	 * 
	 * @return An array of 128 instruments, all belonging to the first
	 *         soundbank.
	 */
	private Instrument[] getAvailableInstruments() {
		Instrument[] allInstruments = synth.getAvailableInstruments();
		Instrument[] bank1 = new Instrument[128];
		for (int i = 0; i < 128; i++) {
			if (allInstruments[i].getPatch().getBank() == 0)
				bank1[i] = allInstruments[i];
		}
		return bank1;
	}

	/**
	 * ActionPerformed method, determines what should happen when the buttons
	 * are pressed. If the OK button is pressed the program change events should
	 * be added to each of the channels on the track, and then the window should
	 * be disposed of. If the Cancel button is pressed the window should be
	 * disposed.
	 */
	public void actionPerformed(ActionEvent evt) {
		String ac = evt.getActionCommand();
		if (ac == OK) {
			try {
				changeInstruments();
			} catch (MidiUnavailableException e) {
				theFrame.midiUnavailable();
			}
		}
		// close window if OK or Cancel was pressed
		dispose();
	}

	/**
	 * Change the instruments which is to be played on each of the channels of
	 * the track.
	 * 
	 * @throws MidiUnavailableException
	 *             If the Midi System is unavailable.
	 */
	private void changeInstruments() throws MidiUnavailableException {
		for (int channel = 0; channel < 16; channel++) {
			Instrument ins = (Instrument) comboBoxes[channel].getSelectedItem();
			if (!isInstrumentLoaded(ins)) {
				synth.loadInstrument(ins);
			}
			Patch patch = ins.getPatch();
			int program = patch.getProgram();
			pianoRollPanel.changeProgram(channel, program);
		}
	}

	private boolean isInstrumentLoaded(Instrument instrument) {
		for (Instrument i : synth.getLoadedInstruments())
			if (i == instrument)
				return true;
		return false;
	}

	public static void show(MIDIEd midied, PianoRollPanel pianoRollPanel) {
		InstrumentChooser ic = new InstrumentChooser(midied, pianoRollPanel);
		ic.setVisible(true);
	}
}
