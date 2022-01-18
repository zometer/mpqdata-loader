package com.mpqdata.app.data.mpqdataloader.archive;

import static com.mpqdata.app.data.mpqdataloader.archive.util.CompressionUtils.*;
import static com.mpqdata.app.data.mpqdataloader.archive.util.EncryptionUtils.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mpqdata.app.data.mpqdataloader.archive.util.ByteBufferFactory;
import com.mpqdata.app.data.mpqdataloader.archive.util.CompressionUtils;
import com.mpqdata.app.data.mpqdataloader.archive.util.EncryptionUtils;

@Component
public class SarArchiveExtractor {

	private static final List<String> ZSTD_DICTIONARY_FILE_NAMES = Arrays.asList("pkgcdict_pc.dat", "pkgcdict_ios.dat", "pkgcdict_android.dat");

	private Logger logger = LoggerFactory.getLogger(getClass());

	public void expandFileFromArchive(RandomAccessFile archive, SarArchiveMetadata metadata, ArchiveTableEntry entry, File targetDir) {
		File outputFile = new File(targetDir, entry.getFileName());
		outputFile.getParentFile().mkdirs();

		try {
			FileChannel channel = archive.getChannel();
			byte[] compressedBytes = new byte[ (int) entry.getCompressedSize() ];
			ByteBuffer compressedByteBuffer = ByteBufferFactory.allocate((int) entry.getCompressedSize());

			channel.position( entry.getOffset() );
			channel.read(compressedByteBuffer);
			compressedByteBuffer.flip();
			compressedByteBuffer.get(compressedBytes);

			logger.info("    [RAW BYTES]: " + EncryptionUtils.md5CheckSum(compressedBytes) );
			decrypt(metadata, entry, compressedBytes);

			if (entry.getSize() <= 40) {
				logger.info("    [SAVE] small (compressedSize, size): " + entry.getCompressedSize() + ", " + entry.getSize());
				Files.write(outputFile.toPath(), compressedBytes);
				return;
			}

			if (entry.getCompressedSize() != entry.getSize() && metadata.getZstdDictionaryBytes() == null) {
				expandAndHandleDictionary(metadata, entry, outputFile, compressedBytes);
				return;
			}

			expandAndWriteFile(metadata, entry, outputFile, compressedBytes);

		} catch (IOException e) {
			throw new MpqSarExtractorExcpetion("Error extracting file from archive: " + entry.getFileName(), e);
		}
	}

	public void decrypt(SarArchiveMetadata metadata, ArchiveTableEntry entry, byte[] bytes) {
		String[] pathElems = entry.getFileName().split("/");
		String filenameOnly = pathElems[pathElems.length - 1];
		String windowsFileName = String.join("\\", pathElems); // Ooof... this one was a pain to find. Stupid windows...
		String extension = filenameOnly.replaceAll("^.*(\\..+)$", "$1");

		int dwEncryptionSeed = 0;

		if (extension.equals(".lbc") ) {
			logger.info("    [DECRYPT]: ENCRYPTION_HASH_SCRIPTS - " + ENCRYPTION_HASH_SCRIPTS );
			dwEncryptionSeed = EncryptionUtils.hash(filenameOnly.replaceAll(extension, ""), ENCRYPTION_HASH_SCRIPTS);
			logger.info("    [DECRYPT]: dwEncryptionSeed - " + dwEncryptionSeed );
			EncryptionUtils.decrypt(bytes, dwEncryptionSeed, (int) entry.getCompressedSize());
		} else if (extension.equals(".json") || extension.equals(".dat") || extension.equals(".pem") ) {
			logger.info("    [DECRYPT]: start - " + EncryptionUtils.md5CheckSum(bytes) );
			logger.info("    [DECRYPT]: ENCRYPTION_HASH_MAIN - " + ENCRYPTION_HASH_MAIN );
			dwEncryptionSeed = EncryptionUtils.hash(windowsFileName.toLowerCase(), ENCRYPTION_HASH_MAIN);
			logger.info("    [DECRYPT]: dwEncryptionSeed - " + dwEncryptionSeed);
			EncryptionUtils.decrypt(bytes, dwEncryptionSeed, (int) entry.getCompressedSize());
			logger.info("    [DECRYPT]: finish - " + EncryptionUtils.md5CheckSum(bytes));
		}
	}

