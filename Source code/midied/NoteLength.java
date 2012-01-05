package midied;

/**
 * Enumeration type which represents possible note length.
 * 
 * @author 090010514
 */
public enum NoteLength {
	WHOLE, HALF, QUARTER, EIGHTH, SIXTEENTH;

	/**
	 * Returns the value of this note.
	 * 
	 * @return The value of this note.
	 */
	public int value() {
		switch (this) {
		case WHOLE:
			return 1;
		case HALF:
			return 2;
		case QUARTER:
			return 4;
		case EIGHTH:
			return 8;
		case SIXTEENTH:
			return 16;
		default:
			return 0;
		}
	}

	public String toString() {
		switch (this) {
		case WHOLE:
			return "Whole";
		case HALF:
			return "Half";
		case QUARTER:
			return "Quarter";
		case EIGHTH:
			return "Eighth";
		case SIXTEENTH:
			return "Sixteenth";
		default:
			return null;
		}
	}
}
