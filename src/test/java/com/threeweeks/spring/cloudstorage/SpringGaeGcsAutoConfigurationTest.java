package com.threeweeks.spring.cloudstorage;

import org.junit.Before;
import org.junit.Test;

public class SpringGaeGcsAutoConfigurationTest {
    private SpringGaeGcsAutoConfiguration springGaeGcsAutoConfiguration;

    @Before
    public void before() {
        springGaeGcsAutoConfiguration = new SpringGaeGcsAutoConfiguration();
    }

    @Test(expected = IllegalArgumentException.class)
    public void springGaeGcsConfiguration_throwsException_whenNoProjectProvided() {
        springGaeGcsAutoConfiguration.localCloudStorageService("myBucket", "blah", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void springGaeGcsConfiguration_throwsException_whenEmptyProjectProvided() {
        springGaeGcsAutoConfiguration.localCloudStorageService("myBucket", "blah", " ");
    }

}
