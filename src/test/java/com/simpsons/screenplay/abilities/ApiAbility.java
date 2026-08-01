package com.simpsons.screenplay.abilities;

import com.simpsons.client.SimpsonsApiClient;
import com.simpsons.screenplay.Ability;
import io.restassured.response.Response;

import java.util.function.Function;

/**
 * Grants an {@link com.simpsons.screenplay.Actor} the ability to call the
 * Simpsons API. Keeps track of the most recent {@link Response} so questions
 * can inspect it.
 */
public class ApiAbility implements Ability {

    private final SimpsonsApiClient client = new SimpsonsApiClient();
    private Response lastResponse;

    public static ApiAbility callingTheSimpsonsApi() {
        return new ApiAbility();
    }

    public SimpsonsApiClient client() {
        return client;
    }

    public Response lastResponse() {
        return lastResponse;
    }

    /**
     * Executes an API call through the facade and records the response as the
     * current one, ready for questions to inspect.
     */
    public Response perform(Function<SimpsonsApiClient, Response> call) {
        this.lastResponse = call.apply(client);
        return lastResponse;
    }
}
