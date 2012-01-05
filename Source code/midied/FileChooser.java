package midied;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * The FileChooser class lets the user open or save a file of a given file
 * format.
 * 
 * @author 090010514
 */
public class FileChooser extends JFileChooser {
	// unused
	private static final long serialVersionUID = 1L;
	// the chosen extension for this File Chooser.
	private final String extension;
	// a description of the file type
	private final String description;

	/**
	 * Creates a new FileChooser object with a given extension and file type
	 * description.
	 * 
	 * @param extension
	 *            A file extension (e.g. ".mid")
	 * @param description
	 *            A file type description.
	 */
	public FileChooser(String extension, String description) {
		super();
		this.extension = extension;
		this.description = description;
		setFileFilter(new ReplayFileFilter());
	}

	/**
	 * Get the file selected in the FileChooser dialog.
	 * 
	 * @return If opening a file, simply returns the selected file, otherwise,
	 *         if saving a file and the selected file name is lacking an
	 *         extension this method returns a file of the same name, but with
	 *         the extension.
	 */
	public File getSelectedFile() {
		File file = super.getSelectedFile();
		if (getDialogType() == OPEN_DIALOG)
			return file;
		if (file != null && !file.getName().endsWith(extension))
			return new File(file.getAbsolutePath() + extension);
		return file;
	}

	/**
	 * Extends FileFilter and hence determines which files should be accepted by
	 * A JFileChooser. Files are only accepted if they are files (i.e. not
	 * folders) and have the appropriate extension (which is specified to the
	 * FileChooser constructor).
	 * 
	 * @author 090010514
	 */
	private class ReplayFileFilter extends FileFilter {
		public boolean accept(File file) {
			if (file == null)
				return false;
			String name = file.getName();
			return file.isFile() && name.endsWith(extension);
		}

		public String getDescription() {
			return description;
		}
	}
}
