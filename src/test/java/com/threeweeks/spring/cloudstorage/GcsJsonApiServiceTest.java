package com.threeweeks.spring.cloudstorage;


import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

public class GcsJsonApiServiceTest {

    @Test
    public void fileNameShouldBeEscapedWhenDownlading() {
        assertThat(GcsJsonApiService
                .getFullPathFromObjectName("attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My report.pdf"),
                is("attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My%20report.pdf"));
    }

    @Test
    public void buildsThePathCorrectly() {
        GcsJsonApiService mockedService = new GcsJsonApiService(null, null, null, null);
        String folder = mockedService.buildBasePath("my/folder");
        assertThat(folder, startsWith("my/folder/"));
    }

}