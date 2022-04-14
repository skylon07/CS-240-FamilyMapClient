package org.skylon07.familymapclient.server.handlers;

import services.requests.PersonRequest;
import services.responses.PersonResponse;

public class PersonHandler extends GenericHandler<PersonRequest, PersonResponse> {
    @Override
    protected String getEndpointURL(PersonRequest request) {
        if (request.all) {
            return "/person";
        } else {
            return "/person/" + request.personID;
        }
    }

    @Override
    protected String getEndpointMethod(PersonRequest request) {
        return "GET";
    }

    @Override
    protected String getAuthHeader(PersonRequest request) {
        return request.authtoken;
    }

    @Override
    protected Class<PersonResponse> getResponseClass() {
        return PersonResponse.class;
    }
}
