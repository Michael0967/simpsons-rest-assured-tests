package com.simpsons.screenplay.interactions;

import com.simpsons.screenplay.Actor;
import com.simpsons.screenplay.Interaction;
import com.simpsons.screenplay.abilities.ApiAbility;

/**
 * Sends a POST request to a resource and stores the response in the actor's
 * {@link ApiAbility}.
 */
public class Post implements Interaction {

    private final String resource;

    private Post(String resource) {
        this.resource = resource;
    }

    public static Post to(String resource) {
        return new Post(resource);
    }

    @Override
    public void performAs(Actor actor) {
        ApiAbility ability = actor.using(ApiAbility.class);
        ability.perform(client -> client.post(resource));
    }
}
