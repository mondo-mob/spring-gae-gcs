package com.threeweeks.spring.cloudstorage.apiclient;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequestFactory;
import com.google.appengine.api.appidentity.AppIdentityService;

import java.security.Signature;

public class LocalGcsJsonApiClient extends GcsJsonApiClient {

    public LocalGcsJsonApiClient(HttpRequestFactory httpRequestFactory, AppIdentityService appIdentityService, GoogleCredential gcsCredential) {
        super(httpRequestFactory, appIdentityService, gcsCredential);
    }

    @Override
    protected String getGoogleAccessId() {
        return gcsCredential.getServiceAccountId();
    }

    @Override
    protected byte[] sign(byte[] data) {
        byte[] signature;
        try {
            Signature rsa = Signature.getInstance("SHA256withRSA");
            rsa.initSign(gcsCredential.getServiceAccountPrivateKey());
            rsa.update(data);
            signature = rsa.sign();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error signing URL: %s", e.getMessage()) , e);
        }

        return signature;
    }
}
