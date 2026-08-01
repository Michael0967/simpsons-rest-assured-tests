package com.simpsons.screenplay.interactions;

import com.simpsons.screenplay.Actor;
import com.simpsons.screenplay.Interaction;
import com.simpsons.screenplay.abilities.ApiAbility;

/**
 * Sends an arbitrary HTTP method (PUT, PATCH, TRACE, OPTIONS, ...) to a
 * resource and stores the response in the actor's {@link ApiAbility}.
 */
public class SendRequest implements Interaction {

    private final String method;
    private final String resource;

    private SendRequest(String method, String resource) {
        this.method = method;
        this.resource = resource;
    }

    public static SendRequest with(String method, String resource) {
        return new SendRequest(method, resource);
    }

    @Override
    public void performAs(Actor actor) {
        ApiAbility ability = actor.using(ApiAbility.class);
        ability.perform(client -> client.request(method, resource));
    }
}
