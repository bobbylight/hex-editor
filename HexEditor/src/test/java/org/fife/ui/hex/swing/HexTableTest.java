package org.fife.ui.hex.swing;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.fife.ui.SwingRunnerExtension;
import org.fife.ui.hex.event.SelectionChangedEvent;
import org.fife.ui.hex.event.SelectionChangedListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * Unit tests for {@link HexTable}.
 */
@ExtendWith(SwingRunnerExtension.class)
class HexTableTest {

	private static HexTable createTableWithBytes(int byteCount) throws IOException {
		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[byteCount]));
		return editor.getTable();
	}


	@Test
	void testCellToOffset_validCell() throws IOException {
		HexTable table = createTableWithBytes(20);
		assertEquals(0, table.cellToOffset(0, 0));
		assertEquals(19, table.cellToOffset(1, 3));
	}


	@Test
	void testCellToOffset_asciiDumpColumn_returnsNegativeOne() throws IOException {
		HexTable table = createTableWithBytes(20);
		assertEquals(-1, table.cellToOffset(0, 16));
	}


	@Test
	void testCellToOffset_outOfRange_returnsNegativeOne() throws IOException {
		HexTable table = createTableWithBytes(20);
		assertEquals(-1, table.cellToOffset(-1, 0));
		assertEquals(-1, table.cellToOffset(0, -1));
		assertEquals(-1, table.cellToOffset(5, 0)); // row doesn't exist
		assertEquals(-1, table.cellToOffset(1, 4)); // 2nd row only has 4 bytes
	}


	@Test
	void testOffsetToCell() throws IOException {
		HexTable table = createTableWithBytes(20);
		assertEquals(new Point(1, 3), table.offsetToCell(19));
		assertEquals(new Point(-1, -1), table.offsetToCell(20));
		assertEquals(new Point(-1, -1), table.offsetToCell(-1));
	}


	@Test
	void testChangeSelectionByOffset() throws IOException {
		HexTable table = createTableWithBytes(20);
		table.changeSelectionByOffset(5, false);
		assertEquals(5, table.getSmallestSelectionIndex());
		assertEquals(5, table.getLargestSelectionIndex());

		table.changeSelectionByOffset(10, true);
		assertEquals(5, table.getSmallestSelectionIndex());
		assertEquals(10, table.getLargestSelectionIndex());
	}


	@Test
	void testChangeSelectionByOffset_clampsToValidRange() throws IOException {
		HexTable table = createTableWithBytes(20);
		table.changeSelectionByOffset(-5, false);
		assertEquals(0, table.getSmallestSelectionIndex());

		table.changeSelectionByOffset(1000, false);
		assertEquals(19, table.getSmallestSelectionIndex());
	}


	@Test
	void testSetSelectionByOffsets() throws IOException {
		HexTable table = createTableWithBytes(20);
		table.setSelectionByOffsets(3, 8);
		assertEquals(3, table.getSmallestSelectionIndex());
		assertEquals(8, table.getLargestSelectionIndex());
	}


	@Test
	void testIsCellEditable() throws IOException {
		HexTable table = createTableWithBytes(20);
		assertTrue(table.isCellEditable(0, 0));
		assertFalse(table.isCellEditable(0, 16)); // ascii dump column
		assertFalse(table.isCellEditable(1, 4)); // no byte backing this cell
	}


	@Test
	void testIsCellSelected() throws IOException {
		HexTable table = createTableWithBytes(20);
		table.setSelectionByOffsets(2, 4);
		assertFalse(table.isCellSelected(0, 1));
		assertTrue(table.isCellSelected(0, 2));
		assertTrue(table.isCellSelected(0, 3));
		assertTrue(table.isCellSelected(0, 4));
		assertFalse(table.isCellSelected(0, 5));
		assertFalse(table.isCellSelected(0, 16)); // ascii dump column is never "selected"
	}


	@Test
	void testSetSelectedRows_invalidRange_throws() throws IOException {
		HexTable table = createTableWithBytes(20);
		assertThrows(IllegalArgumentException.class, () -> table.setSelectedRows(-1, 0));
		assertThrows(IllegalArgumentException.class, () -> table.setSelectedRows(0, 100));
	}


	@Test
	void testSetSelectedRows_validRange() throws IOException {
		HexTable table = createTableWithBytes(20);
		table.setSelectedRows(0, 1);
		assertEquals(0, table.getSmallestSelectionIndex());
		assertEquals(19, table.getLargestSelectionIndex());
	}


	@Test
	void testAddAndRemoveSelectionChangedListener() throws IOException {

		HexTable table = createTableWithBytes(20);
		SelectionChangedListener listener = mock(SelectionChangedListener.class);
		table.addSelectionChangedListener(listener);

		table.changeSelection(1, 0, false, false);
		verify(listener, times(1)).selectionChanged(any(SelectionChangedEvent.class));

		table.removeSelectionChangedListener(listener);
		table.changeSelection(0, 0, false, false);
		verify(listener, times(1)).selectionChanged(any(SelectionChangedEvent.class));

	}


	@Test
	void testChangeSelection_reportsCorrectPreviousAndNewIndices() throws IOException {

		HexTable table = createTableWithBytes(20);
		table.changeSelectionByOffset(2, false);

		SelectionChangedListener listener = mock(SelectionChangedListener.class);
		table.addSelectionChangedListener(listener);

		table.changeSelection(1, 0, false, false); // offset 16

		org.mockito.ArgumentCaptor<SelectionChangedEvent> captor =
				org.mockito.ArgumentCaptor.forClass(SelectionChangedEvent.class);
		verify(listener).selectionChanged(captor.capture());

		SelectionChangedEvent event = captor.getValue();
		assertEquals(2, event.getPreviousSelecStart());
		assertEquals(2, event.getPreviousSelecEnd());
		assertEquals(16, event.getNewSelecStart());
		assertEquals(16, event.getNewSelecEnd());

	}


	@Test
	void testGetByte_and_getByteCount() throws IOException {
		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));
		HexTable table = editor.getTable();
		assertEquals(3, table.getByteCount());
		assertEquals(2, table.getByte(1));
	}


	@Test
	void testUndoRedo() throws IOException {

		HexTable table = createTableWithBytes(3);
		table.removeBytes(0, 1);
		assertEquals(2, table.getByteCount());

		boolean canUndoAgain = table.undo();
		assertEquals(3, table.getByteCount());
		assertFalse(canUndoAgain);

		boolean canRedoAgain = table.redo();
		assertEquals(2, table.getByteCount());
		assertFalse(canRedoAgain);

	}


}
