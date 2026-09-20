package org.fife.ui.hex;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Unit tests for {@link ByteBuffer}.
 */
class ByteBufferTest {

	@TempDir
	private File tempDir;


	@Test
	void testConstructor_size() {
		ByteBuffer buffer = new ByteBuffer(10);
		assertEquals(10, buffer.getSize());
		for (int i = 0; i < 10; i++) {
			assertEquals(0, buffer.getByte(i));
		}
	}


	@Test
	void testConstructor_size_zero() {
		ByteBuffer buffer = new ByteBuffer(0);
		assertEquals(0, buffer.getSize());
	}


	@Test
	void testConstructor_file() throws IOException {
		byte[] content = { 1, 2, 3, 4, 5 };
		File file = writeTempFile(content);
		ByteBuffer buffer = new ByteBuffer(file);
		assertEquals(content.length, buffer.getSize());
		assertContentEquals(content, buffer);
	}


	@Test
	void testConstructor_file_empty() throws IOException {
		File file = writeTempFile(new byte[0]);
		ByteBuffer buffer = new ByteBuffer(file);
		assertEquals(0, buffer.getSize());
	}


	@Test
	void testConstructor_file_nonExistent_yieldsEmptyBuffer() throws IOException {
		// File.length() returns 0 for a nonexistent file, so no read is
		// ever attempted and no exception is thrown.
		File file = new File(tempDir, "does-not-exist.bin");
		ByteBuffer buffer = new ByteBuffer(file);
		assertEquals(0, buffer.getSize());
	}


	@Test
	void testConstructor_file_directory_throwsIOException() {
		// Directories generally report a nonzero length, so an attempt is
		// made to open a FileInputStream on one, which fails.
		assertThrows(IOException.class, () -> new ByteBuffer(tempDir));
	}


	@Test
	void testConstructor_fileName() throws IOException {
		byte[] content = { 9, 8, 7 };
		File file = writeTempFile(content);
		ByteBuffer buffer = new ByteBuffer(file.getAbsolutePath());
		assertEquals(content.length, buffer.getSize());
		assertContentEquals(content, buffer);
	}


	@Test
	void testConstructor_inputStream() throws IOException {
		byte[] content = { 10, 20, 30, 40 };
		ByteBuffer buffer = new ByteBuffer(new ByteArrayInputStream(content));
		assertEquals(content.length, buffer.getSize());
		assertContentEquals(content, buffer);
	}


	@Test
	void testConstructor_inputStream_empty() throws IOException {
		ByteBuffer buffer = new ByteBuffer(new ByteArrayInputStream(new byte[0]));
		assertEquals(0, buffer.getSize());
	}


	@Test
	void testConstructor_inputStream_largerThanInternalBufferSize() throws IOException {
		// The implementation reads in 4096-byte chunks, so verify a stream
		// that requires multiple reads is handled correctly.
		byte[] content = new byte[10_000];
		for (int i = 0; i < content.length; i++) {
			content[i] = (byte)(i % 256);
		}
		ByteBuffer buffer = new ByteBuffer(new ByteArrayInputStream(content));
		assertEquals(content.length, buffer.getSize());
		assertContentEquals(content, buffer);
	}


	@Test
	void testGetByte_and_setByte() {
		ByteBuffer buffer = new ByteBuffer(3);
		buffer.setByte(1, (byte)42);
		assertEquals(0, buffer.getByte(0));
		assertEquals(42, buffer.getByte(1));
		assertEquals(0, buffer.getByte(2));
	}


	@Test
	void testInsertByte_atStart() {
		ByteBuffer buffer = new ByteBuffer(new byte[] { 1, 2, 3 }.length);
		for (int i = 0; i < 3; i++) {
			buffer.setByte(i, (byte)(i + 1));
		}
		buffer.insertByte(0, (byte)99);
		assertEquals(4, buffer.getSize());
		assertArrayEquals(new byte[] { 99, 1, 2, 3 }, toArray(buffer));
	}


	@Test
	void testInsertByte_inMiddle() {
		ByteBuffer buffer = fromArray(new byte[] { 1, 2, 3 });
		buffer.insertByte(1, (byte)99);
		assertEquals(4, buffer.getSize());
		assertArrayEquals(new byte[] { 1, 99, 2, 3 }, toArray(buffer));
	}


	@Test
	void testInsertByte_atEnd() {
		ByteBuffer buffer = fromArray(new byte[] { 1, 2, 3 });
		buffer.insertByte(3, (byte)99);
		assertEquals(4, buffer.getSize());
		assertArrayEquals(new byte[] { 1, 2, 3, 99 }, toArray(buffer));
	}


