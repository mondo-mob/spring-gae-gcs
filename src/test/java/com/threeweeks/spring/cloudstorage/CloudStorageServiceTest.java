package com.threeweeks.spring.cloudstorage;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

@EnableAutoConfiguration
@ComponentScan("org.springframework.contrib.gae.storage")
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "app.id=my-test-app-id",
        "spring.contrib.gae.storage.bucket=my-test-bucket-name",
})
@Ignore("missing test credentials json file. fix later.")
public class CloudStorageServiceTest {

    @Autowired
    private CloudStorageService cloudStorageService;

    @Test
    public void cloudStorageConfiguration_willCreateCloudStorageServiceBean() {
        assertThat(cloudStorageService, not(nullValue()));
    }
}
