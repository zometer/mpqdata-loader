package com.mpqdata.app.data.mpqdataloader.exec;

import static com.mpqdata.app.data.mpqdataloader.exec.DownloadAndExpandSarArchiveCommandLineRunner.EXPANDED_ARCHIVE_SUBDIR;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mpqdata.app.data.mpqdataloader.model.service.CharacterCoverService;
import com.mpqdata.app.data.mpqdataloader.model.service.LocaleTextService;
import com.mpqdata.app.data.mpqdataloader.model.service.MpqCharcterFileSystemService;

import lombok.Setter;

@Order(2)
@Profile("load-database")
@Component
public class LoadDatabaseCommandLineRunner implements CommandLineRunner {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${mpq.load.download-dir}")
	private String downloadDir;

	@Value("#{'${mpq.load.languages}'.split(',')}")
	@Setter
	private List<String> languages;

	@Autowired
	@Setter
	private MpqCharcterFileSystemService mpqCharcterFileSystemService;

	@Autowired
	@Setter
	private LocaleTextService localeTextService;

	@Autowired
	@Setter
	private CharacterCoverService characterCoverService;

	@Override
	public void run(String... args) throws Exception {
		logger.info("Database Load Process start");

		File dataDir = new File(downloadDir + "/" + EXPANDED_ARCHIVE_SUBDIR);
		File characterDir = new File(dataDir, "Characters");
		File localeDir = new File(dataDir, "Loc");
		mpqCharcterFileSystemService.loadCharacterConfigs(characterDir);

		localeTextService.setLanguages(languages);
		localeTextService.loadLocaleText(localeDir);

		characterCoverService.insertForNewCharacters();
		characterCoverService.loadCharacterCovers(dataDir);

		logger.info("Database Load Process complete");
	}

}
