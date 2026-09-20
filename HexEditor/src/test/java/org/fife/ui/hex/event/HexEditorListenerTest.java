package org.fife.ui.hex.event;

import org.fife.ui.hex.swing.HexEditor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Unit tests for {@link HexEditorListener}.
 */
class HexEditorListenerTest {

	@Test
	void testHexBytesChanged_isCalledWithExpectedEvent() {

		HexEditor editor = mock(HexEditor.class);
		HexEditorEvent event = new HexEditorEvent(editor, 0, 1, 0);
		HexEditorListener listener = mock(HexEditorListener.class);

		listener.hexBytesChanged(event);

		verify(listener).hexBytesChanged(event);

	}


	@Test
	void testHexBytesChanged_canBeImplementedAsLambda() {

		HexEditor editor = mock(HexEditor.class);
		HexEditorEvent event = new HexEditorEvent(editor, 3, 0, 1);
		HexEditorEvent[] received = new HexEditorEvent[1];

		HexEditorListener listener = e -> received[0] = e;
		listener.hexBytesChanged(event);

		assertSame(event, received[0]);

	}


}
