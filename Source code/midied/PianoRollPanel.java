package midied;

import javax.sound.midi.*;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The PianoRollPanel class deals with the editing and graphical representation
 * of the notes in a MIDI file.
 * 
 * @author 090010514
 */
public final class PianoRollPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	// parent frame
	private MIDIEd theFrame;

	// Useful colour
	private static final Color LIGHT_GREY = new Color(180, 180, 180);

	// vertical zoom varies from 1-10
	private int verticalZoom = 1;
	private int horizontalZoom = 1;
	private static int NOTE_HEIGHT_UNIT = 10;
	private static int NOTE_WIDTH_UNIT = 100;
	// noteHeight varies with vertical zoom
	private int noteHeight = 10;
	// beatWidth varies with horizontal zoom
	private int beatWidth = 100;
	// beatScaleFactor translates from panel positions to ticks
	private float beatScaleFactor = 1f;

	// Ticks per beat?
	private int resolution = 96;

	// How many beats do we want to display?
	private int displayBeats = Constants.DEFAULT_NUM_BARS
			* Constants.BEATS_IN_BAR;

	// The sequence that this panel displays.
	private Sequence sequence;
	private Track track;
	private int noteChannel = Constants.DEFAULT_NOTE_CHANNEL;
	private int noteVelocity = Constants.DEFAULT_NOTE_VELOCITY;
	private int noteLength = Constants.DEFAULT_NOTE_LENGTH;
	private int quantisationNoteLength = Constants.DEFAULT_QUANTISATION.value();

	private boolean changeMade = false;
	private boolean[] ignoreChannel = new boolean[16];
	private MidiEvent[] programEvent = new MidiEvent[16];

	/**
	 * Creates a new PianoRollPanel.
	 * 
	 * @param inFrame
	 *            The frame in which this PianoRollPanel is to appear.
	 */
	public PianoRollPanel(MIDIEd inFrame) {
		theFrame = inFrame;
		// want to position the noteButtons manually
		setLayout(null);
		// parent paintComponent can take care of the bg
		setBackground(Color.white);
		setHorizontalZoom(horizontalZoom);
		setVerticalZoom(verticalZoom);

		handleMouseInput();
		setFocusable(true);
		requestFocus();
	} // end of PianoRollPanel constructor

	/**
	 * paintComponents method which draws the bar and note boundaries.
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// horizontal note boundaries
		g.setColor(LIGHT_GREY);
		for (int i = 0; i < 128; i++)
			g.drawLine(0, i * noteHeight, displayBeats * beatWidth, i
					* noteHeight);
		// Draw sub-beat, beat, bar boundaries
		int panelHeight = noteHeight * 128;
		int x = 0;
		for (int bar = 0; bar < displayBeats / 4; bar++) {
			g.setColor(Color.RED);
			g.drawLine(x, 0, x, panelHeight);
			x += beatWidth / 4;
			for (int beat = 0; beat < 4; beat++) {
				g.setColor(LIGHT_GREY);
				for (int subBeat = 1; subBeat < 4; subBeat++) {
					g.drawLine(x, 0, x, panelHeight);
					x += beatWidth / 4;
				}
				if (beat < 3) {
					g.setColor(Color.black);
					g.drawLine(x, 0, x, panelHeight);
					x += beatWidth / 4;
				}
			}
		}
	}

	/**
	 * Clear the graphical interface and HashMap for note buttons.
	 */
	public void clear() {
		Component[] components = getComponents();
		for (Component c : components) {
			if (c instanceof NoteButton)
				remove(c);
		}
		repaint();
	}

	/**
	 * Set the sequence displayed in this pianoRollPanel.
	 * 
	 * @param inSequence
	 *            A Midi Sequence object.
	 */
	public void setSequence(Sequence inSequence) {
		sequence = inSequence;
		track = sequence.getTracks()[0];
		resolution = sequence.getResolution();
		// assumes that the division type is PPQ
		System.out.println(sequence.getDivisionType() == Sequence.PPQ);
		System.out.println(sequence.getTickLength());
		int beats = (int) (sequence.getTickLength() / resolution);
		int bars = beats / 4 + 1;
		if (bars < Constants.DEFAULT_NUM_BARS)
			bars = Constants.DEFAULT_NUM_BARS;
		setDisplayBars(bars);
		// beatScaleFactor translates from panel positions to ticks
		beatScaleFactor = resolution / (float) beatWidth;
		findNotes();
		update();
	}

	/**
	 * Modify vertical zoom.
	 * 
	 * @param in
	 *            whether to zoom in or out.
	 */
	public void modifyVerticalZoom(boolean in) {
		if (in && (verticalZoom < 10)) {
			verticalZoom++;
		} else if (!in && (verticalZoom > 1)) {
			verticalZoom--;
		}
		setVerticalZoom(verticalZoom);
	}

	/**
	 * Set the vertical zoom.
	 * 
	 * @param zoom
	 *            Integer representation of the zoom value (1-10, 1 is zoomed
	 *            out completely, 10 zoomed in completely.
	 */
	private void setVerticalZoom(int zoom) {
		noteHeight = verticalZoom * NOTE_HEIGHT_UNIT;
		update();
	}

	/**
	 * Modify horizontal zoom.
	 * 
	 * @param in
	 *            whether to zoom in or out.
	 */
	public void modifyHorizontalZoom(boolean in) {
		if (in && (horizontalZoom < 10)) {
			horizontalZoom++;
		} else if (!in && (horizontalZoom > 1)) {
			horizontalZoom--;
		}
		setHorizontalZoom(horizontalZoom);
	}

	/**
	 * Set the horizontal zoom.
	 * 
	 * @param zoom
	 *            Integer representation of the zoom value (1-10, 1 is zoomed
	 *            out completely, 10 zoomed in completely.
	 */
	private void setHorizontalZoom(int zoom) {
		beatWidth = horizontalZoom * NOTE_WIDTH_UNIT;
		beatScaleFactor = 100F / beatWidth;
		update();
	}

	/**
	 * Update the displayed noteButtons.
	 */
	public void update() {
		setPreferredSize();
		// make sure the note buttons are the right size.
		Component[] components = getComponents();
		for (Component c : components) {
			if (c instanceof NoteButton) {
				NoteButton but = (NoteButton) c;
				but.setPositionAndSize(beatScaleFactor, noteHeight);
			}
		}
		revalidate();
	}

	private void setPreferredSize() {
		setPreferredSize(new Dimension(beatWidth * displayBeats,
				noteHeight * 128));
	}

	/**
	 * Find notes in on the first track of the selected sequence. Assumes that
	 * the sequence only has a single track, i.e. is of type 0.
	 */
	private void findNotes() {
		// Keep a reference to the start of each note
		MidiEvent[][] noteStarts = new MidiEvent[128][16];
		// Iterate over track.
		for (int e = 0; e < track.size(); e++) {
			MidiEvent event = track.get(e);
			MidiMessage msg = event.getMessage();
			// we only care about short messages
			if (msg instanceof ShortMessage) {
				ShortMessage shortMsg = (ShortMessage) msg;
				int command = shortMsg.getCommand();
				int channel = shortMsg.getChannel();
				if (ignoreChannel[channel])
					continue;
				if (command == ShortMessage.NOTE_ON) {
					int key = shortMsg.getData1();
					// is this the start of a new note?
					if (noteStarts[key][channel] == null)
						noteStarts[key][channel] = event;
					// if not, check that vel is 0 (note end)
					else if (shortMsg.getData2() == 0) {
						addNoteButton(track, noteStarts[key][channel], event);
						// get ready for new note
						noteStarts[key][channel] = null;
					}
				} // end of NOTE_ON block
				else if (command == ShortMessage.NOTE_OFF) {
					int key = shortMsg.getData1();
					// have we seen a corresponding note on?
					if (noteStarts[key][channel] != null) {
						addNoteButton(track, noteStarts[key][channel], event);
						// get ready for new note
						noteStarts[key][channel] = null;
					}
				} // end of NOTE_OFF block
				else if (command == ShortMessage.PROGRAM_CHANGE) {
					programEvent[channel] = event;
				}
			} // end of ShortMessage test
		} // end of MidiEvent loop
	}

	/**
	 * Add a note-button which is associated the given on- and off-events.
	 * 
	 * @param t
	 *            A track.
	 * @param onEvent
	 *            A NOTE_ON event.
	 * @param offEvent
	 *            A corresponding NOTE_OFF event.
	 */
	private void addNoteButton(Track t, MidiEvent onEvent, MidiEvent offEvent) {
		ShortMessage shortMsg = (ShortMessage) onEvent.getMessage();
		NoteButton newNoteButton = new NoteButton(t, onEvent, offEvent,
				shortMsg.getChannel(), shortMsg.getData1(), shortMsg.getData2());
		newNoteButton.addActionListener(new NoteButtonListener());
		add(newNoteButton);
	}

	/**
	 * Add a mouse listener which adds notes when the mouse is clicked anywhere
	 * where there isn't already a note.
	 */
	private void handleMouseInput() {
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				int x = evt.getX();
				int y = evt.getY();
				addNote(x, y);
			}
		});
	}

	/**
	 * Add a note at the given x- and y-coordinates. This includes the button
	 * and the associated NOTE_ON and NOTE_OFF event.
	 * 
	 * @param x
	 *            A x-coordinate.
	 * @param y
	 *            A y-coordinate.
	 */
	private void addNote(int x, int y) {
		// subtract the reminder when diving by the length of
		x -= x % ((double) beatWidth / quantisationNoteLength * 4);
		int startX = x;
		int endX = (int) (x + beatWidth * 4D / noteLength);
		int startTime = (int) (startX * beatScaleFactor);
		int endTime = (int) (endX * beatScaleFactor);
		int note = 127 - y / noteHeight;
		MidiEvent onEvent = createNoteOnEvent(note, startTime);
		MidiEvent offEvent = createNoteOffEvent(note, endTime);
		addNoteButton(track, onEvent, offEvent);
		track.add(onEvent);
		track.add(offEvent);
		changeMade = true;
		update();
	}

	/**
	 * Create a noteOnEvent playing the given note number at the given tick.
	 * 
	 * @param note
	 *            The note number to be played.
	 * @param tick
	 *            The tick at which this event will occur.
	 * @return A NOTE_ON MidiEvent.
	 */
	private MidiEvent createNoteOnEvent(int note, int tick) {
		return createNoteEvent(ShortMessage.NOTE_ON, note, tick);
	}

	/**
	 * Create a noteOffEvent playing the given note number at the given tick.
	 * 
	 * @param note
	 *            The note number which is to stop playing.
	 * @param tick
	 *            The tick at which this event will occur.
	 * @return A NOTE_OFF MidiEvent.
	 */
	private MidiEvent createNoteOffEvent(int note, int tick) {
		return createNoteEvent(ShortMessage.NOTE_OFF, note, tick);
	}

	/**
	 * Remove the note associated with the given actionCommand.
	 * 
	 * @param but
	 *            The NoteButton to be removed.
	 */
	private void removeNote(NoteButton but) {
		// remove MidiEvents from track
		but.remove();
		// remove from grid
		remove(but);
		repaint();
		changeMade = true;
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
	public void changeProgram(int channel, int program) {
		if (getProgram(channel) == program) {
			// interrupt if the chosen program is the existing one.
			return;
		}
		if (programEvent[channel] != null)
			track.remove(programEvent[channel]);
		programEvent[channel] = createProgramChangeEvent(channel, program);
		track.add(programEvent[channel]);
		changeMade = true;
	}

	/**
	 * NoteButtonListener which removes clicked notes.
	 */
	private class NoteButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			Object source = ev.getSource();
			if (source instanceof NoteButton)
				removeNote((NoteButton) source);
		}
	}

	/**
	 * Create a new Midi note event. The noteChannel and velocity is taken from
	 * the GUI components.
	 * 
	 * @param command
	 *            A command (either NOTE_ON or NOTE_OFF).
	 * @param note
	 *            A note number.
	 * @param tick
	 *            The tick at which this note event should occur.
	 * @return A Midi note event with the given command, note and tick number.
	 */
	public MidiEvent createNoteEvent(int command, int note, long tick) {
		return createMidiEvent(command, noteChannel, note, noteVelocity, tick);
	}

	/**
	 * Creates program change events on a given channel to a given program at
	 * the very beginning of the track (i.e. at tick 0).
	 * 
	 * @param channel
	 *            A MIDI channel number.
	 * @param program
	 *            A program number.
	 * @return A Midi program change event channel and program which will occur
	 *         at tick 0.
	 */
	public MidiEvent createProgramChangeEvent(int channel, int program) {
		// The first zero has no function, the second zero is the tick.
		return createMidiEvent(ShortMessage.PROGRAM_CHANGE, channel, program,
				0, 0);
	}

	/**
	 * Create a new MIDI event.
	 * 
	 * @param command
	 *            Midi command.
	 * @param channel
	 *            Channel on which the event will occur.
	 * @param data1
	 *            First data byte.
	 * @param data2
	 *            Second data byte.
	 * @param tick
	 *            The tick at which the sound will occur.
	 * @return A new MidiEvent.
	 */
	public MidiEvent createMidiEvent(int command, int channel, int data1,
			int data2, long tick) {
		ShortMessage onMessage = new ShortMessage();
		try {
			onMessage.setMessage(command, channel, data1, data2);
		} catch (InvalidMidiDataException e) {
			theFrame.reportCriticalError(e);
		}
		return new MidiEvent(onMessage, tick);
	}

	/**
	 * Set the velocity at which new notes will be played.
	 * 
	 * @param velocity
	 *            Note-velocity (1-127).
	 */
	public void setVelocity(int velocity) {
		noteVelocity = velocity;
	}

	/**
	 * Set the channel to which new notes will be added.
	 * 
	 * @param channel
	 *            A MIDI channel number (0-15).
	 */
	public void setChannel(int channel) {
		noteChannel = channel;
	}

	/**
	 * Set the note length new notes will have.
	 * 
	 * @param length
	 *            A note-length as an integer. i.e. 4 represents a quarter.
	 */
	public void setNoteLength(int length) {
		noteLength = length;
	}

	/**
	 * Sets how quantisation is performed.
	 * 
	 * @param noteLength
	 *            Note-length to which notes should be quantised. 8 means that
	 *            they will stick to the nearest 1/8th note to the left.
	 */
	public void setQuantisation(int noteLength) {
		quantisationNoteLength = noteLength;
	}

	/**
	 * Set whether or not a channel should be displayed.
	 * 
	 * @param channel
	 *            A channel number.
	 * @param enable
	 *            Whether or not to display the channel in the PianoRollPanel.
	 */
	public void setChannelDisplayed(int channel, boolean enable) {
		ignoreChannel[channel] = !enable;
		clear();
		findNotes();
		update();
	}

	/**
	 * Tells if any changes have been made to the open MIDI sequence.
	 * 
	 * @return true if any changes have been made to the open MIDI sequence,
	 *         false otherwise.
	 */
	public boolean isChangeMade() {
		return changeMade;
	}

	/**
	 * Set whether or not a change has been made to the open MIDI sequence.
	 * 
	 * @param changeMade
	 *            Whether or not a change has been made to the open MIDI
	 *            sequence.
	 */
	public void setChangeMade(boolean changeMade) {
		this.changeMade = changeMade;
	}

	/**
	 * Returns the program of the given channel number.
	 * 
	 * @param channel
	 *            A MIDI channel number.
	 * @return The program at the given channel.
	 */
	public int getProgram(int channel) {
		MidiEvent event = programEvent[channel];
		if (event == null) {
			return 0;
		} else {
			ShortMessage message = (ShortMessage) event.getMessage();
			return message.getData1();
		}
	}

	/**
	 * Add another bar to the displayed sequence.
	 */
	public void addBar() {
		displayBeats += 4;
		update();
	}

	/**
	 * Remove last bar from the displayed sequence.
	 */
	public void removeBar() {
		displayBeats -= 4;
		update();
	}

	/**
	 * Set the number of bars displayed.
	 * 
	 * @param displayBars
	 *            The number of bars to be displayed.
	 */
	public void setDisplayBars(int displayBars) {
		displayBeats = Constants.BEATS_IN_BAR * displayBars;
		update();
	}

	/**
	 * Get the number of bars displayed.
	 * 
	 * @return The number of bars displayed.
	 */
	public int getDisplayBars() {
		return displayBeats / Constants.BEATS_IN_BAR;
	}
} // end of GamePanel class
