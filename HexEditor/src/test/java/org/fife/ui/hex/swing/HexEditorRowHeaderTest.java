package org.fife.ui.hex.swing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.swing.JViewport;

import org.fife.ui.SwingRunnerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


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


	@Test
	void testSetSelectionInterval_whenMaxExceedsTableRowCount_doesNotTouchTableSelection()
			throws IOException {

		HexEditor editor = new HexEditor();
		HexEditorRowHeader header = getRowHeader(editor);
		editor.open(new ByteArrayInputStream(new byte[32])); // rows 0 and 1
		editor.setSelectedRange(5, 5);

		// The row header can show more rows than the table actually has (it
		// always shows at least 1, even for an empty table), so selecting a
		// row beyond the table's real row count must leave the table's byte
		// selection completely untouched.
		header.setSelectionInterval(5, 5);

		assertEquals(5, editor.getSmallestSelectionIndex());
		assertEquals(5, editor.getLargestSelectionIndex());

	}


	@Test
	void testAddSelectionInterval_selectsCorrespondingTableRows() throws IOException {

		HexEditor editor = new HexEditor();
		HexEditorRowHeader header = getRowHeader(editor);
		editor.open(new ByteArrayInputStream(new byte[32]));

		header.addSelectionInterval(0, 1);

		assertTrue(header.isSelectedIndex(0));
		assertTrue(header.isSelectedIndex(1));
		assertEquals(0, editor.getSmallestSelectionIndex());
		assertEquals(31, editor.getLargestSelectionIndex());

	}


	@Test
	void testAddSelectionInterval_reversedAnchorAndLead_computesSameRange()
			throws IOException {

		HexEditor editor = new HexEditor();
		HexEditorRowHeader header = getRowHeader(editor);
		editor.open(new ByteArrayInputStream(new byte[32]));

		header.addSelectionInterval(1, 0); // anchor=1, lead=0 (reversed)

		assertEquals(0, editor.getSmallestSelectionIndex());
		assertEquals(31, editor.getLargestSelectionIndex());

	}


	@Test
	void testRemoveSelectionInterval_updatesTableSelectionFromRemainingAnchorLead()
			throws IOException {

		HexEditor editor = new HexEditor();
		HexEditorRowHeader header = getRowHeader(editor);
		editor.open(new ByteArrayInputStream(new byte[32])); // rows 0 and 1

		header.addSelectionInterval(0, 1);
		assertEquals(0, editor.getSmallestSelectionIndex());
		assertEquals(31, editor.getLargestSelectionIndex());

		header.removeSelectionInterval(0, 0);

		// Quirk: removeSelectionInterval() re-derives the table selection
		// from the *call's own* anchor/lead arguments (0, 0) via
		// getAnchorSelectionIndex()/getLeadSelectionIndex(), not from
		// whatever is actually still selected in the list afterward - so
		// even though row 1 remains visually selected in the header, the
		// table's byte selection collapses down to just row 0.
		assertFalse(header.isSelectedIndex(0));
		assertTrue(header.isSelectedIndex(1));
		assertEquals(0, editor.getSmallestSelectionIndex());
		assertEquals(15, editor.getLargestSelectionIndex());

	}


}
