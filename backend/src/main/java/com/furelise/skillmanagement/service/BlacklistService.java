package com.furelise.skillmanagement.service;

public interface BlacklistService {

    void blacklistToken(String jwtToken);

    boolean isTokenBlacklisted(String jwtToken);
}
