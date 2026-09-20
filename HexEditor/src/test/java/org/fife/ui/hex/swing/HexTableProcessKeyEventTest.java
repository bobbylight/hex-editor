package org.fife.ui.hex.swing;

import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.swing.JFrame;

import org.fife.ui.SwingRunnerExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit tests for {@link HexTable#processKeyEvent(KeyEvent)}.
 */
@ExtendWith(SwingRunnerExtension.class)
class HexTableProcessKeyEventTest {

	private JFrame frame;


	@AfterEach
	void tearDown() {
		if (frame != null) {
			frame.dispose();
		}
	}


	private static HexTable createTableWithBytes(int byteCount) throws IOException {
		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[byteCount]));
		return editor.getTable();
	}


	private static KeyEvent keyPressed(HexTable table, int keyCode, int modifiers) {
		return new KeyEvent(table, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
				modifiers, keyCode, KeyEvent.CHAR_UNDEFINED);
	}


	@Test
	void testLeft_movesSelectionBackOne() throws IOException {
		HexTable table = createTableWithBytes(40);
		table.changeSelectionByOffset(5, false);

		table.processKeyEvent(keyPressed(table, KeyEvent.VK_LEFT, 0));

		assertEquals(4, table.getSmallestSelectionIndex());
		assertEquals(4, table.getLargestSelectionIndex());
	}


	@Test
	void testLeft_atStartOfBuffer_staysClampedAtZero() throws IOException {
		HexTable table = createTableWithBytes(40);
		table.changeSelectionByOffset(0, false);

		table.processKeyEvent(keyPressed(table, KeyEvent.VK_LEFT, 0));

		assertEquals(0, table.getSmallestSelectionIndex());
		assertEquals(0, table.getLargestSelectionIndex());
	}


	@Test
	void testLeft_withShiftDown_extendsSelectionInsteadOfMovingIt() throws IOException {
		HexTable table = createTableWithBytes(40);
		table.changeSelectionByOffset(5, false);

		table.processKeyEvent(keyPressed(table, KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK));

		// Anchor (5) stays put; only the lead moves, extending the selection.
		assertEquals(4, table.getSmallestSelectionIndex());
		assertEquals(5, table.getLargestSelectionIndex());
	}


	@Test
	void testRight_movesSelectionForwardOne() throws IOException {
		HexTable table = createTableWithBytes(40);
		table.changeSelectionByOffset(5, false);

		table.processKeyEvent(keyPressed(table, KeyEvent.VK_RIGHT, 0));

		assertEquals(6, table.getSmallestSelectionIndex());
		assertEquals(6, table.getLargestSelectionIndex());
	}


	@Test
	void testRight_atEndOfBuffer_staysClampedAtLastByte() throws IOException {
		HexTable table = createTableWithBytes(40);
		table.changeSelectionByOffset(39, false);

		table.processKeyEvent(keyPressed(table, KeyEvent.VK_RIGHT, 0));

		assertEquals(39, table.getSmallestSelectionIndex());
		assertEquals(39, table.getLargestSelectionIndex());
	}


	@Test
	void testRight_withShiftDown_extendsSelection() throws IOException {
		HexTable table = createTableWithBytes(40);
		table.changeSelectionByOffset(5, false);

		table.processKeyEvent(keyPressed(table, KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK));

		assertEquals(5, table.getSmallestSelectionIndex());
		assertEquals(6, table.getLargestSelectionIndex());
	}


	@Test
	void testUp_movesSelectionBackOneRow() throws IOException {
		HexTable table = createTableWithBytes(40);
		table.changeSelectionByOffset(20, false);

		table.processKeyEvent(keyPressed(table, KeyEvent.VK_UP, 0));

		assertEquals(4, table.getSmallestSelectionIndex());
		assertEquals(4, table.getLargestSelectionIndex());
	}


	@Test
	void testUp_pastFirstRow_staysClampedAtZero() throws IOException {
		HexTable table = createTableWithBytes(40);
		table.changeSelectionByOffset(5, false);

		table.processKeyEvent(keyPressed(table, KeyEvent.VK_UP, 0));

		assertEquals(0, table.getSmallestSelectionIndex());
		assertEquals(0, table.getLargestSelectionIndex());
	}


	@Test
	void testDown_movesSelectionForwardOneRow() throws IOException {
		HexTable table = createTableWithBytes(40);
		table.changeSelectionByOffset(4, false);

		table.processKeyEvent(keyPressed(table, KeyEvent.VK_DOWN, 0));

		assertEquals(20, table.getSmallestSelectionIndex());
		assertEquals(20, table.getLargestSelectionIndex());
	}


	@Test
	void testDown_pastLastRow_staysClampedAtLastByte() throws IOException {
		HexTable table = createTableWithBytes(40);
		table.changeSelectionByOffset(39, false);

		table.processKeyEvent(keyPressed(table, KeyEvent.VK_DOWN, 0));

		assertEquals(39, table.getSmallestSelectionIndex());
		assertEquals(39, table.getLargestSelectionIndex());
	}


	@Test
	void testHome_movesToStartOfCurrentRow() throws IOException {
		HexTable table = createTableWithBytes(40);
		table.changeSelectionByOffset(20, false); // row 1, col 4

		table.processKeyEvent(keyPressed(table, KeyEvent.VK_HOME, 0));

		assertEquals(16, table.getSmallestSelectionIndex());
		assertEquals(16, table.getLargestSelectionIndex());
	}


	@Test
	void testEnd_movesToEndOfCurrentRow() throws IOException {
		HexTable table = createTableWithBytes(40);
		table.changeSelectionByOffset(20, false); // row 1, col 4

		table.processKeyEvent(keyPressed(table, KeyEvent.VK_END, 0));

		assertEquals(31, table.getSmallestSelectionIndex());
		assertEquals(31, table.getLargestSelectionIndex());
	}


	@Test
	void testEnd_onPartialLastRow_isClampedToLastByte() throws IOException {
		// 40 bytes = row 0 (0-15), row 1 (16-31), row 2 (32-39, partial)
		HexTable table = createTableWithBytes(40);
		table.changeSelectionByOffset(35, false); // row 2, col 3

		table.processKeyEvent(keyPressed(table, KeyEvent.VK_END, 0));

		// Unclamped row-end would be 32+15=47, which is beyond the buffer.
		assertEquals(39, table.getSmallestSelectionIndex());
		assertEquals(39, table.getLargestSelectionIndex());
	}


	@Test
	void testHandledKey_consumesEvent() throws IOException {
		HexTable table = createTableWithBytes(40);
		KeyEvent e = keyPressed(table, KeyEvent.VK_LEFT, 0);

		table.processKeyEvent(e);

		assertTrue(e.isConsumed());
	}


	@Test
	void testUnrecognizedKeyCode_doesNotChangeSelection() throws IOException {
		HexTable table = createTableWithBytes(40);
		table.changeSelectionByOffset(5, false);

		table.processKeyEvent(keyPressed(table, KeyEvent.VK_A, 0));

		assertEquals(5, table.getSmallestSelectionIndex());
		assertEquals(5, table.getLargestSelectionIndex());
	}


	@Test
	void testKeyReleased_isIgnored() throws IOException {

		HexTable table = createTableWithBytes(40);
		table.changeSelectionByOffset(5, false);

		KeyEvent released = new KeyEvent(table, KeyEvent.KEY_RELEASED,
				System.currentTimeMillis(), 0, KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED);
		table.processKeyEvent(released);

		// Only KEY_PRESSED is handled; a released arrow key should not
		// move the selection.
		assertEquals(5, table.getSmallestSelectionIndex());
		assertEquals(5, table.getLargestSelectionIndex());

	}


	@Test
	void testPageDown_movesSelectionByVisibleRowCount() throws IOException {

		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());

		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[400]));
		HexTable table = editor.getTable();

		frame = new JFrame();
		frame.add(editor);
		frame.pack();
		frame.setVisible(true);

		int visibleRowCount = table.getVisibleRect().height / table.getRowHeight();
		assertTrue(visibleRowCount > 0,
				"Test setup problem: table has no visible rows to page through");

		table.changeSelectionByOffset(0, false);
		table.processKeyEvent(keyPressed(table, KeyEvent.VK_PAGE_DOWN, 0));

		int expected = Math.min(visibleRowCount * 16, table.getByteCount() - 1);
		assertEquals(expected, table.getSmallestSelectionIndex());
		assertEquals(expected, table.getLargestSelectionIndex());

	}


	@Test
	void testPageUp_movesSelectionByVisibleRowCount() throws IOException {

		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());

		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[400]));
		HexTable table = editor.getTable();

		frame = new JFrame();
		frame.add(editor);
		frame.pack();
		frame.setVisible(true);

		int visibleRowCount = table.getVisibleRect().height / table.getRowHeight();
		assertTrue(visibleRowCount > 0,
				"Test setup problem: table has no visible rows to page through");

		int start = table.getByteCount() - 1;
		table.changeSelectionByOffset(start, false);
		table.processKeyEvent(keyPressed(table, KeyEvent.VK_PAGE_UP, 0));

		int expected = Math.max(start - visibleRowCount * 16, 0);
		assertEquals(expected, table.getSmallestSelectionIndex());
		assertEquals(expected, table.getLargestSelectionIndex());

	}


}
