package com.mpqdata.app.data.mpqdataloader.spring.exec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MpqdataLoaderCommandLineRunner implements CommandLineRunner {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void run(String... args) throws Exception {
		logger.info("Process start");
		
		logger.info("Process complete");
	}

}
