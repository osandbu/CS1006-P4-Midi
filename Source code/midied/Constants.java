package midied;

import java.awt.Color;

/**
 * This class defines constants which are used elsewhere.
 * 
 * @author 090010514
 */
public class Constants {
	/**
	 * The default note velocity.
	 */
	public static final int DEFAULT_NOTE_VELOCITY = 64;
	/**
	 * The default note channel.
	 */
	public static final int DEFAULT_NOTE_CHANNEL = 0;
	/**
	 * The default note length.
	 */
	public static final int DEFAULT_NOTE_LENGTH = NoteLength.QUARTER.value();
	public static final int DEFAULT_RESOLUTION = 96;
	public static final NoteLength[] QUANTISATION_OPTIONS = {
			NoteLength.QUARTER, NoteLength.EIGHTH, NoteLength.SIXTEENTH };
	public static final NoteLength DEFAULT_QUANTISATION = NoteLength.SIXTEENTH;
	/**
	 * Display colour for the 16 channels Palette from:
	 * http://web.media.mit.edu/~wad/color/palette.html 
	 * Except last, from: http://www.tayloredmktg.com/rgb/
	 */
	public static final Color[] CHANNEL_COLORS = { Color.black,
			new Color(87, 87, 87), // Dark grey
			new Color(173, 35, 35), // a red
			new Color(42, 75, 215), // a blue
			new Color(29, 105, 20), // a green
			new Color(129, 74, 25), // a brown
			new Color(129, 38, 192), // a purple
			new Color(160, 160, 160), // a light gray
			new Color(129, 197, 122), // a light green
			new Color(157, 175, 255), // a light blue
			new Color(41, 208, 208), // a cyan
			new Color(255, 146, 51), // an orange
			new Color(255, 238, 51), // a yellow
			new Color(233, 222, 187), // a tan
			new Color(255, 205, 243), // a pink
			new Color(49, 79, 79), // dark slate gray
	};
	/**
	 * Default number of bars in a sequence.
	 */
	public static final int DEFAULT_NUM_BARS = 4;
	public static final int BEATS_IN_BAR = 4;
}