	@Test
	void testInsertBytes_null() {
		ByteBuffer buffer = fromArray(new byte[] { 1, 2, 3 });
		buffer.insertBytes(1, null);
		assertEquals(3, buffer.getSize());
		assertArrayEquals(new byte[] { 1, 2, 3 }, toArray(buffer));
	}


	@Test
	void testInsertBytes_empty() {
		ByteBuffer buffer = fromArray(new byte[] { 1, 2, 3 });
		buffer.insertBytes(1, new byte[0]);
		assertEquals(3, buffer.getSize());
		assertArrayEquals(new byte[] { 1, 2, 3 }, toArray(buffer));
	}


	@Test
	void testInsertBytes_middle() {
		ByteBuffer buffer = fromArray(new byte[] { 1, 2, 3 });
		buffer.insertBytes(1, new byte[] { 10, 11 });
		assertEquals(5, buffer.getSize());
		assertArrayEquals(new byte[] { 1, 10, 11, 2, 3 }, toArray(buffer));
	}


	@Test
	void testInsertBytes_atStart() {
		ByteBuffer buffer = fromArray(new byte[] { 1, 2, 3 });
		buffer.insertBytes(0, new byte[] { 10, 11 });
		assertEquals(5, buffer.getSize());
		assertArrayEquals(new byte[] { 10, 11, 1, 2, 3 }, toArray(buffer));
	}


	@Test
	void testInsertBytes_atEnd() {
		ByteBuffer buffer = fromArray(new byte[] { 1, 2, 3 });
		buffer.insertBytes(3, new byte[] { 10, 11 });
		assertEquals(5, buffer.getSize());
		assertArrayEquals(new byte[] { 1, 2, 3, 10, 11 }, toArray(buffer));
	}


	@Test
	void testRead_fullBuffer() {
		ByteBuffer buffer = fromArray(new byte[] { 1, 2, 3, 4 });
		byte[] out = new byte[4];
		int count = buffer.read(0, out);
		assertEquals(4, count);
		assertArrayEquals(new byte[] { 1, 2, 3, 4 }, out);
	}


	@Test
	void testRead_partial_nearEnd() {
		ByteBuffer buffer = fromArray(new byte[] { 1, 2, 3, 4 });
		byte[] out = new byte[4];
		int count = buffer.read(2, out);
		assertEquals(2, count);
		assertArrayEquals(new byte[] { 3, 4, 0, 0 }, out);
	}


	@Test
	void testRead_nullBuffer() {
		ByteBuffer buffer = fromArray(new byte[] { 1, 2, 3 });
		assertEquals(-1, buffer.read(0, null));
	}


	@Test
	void testRemove_withoutCapturing() {
		ByteBuffer buffer = fromArray(new byte[] { 1, 2, 3, 4, 5 });
		buffer.remove(1, 2);
		assertEquals(3, buffer.getSize());
		assertArrayEquals(new byte[] { 1, 4, 5 }, toArray(buffer));
	}


	@Test
	void testRemove_capturingRemovedBytes() {
		ByteBuffer buffer = fromArray(new byte[] { 1, 2, 3, 4, 5 });
		byte[] removed = new byte[2];
		buffer.remove(1, 2, removed);
		assertEquals(3, buffer.getSize());
		assertArrayEquals(new byte[] { 2, 3 }, removed);
		assertArrayEquals(new byte[] { 1, 4, 5 }, toArray(buffer));
	}


	@Test
	void testRemove_entireBuffer() {
		ByteBuffer buffer = fromArray(new byte[] { 1, 2, 3 });
		buffer.remove(0, 3);
		assertEquals(0, buffer.getSize());
	}


	@Test
	void testRemove_atEnd() {
		ByteBuffer buffer = fromArray(new byte[] { 1, 2, 3, 4, 5 });
		buffer.remove(3, 2);
		assertEquals(3, buffer.getSize());
		assertArrayEquals(new byte[] { 1, 2, 3 }, toArray(buffer));
	}


	private File writeTempFile(byte[] content) throws IOException {
		File file = new File(tempDir, "test-" + System.nanoTime() + ".bin");
		Files.write(file.toPath(), content);
		return file;
	}


	private static ByteBuffer fromArray(byte[] content) {
		ByteBuffer buffer = new ByteBuffer(content.length);
		for (int i = 0; i < content.length; i++) {
			buffer.setByte(i, content[i]);
		}
		return buffer;
	}


	private static byte[] toArray(ByteBuffer buffer) {
		byte[] out = new byte[buffer.getSize()];
		for (int i = 0; i < out.length; i++) {
			out[i] = buffer.getByte(i);
		}
		return out;
	}


	private static void assertContentEquals(byte[] expected, ByteBuffer buffer) {
		assertArrayEquals(expected, toArray(buffer));
	}


}
