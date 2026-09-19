/*
 * Copyright (c) 2008 Robert Futrell
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name "HexEditor" nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.fife.ui.hex.swing.demo;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import org.fife.ui.hex.event.HexEditorEvent;
import org.fife.ui.hex.event.HexEditorListener;
import org.fife.ui.hex.event.SelectionChangedEvent;
import org.fife.ui.hex.event.SelectionChangedListener;
import org.fife.ui.hex.swing.HexEditor;



/**
 * Content pane for the demo applet/standalone app, that demonstrates
 * the functionality of the Swing {@link HexEditor} component.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class HexEditorDemoPanel extends JPanel implements ActionListener,
							HexEditorListener, SelectionChangedListener {

	private static final long serialVersionUID = 1L;

	private HexEditor editor;
	private JFileChooser chooser;
	private JCheckBox colHeaderCB;
	private JCheckBox rowHeaderCB;
	private JCheckBox showGridCB;
	private JComboBox lafCombo;
	private JCheckBox altRowBGCB;
	private JCheckBox altColBGCB;
	private JCheckBox highlightAsciiSelCB;
	private JComboBox highlightAsciiSelCombo;
	private JCheckBox lowBytePaddingCB;
	private JCheckBox cellEditableCB;
	private JTextField infoField;
	private JTextField selField;
	private JTextField sizeField;

	private Action openAction;
	private Action cutAction;
	private Action copyAction;
	private Action pasteAction;
	private Action deleteAction;
	private Action undoAction;
	private Action redoAction;

	private static final String MSG = "org.fife.ui.hex.swing.demo.HexEditorDemoPanel";
	private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);


	/**
	 * Constructor.
	 */
	public HexEditorDemoPanel() {

		setLayout(new BorderLayout());

		createActions(msg);
		add(createToolBar(), BorderLayout.NORTH);

		JPanel temp = new JPanel(new BorderLayout());
		ConfigPanel configPanel = new ConfigPanel();
		temp.add(configPanel, BorderLayout.LINE_START);

		infoField = new JTextField();
		infoField.setEditable(false);
		infoField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 5, 5, 5),
				infoField.getBorder()));

		selField = new JTextField(30);
		selField.setEditable(false);
		selField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 0, 5, 5),
				selField.getBorder()));

		sizeField = new JTextField(15);
		sizeField.setEditable(false);
		sizeField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 0, 5, 5),
				sizeField.getBorder()));

		JPanel temp2 = new JPanel(new BorderLayout());
		JPanel temp3 = new JPanel(new BorderLayout());
		temp2.add(infoField);
		temp3.add(selField, BorderLayout.LINE_START);
		temp3.add(sizeField, BorderLayout.LINE_END);
		temp2.add(temp3, BorderLayout.LINE_END);
		temp.add(temp2, BorderLayout.SOUTH);
		add(temp, BorderLayout.SOUTH);

		// Add the editor after infoField as it listens to byte changes.
		editor = new HexEditor();
		editor.addHexEditorListener(this);
		editor.addSelectionChangedListener(this);
		editor.setCellEditable(true);
		//handleOpenFile("org/fife/ui/hex/swing/demo/HexEditorDemoPanel.class");
		handleOpenFile("copy.gif");
		add(editor);

	}


	/**
	 * Listens for events in this panel.
	 *
	 * @param e The event that occurred.
	 */
	public void actionPerformed(ActionEvent e) {

		Object source = e.getSource();

		if (source==colHeaderCB) {
			editor.setShowColumnHeader(colHeaderCB.isSelected());
		}
		else if (source==rowHeaderCB) {
			editor.setShowRowHeader(rowHeaderCB.isSelected());
		}
		else if (source==showGridCB) {
			editor.setShowGrid(showGridCB.isSelected());
		}
		else if (source==lafCombo) {
			String value = (String)lafCombo.getSelectedItem();
			String laf = null;
			if ("System".equals(value)) {
				laf = UIManager.getSystemLookAndFeelClassName();
			}
			else if ("Metal".equals(value)) {
				laf = UIManager.getCrossPlatformLookAndFeelClassName();
			}
			else if ("Motif".equals(value)) {
				laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
			}
			else if ("Nimbus".equals(value)) {
				LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
				for (int i=0; i<infos.length; i++) {
					if ("Nimbus".equals(infos[i].getName())) {
						laf = infos[i].getClassName();
						break;
					}
				}
			}
			try {
				UIManager.setLookAndFeel(laf);
				Window w = SwingUtilities.getWindowAncestor(this);
				if (w!=null) {
					SwingUtilities.updateComponentTreeUI(w);
				}
				else {
					SwingUtilities.updateComponentTreeUI(this);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		else if (source==altColBGCB) {
			editor.setAlternateColumnBG(altColBGCB.isSelected());
		}
		else if (source==altRowBGCB) {
			editor.setAlternateRowBG(altRowBGCB.isSelected());
		}
		else if (source==highlightAsciiSelCB) {
			editor.setHighlightSelectionInAsciiDump(
								highlightAsciiSelCB.isSelected());
		}
		else if (source==highlightAsciiSelCombo) {
			editor.setHighlightSelectionInAsciiDumpColor(
					(Color)highlightAsciiSelCombo.getSelectedItem());
		}
		else if (source==lowBytePaddingCB) {
		    editor.setPadLowBytes(lowBytePaddingCB.isSelected());
		} 
		else if (source==cellEditableCB) {
			editor.setCellEditable(cellEditableCB.isSelected());
		}

	}


	/**
	 * Creates the actions used by this panel.
	 *
	 * @param msg The resource bundle used to localize the actions.
	 */
	private void createActions(ResourceBundle msg) {
		openAction = new OpenAction();
		cutAction = new CutAction();
		copyAction = new CopyAction();
		pasteAction = new PasteAction();
		deleteAction = new DeleteAction();
		undoAction = new UndoAction();
		redoAction = new RedoAction();
	}


	/**
	 * Creates the toolbar used in this demo panel.
	 *
	 * @return The toolbar.
	 */
	private JToolBar createToolBar() {

		JToolBar toolbar = new JToolBar();

		toolbar.add(createToolBarButton(openAction));
		toolbar.addSeparator();

		toolbar.add(createToolBarButton(cutAction));
		toolbar.add(createToolBarButton(copyAction));
		toolbar.add(createToolBarButton(pasteAction));
		toolbar.add(createToolBarButton(deleteAction));
		toolbar.addSeparator();

		toolbar.add(createToolBarButton(undoAction));
		toolbar.add(createToolBarButton(redoAction));

		return toolbar;

	}


	/**
	 * Creates a button to add to the toolbar for an action.
	 *
	 * @param a The action.
	 * @return The button.
	 */
	private JButton createToolBarButton(Action a) {
		JButton b = new JButton(a);
		b.setText(null);
		return b;
	}


	/**
	 * Prompts the user for a file to open, and opens it.
	 */
	public void doOpen() {
		JFileChooser chooser = getFileChooser();
		int rc = chooser.showOpenDialog(this);
		if (rc==JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			handleOpenFile(file.getAbsolutePath());
		}
	}


	/**
	 * Returns the file chooser for opening files, lazily creating it if
	 * necessary.
	 *
	 * @return The file chooser.
	 */
	private JFileChooser getFileChooser() {
		if (chooser==null) {
			chooser = new JFileChooser();
		}
		return chooser;
	}


	/**
	 * Creates and returns the specified image.
	 *
	 * @param name The name of the image file, such as <code>copy.gif</code>.
	 * @return The image, or <code>null</code> if it cannot be found.
	 */
	private Image getImage(String name) {
		// Check jar first to prevent Applet AccessControlException
		ClassLoader cl = this.getClass().getClassLoader();
		InputStream in = cl.getResourceAsStream(name);
		try {
			if (in!=null) { // In a jar
				return ImageIO.read(in);
			}
			else { // e.g. in Eclipse debugger
				File file = new File(name);
				if (file.isFile()) {
					return ImageIO.read(file);
				}
				else {
					throw new IOException("Image not found: " + name);
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}


	private String getInfoString(String key, int offs, int param) {
		String text = msg.getString(key);
		text = MessageFormat.format(text,
				new Object[] { new Integer(offs), new Integer(param) });
		return text;
	}


	private String getInfoString(String key, int offs, int param1, int param2) {
		String text = msg.getString(key);
		text = MessageFormat.format(text,
				new Object[] { new Integer(offs), new Integer(param1),
								new Integer(param2) });
		return text;
	}


	/**
	 * Opens a file.  Displays an error dialog if the file cannot be
	 * opened.
	 *
	 * @param fileName The file to open.
	 */
	private void handleOpenFile(String fileName) {
		// Check jar first to prevent Applet AccessControlException
		ClassLoader cl = this.getClass().getClassLoader();
		InputStream in = cl.getResourceAsStream(fileName);
		try {
			if (in!=null) { // In a jar
				editor.open(new BufferedInputStream(in));
			}
			else { // e.g. in Eclipse debugger
				File file = new File(fileName);
				if (file.isFile()) { // e.g. in Eclipse debugger
					editor.open(fileName);
				}
				else { // In a jar
					throw new IOException("Resource not found: " + fileName);
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			String message = msg.getString("Error.Title");
			String title = msg.getString("Error.Desc");
			title = MessageFormat.format(title,
										new Object[] { ioe.toString() });
			JOptionPane.showMessageDialog(this, message, title,
										JOptionPane.ERROR_MESSAGE);
		}
	}


	/**
	 * Called when the bytes in the hex editor change.
	 *
	 * @param e The event.
	 */
	public void hexBytesChanged(HexEditorEvent e) {

		String text = null;

		if (e.isModification()) {
			text = getInfoString("InfoFieldModified",
									e.getOffset(), e.getAddedCount());
		}
		else {
			int added = e.getAddedCount();
			int removed = e.getRemovedCount();
			if (added>0 && removed==0) {
				text = getInfoString("InfoFieldAdded",
									e.getOffset(), e.getAddedCount());
			}
			else if (added==0 && removed>0) {
				text = getInfoString("InfoFieldRemoved",
									e.getOffset(), e.getRemovedCount());
			}
			else {
				text = getInfoString("InfoFieldBoth", e.getOffset(),
									e.getAddedCount(), e.getRemovedCount());
			}
		}
		infoField.setText(text);

		text = msg.getString("SizeField");
		text = MessageFormat.format(text,
				new Object[] { new Integer(e.getHexEditor().getByteCount()) });
		sizeField.setText(text);

	}


	/**
	 * Returns whether Nimbus is supported by the current JVM.  Nimbus was
	 * added in 6u10, and the package containing it changed in Java 7, so this
	 * is the only reliable way to check for it.
	 *
	 * @return Whether Nimbus is installed.
	 */
	private boolean isNimbusSupported() {
		// Overridden to always return false, since Nimbus doesn't uninstall
		// cleanly (leaves table selection color behind) and we don't want it
		// to look like a HexEditor bug.
		return false;
		/*
		LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
		for (int i=0; i<infos.length; i++) {
			if ("Nimbus".equals(infos[i].getName())) {
				return true;
			}
		}
		return false;
		*/
	}


	/**
	 * Called when the selection changes in the hex editor.
	 *
	 * @param An object describing the selection.
	 */
	public void selectionChanged(SelectionChangedEvent e) {
		int offs = e.getNewSelecStart();
		int count = e.getNewSelecEnd() - offs + 1;
		String subject = count==1 ? " byte" : " bytes";
		selField.setText(count + subject + " selected at offset " + offs);
	}


	/**
	 * Base class for all actions in the demo.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private abstract class ActionBase extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ActionBase() {
			String name = msg.getString(getNameKey());
			putValue(Action.NAME, name);
			putValue(Action.SHORT_DESCRIPTION, name);
			ImageIcon icon = new ImageIcon(getImage(getIconKey()));
			putValue(Action.SMALL_ICON, icon);
		}

		protected abstract String getNameKey();

		protected abstract String getIconKey();

	}


	/**
	 * Renderer for JComboBox content lists containing colors.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private static class ColorCellRenderer extends DefaultListCellRenderer
											implements Icon {

		private static final long serialVersionUID = 1L;

		private Color c;

		public Component getListCellRendererComponent(JList list, Object value,
							int index, boolean selected, boolean hasFocus) {
			super.getListCellRendererComponent(list, null, index, selected,
												hasFocus);
			if (value instanceof Color) {
				c = (Color)value;
			}
			return this;
		}

		public Icon getIcon() {
			return this;
		}

		public int getIconHeight() {
			return 16;
		}

		public int getIconWidth() {
			return 16;
		}

		public void paintIcon(Component comp, Graphics g, int x, int y) {
			g.setColor(c);
			g.fillRect(x, y, getIconWidth(), getIconHeight());
			g.setColor(Color.BLACK);
			g.drawRect(x,y, getIconWidth(), getIconHeight());
		}

	}


	private class ConfigPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		public ConfigPanel() {

			setLayout(new BorderLayout());
			setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			JPanel temp = new JPanel(new GridLayout(3,3, 5,5));
	
			colHeaderCB = new JCheckBox(msg.getString("ColHeaderCB"), true);
			colHeaderCB.addActionListener(HexEditorDemoPanel.this);
			temp.add(colHeaderCB);

			rowHeaderCB = new JCheckBox(msg.getString("RowHeaderCB"), true);
			rowHeaderCB.addActionListener(HexEditorDemoPanel.this);
			temp.add(rowHeaderCB);

			showGridCB = new JCheckBox(msg.getString("GridLinesCB"), false);
			showGridCB.addActionListener(HexEditorDemoPanel.this);
			temp.add(showGridCB);

			altRowBGCB = new JCheckBox(msg.getString("AlternateRowBG"), false);
			altRowBGCB.addActionListener(HexEditorDemoPanel.this);
			temp.add(altRowBGCB);

			altColBGCB = new JCheckBox(msg.getString("AlternateColBG"), false);
			altColBGCB.addActionListener(HexEditorDemoPanel.this);
			temp.add(altColBGCB);

            lowBytePaddingCB = new JCheckBox(msg.getString("PadLowBytesCB"), true);
            lowBytePaddingCB.addActionListener(HexEditorDemoPanel.this);
            temp.add(lowBytePaddingCB);

            cellEditableCB = new JCheckBox(msg.getString("CellEditable"), true);
            cellEditableCB.addActionListener(HexEditorDemoPanel.this);
            temp.add(cellEditableCB);
            
			highlightAsciiSelCB = new JCheckBox(
									msg.getString("HighlightAsciiSel"), true);
			highlightAsciiSelCB.addActionListener(HexEditorDemoPanel.this);
			temp.add(highlightAsciiSelCB);

			highlightAsciiSelCombo = new JComboBox();
			highlightAsciiSelCombo.setRenderer(new ColorCellRenderer());
			highlightAsciiSelCombo.addItem(new Color(255,255,192));
			highlightAsciiSelCombo.addItem(new Color(224,224,255));
			highlightAsciiSelCombo.addItem(new Color(224,224,224));
			highlightAsciiSelCombo.addActionListener(HexEditorDemoPanel.this);
			JPanel temp2 = new JPanel(new BorderLayout());
			String text = msg.getString("HighlightColor");
			temp2.add(new JLabel(text), BorderLayout.LINE_START);
			temp2.add(highlightAsciiSelCombo);
			temp.add(temp2);

			add(temp);

			temp = new JPanel(new BorderLayout());
			temp.add(new JLabel(msg.getString("LafLabel")),
									BorderLayout.LINE_START);
			lafCombo = new JComboBox();
			lafCombo.addItem("System");
			lafCombo.addItem("Metal");
			lafCombo.addItem("Motif");
			if (isNimbusSupported()) {
				lafCombo.addItem("Nimbus");
			}
			lafCombo.addActionListener(HexEditorDemoPanel.this);
			temp.add(lafCombo);
			temp2 = new JPanel(new BorderLayout());
			temp2.add(temp, BorderLayout.LINE_START);
			add(temp2, BorderLayout.SOUTH);

		}

	}


	/**
	 * Copies the currently selected bytes to the clipboard.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class CopyAction extends ActionBase {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			editor.copy();
		}

		protected String getNameKey() {
			return "Action.Copy.Name";
		}

		protected String getIconKey() {
			return "copy.gif";
		}

	}


	/**
	 * Moves the currently selected bytes to the clipboard, similar to
	 * a "cut" action in a text editor.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class CutAction extends ActionBase {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			editor.cut();
		}

		protected String getNameKey() {
			return "Action.Cut.Name";
		}

		protected String getIconKey() {
			return "cut.gif";
		}

	}


	/**
	 * Deletes the currently selected bytes.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class DeleteAction extends ActionBase {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			editor.delete();
		}

		protected String getNameKey() {
			return "Action.Delete.Name";
		}

		protected String getIconKey() {
			return "delete.gif";
		}

	}


	/**
	 * Action that prompts a user to open a file.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class OpenAction extends ActionBase {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			doOpen();
		}

		protected String getNameKey() {
			return "Action.Open.Name";
		}

		protected String getIconKey() {
			return "open.gif";
		}

	}


	/**
	 * Pastes the current clipboard contents into the hex editor at
	 * the currently selected byte location.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class PasteAction extends ActionBase {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			editor.paste();
		}

		protected String getNameKey() {
			return "Action.Paste.Name";
		}

		protected String getIconKey() {
			return "paste.gif";
		}

	}


	/**
	 * Undoes the last "undo" operation.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class RedoAction extends ActionBase {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			editor.redo();
		}

		protected String getNameKey() {
			return "Action.Redo.Name";
		}

		protected String getIconKey() {
			return "redo.gif";
		}

	}


	/**
	 * Takes back the last operation done.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class UndoAction extends ActionBase {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			editor.undo();
		}

		protected String getNameKey() {
			return "Action.Undo.Name";
		}

		protected String getIconKey() {
			return "undo.gif";
		}

	}


}