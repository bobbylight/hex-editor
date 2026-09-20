package org.fife.ui.hex.swing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.fife.ui.SwingRunnerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Unit tests for {@code HexTable}'s package-private {@code
 * EditorDocumentFilter}. Since that class is private, it's exercised here
 * through its public supertype ({@link javax.swing.text.DocumentFilter}) by
 * driving the actual {@link Document} it's installed on.
 *
 * <p>Note: a real keystroke (or paste) always reaches this filter's {@code
 * replace()} method, never {@code insertString()} - {@code
 * JTextComponent.replaceSelection()}, which Swing's default key-typed and
 * paste actions both call, always delegates to {@code
 * AbstractDocument.replace()} even when nothing is selected. The {@code
 * insertString()} tests below exercise that method directly since it's
 * still part of the filter's public contract, even though nothing in this
 * app currently reaches it that way.</p>
 */
@ExtendWith(SwingRunnerExtension.class)
class EditorDocumentFilterTest {

	private static AbstractDocument getEditorDocument(String initialValue) throws IOException {
		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 0 }));
		HexTable table = editor.getTable();
		TableCellEditor cellEditor = table.getDefaultEditor(Object.class);
		JTextField field = (JTextField)cellEditor.getTableCellEditorComponent(
				table, initialValue, true, 0, 0);
		return (AbstractDocument)field.getDocument();
	}


	private static String getText(Document doc) throws BadLocationException {
		return doc.getText(0, doc.getLength());
	}


	@Test
	void testInsertString_intoEmptyField_insertsCleanly() throws Exception {
		AbstractDocument doc = getEditorDocument("");
		doc.insertString(0, "5", null);
		assertEquals("5", getText(doc));
	}


	@Test
	void testInsertString_atEndOfExistingText_appendsCleanly() throws Exception {
		// Regression test: insertString() used to validate against a
		// reconstructed "temp" string (prefix + new text + suffix) but then
		// insert that *whole* temp string instead of just the new text,
		// duplicating the surrounding content - "0" + "f" produced "00f"
		// instead of "0f".
		AbstractDocument doc = getEditorDocument("0");
		doc.insertString(1, "f", null);
		assertEquals("0f", getText(doc));
	}


	@Test
	void testInsertString_atStartOfExistingText_prependsCleanly() throws Exception {
		AbstractDocument doc = getEditorDocument("0");
		doc.insertString(0, "f", null);
		assertEquals("f0", getText(doc));
	}


	@Test
	void testInsertString_resultingInNonHexText_isRejected() throws Exception {
		Document doc = getEditorDocument("0");
		doc.insertString(1, "g", null);
		assertEquals("0", getText(doc));
	}


	@Test
	void testInsertString_negativeSign_isRejected() throws Exception {
		// Integer.parseInt(str, 16) treats a leading "-" as a sign rather
		// than an invalid digit, so e.g. "-5" parses successfully to -5 -
		// exercising the explicit "i < 0" range check, not just the
		// NumberFormatException catch.
		AbstractDocument doc = getEditorDocument("");
		doc.insertString(0, "-5", null);
		assertEquals("", getText(doc));
	}


	@Test
	void testInsertString_resultingInValueAboveByteRange_isRejected() throws Exception {
		// "10" is a valid hex value (0x10) but "100" (0x100) exceeds a byte.
		Document doc = getEditorDocument("10");
		doc.insertString(2, "0", null);
		assertEquals("10", getText(doc));
	}


	@Test
	void testReplace_resultingInValidHexByte_isAccepted() throws Exception {
		AbstractDocument doc = getEditorDocument("a");
		doc.replace(0, 1, "f", null);
		assertEquals("f", getText(doc));
	}


	@Test
	void testReplace_resultingInNonHexText_isRejected() throws Exception {
		AbstractDocument doc = getEditorDocument("a");
		doc.replace(0, 1, "z", null);
		assertEquals("a", getText(doc));
	}


}
