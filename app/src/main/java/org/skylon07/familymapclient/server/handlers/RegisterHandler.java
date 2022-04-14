package org.skylon07.familymapclient.server.handlers;

import services.requests.RegisterRequest;
import services.responses.RegisterResponse;

public class RegisterHandler extends GenericHandler<RegisterRequest, RegisterResponse> {
    @Override
    protected String getEndpointURL(RegisterRequest request) { return "/user/register"; }

    @Override
    protected String getEndpointMethod(RegisterRequest request) {
        return "POST";
    }

    @Override
    protected Class<RegisterResponse> getResponseClass() {
        return RegisterResponse.class;
    }
}
