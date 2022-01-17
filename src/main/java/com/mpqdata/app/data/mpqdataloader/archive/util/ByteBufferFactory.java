package com.mpqdata.app.data.mpqdataloader.archive.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteBufferFactory {

	public static ByteBuffer allocate(int size) {
		ByteBuffer buffer = ByteBuffer.allocate(size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer;
	}

	public static ByteBuffer allocateDirect(int size) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer;
	}

	public static ByteBuffer wrap(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer;
	}

	public static ByteBuffer wrapDirect(byte[] bytes) {
		ByteBuffer buffer = allocateDirect( bytes.length );
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(bytes);
		buffer.flip();

		return buffer;
	}

}