	private void expandAndHandleDictionary(SarArchiveMetadata metadata, ArchiveTableEntry entry, File outputFile, byte[] compressedBytes) throws IOException {
		byte[] expandedBytes = CompressionUtils.decompressZstd( compressedBytes, (int) entry.getSize() );
		logger.info("    [ZSTD]: decompress " + entry.getFileName());

		if (ZSTD_DICTIONARY_FILE_NAMES.contains( entry.getFileName().toLowerCase() ) ) {
			byte[] dictionaryBytes = Arrays.copyOf(expandedBytes, expandedBytes.length);
			metadata.setZstdDictionaryBytes(dictionaryBytes);
		}

		logger.info("    [SAVE]: " + EncryptionUtils.md5CheckSum(expandedBytes));
		Files.write(outputFile.toPath(), expandedBytes);
	}

	private void expandAndWriteFile(SarArchiveMetadata metadata, ArchiveTableEntry entry, File outputFile, byte[] compressedBytes) {

		try {
			ByteBuffer compressedBuffer = ByteBufferFactory.wrap(compressedBytes);
			int compressionType = compressedBuffer.getInt();

			if (COMPRESSION_TYPE_ZSTD == compressionType) {
				expandZstdAndWriteFile(metadata, entry, outputFile, compressedBytes);
				return;
			}

			if (COMPRESSION_TYPE_LZ4C == compressionType) {
				expandLz4AndWriteFile(metadata, entry, outputFile, compressedBytes);
				return;
			}

			logger.info("    [SAVE]: NO COMPRESSION - " + EncryptionUtils.md5CheckSum(compressedBytes));
			Files.write(outputFile.toPath(), compressedBytes);
		} catch (IOException e) {
			throw new MpqSarExtractorExcpetion("Error expanding file from archive: " + entry.getFileName(), e);
		}

	}

	private void expandZstdAndWriteFile(SarArchiveMetadata metadata, ArchiveTableEntry entry, File outputFile, byte[] compressedBytes)
			throws IOException
	{
		logger.info("    [ZSTD] start: " + EncryptionUtils.md5CheckSum(compressedBytes));
		byte[] expandedBytes = new byte[ (int) entry.getSize() ];

		if (metadata.getZstdDictionaryBytes() != null) {
			logger.info("    [ZSTD]: decompress with dictionary");
			expandedBytes = CompressionUtils.decompressZstd(compressedBytes, (int) entry.getSize(), metadata.getZstdDictionaryBytes());
		} else {
			logger.info("    [ZSTD]: decompress NO DICT");
			expandedBytes = CompressionUtils.decompressZstd(compressedBytes, (int) entry.getSize());
		}

		logger.info("    [SAVE]: " + EncryptionUtils.md5CheckSum(expandedBytes));
		Files.write(outputFile.toPath(), expandedBytes);
	}

	private void expandLz4AndWriteFile(SarArchiveMetadata metadata, ArchiveTableEntry entry, File outputFile, byte[] compressedBytes)
			throws IOException
	{
		byte[] expandedBytes = new byte[ (int) entry.getSize() ];
		logger.info("    [LZ4C]: decompress");
		expandedBytes = CompressionUtils.decompressLz4(compressedBytes, (int) entry.getCompressedSize());

		logger.info("    [SAVE]: " + EncryptionUtils.md5CheckSum(expandedBytes));
		Files.write(outputFile.toPath(), expandedBytes);
	}

}
