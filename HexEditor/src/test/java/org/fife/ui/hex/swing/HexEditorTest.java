package org.fife.ui.hex.swing;

import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.fife.ui.SwingRunnerExtension;
import org.fife.ui.hex.event.HexEditorEvent;
import org.fife.ui.hex.event.HexEditorListener;
import org.fife.ui.hex.event.SelectionChangedListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;


/**
 * Unit tests for {@link HexEditor}.
 */
@ExtendWith(SwingRunnerExtension.class)
class HexEditorTest {

	@TempDir
	private File tempDir;


	@Test
	void testDefaults() {
		HexEditor editor = new HexEditor();
		// HexTableModel initializes its document to a 16-byte (all zero)
		// buffer by default, rather than an empty one.
		assertEquals(16, editor.getByteCount());
		assertFalse(editor.getAlternateRowBG());
		assertFalse(editor.getAlternateColumnBG());
		assertTrue(editor.getHighlightSelectionInAsciiDump());
		assertTrue(editor.getPadLowBytes());
	}


	@Test
	void testOpen_inputStream() throws IOException {
		HexEditor editor = new HexEditor();
		byte[] content = { 1, 2, 3, 4 };
		editor.open(new ByteArrayInputStream(content));
		assertEquals(4, editor.getByteCount());
		for (int i = 0; i < content.length; i++) {
			assertEquals(content[i], editor.getByte(i));
		}
	}


	@Test
	void testOpen_fileName() throws IOException {
		HexEditor editor = new HexEditor();
		byte[] content = { 9, 8, 7 };
		File file = new File(tempDir, "test.bin");
		Files.write(file.toPath(), content);
		editor.open(file.getAbsolutePath());
		assertEquals(3, editor.getByteCount());
		assertEquals(9, editor.getByte(0));
	}


	@Test
	void testOpen_directory_throwsIOException() {
		HexEditor editor = new HexEditor();
		assertThrows(IOException.class, () -> editor.open(tempDir.getAbsolutePath()));
	}


	@Test
	void testCellToOffsetAndOffsetToCell_roundTrip() throws IOException {
		HexEditor editor = new HexEditor();
		byte[] content = new byte[20]; // 2 rows: 16 + 4
		editor.open(new ByteArrayInputStream(content));

		assertEquals(0, editor.cellToOffset(0, 0));
		assertEquals(15, editor.cellToOffset(0, 15));
		assertEquals(16, editor.cellToOffset(1, 0));
		assertEquals(19, editor.cellToOffset(1, 3));
		// Cell in row 1 with no backing byte (only 4 bytes in that row)
		assertEquals(-1, editor.cellToOffset(1, 4));
		// "Ascii dump" column never maps to an offset
		assertEquals(-1, editor.cellToOffset(0, 16));

		Point cell = editor.offsetToCell(19);
		assertEquals(new Point(1, 3), cell);

		Point invalid = editor.offsetToCell(20);
		assertEquals(new Point(-1, -1), invalid);
	}


