package org.fife.ui.hex.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Reader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit tests for {@link ByteArrayTransferable}.
 */
class ByteArrayTransferableTest {

	@Test
	void testConstructor_nullBytes_yieldsEmptyArray() {
		ByteArrayTransferable bat = new ByteArrayTransferable(3, null);
		assertEquals(0, bat.getLength());
	}


	@Test
	void testGetOffsetAndGetLength() {
		byte[] bytes = { 1, 2, 3 };
		ByteArrayTransferable bat = new ByteArrayTransferable(5, bytes);
		assertEquals(5, bat.getOffset());
		assertEquals(3, bat.getLength());
	}


	@Test
	void testConstructor_clonesInputArray() throws Exception {
		byte[] bytes = "abc".getBytes();
		ByteArrayTransferable bat = new ByteArrayTransferable(0, bytes);
		bytes[0] = 'z';
		assertEquals("abc", bat.getTransferData(DataFlavor.stringFlavor));
	}


	@Test
	void testGetTransferDataFlavors() {
		ByteArrayTransferable bat = new ByteArrayTransferable(0, new byte[] { 1 });
		DataFlavor[] flavors = bat.getTransferDataFlavors();
		assertArrayEquals(new DataFlavor[] {
				DataFlavor.stringFlavor, DataFlavor.plainTextFlavor
		}, flavors);
	}


	@Test
	void testGetTransferDataFlavors_returnsClone() {
		ByteArrayTransferable bat = new ByteArrayTransferable(0, new byte[] { 1 });
		DataFlavor[] flavors1 = bat.getTransferDataFlavors();
		flavors1[0] = null;
		DataFlavor[] flavors2 = bat.getTransferDataFlavors();
		assertEquals(DataFlavor.stringFlavor, flavors2[0]);
	}


	@Test
	void testIsDataFlavorSupported() {
		ByteArrayTransferable bat = new ByteArrayTransferable(0, new byte[] { 1 });
		assertTrue(bat.isDataFlavorSupported(DataFlavor.stringFlavor));
		assertTrue(bat.isDataFlavorSupported(DataFlavor.plainTextFlavor));
		assertFalse(bat.isDataFlavorSupported(DataFlavor.imageFlavor));
	}


	@Test
	void testGetTransferData_stringFlavor() throws Exception {
		byte[] bytes = "hello".getBytes();
		ByteArrayTransferable bat = new ByteArrayTransferable(0, bytes);
		Object data = bat.getTransferData(DataFlavor.stringFlavor);
		assertEquals("hello", data);
	}


	@Test
	void testGetTransferData_plainTextFlavor() throws Exception {
		byte[] bytes = "hello".getBytes();
		ByteArrayTransferable bat = new ByteArrayTransferable(0, bytes);
		Object data = bat.getTransferData(DataFlavor.plainTextFlavor);
		assertTrue(data instanceof Reader);
		char[] buf = new char[5];
		int read = ((Reader)data).read(buf);
		assertEquals(5, read);
		assertEquals("hello", new String(buf));
	}


	@Test
	void testGetTransferData_unsupportedFlavor_throws() {
		ByteArrayTransferable bat = new ByteArrayTransferable(0, new byte[] { 1 });
		assertThrows(UnsupportedFlavorException.class,
				() -> bat.getTransferData(DataFlavor.imageFlavor));
	}


}
