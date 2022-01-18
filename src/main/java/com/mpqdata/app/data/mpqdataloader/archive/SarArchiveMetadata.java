package com.mpqdata.app.data.mpqdataloader.archive;

import lombok.Data;

@Data
public class SarArchiveMetadata {

	private boolean dictionaryPresent = false;
	private long encryptionSeed = 0;
	private int magicNumber ;
	private int version;
	private long archiveSize;
	private long tableOffset;
	private int totalFiles;
	private int gameDirectory;
	private int tableCompressedSize;
	private int buildVersionMinor;
	private int buildVersionMajor;
	private int buildChangeList;
	private int directoryQueries;
	private byte obfuscated;
	private byte platform;

	private String encryptionKey;
	private byte[] zstdDictionaryBytes;
}