	@Test
	void testSetSelectedRangeAndGetters() throws IOException {
		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[10]));
		editor.setSelectedRange(2, 5);
		assertEquals(2, editor.getSmallestSelectionIndex());
		assertEquals(5, editor.getLargestSelectionIndex());
	}


	@Test
	void testRemoveBytes_updatesByteCountAndFiresEvent() throws IOException {

		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5 }));

		HexEditorListener listener = mock(HexEditorListener.class);
		editor.addHexEditorListener(listener);

		editor.removeBytes(1, 2);

		assertEquals(3, editor.getByteCount());
		assertEquals(1, editor.getByte(0));
		assertEquals(4, editor.getByte(1));

		verify(listener, times(1)).hexBytesChanged(any(HexEditorEvent.class));

	}


	@Test
	void testRemoveHexEditorListener_stopsReceivingEvents() throws IOException {

		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));

		HexEditorListener listener = mock(HexEditorListener.class);
		editor.addHexEditorListener(listener);
		editor.removeHexEditorListener(listener);

		editor.removeBytes(0, 1);

		verifyNoInteractions(listener);

	}


	@Test
	void testReplaceBytes_lenOfOne_isTreatedAsPureInsert() throws IOException {

		// Quirk: replaceBytes() special-cases len==1 to mean "just insert,
		// don't remove anything" - the byte originally at the offset is
		// preserved rather than replaced.
		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));

		editor.replaceBytes(1, 1, new byte[] { 9, 9 });

		assertEquals(5, editor.getByteCount());
		assertEquals(1, editor.getByte(0));
		assertEquals(9, editor.getByte(1));
		assertEquals(9, editor.getByte(2));
		assertEquals(2, editor.getByte(3));
		assertEquals(3, editor.getByte(4));

	}


	@Test
	void testReplaceBytes_lenGreaterThanOne_removesAndInserts() throws IOException {

		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));

		editor.replaceBytes(1, 2, new byte[] { 9, 9 });

		assertEquals(3, editor.getByteCount());
		assertEquals(1, editor.getByte(0));
		assertEquals(9, editor.getByte(1));
		assertEquals(9, editor.getByte(2));

	}


	@Test
	void testReplaceSelection() throws IOException {

		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 1, 2, 3, 4 }));
		editor.setSelectedRange(1, 2);

		editor.replaceSelection(new byte[] { 8 });

		assertEquals(3, editor.getByteCount());
		assertEquals(1, editor.getByte(0));
		assertEquals(8, editor.getByte(1));
		assertEquals(4, editor.getByte(2));

	}


	@Test
	void testUndoRedo() throws IOException {

		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));

		editor.removeBytes(0, 1);
		assertEquals(2, editor.getByteCount());

		boolean canUndoAgain = editor.undo();
		assertEquals(3, editor.getByteCount());
		assertEquals(1, editor.getByte(0));
		assertFalse(canUndoAgain);

		boolean canRedoAgain = editor.redo();
		assertEquals(2, editor.getByteCount());
		assertFalse(canRedoAgain);

	}


	@Test
	void testUndo_whenNothingToUndo_returnsFalse() {
		HexEditor editor = new HexEditor();
		assertFalse(editor.undo());
	}


	@Test
	void testRedo_whenNothingToRedo_returnsFalse() {
		HexEditor editor = new HexEditor();
		assertFalse(editor.redo());
	}


	@Test
	void testSetAlternateRowBG_firesPropertyChangeEvent() {

		HexEditor editor = new HexEditor();
		PropertyChangeListener listener = mock(PropertyChangeListener.class);
		editor.addPropertyChangeListener(HexEditor.PROPERTY_ALTERNATE_ROW_BG, listener);

		editor.setAlternateRowBG(true);
		assertTrue(editor.getAlternateRowBG());
		verify(listener, times(1)).propertyChange(any());

		// Setting to the same value again should not re-fire the event.
		editor.setAlternateRowBG(true);
		verify(listener, times(1)).propertyChange(any());

	}


	@Test
	void testSetAlternateColumnBG_firesPropertyChangeEvent() {

		HexEditor editor = new HexEditor();
		PropertyChangeListener listener = mock(PropertyChangeListener.class);
		editor.addPropertyChangeListener(HexEditor.PROPERTY_ALTERNATE_COLUMN_BG, listener);

		editor.setAlternateColumnBG(true);
		assertTrue(editor.getAlternateColumnBG());
		verify(listener, times(1)).propertyChange(any());

	}


	@Test
	void testSetHighlightSelectionInAsciiDump_firesPropertyChangeEvent() {

		HexEditor editor = new HexEditor();
		PropertyChangeListener listener = mock(PropertyChangeListener.class);
		editor.addPropertyChangeListener(HexEditor.PROPERTY_HIGHLIGHT_ASCII_DUMP, listener);

		editor.setHighlightSelectionInAsciiDump(false);
		assertFalse(editor.getHighlightSelectionInAsciiDump());
		verify(listener, times(1)).propertyChange(any());

	}


	@Test
	void testSetHighlightSelectionInAsciiDumpColor_firesPropertyChangeEvent() {

		HexEditor editor = new HexEditor();
		PropertyChangeListener listener = mock(PropertyChangeListener.class);
		editor.addPropertyChangeListener(
				HexEditor.PROPERTY_ASCII_DUMP_HIGHLIGHT_COLOR, listener);

		java.awt.Color newColor = java.awt.Color.RED;
		editor.setHighlightSelectionInAsciiDumpColor(newColor);
		assertEquals(newColor, editor.getHighlightSelectionInAsciiDumpColor());
		verify(listener, times(1)).propertyChange(any());

		// Setting to null is a no-op per the implementation.
		editor.setHighlightSelectionInAsciiDumpColor(null);
		assertEquals(newColor, editor.getHighlightSelectionInAsciiDumpColor());
		verify(listener, times(1)).propertyChange(any());

	}


	@Test
	void testSetPadLowBytes_firesPropertyChangeEvent() {

		HexEditor editor = new HexEditor();
		PropertyChangeListener listener = mock(PropertyChangeListener.class);
		editor.addPropertyChangeListener(HexEditor.PROPERTY_PAD_LOW_BYTES, listener);

		editor.setPadLowBytes(false);
		assertFalse(editor.getPadLowBytes());
		verify(listener, times(1)).propertyChange(any());

	}


	@Test
	void testSetShowGrid_firesPropertyChangeEvent() {

		HexEditor editor = new HexEditor();
		PropertyChangeListener listener = mock(PropertyChangeListener.class);
		editor.addPropertyChangeListener(HexEditor.PROPERTY_SHOW_GRID, listener);

		editor.setShowGrid(true);
		verify(listener, times(1)).propertyChange(any());

		// No-op if already the desired value.
		editor.setShowGrid(true);
		verify(listener, times(1)).propertyChange(any());

	}


	@Test
	void testSetShowColumnHeader() {
		HexEditor editor = new HexEditor();
		editor.setShowColumnHeader(true);
		assertTrue(editor.getColumnHeader().getView() != null);
		editor.setShowColumnHeader(false);
		assertEquals(null, editor.getColumnHeader().getView());
	}


	@Test
	void testSetShowRowHeader() {
		HexEditor editor = new HexEditor();
		editor.setShowRowHeader(true);
		assertTrue(editor.getRowHeader().getView() != null);
		editor.setShowRowHeader(false);
		assertEquals(null, editor.getRowHeader().getView());
	}


	@Test
	void testAddRemoveSelectionChangedListener() throws IOException {

		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5 }));

		SelectionChangedListener listener = mock(SelectionChangedListener.class);
		editor.addSelectionChangedListener(listener);

		// removeBytes() ultimately calls into HexTable#changeSelection(),
		// which fires a SelectionChangedEvent.
		editor.removeBytes(0, 1);
		verify(listener, times(1)).selectionChanged(any());

		editor.removeSelectionChangedListener(listener);
		editor.removeBytes(0, 1);
		verify(listener, times(1)).selectionChanged(any());

	}


	@Test
	void testSetCellEditable_doesNotThrow() {
		HexEditor editor = new HexEditor();
		editor.setCellEditable(false);
		editor.setCellEditable(true);
	}


}
