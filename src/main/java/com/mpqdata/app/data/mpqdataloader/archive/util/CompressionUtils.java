package com.mpqdata.app.data.mpqdataloader.archive.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.luben.zstd.ZstdDirectBufferDecompressingStream;
import com.mpqdata.app.data.mpqdataloader.archive.MpqSarExtractorExcpetion;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

public class CompressionUtils {

	private static Logger logger = LoggerFactory.getLogger(CompressionUtils.class);

	public static final int COMPRESSION_TYPE_ZSTD = 0x4454535A;
	public static final int COMPRESSION_TYPE_LZ4C = 0x43345A4C;

	public static byte[] decompressZstd(byte[] compressedBytes, int compressedSize, byte[] dictionaryBytes) {
		logger.trace("zstd compression");
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

	public static byte[] decompressLz4(byte[] compressedBytes, int compressedSize) {
		logger.trace("lz4 compression");
		ByteBuffer compressedBuffer = ByteBufferFactory.wrap(compressedBytes);
		compressedBuffer.flip();

		int headerSize = 8;
		@SuppressWarnings("unused")
		int signature = compressedBuffer.getInt(); //LZ4C
		int size = compressedBuffer.getInt();

		// Compressed Data body
		byte[] bytesToExpand = Arrays.copyOfRange(compressedBytes, headerSize, compressedSize - headerSize);

		LZ4Factory factory = LZ4Factory.fastestInstance();
		LZ4FastDecompressor decompressor = factory.fastDecompressor();
		byte[] restored = new byte[size];
		int numBytesRead = decompressor.decompress(bytesToExpand, 0, restored, 0, size);
		// compressedLength == compressedLength2

		if (numBytesRead != compressedSize) {
			throw new MpqSarExtractorExcpetion("compressed length from LZ4Decompressor (" + numBytesRead + ") does not match the given compressed length (" + compressedSize + ")");
		}

		return restored;

	}


}
