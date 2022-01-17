package com.mpqdata.app.data.mpqdataloader.archive.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.github.luben.zstd.ZstdDirectBufferDecompressingStream;
import com.mpqdata.app.data.mpqdataloader.archive.MpqSarExtractorExcpetion;

public class CompressionUtils {

	public static final int COMPRESSION_TYPE_ZSTD = 0x4454535A;
	public static final int COMPRESSION_TYPE_LZ4C = 0x43345A4C;

	public static byte[] decompressZstd(byte[] compressedBytes, int compressedSize, byte[] dictionaryBytes) {
		System.out.println("zstd compression");
		ByteBuffer compressedBytesBuffer = ByteBufferFactory.wrap(compressedBytes);

		int dwHeaderSize = 8;

		@SuppressWarnings("unused")
		int dwSignature = compressedBytesBuffer.getInt(); //ZSTD marker

		int dwSize = compressedBytesBuffer.getInt();

		byte[] expandedBytes = new byte[dwSize];
		byte[] compressedDataBodyBytes = new byte[compressedSize - dwHeaderSize];
		compressedDataBodyBytes = Arrays.copyOfRange(compressedBytes, dwHeaderSize, dwSize);

		ByteBuffer compressedDataBodyBuffer = ByteBufferFactory.wrapDirect(compressedDataBodyBytes);
		ByteBuffer expandedDataBuffer = ByteBufferFactory.allocateDirect(dwSize);
		try (ZstdDirectBufferDecompressingStream zstdStream = new ZstdDirectBufferDecompressingStream(compressedDataBodyBuffer)) {
			if (dictionaryBytes != null) {
				zstdStream.setDict(dictionaryBytes);
			}

			zstdStream.read(expandedDataBuffer);
		} catch (IOException e) {
			throw new MpqSarExtractorExcpetion("Error decompressing with ZstdDirectBufferDecompressingStream", e);
		}
		expandedDataBuffer.flip();
		expandedDataBuffer.get(expandedBytes);

		return expandedBytes;
	}

	public static byte[] decompressZstd(byte[] compressedBytes, int compressedSize) {
		return decompressZstd(compressedBytes, compressedSize, null);
	}

}
