package org.fife.ui.hex.swing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import org.fife.ui.SwingRunnerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit tests for {@code HexTable}'s package-private {@code CellEditor}.
 * Since that class is private, it's exercised here through the public
 * {@link TableCellEditor} interface (and {@link javax.swing.CellEditor},
 * which it extends), rather than by naming the concrete type.
 */
@ExtendWith(SwingRunnerExtension.class)
class CellEditorTest {

	private static HexTable createTable() throws IOException {
		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 0 }));
		return editor.getTable();
	}


	@Test
	void testGetTableCellEditorComponent_whenCellEditable_returnsComponent()
			throws IOException {
		HexTable table = createTable();
		table.setCellEditable(true);
		TableCellEditor editor = table.getDefaultEditor(Object.class);
		assertNotNull(editor.getTableCellEditorComponent(table, "0", true, 0, 0));
	}


	@Test
	void testGetTableCellEditorComponent_whenCellNotEditable_returnsNull()
			throws IOException {
		HexTable table = createTable();
		table.setCellEditable(false);
		TableCellEditor editor = table.getDefaultEditor(Object.class);
		assertNull(editor.getTableCellEditorComponent(table, "0", true, 0, 0));
	}


	@Test
	void testStopCellEditing_emptyValue_returnsFalse() throws Exception {

		HexTable table = createTable();
		table.setCellEditable(true);
		TableCellEditor editor = table.getDefaultEditor(Object.class);

		JTextField field = (JTextField)editor.getTableCellEditorComponent(
				table, "0", true, 0, 0);

		// Note: field.setText("") won't work here - EditorDocumentFilter
		// rejects any insertString()/replace() that would leave the field
		// non-hex, including empty, and setText() goes through replace().
		// remove() isn't filtered though (mirroring how a real backspace
		// empties the field), so use that directly to get into this state.
		field.getDocument().remove(0, field.getDocument().getLength());
		assertEquals("", field.getText());

		assertFalse(editor.stopCellEditing());

	}


	@Test
	void testStopCellEditing_nonEmptyValue_returnsTrue() throws IOException {

		HexTable table = createTable();
		table.setCellEditable(true);
		TableCellEditor editor = table.getDefaultEditor(Object.class);

		editor.getTableCellEditorComponent(table, "0", true, 0, 0);

		assertTrue(editor.stopCellEditing());
		assertEquals("0", editor.getCellEditorValue());

	}


	/**
	 * An end-to-end smoke test that goes through {@code JTable}'s real
	 * editing API, confirming the editor is actually wired up correctly
	 * (as opposed to the other tests here, which call the {@code
	 * TableCellEditor} directly).
	 */
	@Test
	void testEditCellAt_realEditingSession_usesThisEditor() throws IOException {

		HexTable table = createTable();
		table.setCellEditable(true);

		boolean started = table.editCellAt(0, 0);
		assertTrue(started);

		assertTrue(table.getEditorComponent() instanceof JTextField);
		assertEquals(0, table.getEditingRow());
		assertEquals(0, table.getEditingColumn());

		assertTrue(table.getCellEditor().stopCellEditing());
		assertNull(table.getEditorComponent());

	}


}
