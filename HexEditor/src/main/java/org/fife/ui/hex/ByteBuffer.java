/*
 * Copyright (c) 2008 Robert Futrell
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name "HexEditor" nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.fife.ui.hex;

import java.io.*;
import java.nio.file.Files;


/**
 * Stores a dynamic number of bytes.
 */
public final class ByteBuffer {

	/**
	 * The byte buffer that contains the document content.
	 */
	private byte[] buffer;


	/**
	 * Creates an empty buffer of a given size.
	 *
	 * @param size The sie of the buffer.
	 */
	public ByteBuffer(int size) {
		buffer = new byte[size];
	}


	/**
	 * Creates a new buffer with the contents of a file.
	 *
	 * @param file The file to read.
	 * @throws IOException If an IO error occurs.
	 */
	public ByteBuffer(String file) throws IOException {
		this(new File(file));
	}


	/**
	 * Creates a new buffer with the contents of a file.
	 *
	 * @param file The file to read.
	 * @throws IOException If an IO error occurs.
	 */
	public ByteBuffer(File file) throws IOException {

		int size = (int)file.length();
		buffer = new byte[size];

		if (size>0) {
            try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
                int pos = 0;
                int count;
                while (pos < buffer.length &&
                        (count = in.read(buffer, pos, buffer.length - pos)) > -1) {
                    pos += count;
                }
            }
		}

	}


	/**
	 * Creates a buffer representing the contents read from an input stream.
	 *
	 * @param in The input stream to read from.
	 * @throws IOException If an IO error occurs.
	 */
	public ByteBuffer(InputStream in) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		buffer = new byte[4096]; // Use as a temporary buffer.
		int count;
		while ((count=in.read(buffer, 0,buffer.length))>-1) {
			baos.write(buffer, 0,count);
		}
		buffer = baos.toByteArray();
	}


	/**
	 * Returns a byte in this buffer.
	 *
	 * @param offset The offset of the byte.
	 * @return The byte.
	 */
	public byte getByte(int offset) {
		return buffer[offset];
	}


	/**
	 * Returns the size of this buffer.
	 *
	 * @return The size of this buffer.
	 */
	public int getSize() {
		return buffer.length;
	}


	/**
	 * Inserts a bytes this buffer.
	 *
	 * @param offset The offset to insert at.
	 * @param b The byte to insert.
	 * @see #insertBytes(int, byte[])
	 */
	public void insertByte(int offset, byte b) {
		byte[] buf2 = new byte[buffer.length+1];
		System.arraycopy(buffer,0, buf2,0, offset);
		buf2[offset] = b;
		System.arraycopy(buffer,offset, buf2,offset+1, buffer.length-offset);
		buffer = buf2;
	}


	/**
	 * Inserts bytes this buffer.
	 *
	 * @param offs The offset to insert at.
	 * @param b The bytes to insert. If this is {@code null} or an empty array, nothing happens.
	 * @see #insertByte(int, byte)
	 */
	public void insertBytes(int offs, byte[] b) {

		if (b==null || b.length==0) {
			return;
		}

		byte[] buf2 = new byte[buffer.length+b.length];
		System.arraycopy(buffer,0,    buf2,0,             offs);
		System.arraycopy(b,0,         buf2,offs,          b.length);
		System.arraycopy(buffer,offs, buf2,offs+b.length, buffer.length-offs);
		buffer = buf2;

	}


	/**
	 * Reads a region of bytes. The number of bytes read is
	 * {@code min(buf.length, number-of-bytes-from-offset-to-end)}.
	 *
	 * @param offset The starting offset of the bytes to read.
	 * @param buf The buffer to store the bytes in.
	 * @return The number of bytes read.
	 */
	public int read(int offset, byte[] buf) {
		if (buf==null) {
			return -1;
		}
		int count = Math.min(buf.length, getSize()-offset);
		System.arraycopy(buffer,offset, buf,0, count);
		return count;
	}


	/**
	 * Removes a region of bytes.
	 *
	 * @param offset The starting offset to remove.
	 * @param len The count of bytes to remove.
	 * @see #remove(int, int, byte[])
	 */
	public void remove(int offset, int len) {
		remove(offset, len, null);
	}


	/**
	 * Removes a region of bytes, optionally capturing what was removed.
	 *
	 * @param offset The starting offset to remove.
	 * @param len The count of bytes to remove.
	 * @param removed If non-{@code null} this is assumed to be large enough to contain the removed bytes.
	 *        The removed bytes are copied into this array. If {@code null}, the operation succeeds,
	 *        but removed bytes are simply not stored anywhere.
	 * @see #remove(int, int)
	 */
	public void remove(int offset, int len, byte[] removed) {
		if (removed!=null) {
			System.arraycopy(buffer,offset, removed,0, len);
		}
		byte[] buf = new byte[buffer.length-len];
		System.arraycopy(buffer,0, buf,0, offset);
		System.arraycopy(buffer,offset+len, buf,offset, buf.length-offset);
		buffer = buf;
	}


	/**
	 * Sets the value of a byte.
	 *
	 * @param offset The byte to set.
	 * @param b The new value.
	 */
	public void setByte(int offset, byte b) {
		buffer[offset] = b;
	}


}
