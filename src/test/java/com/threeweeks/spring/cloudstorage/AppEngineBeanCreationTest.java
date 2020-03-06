package com.threeweeks.spring.cloudstorage;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.HttpTransport;
import com.threeweeks.spring.cloudstorage.apiclient.GcsJsonApiClient;
import com.threeweeks.spring.cloudstorage.apiclient.LocalGcsJsonApiClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application.properties")
@SpringBootTest(classes = SpringGaeGcsConfiguration.class)
@ActiveProfiles(profiles = "gae")
public class AppEngineBeanCreationTest {

    @Autowired
    private GcsJsonApiClient apiClient;

    @Autowired
    private GcsJsonApiService apiService;

    @Autowired
    private HttpTransport httpTransport;

    @Test
    public void libraryShouldNotLoadTheLocalClient() {
        assertFalse(apiClient instanceof LocalGcsJsonApiClient);
    }

    @Test
    public void shouldSetTheDefaultValuesForAttachmentFolder() {
        assertThat(apiService.getGcsDefaultBucket(), is("testBucket"));
        assertThat(apiService.getDefaultAttachmentsFolder(), is("attachments"));
    }

    @Test
    public void httpTransportShouldDefaultToUrlFetch() {
        assertTrue(httpTransport instanceof UrlFetchTransport);
    }


}
