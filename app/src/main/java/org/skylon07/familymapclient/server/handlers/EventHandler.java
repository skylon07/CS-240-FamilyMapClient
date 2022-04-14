package org.skylon07.familymapclient.server.handlers;

import services.requests.EventRequest;
import services.responses.EventResponse;

public class EventHandler extends GenericHandler<EventRequest, EventResponse> {
    @Override
    protected String getEndpointURL(EventRequest request) {
        if (request.all) {
            return "/event";
        } else {
            return "/event/" + request.eventID;
        }
    }

    @Override
    protected String getEndpointMethod(EventRequest request) {
        return "GET";
    }

    @Override
    protected String getAuthHeader(EventRequest request) {
        return request.authtoken;
    }

    @Override
    protected Class<EventResponse> getResponseClass() {
        return EventResponse.class;
    }
}
