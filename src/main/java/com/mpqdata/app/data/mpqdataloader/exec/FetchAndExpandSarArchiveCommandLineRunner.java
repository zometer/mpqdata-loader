package com.mpqdata.app.data.mpqdataloader.exec;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mpqdata.app.data.mpqdataloader.archive.ArchiveTableEntry;
import com.mpqdata.app.data.mpqdataloader.archive.SarArchiveDownloader;
import com.mpqdata.app.data.mpqdataloader.archive.SarArchiveExtractor;
import com.mpqdata.app.data.mpqdataloader.archive.SarArchiveMetadata;
import com.mpqdata.app.data.mpqdataloader.archive.SarArchiveReader;
import com.mpqdata.app.data.mpqdataloader.model.service.SarUrlLookupService;

import lombok.Setter;

@Order(0)
@Component
public class FetchAndExpandSarArchiveCommandLineRunner implements CommandLineRunner {

	public static final String EXPANDED_ARCHIVE_SUBDIR = "/expanded";

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${mpq.config.download-dir}")
	private String downloadDir;

	@Autowired
	@Setter
	private SarUrlLookupService sarUrlLookupService;

	@Autowired
	@Setter
	private SarArchiveDownloader sarArchiveDownloader;

	@Autowired
	@Setter
	private SarArchiveReader sarArchiveReader;

	@Autowired
	@Setter
	private SarArchiveExtractor sarArchiveExtractor;

	@Override
	public void run(String... args) throws Exception {
		logger.info("Process start");
		logger.info( sarUrlLookupService.getAppStoreUrl() );

		String configSarUrl = sarUrlLookupService.retrieveConfigSarUrl();
		logger.info("configSarUrl: " + configSarUrl);

		String path = downloadDir + "/" + configSarUrl.replaceAll("^.*/", "");
		File outputFile = new File(path);
		sarArchiveDownloader.downloadFile(configSarUrl, outputFile);

		RandomAccessFile archive = new RandomAccessFile(outputFile, "r");
		SarArchiveMetadata metadata = sarArchiveReader.extractMetaData(archive);
		logger.info("metadata: " + metadata);

		List<ArchiveTableEntry> tableEntries = sarArchiveReader.extractArchiveTableEntries(archive, metadata);
		File targetDir = new File(downloadDir + EXPANDED_ARCHIVE_SUBDIR);
		tableEntries.forEach(entry -> {
			logger.info("Entry: " + entry.getFileName() + " - " + entry.getCompressedSize() + " - " + entry.getSize() );
			sarArchiveExtractor.expandFileFromArchive(archive, metadata, entry, targetDir);
		});

		archive.close();

		logger.info("SAR Archive Fetch and Expand Process complete");
	}


}
