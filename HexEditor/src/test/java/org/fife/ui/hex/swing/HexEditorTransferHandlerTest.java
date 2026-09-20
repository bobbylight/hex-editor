package org.fife.ui.hex.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.swing.TransferHandler;

import org.fife.ui.SwingRunnerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit tests for {@link HexEditorTransferHandler}.
 */
@ExtendWith(SwingRunnerExtension.class)
class HexEditorTransferHandlerTest {

	private final HexEditorTransferHandler handler = new HexEditorTransferHandler();


	private static HexEditor createEditorWithBytes(byte[] bytes) throws IOException {
		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(bytes));
		return editor;
	}


	@Test
	void testCanImport_enabledEditor_withStringFlavor() throws IOException {
		HexEditor editor = createEditorWithBytes(new byte[] { 1 });
		DataFlavor[] flavors = { DataFlavor.stringFlavor };
		assertTrue(handler.canImport(editor, flavors));
	}


	@Test
	void testCanImport_disabledEditor_returnsFalse() throws IOException {
		HexEditor editor = createEditorWithBytes(new byte[] { 1 });
		editor.setEnabled(false);
		DataFlavor[] flavors = { DataFlavor.stringFlavor };
		assertFalse(handler.canImport(editor, flavors));
	}


	@Test
	void testCanImport_noStringFlavor_returnsFalse() throws IOException {
		HexEditor editor = createEditorWithBytes(new byte[] { 1 });
		DataFlavor[] flavors = { DataFlavor.imageFlavor };
		assertFalse(handler.canImport(editor, flavors));
	}


	@Test
	void testGetSourceActions_enabledEditor() throws IOException {
		HexEditor editor = createEditorWithBytes(new byte[] { 1 });
		assertEquals(TransferHandler.COPY_OR_MOVE, handler.getSourceActions(editor));
	}


	@Test
	void testGetSourceActions_disabledEditor() throws IOException {
		HexEditor editor = createEditorWithBytes(new byte[] { 1 });
		editor.setEnabled(false);
		assertEquals(TransferHandler.COPY, handler.getSourceActions(editor));
	}


	@Test
	void testCreateTransferable() throws Exception {

		HexEditor editor = createEditorWithBytes(new byte[] { 'a', 'b', 'c', 'd' });
		editor.setSelectedRange(1, 2);

		Transferable transferable = handler.createTransferable(editor);

		assertTrue(transferable instanceof ByteArrayTransferable);
		ByteArrayTransferable bat = (ByteArrayTransferable)transferable;
		assertEquals(1, bat.getOffset());
		assertEquals(2, bat.getLength());
		assertEquals("bc", bat.getTransferData(DataFlavor.stringFlavor));

	}


	@Test
	void testExportDone_moveAction_removesTransferredBytes() throws Exception {

		HexEditor editor = createEditorWithBytes(new byte[] { 'a', 'b', 'c', 'd' });
		editor.setSelectedRange(1, 2);

		Transferable transferable = handler.createTransferable(editor);
		handler.exportDone(editor, transferable, TransferHandler.MOVE);

		assertEquals(2, editor.getByteCount());
		assertEquals('a', editor.getByte(0));
		assertEquals('d', editor.getByte(1));

	}


	@Test
	void testExportDone_copyAction_doesNotRemoveBytes() throws Exception {

		HexEditor editor = createEditorWithBytes(new byte[] { 'a', 'b', 'c', 'd' });
		editor.setSelectedRange(1, 2);

		Transferable transferable = handler.createTransferable(editor);
		handler.exportDone(editor, transferable, TransferHandler.COPY);

		assertEquals(4, editor.getByteCount());

	}


	@Test
	void testImportData_stringFlavor_replacesSelection() throws IOException {

		HexEditor editor = createEditorWithBytes(new byte[] { 'a', 'b', 'c', 'd' });
		editor.setSelectedRange(1, 2);

		boolean imported = handler.importData(editor, new StringSelection("XY"));

		// Quirk: the "imported" local variable is never actually set to
		// true in the implementation, so this always returns false even
		// though the import itself succeeds (see assertions below).
		assertFalse(imported);
		assertEquals(4, editor.getByteCount());
		assertEquals('a', editor.getByte(0));
		assertEquals('X', editor.getByte(1));
		assertEquals('Y', editor.getByte(2));
		assertEquals('d', editor.getByte(3));

	}


	@Test
	void testImportData_unsupportedFlavor_returnsFalse() throws IOException {
		HexEditor editor = createEditorWithBytes(new byte[] { 'a' });
		Transferable transferable = new Transferable() {
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] { DataFlavor.imageFlavor };
			}
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return flavor.equals(DataFlavor.imageFlavor);
			}
			public Object getTransferData(DataFlavor flavor) {
				return null;
			}
		};
		assertFalse(handler.importData(editor, transferable));
	}


}
