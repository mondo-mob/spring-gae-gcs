package com.mondomob.spring.cloudstorage;


import com.mondomob.spring.cloudstorage.apiclient.GcsJsonApiClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.mondomob.spring.cloudstorage.GcsJsonApiService.DEFAULT_EXPIRY_DURATION;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GcsJsonApiServiceTest {

    private static final String DEFAULT_BUCKET = "default-bucket";
    private static final String HOST = "host";
    private static final String DEFAULT_FOLDER = "default-folder";
    @Mock
    private GcsJsonApiClient cloudStorage;

    private GcsJsonApiService service;

    @Before
    public void before() {
         service = new GcsJsonApiService(cloudStorage, DEFAULT_BUCKET, HOST, DEFAULT_FOLDER);
    }


    @Test
    public void fileNameShouldBeEscapedWhenDownlading() {
        assertThat(GcsJsonApiService
                .getFullPathFromObjectName("attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My report.pdf"),
                is("attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My%20report.pdf"));
    }

    @Test
    public void getUploadUrl() {
        String expectedUploadUrl = "https://upload";
        when(cloudStorage.initiateResumableUpload(eq(DEFAULT_BUCKET), ArgumentMatchers.startsWith(DEFAULT_FOLDER + "/"),
                eq("filename.csv"), eq("text/csv"), eq("origin-host")))
                .thenReturn(expectedUploadUrl);

        String uploadUrl = service.getUploadUrl("text/csv", "filename.csv", "origin-host");

        assertThat(uploadUrl, is(expectedUploadUrl));
    }

    @Test
    public void getUploadUrl_willUseConfiguredHost_whenOriginIsNull() {
        String expectedUploadUrl = "https://upload";
        when(cloudStorage.initiateResumableUpload(eq(DEFAULT_BUCKET), ArgumentMatchers.startsWith(DEFAULT_FOLDER + "/"),
                eq("filename.csv"), eq("text/csv"), eq(HOST)))
                .thenReturn(expectedUploadUrl);

        String uploadUrl = service.getUploadUrl("text/csv", "filename.csv", null);

        assertThat(uploadUrl, is(expectedUploadUrl));
    }

    @Test
    public void getUploadUrl_willUseSuppliedBucketAndFolder() {
        String expectedUploadUrl = "https://upload";
        when(cloudStorage.initiateResumableUpload(eq("custom-bucket"), ArgumentMatchers.startsWith("custom-folder/"),
                eq("filename.csv"), eq("text/csv"), eq(HOST)))
                .thenReturn(expectedUploadUrl);

        String uploadUrl = service.getUploadUrl("custom-bucket", "custom-folder", "text/csv", "filename.csv", null);

        assertThat(uploadUrl, is(expectedUploadUrl));
    }

    @Test
    public void getDownloadUrl() {
        String expectedDownloadUrl = "https://download";
        when(cloudStorage.generateSignedUrl(DEFAULT_BUCKET, "attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My%20report.pdf", DEFAULT_EXPIRY_DURATION))
                .thenReturn(expectedDownloadUrl);

        String downloadUrl = service.getDownloadUrl("attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My report.pdf");

        assertThat(downloadUrl, is(expectedDownloadUrl));
    }

    @Test
    public void getDownloadUrl_willUseSuppliedBucket() {
        String expectedDownloadUrl = "https://download";
        when(cloudStorage.generateSignedUrl("custom-bucket", "attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My%20report.pdf", DEFAULT_EXPIRY_DURATION))
                .thenReturn(expectedDownloadUrl);

        String downloadUrl = service.getDownloadUrl("custom-bucket", "attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My report.pdf");

        assertThat(downloadUrl, is(expectedDownloadUrl));
    }

    @Test
    public void buildBasePath() {
        String folder = service.buildBasePath("my/folder");

        assertThat(folder, startsWith("my/folder/"));
    }

}
