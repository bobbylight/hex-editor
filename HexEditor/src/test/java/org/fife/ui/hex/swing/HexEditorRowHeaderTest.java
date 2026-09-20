package org.fife.ui.hex.swing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.swing.JViewport;

import org.fife.ui.SwingRunnerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Unit tests for {@link HexEditorRowHeader}.
 */
@ExtendWith(SwingRunnerExtension.class)
class HexEditorRowHeaderTest {

	/**
	 * Returns the row header automatically created and wired up by a
	 * {@code HexEditor} in its constructor.
	 */
	private static HexEditorRowHeader getRowHeader(HexEditor editor) {
		JViewport viewport = editor.getRowHeader();
		return (HexEditorRowHeader)viewport.getView();
	}


	@Test
	void testInitialModelSize_isAtLeastOne() {
		HexEditor editor = new HexEditor();
		HexEditorRowHeader header = getRowHeader(editor);
		assertEquals(1, header.getModel().getSize());
	}


	@Test
	void testModelSize_growsWithTableRowCount() throws IOException {
		HexEditor editor = new HexEditor();
		HexEditorRowHeader header = getRowHeader(editor);

		editor.open(new ByteArrayInputStream(new byte[32]));
		assertEquals(2, header.getModel().getSize());
	}


	@Test
	void testModelSize_shrinksWithTableRowCount() throws IOException {
		HexEditor editor = new HexEditor();
		HexEditorRowHeader header = getRowHeader(editor);

		editor.open(new ByteArrayInputStream(new byte[32]));
		assertEquals(2, header.getModel().getSize());

		editor.removeBytes(0, 20);
		assertEquals(1, header.getModel().getSize());
	}


	@Test
	void testModelElementAt_isFormattedAsHexOffset() throws IOException {
		HexEditor editor = new HexEditor();
		HexEditorRowHeader header = getRowHeader(editor);
		editor.open(new ByteArrayInputStream(new byte[32]));
		assertEquals("0x0", header.getModel().getElementAt(0));
		assertEquals("0x10", header.getModel().getElementAt(1));
	}


	@Test
	void testSetSelectionInterval_selectsCorrespondingTableRows() throws IOException {

		HexEditor editor = new HexEditor();
		HexEditorRowHeader header = getRowHeader(editor);
		editor.open(new ByteArrayInputStream(new byte[32]));

		header.setSelectionInterval(0, 1);

		assertEquals(0, editor.getSmallestSelectionIndex());
		assertEquals(31, editor.getLargestSelectionIndex());

	}


	@Test
	void testSetSelectionInterval_singleRow() throws IOException {

		HexEditor editor = new HexEditor();
		HexEditorRowHeader header = getRowHeader(editor);
		editor.open(new ByteArrayInputStream(new byte[32]));

		header.setSelectionInterval(1, 1);

		assertEquals(16, editor.getSmallestSelectionIndex());
		assertEquals(31, editor.getLargestSelectionIndex());

	}


}
