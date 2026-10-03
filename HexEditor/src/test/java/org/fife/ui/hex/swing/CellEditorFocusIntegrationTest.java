package org.fife.ui.hex.swing;

import java.awt.GraphicsEnvironment;
import java.io.ByteArrayInputStream;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * An integration test that drives {@code HexTable}'s cell editor through a
 * real, visible {@code JTable} editing session and an actual focus
 * transfer, to verify the editor's {@code FocusListener} (which selects
 * all text when the editor gains focus).
 *
 * <p>Unlike the other Swing tests in this package, this class deliberately
 * does <em>not</em> use {@code SwingRunnerExtension}: a real focus change
 * is delivered asynchronously by the EDT, so the test thread needs to stay
 * off the EDT and poll for it, rather than blocking the EDT for the
 * entire test body.
 */
class CellEditorFocusIntegrationTest {

	private JFrame frame;


	@AfterEach
	void tearDown() {
		if (frame != null) {
			frame.dispose();
		}
	}


	@Test
	void testFocusGained_selectsAllTextInEditor() throws Exception {

		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());

		HexEditor editor = new HexEditor();
		editor.open(new ByteArrayInputStream(new byte[] { 0x0a }));
		HexTable table = editor.getTable();

		SwingUtilities.invokeAndWait(() -> {
			frame = new JFrame();
			frame.add(editor);
			frame.pack();
			frame.setVisible(true);
			table.editCellAt(0, 0);
		});

		JTextField field = (JTextField)table.getEditorComponent();
		SwingUtilities.invokeAndWait(field::requestFocusInWindow);

		waitUntil(field::hasFocus, "Editor component never gained focus");

		assertEquals(field.getText(), field.getSelectedText());

	}


	/**
	 * Waits for the given condition, or aborts (skips) the test if it's
	 * never met. This is used rather than failing outright because window
	 * focus fundamentally depends on a window manager being present -
	 * something a window-manager-less virtual display (as is sometimes used
	 * in CI) can't provide, no matter how long we wait.
	 */
	private static void waitUntil(java.util.function.BooleanSupplier condition,
									String message) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 2000;
		while (!condition.getAsBoolean()) {
			if (System.currentTimeMillis() > deadline) {
				Assumptions.assumeTrue(false, message);
			}
			Thread.sleep(20);
		}
	}


}
