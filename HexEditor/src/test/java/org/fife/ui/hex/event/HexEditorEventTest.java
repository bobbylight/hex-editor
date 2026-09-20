package org.fife.ui.hex.event;

import org.fife.ui.hex.swing.HexEditor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;


/**
 * Unit tests for {@link HexEditorEvent}.
 */
class HexEditorEventTest {

	@Test
	void testGetters() {

		HexEditor editor = mock(HexEditor.class);
		HexEditorEvent e = new HexEditorEvent(editor, 5, 2, 3);

		assertSame(editor, e.getSource());
		assertSame(editor, e.getHexEditor());
		assertEquals(5, e.getOffset());
		assertEquals(2, e.getAddedCount());
		assertEquals(3, e.getRemovedCount());

	}


	@Test
	void testIsModification_true_whenAddedEqualsRemoved() {
		HexEditor editor = mock(HexEditor.class);
		HexEditorEvent e = new HexEditorEvent(editor, 0, 4, 4);
		assertTrue(e.isModification());
	}


	@Test
	void testIsModification_false_whenAddedDoesNotEqualRemoved() {
		HexEditor editor = mock(HexEditor.class);
		HexEditorEvent e = new HexEditorEvent(editor, 0, 4, 1);
		assertFalse(e.isModification());
	}


	@Test
	void testIsModification_true_whenNoBytesChanged() {
		// Both added and removed are 0 - this is still considered a
		// "modification" per the added == removed contract.
		HexEditor editor = mock(HexEditor.class);
		HexEditorEvent e = new HexEditorEvent(editor, 0, 0, 0);
		assertTrue(e.isModification());
	}


}
