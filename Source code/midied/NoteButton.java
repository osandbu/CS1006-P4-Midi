package midied;

import java.awt.Color;
import java.awt.Insets;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;
import javax.swing.JButton;

/**
 * The NoteButton class extends JButton and is the graphical representation of
 * single notes.
 * 
 * @author 090010514
 */
public final class NoteButton extends JButton {
	private static final long serialVersionUID = 1L;
	// The track in which this note exists.
	private Track track;
	// MIDI noteOn event
	private MidiEvent onEvent;
	// MIDI noteOff event
	private MidiEvent offEvent;
	// the channel on which this note exists.
	private int channel;
	// the key this note appears
	private int key;
	// the velocity at which this note is played
	private int velocity;

	// To remove the margin
	private static Insets noMargin = new Insets(0, 0, 0, 0);

	/**
	 * Creates a new NoteButton with a given track, noteOn- and noteOff-event,
	 * channel, note-key and velocity.
	 * 
	 * @param t
	 *            The track onto which the note represented by this button
	 *            belongs.
	 * @param on
	 *            A noteOn-event.
	 * @param off
	 *            A noteOff-event.
	 * @param chan
	 *            A channel (0-15)
	 * @param key
	 *            A note-key.
	 * @param vel
	 *            The velocity at which this note is to be played.
	 */
	public NoteButton(Track t, MidiEvent on, MidiEvent off, int chan, int key,
			int vel) {
		super(Integer.toString(vel));
		track = t;
		onEvent = on;
		offEvent = off;
		channel = chan;
		this.key = key;
		velocity = vel;
		setBackground(Color.white);
		setForeground(Constants.CHANNEL_COLORS[chan]);
		setRolloverEnabled(false);
	}

	/**
	 * Makes the text able to be painted anywhere on the button.
	 */
	public Insets getInsets() {
		return noMargin;
	}

	/**
	 * toString method which includes the channel, key and velocity of this
	 * note.
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(" channel: " + channel);
		result.append(" key: " + key);
		result.append(" vel: " + velocity);
		return result.toString();
	}

	/**
	 * Sets the position and size of this NoteButton based on a given
	 * beatScaleFactor and noteHeight.
	 * 
	 * @param beatScaleFactor
	 *            The
	 * @param noteHeight
	 *            The desired noteheight of the button.
	 */
	public void setPositionAndSize(float beatScaleFactor, int noteHeight) {
		long onTick = onEvent.getTick();
		long offTick = offEvent.getTick();
		setBounds((int) (onTick / beatScaleFactor), (127 - key) * noteHeight,
				(int) ((offTick - onTick) / beatScaleFactor), noteHeight);
	}

	/**
	 * Removes the MidiEvents (noteOn and noteOff) corresponding to this
	 * NoteButton from the track.
	 */
	public void remove() {
		track.remove(onEvent);
		track.remove(offEvent);
	}
}