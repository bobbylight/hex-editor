package org.fife.ui.hex.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Unit tests for {@link SelectionChangedListener}.
 */
class SelectionChangedListenerTest {

	@Test
	void testSelectionChanged_isCalledWithExpectedEvent() {

		SelectionChangedEvent event = new SelectionChangedEvent(new Object(), 0, 1, 2, 3);
		SelectionChangedListener listener = mock(SelectionChangedListener.class);

		listener.selectionChanged(event);

		verify(listener).selectionChanged(event);

	}


	@Test
	void testSelectionChanged_canBeImplementedAsLambda() {

		SelectionChangedEvent event = new SelectionChangedEvent(new Object(), 0, 1, 2, 3);
		SelectionChangedEvent[] received = new SelectionChangedEvent[1];

		SelectionChangedListener listener = e -> received[0] = e;
		listener.selectionChanged(event);

		assertSame(event, received[0]);

	}


}
