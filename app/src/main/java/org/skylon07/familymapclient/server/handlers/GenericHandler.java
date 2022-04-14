package org.skylon07.familymapclient.server.handlers;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Provides an interface to interact with all types of Handlers
 */
public abstract class GenericHandler<RequestType, ResponseType> {
    /**
     * Performs the operations necessary to call the endpoint and retrieve a response.
     * All handlers can do this, but must provide specific details for their specific endpoint
     * implementations.
     *
     * @param serverUrl is the root URL of the server to send the request to
     * @param request is the specific request for the handler to process
     * @return the specific response returned from the server
     */
    public ResponseType handle(String serverUrl, RequestType request) throws MalformedURLException, IOException {
        String urlStr = this.getEndpointURL(request);
        URL url = new URL(serverUrl + urlStr);

        Gson gson = new Gson();
        String requestJson = gson.toJson(request);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String requestMethod = this.getEndpointMethod(request);
        connection.setRequestMethod(requestMethod);
        connection.setRequestProperty("Authorization", this.getAuthHeader(request));

        boolean hasBody = requestMethod == "POST";
        connection.setDoOutput(hasBody);
        if (hasBody) {
            try (OutputStream requestBody = connection.getOutputStream()) {
                this.writeRequestJson(request, requestBody);
                requestBody.flush();
            }
        }

        ResponseType response;
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            response = this.readResponseJson(connection.getInputStream());
        } else {
            String errMsg = connection.getResponseMessage();
            // TODO: should log errMsg somewhere... somehow...
            response = this.readResponseJson(connection.getErrorStream());
        }
        connection.disconnect();
        return response;
    }

    /**
     * Overridable method that gets an authorization token from a request. If the endpoint does
     * not require authorization, then this method need not be overridden
     *
     * @param request is the specific request instance containing the auth token, if needed
     * @return the auth token contained in the request
     */
    protected String getAuthHeader(RequestType request) {
        return "(none)";
    }

    /**
     * Provides a necessary implementation detail for handle()
     *
     * @return a String representing the URL of the endpoint this handler calls
     */
    protected abstract String getEndpointURL(RequestType request);

    /**
     * Provides a necessary implementation detail for handle()
     *
     * @param request is the request given to handle()
     * @return a String representing the HTTP method to use for handling
     */
    protected abstract String getEndpointMethod(RequestType request);

    /**
     * Provides a necessary implementation detail for handle()
     *
     * @return the .class of the specific Response type
     */
    protected abstract Class<ResponseType> getResponseClass();

    /**
     * Writes a request into a request body
     *
     * @param request is the request to write
     * @param requestBody is the request body to write to
     * @throws IOException when the network breaks its legs and decides not to work
     */
    private void writeRequestJson(RequestType request, OutputStream requestBody) throws IOException {
        Gson gson = new Gson();
        String requestJson = gson.toJson(request);

        try (OutputStreamWriter requestBodyWriter = new OutputStreamWriter(requestBody)) {
            requestBodyWriter.write(requestJson);
        }
    }

    /**
     * Reads a response body and converts it into a specific response
     *
     * @param responseBody is the response body to read from
     * @throws IOException when the network breaks its arms and decides not to work
     */
    private ResponseType readResponseJson(InputStream responseBody) throws IOException {
        Gson gson = new Gson();
        InputStreamReader responseBodyReader = new InputStreamReader(responseBody);
        return gson.fromJson(responseBodyReader, this.getResponseClass());
    }
}
