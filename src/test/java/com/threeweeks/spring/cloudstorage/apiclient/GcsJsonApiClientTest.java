package com.threeweeks.spring.cloudstorage.apiclient;

import com.google.appengine.api.appidentity.AppIdentityService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.time.OffsetDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GcsJsonApiClientTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AppIdentityService identityService;
    @Mock
    private AppIdentityService.SigningResult signing;

    private GcsJsonApiClient gcsJsonApiClient;

    @Before
    public void before() {
        gcsJsonApiClient = spy(new GcsJsonApiClient(null, identityService));
    }

    @Test
    public void generateSignedUrl() {
        when(signing.getSignature()).thenReturn("MySignedStr".getBytes());
        when(gcsJsonApiClient.getExpiryDateTime(Duration.ofMinutes(2))).thenReturn(OffsetDateTime.parse("2020-01-01T00:00:00Z"));
        when(identityService.signForApp(any())).thenReturn(signing);
        when(identityService.getServiceAccountName()).thenReturn("googleAccessId");

        String signed = gcsJsonApiClient.generateSignedUrl("myBucket", "attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My%20report.pdf", Duration.ofMinutes(2));

        assertThat(signed, is("https://storage.googleapis.com/myBucket/attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My%20report.pdf?GoogleAccessId=googleAccessId&Expires=1577836800&Signature=TXlTaWduZWRTdHI%3D"));
        byte[] data = String.format(
                "%s\n%s\n%s\n%s\n%s",
                "GET",
                "",  // Content MD5 is not required but could added for extra security
                "",  // Content Type is optional and best left out as it isn't included by default in browser GET requests
                1577836800,
                "/myBucket/attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My%20report.pdf").getBytes();
        verify(identityService).signForApp(data);
    }

    @Test
    public void generateSignedUrlWithExpiry() {
        OffsetDateTime expirationDateTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        when(signing.getSignature()).thenReturn("MySignedStr".getBytes());
        when(gcsJsonApiClient.getExpiryDateTime(Duration.ofMinutes(2))).thenReturn(expirationDateTime);
        when(identityService.signForApp(any())).thenReturn(signing);
        when(identityService.getServiceAccountName()).thenReturn("googleAccessId");

        SignedUrl response = gcsJsonApiClient.generateSignedUrlWithExpiry("myBucket", "attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My%20report.pdf", Duration.ofMinutes(2));

        assertThat(response.getExpiresAt(), is(expirationDateTime));
        assertThat(response.getUrl(), is("https://storage.googleapis.com/myBucket/attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My%20report.pdf?GoogleAccessId=googleAccessId&Expires=1577836800&Signature=TXlTaWduZWRTdHI%3D"));
        byte[] data = String.format(
                "%s\n%s\n%s\n%s\n%s",
                "GET",
                "",  // Content MD5 is not required but could added for extra security
                "",  // Content Type is optional and best left out as it isn't included by default in browser GET requests
                1577836800,
                "/myBucket/attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My%20report.pdf").getBytes();
        verify(identityService).signForApp(data);
    }

}
