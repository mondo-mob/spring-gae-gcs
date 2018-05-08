package com.threeweeks.spring.cloudstorage;


import com.threeweeks.spring.cloudstorage.apiclient.GcsJsonApiClient;
import com.threeweeks.spring.cloudstorage.apiclient.LocalGcsJsonApiClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Properties;

import static junit.framework.TestCase.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PropertiesConfig.class, SpringGaeGcsConfiguration.class})
@ActiveProfiles(profiles = "local")
public class LocalProfileBeanCreationTest {

    @Autowired
    private GcsJsonApiClient apiClient;

    @Test
    public void libraryShouldLoadTheLocalClient() {
        assertTrue(apiClient instanceof LocalGcsJsonApiClient);
    }



}


@Configuration
@TestPropertySource("classpath:application.properties")
class PropertiesConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        final PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
        Properties properties = new Properties();

        properties.setProperty("gcs.devCredentialsFile", "/test_dev_credentials.json");
        properties.setProperty("gcs.defaultBucket", "testBucket");

        pspc.setProperties(properties);
        return pspc;
    }

}