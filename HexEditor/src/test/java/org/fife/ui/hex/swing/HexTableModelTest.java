package org.fife.ui.hex.swing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.fife.ui.SwingRunnerExtension;
import org.fife.ui.hex.event.HexEditorEvent;
import org.fife.ui.hex.event.HexEditorListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


/**
 * Unit tests for {@link HexTableModel}.
 */
@ExtendWith(SwingRunnerExtension.class)
class HexTableModelTest {

	@TempDir
	private File tempDir;


	/**
	 * Returns the actual model backing the given editor's table, as opposed
	 * to a standalone instance, so that calls into the editor (e.g.
	 * {@code cellToOffset()}) stay in sync with the model under test.
	 */
	private static HexTableModel getModel(HexEditor editor) {
		return (HexTableModel)editor.getTable().getModel();
	}


	@Test
	void testGetRowCount_defaultSixteenByteBuffer() {
		// The model defaults to a 16-byte buffer rather than an empty one.
		HexEditor editor = new HexEditor();
		assertEquals(1, getModel(editor).getRowCount());
	}


	@Test
	void testGetRowCount_exactMultipleOfBytesPerRow() throws IOException {
		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[32]));
		assertEquals(2, getModel(editor).getRowCount());
	}


	@Test
	void testGetRowCount_partialLastRow() throws IOException {
		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[20]));
		assertEquals(2, getModel(editor).getRowCount());
	}


	@Test
	void testGetColumnCount() {
		HexEditor editor = new HexEditor();
		assertEquals(17, getModel(editor).getColumnCount());
	}


	@Test
	void testGetBytesPerRow() {
		HexEditor editor = new HexEditor();
		assertEquals(16, getModel(editor).getBytesPerRow());
	}


	@Test
	void testGetColumnName() {
		HexTableModel model = getModel(new HexEditor());
		assertEquals("+0", model.getColumnName(0));
		assertEquals("+F", model.getColumnName(15));
		assertEquals("ASCII Dump", model.getColumnName(16));
	}


	@Test
	void testGetByte_and_getByteCount() throws IOException {
		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 5, 6, 7 }));
		HexTableModel model = getModel(editor);
		assertEquals(3, model.getByteCount());
		assertEquals(5, model.getByte(0));
		assertEquals(7, model.getByte(2));
	}


	@Test
	void testGetValueAt_hexByte_padded() throws IOException {
		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 0x05, (byte)0xff }));
		HexTableModel model = getModel(editor);
		assertEquals("05", model.getValueAt(0, 0));
		assertEquals("ff", model.getValueAt(0, 1));
	}


	@Test
	void testGetValueAt_hexByte_notPadded() throws IOException {
		HexEditor editor = new HexEditor();
		editor.setPadLowBytes(false);
		editor.open(new ByteArrayInputStream(new byte[] { 0x05 }));
		HexTableModel model = getModel(editor);
		assertEquals("5", model.getValueAt(0, 0));
	}


	@Test
	void testGetValueAt_invalidCell_returnsEmptyString() throws IOException {
		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[20])); // 2nd row has only 4 bytes
		HexTableModel model = getModel(editor);
		assertEquals("", model.getValueAt(1, 4));
	}


	@Test
	void testGetValueAt_asciiDumpColumn() throws IOException {
		HexEditor editor = new HexEditor();
		// 'A' (printable) and a control character, which should render as '.'
		editor.open(new ByteArrayInputStream(new byte[] { 'A', 1 }));
		HexTableModel model = getModel(editor);
		assertEquals("A.", model.getValueAt(0, 16));
	}


	@Test
	void testSetValueAt_updatesByteAndFiresEvent() throws IOException {

		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 0x00 }));
		HexTableModel model = getModel(editor);

		HexEditorListener listener = mock(HexEditorListener.class);
		editor.addHexEditorListener(listener);

		model.setValueAt("ff", 0, 0);

		assertEquals((byte)0xff, model.getByte(0));
		verify(listener, times(1)).hexBytesChanged(any(HexEditorEvent.class));

	}


	@Test
	void testSetValueAt_sameValue_isNoOp() throws IOException {

		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 0x00 }));
		HexTableModel model = getModel(editor);

		HexEditorListener listener = mock(HexEditorListener.class);
		editor.addHexEditorListener(listener);

		model.setValueAt("00", 0, 0);

		verifyNoMoreInteractions(listener);

	}


	@Test
	void testRemoveBytes() throws IOException {
		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));
		HexTableModel model = getModel(editor);
		model.removeBytes(0, 1);
		assertEquals(2, model.getByteCount());
		assertEquals(2, model.getByte(0));
	}


	@Test
	void testSetBytes_fileName_discardsUndoHistory() throws IOException {

		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));
		editor.removeBytes(0, 1); // creates an undoable edit

		File file = new File(tempDir, "test.bin");
		Files.write(file.toPath(), new byte[] { 9, 9 });
		getModel(editor).setBytes(file.getAbsolutePath());

		assertEquals(2, editor.getByteCount());
		assertFalse(editor.undo());

	}


	@Test
	void testSetBytes_inputStream_discardsUndoHistory() throws IOException {

		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));
		editor.removeBytes(0, 1); // creates an undoable edit

		getModel(editor).setBytes(new ByteArrayInputStream(new byte[] { 9, 9 }));

		assertEquals(2, editor.getByteCount());
		assertFalse(editor.undo());

	}


	@Test
	void testUndoRedo_throughModel() throws IOException {

		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));
		HexTableModel model = getModel(editor);

		model.setValueAt("ff", 0, 0);
		assertEquals((byte)0xff, model.getByte(0));

		boolean canUndoAgain = model.undo();
		assertFalse(canUndoAgain);
		assertEquals(1, model.getByte(0));

		boolean canRedoAgain = model.redo();
		assertFalse(canRedoAgain);
		assertEquals((byte)0xff, model.getByte(0));

	}


}
