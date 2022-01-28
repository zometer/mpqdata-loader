package com.mpqdata.app.data.mpqdataloader.exec;

import static com.mpqdata.app.data.mpqdataloader.model.Constants.*;
import java.util.stream.Stream;

import org.assertj.core.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.Setter;

@Order(0)
@Component
public class BuildInfoCommandLineRunner implements CommandLineRunner {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	@Setter
	private Environment environment;

	@Autowired
	@Setter
	private BuildProperties buildProperties;

	@Override
	public void run(String... args) throws Exception {
		logger.info("Active Profiles: " + Arrays.asList(environment.getActiveProfiles()) );
		logger.info("Build Info: " );
		Stream.of("group", "artifact", "version", "name").forEachOrdered( k -> logger.info("    " + k + ": " + buildProperties.get(k)) );
		logger.info("    time: " + DATE_FORMAT.format( buildProperties.getTime().toEpochMilli() ) );
	}

}
