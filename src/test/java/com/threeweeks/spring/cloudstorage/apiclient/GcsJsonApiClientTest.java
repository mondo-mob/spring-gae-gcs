package com.threeweeks.spring.cloudstorage.apiclient;

import com.google.appengine.api.appidentity.AppIdentityService;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class GcsJsonApiClientTest {

    @Test
    public void testDownloadUrlGeneratedCorrectly() {
        AppIdentityService mockIdentityService = mock(AppIdentityService.class, RETURNS_DEEP_STUBS);

        AppIdentityService.SigningResult signing = mock(AppIdentityService.SigningResult.class);
        when(signing.getSignature()).thenReturn("MySignedStr".getBytes());

        when(mockIdentityService.signForApp(any())).thenReturn(signing);

        GcsJsonApiClient gcsJsonApiClient = spy(new GcsJsonApiClient(null, mockIdentityService, null));

        when(gcsJsonApiClient.getExpiration(2)).thenReturn(1234L);

        String signed = gcsJsonApiClient.generateSignedUrl("myBucket", "attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My%20report.pdf", 2);


        byte[] data = String.format(
                "%s\n%s\n%s\n%s\n%s",
                "GET",
                "",  // Content MD5 is not required but could added for extra security
                "",  // Content Type is optional and best left out as it isn't included by default in browser GET requests
                1234,
                "/myBucket/attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My%20report.pdf").getBytes();

        verify(mockIdentityService).signForApp(data);
        assertThat(signed, is("https://storage.googleapis.com/myBucket/attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My%20report.pdf?GoogleAccessId=null&Expires=1234&Signature=TXlTaWduZWRTdHI%3D"));
    }

}