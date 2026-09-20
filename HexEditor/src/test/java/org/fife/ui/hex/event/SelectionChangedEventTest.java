package org.fife.ui.hex.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;


/**
 * Unit tests for {@link SelectionChangedEvent}.
 */
class SelectionChangedEventTest {

	@Test
	void testGetters() {

		Object source = new Object();
		SelectionChangedEvent e = new SelectionChangedEvent(source, 1, 2, 3, 4);

		assertSame(source, e.getSource());
		assertEquals(1, e.getPreviousSelecStart());
		assertEquals(2, e.getPreviousSelecEnd());
		assertEquals(3, e.getNewSelecStart());
		assertEquals(4, e.getNewSelecEnd());

	}


	@Test
	void testToString() {

		SelectionChangedEvent e = new SelectionChangedEvent(new Object(), 1, 2, 3, 4);

		String expected = "Old selection: [1, 2]; New selection: [3, 4]";
		assertEquals(expected, e.toString());

	}


}
