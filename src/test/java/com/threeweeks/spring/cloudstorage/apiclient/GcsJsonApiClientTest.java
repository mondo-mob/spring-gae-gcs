package com.threeweeks.spring.cloudstorage.apiclient;

import com.google.appengine.api.appidentity.AppIdentityService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
        gcsJsonApiClient = spy(new GcsJsonApiClient(null, identityService, null));
    }

    @Test
    public void generateSignedUrl() {
        when(signing.getSignature()).thenReturn("MySignedStr".getBytes());
        when(gcsJsonApiClient.getExpiration(2)).thenReturn(1234L);
        when(identityService.signForApp(any())).thenReturn(signing);
        when(identityService.getServiceAccountName()).thenReturn("googleAccessId");

        String signed = gcsJsonApiClient.generateSignedUrl("myBucket", "attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My%20report.pdf", 2);

        assertThat(signed, is("https://storage.googleapis.com/myBucket/attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My%20report.pdf?GoogleAccessId=googleAccessId&Expires=1234&Signature=TXlTaWduZWRTdHI%3D"));
        byte[] data = String.format(
                "%s\n%s\n%s\n%s\n%s",
                "GET",
                "",  // Content MD5 is not required but could added for extra security
                "",  // Content Type is optional and best left out as it isn't included by default in browser GET requests
                1234,
                "/myBucket/attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/My%20report.pdf").getBytes();
        verify(identityService).signForApp(data);
    }

}