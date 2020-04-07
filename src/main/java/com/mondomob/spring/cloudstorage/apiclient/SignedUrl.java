package com.mondomob.spring.cloudstorage.apiclient;

import java.time.OffsetDateTime;

public class SignedUrl {
    private final String url;
    private final OffsetDateTime expiresAt;

    public SignedUrl(String url, OffsetDateTime expiresAt) {
        this.url = url;
        this.expiresAt = expiresAt;
    }

    public String getUrl() {
        return url;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }
}
