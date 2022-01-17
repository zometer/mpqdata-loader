package com.mpqdata.app.data.mpqdataloader.archive;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import com.mpqdata.app.data.mpqdataloader.MpqDataLoaderException;

@Component
public class SarArchiveDownloader {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private RestTemplate restTemplate;

	public void downloadFile(String urlString, File outputFile) {
		outputFile.getParentFile().mkdirs();
		try {
			URI uri = new URI(urlString);
			logger.info("Downloading " + uri);

			restTemplate.execute(uri, HttpMethod.GET, null, (response) -> {
				StreamUtils.copy(response.getBody(), new FileOutputStream(outputFile));
				return outputFile;
			});

			logger.info("Downloaded to " + outputFile);
		} catch (URISyntaxException e) {
			throw new MpqDataLoaderException("Error downloading " + urlString + " to " + outputFile, e);
		}
	}

}
