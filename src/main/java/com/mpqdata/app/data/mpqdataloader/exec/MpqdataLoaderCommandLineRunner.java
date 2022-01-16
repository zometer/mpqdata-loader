package com.mpqdata.app.data.mpqdataloader.spring.exec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mpqdata.app.data.mpqdataloader.model.service.SarUrlLookupService;

@Component
public class MpqdataLoaderCommandLineRunner implements CommandLineRunner {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private SarUrlLookupService sarUrlLookupService; 

	@Override
	public void run(String... args) throws Exception {
		logger.info("Process start");
		logger.info( sarUrlLookupService.getAppStoreUrl() );
		
		String configSarUrl = sarUrlLookupService.retrieveConfigSarUrl();
		logger.info("configSarUrl: " + configSarUrl);
		
		logger.info("Process complete");
	}

}
