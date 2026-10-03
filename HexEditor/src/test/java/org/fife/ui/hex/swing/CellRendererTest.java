package org.fife.ui.hex.swing;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.swing.table.TableCellRenderer;

import org.fife.ui.SwingRunnerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


/**
 * Unit tests for {@code HexTable}'s package-private {@code CellRenderer},
 * covering its alternating row/column background logic. Since that class
 * is private, it's exercised here through the public {@link
 * TableCellRenderer} interface (obtained via {@code
 * JTable.getDefaultRenderer()}) rather than by naming the private type.
 *
 * <p>Note: the renderer returns the <em>same</em> (mutable) component
 * instance on every call, so each render's background is captured into a
 * {@link Color} immediately, before rendering the next cell overwrites it.
 */
@ExtendWith(SwingRunnerExtension.class)
class CellRendererTest {

	private static HexEditor createEditorWithBytes(int byteCount) throws IOException {
		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[byteCount]));
		return editor;
	}


	private static Color renderAndGetBackground(HexTable table, Object value, boolean selected, int row, int col) {
		TableCellRenderer renderer = table.getDefaultRenderer(Object.class);
		return renderer.getTableCellRendererComponent(table, value, selected, false, row, col)
				.getBackground();
	}


	@Test
	void testAsciiDumpColumn_rowBackgroundAlternates() throws IOException {

		HexEditor editor = createEditorWithBytes(32); // 2 rows
		editor.setAlternateRowBG(true);
		HexTable table = editor.getTable();
		int asciiCol = table.getColumnCount() - 1;

		Color evenRow = renderAndGetBackground(table, "", false, 0, asciiCol);
		Color oddRow = renderAndGetBackground(table, "", false, 1, asciiCol);

		assertNotEquals(evenRow, oddRow);

	}


	@Test
	void testAsciiDumpColumn_rowBackgroundNotAlternating_isConsistent() throws IOException {

		HexEditor editor = createEditorWithBytes(32);
		editor.setAlternateRowBG(false);
		HexTable table = editor.getTable();
		int asciiCol = table.getColumnCount() - 1;

		Color evenRow = renderAndGetBackground(table, "", false, 0, asciiCol);
		Color oddRow = renderAndGetBackground(table, "", false, 1, asciiCol);

		assertEquals(evenRow, oddRow);

	}


	@Test
	void testAsciiDumpColumn_highlightDisabled_fallsBackToOrdinaryAlternatingLogic()
			throws IOException {

		HexEditor editor = createEditorWithBytes(32);
		editor.setHighlightSelectionInAsciiDump(false);
		editor.setAlternateRowBG(true);
		HexTable table = editor.getTable();
		int asciiCol = table.getColumnCount() - 1;

		// With highlighting disabled, the ascii-dump column is treated like
		// any other column, so row alternation still applies via the
		// "else" branch rather than the dedicated ascii-dump branch.
		Color evenRow = renderAndGetBackground(table, "", false, 0, asciiCol);
		Color oddRow = renderAndGetBackground(table, "", false, 1, asciiCol);

		assertNotEquals(evenRow, oddRow);

	}


	@Test
	void testDataColumn_notSelected_alternatingRowOnly_highlightsOddRows()
			throws IOException {

		HexEditor editor = createEditorWithBytes(32);
		editor.setAlternateRowBG(true);
		editor.setAlternateColumnBG(false);
		HexTable table = editor.getTable();

		Color evenRowCell = renderAndGetBackground(table, "00", false, 0, 0);
		Color oddRowCell = renderAndGetBackground(table, "00", false, 1, 0);

		assertNotEquals(evenRowCell, oddRowCell);

	}


	@Test
	void testDataColumn_notSelected_alternatingRowAndColumn_cancelsOut()
			throws IOException {

		// When both row and column alternation are on, an odd row *and* an
		// odd column cancel out via XOR, leaving the ordinary background -
		// same as if neither were alternated.
		HexEditor editor = createEditorWithBytes(32);
		editor.setAlternateRowBG(true);
		editor.setAlternateColumnBG(true);
		HexTable table = editor.getTable();

		Color row0Col0 = renderAndGetBackground(table, "00", false, 0, 0);
		Color row1Col1 = renderAndGetBackground(table, "00", false, 1, 1);

		assertEquals(row0Col0, row1Col1);

	}


	@Test
	void testDataColumn_selectedCell_skipsAlternatingBackgroundLogic() throws IOException {

		HexEditor editor = createEditorWithBytes(32);
		editor.setAlternateRowBG(true);
		HexTable table = editor.getTable();

		// Selected cells skip the custom alternating-background logic
		// entirely, keeping whatever the base renderer set for selection.
		Color selected = renderAndGetBackground(table, "00", true, 1, 0);

		assertEquals(table.getSelectionBackground(), selected);

	}


}
