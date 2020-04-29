package com.thed.service;

import org.apache.http.auth.AuthenticationException;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by prashant on 24/6/19.
 */
public interface UserService extends BaseService {

    /**
     * Validates given credentials and clears token if validated.
     * @param serverAddress
     * @param username
     * @param password
     * @return
     */
    public Boolean verifyCredentials(String serverAddress, String username, String password) throws URISyntaxException, IOException, AuthenticationException;

    /**
     * Validates given credentials and keeps token and server address if verified.
     * @param serverAddress
     * @param username
     * @param password
     * @return
     */
    public Boolean login(String serverAddress, String username, String password) throws URISyntaxException, IOException, AuthenticationException;

}
