package com.mpqdata.app.data.mpqdataloader.archive;

import lombok.Data;

@Data
public class ArchiveTableEntry {

	private long offset;
	private long compressedSize;
	private long size;
	private long timeStamp;
	private int crc32Pre;
	private int crc32Post;
	private int fileNameLength;
	private String fileName;

}
