package com.threeweeks.spring.cloudstorage;

import org.junit.Before;
import org.junit.Test;

public class SpringGaeGcsConfigurationTest {
    private SpringGaeGcsConfiguration springGaeGcsConfiguration;

    @Before
    public void before() {
        springGaeGcsConfiguration = new SpringGaeGcsConfiguration();
    }

    @Test(expected = IllegalArgumentException.class)
    public void springGaeGcsConfiguration_throwsException_whenNoBucketNameProvided() {
        springGaeGcsConfiguration.cloudStorageService(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void springGaeGcsConfiguration_throwsException_whenEmptyBucketNameProvided() {
        springGaeGcsConfiguration.cloudStorageService(" ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void springGaeGcsConfiguration_throwsException_whenNoProjectProvided() {
        springGaeGcsConfiguration.localCloudStorageService("myBucket", "blah", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void springGaeGcsConfiguration_throwsException_whenEmptyProjectProvided() {
        springGaeGcsConfiguration.localCloudStorageService("myBucket", "blah", " ");
    }

}
