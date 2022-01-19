package com.mpqdata.app.data.mpqdataloader.exec;

import static com.mpqdata.app.data.mpqdataloader.exec.FetchAndExpandSarArchiveCommandLineRunner.EXPANDED_ARCHIVE_SUBDIR;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mpqdata.app.data.mpqdataloader.model.service.MpqCharcterFileSystemService;

@Order(1)
@Component
public class LoadDatabaseCommandLineRunner implements CommandLineRunner {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${mpq.config.download-dir}")
	private String downloadDir;

	@Autowired
	private MpqCharcterFileSystemService mpqCharcterFileSystemService;

	@Override
	public void run(String... args) throws Exception {
		logger.info("Database Load Process start");

		File dataDir = new File(downloadDir + "/" + EXPANDED_ARCHIVE_SUBDIR);
		File characterDir = new File(dataDir, "Characters");
		// Stream.of(dataDir.listFiles()).filter(f -> ! f.getName().contains("_")).forEach(System.out::println);
		mpqCharcterFileSystemService.loadCharacterConfigs(characterDir);


		logger.info("Database Load Process complete");
	}

}
