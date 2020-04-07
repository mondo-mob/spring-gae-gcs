package com.mondomob.spring.cloudstorage;


import com.mondomob.spring.cloudstorage.apiclient.GcsJsonApiClient;
import com.mondomob.spring.cloudstorage.apiclient.LocalGcsJsonApiClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SpringGaeGcsAutoConfiguration.class}, properties = {
        "gcs.dev-credentials-file=/test_dev_credentials.json",
        "gcs.default-bucket=test-bucket",
        "app.id=some-app"
})
@ActiveProfiles(profiles = "local")
public class LocalProfileBeanCreationDevCredentialsTest {

    @Autowired
    private GcsJsonApiClient apiClient;

    @Test
    public void libraryShouldLoadLocalClient_whenCredentialsConfigured() {
        assertTrue(apiClient instanceof LocalGcsJsonApiClient);
    }

}
