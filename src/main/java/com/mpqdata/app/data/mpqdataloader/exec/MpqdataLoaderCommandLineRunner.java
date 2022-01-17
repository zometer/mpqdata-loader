package com.mpqdata.app.data.mpqdataloader.exec;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mpqdata.app.data.mpqdataloader.archive.SarArchiveDownloader;
import com.mpqdata.app.data.mpqdataloader.model.service.SarUrlLookupService;

import lombok.Setter;

@Component
public class MpqdataLoaderCommandLineRunner implements CommandLineRunner {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${mpq.config.download-dir}")
	private String downloadDir;

	@Autowired
	@Setter
	private SarUrlLookupService sarUrlLookupService;

	@Autowired
	@Setter
	private SarArchiveDownloader sarArchiveDownloader;

	@Override
	public void run(String... args) throws Exception {
		logger.info("Process start");
		logger.info( sarUrlLookupService.getAppStoreUrl() );

		String configSarUrl = sarUrlLookupService.retrieveConfigSarUrl();
		logger.info("configSarUrl: " + configSarUrl);

		String path = downloadDir + "/" + configSarUrl.replaceAll("^.*/", "");
		File outputFile = new File(path);
		sarArchiveDownloader.downloadFile(configSarUrl, outputFile);

		logger.info("Process complete");
	}


}
