package com.mpqdata.app.data.mpqdataloader.model.service;

import static com.mpqdata.app.data.mpqdataloader.model.service.SarUrlLookupService.BUILD_CHANGELIST_HEADER;
import static com.mpqdata.app.data.mpqdataloader.model.service.SarUrlLookupService.BUILD_VERSION_HEADER;
import static com.mpqdata.app.data.mpqdataloader.model.service.SarUrlLookupService.CLIENT_PLATFORM_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.net.URISyntaxException;
import java.util.Map;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.mpqdata.app.data.mpqdataloader.MpqDataLoaderException;
import com.mpqdata.app.data.mpqdataloader.test.junit.ClassNameDisplayNameGenerator;

@DisplayNameGeneration(ClassNameDisplayNameGenerator.class)
@ExtendWith(MockitoExtension.class)
class SarUrlLookupServiceTests {
	
	private static final String APP_STORE_URL = "http://appstore.url/mpq";
	private static final String SELECTOR = "p.whats-new__latest__version";
	private static final String MPQ_VERSION = "244.0.591162"; 
	private static final String EXPECTED_CONFIG_SAR_URL = "https://cdn.url/path/to/config.sar";
	private static final String APP_STORE_HTML = "<html> <body>  <p class=\"whats-new__latest__version\">Version " + MPQ_VERSION + "</p> </body> </html>";
	
	@Mock
	RestTemplate restTemplate;
	
	@Mock 
	ResponseEntity<String> appStoreResponseEntity;
	
	@Mock 
	RequestEntity<String> requestEntity ; 
	
	@Mock
	ResponseEntity<Map<String, String>> stringMapResponseEntity; 
	
	@Mock
	HttpHeaders httpHeaders ; 
	
	@Nested 
	class RetrieveMpqVersionFromAppStoreWithString { 
		
		@Test
		void looksUpMpqInAppStoreUrlAndReturnsVersion() {
			doReturn(appStoreResponseEntity).when(restTemplate).getForEntity(APP_STORE_URL, String.class); 
			doReturn(APP_STORE_HTML).when(appStoreResponseEntity).getBody(); 
			
			SarUrlLookupService service = new SarUrlLookupService(); 
			service.setRestTemplate(restTemplate);			
			service.setAppStoreUrl(APP_STORE_URL);
			service.setAppStoreVersionHtmlSelector(SELECTOR);
			
			String version = service.retrieveMpqVersionFromAppStore(APP_STORE_URL); 
			
			assertNotNull(version, "Null version returned");
			assertEquals(MPQ_VERSION, version, "mismatched MPQ version from app store"); 
		}
		
	}
	
	@Nested
	class CreateHttpHeadersWithString { 
		
		@Test 
		void addsCustomHeadersBasedOnMpqVersion() { 
			String major = "99"; 
			String minor = "1"; 
			String patch = "5555"; 
			
			String mpqVersion = major + "." + minor + "." + patch;
			String buildVersion = major + "." + minor; 
			
			SarUrlLookupService service = new SarUrlLookupService(); 
			
			HttpHeaders headers = service.createHttpHeaders(mpqVersion); 
			
			assertNotNull(headers, "null headers"); 
			assertEquals(buildVersion, headers.get(BUILD_VERSION_HEADER).get(0), "mismatched header value: " + BUILD_VERSION_HEADER );
			assertEquals("IOS", headers.get(CLIENT_PLATFORM_HEADER).get(0), "mismatched header value: " + CLIENT_PLATFORM_HEADER );
			assertEquals("CL" + patch, headers.get(BUILD_CHANGELIST_HEADER).get(0), "mismatched header value: " + BUILD_CHANGELIST_HEADER );
		}
		
	}
	
	@Nested 
	class NewRequestEntityWithHttpHeadersAndString { 
		
		@Test 
		void returnsRequestEntityWithUrlAndParameterizedTypeReference() {
			HttpHeaders httpHeaders = new HttpHeaders(); 
		    httpHeaders.add("foo", "bar");
			
			SarUrlLookupService service = new SarUrlLookupService(); 
			
			RequestEntity<String> request = service.newRequestEntity(httpHeaders, EXPECTED_CONFIG_SAR_URL);
			
			assertNotNull(request, "null request"); 
			assertNotSame(httpHeaders, request.getHeaders(), "Expected a new copy of the headers"); 
			httpHeaders.entrySet().forEach( (entry) -> {
				assertEquals(entry.getValue(), request.getHeaders().get(entry.getKey()));
			});
			assertEquals(EXPECTED_CONFIG_SAR_URL, request.getUrl().toString(), "Mismatched request URL");
			assertEquals(HttpMethod.GET, request.getMethod(), "Mismatched HttpMethod");
		}		
		
		@Test 
		void throwsMpqDataLoaderExceptionWhenPassedMalformedUrl() { 
			SarUrlLookupService service = new SarUrlLookupService(); 
			
			MpqDataLoaderException e = assertThrows(MpqDataLoaderException.class, () -> service.newRequestEntity(httpHeaders, "bad url"));
			assertInstanceOf(URISyntaxException.class, e.getCause(), "Unexpected cause type"); 
		}		
		
	}

	@Nested
	class RetrieveConfigSarUrl { 
		
		@Test
		void addsHeadersToRequestAndCallsRemoteApi() { 
			
			ParameterizedTypeReference<Map<String, String>> reference = new ParameterizedTypeReference<Map<String,String>>() {};
			
			SarUrlLookupService spyService = spy(SarUrlLookupService.class);
			spyService.setAppStoreUrl(APP_STORE_URL);
			spyService.setMpqUpdateSarLookupUrl(EXPECTED_CONFIG_SAR_URL);
			spyService.setRestTemplate(restTemplate);
			
			doReturn(MPQ_VERSION).when(spyService).retrieveMpqVersionFromAppStore(APP_STORE_URL); 
			doReturn(httpHeaders).when(spyService).createHttpHeaders(MPQ_VERSION);
			doReturn(requestEntity).when(spyService).newRequestEntity(httpHeaders, EXPECTED_CONFIG_SAR_URL); 
			doReturn(stringMapResponseEntity).when(restTemplate).exchange(same(requestEntity), eq(reference) );
			doReturn(Map.of("config_url", EXPECTED_CONFIG_SAR_URL)).when(stringMapResponseEntity).getBody(); 
			
			String configSarUrl = spyService.retrieveConfigSarUrl();
			
			assertNotNull(configSarUrl, "Null config sar URL");
			assertEquals(EXPECTED_CONFIG_SAR_URL, configSarUrl, "mismatched config sar url"); 			
		}
		
	}


}
