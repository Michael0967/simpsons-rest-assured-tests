package com.simpsons.screenplay.interactions;

import com.simpsons.screenplay.Actor;
import com.simpsons.screenplay.Interaction;
import com.simpsons.screenplay.abilities.ApiAbility;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Performs a single GET request, optionally with path params, query params
 * and/or headers, and stores the response in the actor's {@link ApiAbility}.
 */
public class Get implements Interaction {

    private final String resource;
    private final Map<String, String> queryParams = new LinkedHashMap<>();
    private final Map<String, String> headers = new LinkedHashMap<>();
    private Object[] pathParams = new Object[0];

    private Get(String resource) {
        this.resource = resource;
    }

    public static Get resource(String resource) {
        return new Get(resource);
    }

    public Get withPathParams(Object... params) {
        this.pathParams = params;
        return this;
    }

    public Get withQueryParam(String name, String value) {
        this.queryParams.put(name, value);
        return this;
    }

    public Get withHeaders(Map<String, String> requestHeaders) {
        this.headers.putAll(requestHeaders);
        return this;
    }

    @Override
    public void performAs(Actor actor) {
        ApiAbility ability = actor.using(ApiAbility.class);
        Response response;
        if (!headers.isEmpty()) {
            response = ability.client().getWithHeaders(resource, headers);
        } else if (!queryParams.isEmpty()) {
            response = ability.client().getWithQueryParams(resource, queryParams);
        } else {
            response = ability.client().get(resource, pathParams);
        }
        ability.perform(ignored -> response);
    }
}
