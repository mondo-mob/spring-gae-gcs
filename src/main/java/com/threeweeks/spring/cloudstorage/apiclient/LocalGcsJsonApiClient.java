package com.threeweeks.spring.cloudstorage.apiclient;

import com.google.api.client.http.HttpRequestFactory;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.auth.oauth2.ServiceAccountCredentials;

import java.security.Signature;

public class LocalGcsJsonApiClient extends GcsJsonApiClient {

    private final ServiceAccountCredentials credentials;

    public LocalGcsJsonApiClient(HttpRequestFactory httpRequestFactory, AppIdentityService appIdentityService, ServiceAccountCredentials credentials) {
        super(httpRequestFactory, appIdentityService);
        this.credentials = credentials;

    }

    @Override
    protected String getGoogleAccessId() {
        return credentials.getClientEmail();
    }

    @Override
    protected byte[] sign(byte[] data) {
        byte[] signature;
        try {
            Signature rsa = Signature.getInstance("SHA256withRSA");
            rsa.initSign(credentials.getPrivateKey());
            rsa.update(data);
            signature = rsa.sign();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error signing URL: %s", e.getMessage()) , e);
        }

        return signature;
    }
}
