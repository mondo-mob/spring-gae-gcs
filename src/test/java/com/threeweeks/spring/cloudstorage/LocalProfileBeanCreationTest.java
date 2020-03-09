package com.threeweeks.spring.cloudstorage;


import com.threeweeks.spring.cloudstorage.apiclient.GcsJsonApiClient;
import com.threeweeks.spring.cloudstorage.apiclient.LocalGcsJsonApiClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertFalse;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SpringGaeGcsAutoConfiguration.class}, properties = {
        "gcs.default-bucket=test-bucket",
        "app.id=some-app"
})
@ActiveProfiles(profiles = "local")
public class LocalProfileBeanCreationTest {

    @Autowired
    private GcsJsonApiClient apiClient;

    @Test
    public void libraryShouldNotLoadLocalClient_whenCredentialsFileNotConfiguredOrPresent() {
        assertFalse(apiClient instanceof LocalGcsJsonApiClient);
    }

}
