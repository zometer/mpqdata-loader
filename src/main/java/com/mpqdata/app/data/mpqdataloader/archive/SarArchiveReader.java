package com.mpqdata.app.data.mpqdataloader.archive;

import static com.mpqdata.app.data.mpqdataloader.archive.util.EncryptionUtils.ENCRYPTION_HASH_MAIN;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mpqdata.app.data.mpqdataloader.archive.util.ByteBufferFactory;
import com.mpqdata.app.data.mpqdataloader.archive.util.CompressionUtils;
import com.mpqdata.app.data.mpqdataloader.archive.util.EncryptionUtils;

@Component
public class SarArchiveReader {

	public SarArchiveMetadata extractMetaData(RandomAccessFile archive) {
		FileChannel channel = archive.getChannel();

		ByteBuffer buffer = ByteBufferFactory.allocate(1024);
		try {
			channel.read(buffer);
		} catch (IOException e) {
			throw new MpqSarExtractorExcpetion("Error reading archive metadata", e);
		}
		buffer.flip();

		SarArchiveMetadata metadata = new SarArchiveMetadata();

		metadata.setMagicNumber( buffer.getInt() );
		metadata.setVersion( buffer.getInt() );
		metadata.setArchiveSize( buffer.getLong() );
		metadata.setTableOffset( buffer.getLong() );
		metadata.setTotalFiles( buffer.getInt() );
		metadata.setGameDirectory( buffer.getInt() );
		metadata.setTableCompressedSize( buffer.getInt() );
		metadata.setBuildVersionMinor( buffer.getShort() );
		metadata.setBuildVersionMajor( buffer.getShort() );
		metadata.setBuildChangeList( buffer.getInt() );
		metadata.setDirectoryQueries( buffer.getShort() );
		metadata.setObfuscated( buffer.get() );
		metadata.setPlatform( buffer.get() );

		String encryptionKey = String.format("%s%s", metadata.getBuildVersionMinor() + metadata.getBuildVersionMajor(), metadata.getBuildChangeList());
		metadata.setEncryptionKey(encryptionKey);

		return metadata;
	}

	public List<ArchiveTableEntry> extractArchiveTableEntries(RandomAccessFile archive, SarArchiveMetadata metadata) {
		List<ArchiveTableEntry> entries = new ArrayList<>();

		FileChannel channel = archive.getChannel();

		byte[] compressedEntryTableBytes = null;
		ByteBuffer compressedEntryTableBuffer = null;

		try {
			channel.position( metadata.getTableOffset() );
			compressedEntryTableBytes = new byte[metadata.getTableCompressedSize()];
			compressedEntryTableBuffer = ByteBufferFactory.allocate( metadata.getTableCompressedSize() );
			channel.read(compressedEntryTableBuffer);
		} catch (IOException e) {
			throw new MpqSarExtractorExcpetion("Error reading archive table of content", e);
		}
		compressedEntryTableBuffer.flip();
		compressedEntryTableBuffer.get(compressedEntryTableBytes);

		// byte[] archiveZstdDictBytes = null;
		int encryptionSeed = EncryptionUtils.hash(metadata.getEncryptionKey().toLowerCase(), ENCRYPTION_HASH_MAIN);
		EncryptionUtils.decrypt(compressedEntryTableBytes, encryptionSeed, metadata.getTableCompressedSize());

		byte[] entryTableBytes = CompressionUtils.decompressZstd(compressedEntryTableBytes, metadata.getTableCompressedSize());

		ByteBuffer entryTableBuffer = ByteBufferFactory.wrap(entryTableBytes);

		for (int f=0; f < metadata.getTotalFiles(); f++) {
			ArchiveTableEntry entry = readArchiveTableEntry(entryTableBuffer);
			entries.add(entry);
		}

		return entries;
	}

	private ArchiveTableEntry readArchiveTableEntry(ByteBuffer buffer) {
		ArchiveTableEntry entry = new ArchiveTableEntry();
		entry.setOffset( buffer.getLong() );
		entry.setCompressedSize( buffer.getLong() );
		entry.setSize( buffer.getLong() );
		entry.setTimeStamp( buffer.getLong() );
		entry.setCrc32Pre( buffer.getInt() );
		entry.setCrc32Post( buffer.getInt() );
		entry.setFileNameLength( buffer.getInt() );

		byte[] fileNameBytes = new byte[entry.getFileNameLength()];
		buffer.get(fileNameBytes);
		fileNameBytes = Arrays.copyOfRange(fileNameBytes, 0, fileNameBytes.length - 1);

		entry.setFileName( new String(fileNameBytes).replaceAll("\\\\", File.separator) );

		return entry;
	}

}
