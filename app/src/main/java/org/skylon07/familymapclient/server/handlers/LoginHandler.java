package org.skylon07.familymapclient.server.handlers;

import services.requests.LoginRequest;
import services.responses.LoginResponse;

public class LoginHandler extends GenericHandler<LoginRequest, LoginResponse> {
    @Override
    protected String getEndpointURL(LoginRequest request) {
        return "/user/login";
    }

    @Override
    protected String getEndpointMethod(LoginRequest request) {
        return "POST";
    }

    @Override
    protected Class<LoginResponse> getResponseClass() {
        return LoginResponse.class;
    }
}
