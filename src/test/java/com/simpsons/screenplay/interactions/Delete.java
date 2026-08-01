package com.simpsons.screenplay.interactions;

import com.simpsons.screenplay.Actor;
import com.simpsons.screenplay.Interaction;
import com.simpsons.screenplay.abilities.ApiAbility;

/**
 * Sends a DELETE request to a resource and stores the response in the actor's
 * {@link ApiAbility}.
 */
public class Delete implements Interaction {

    private final String resource;
    private final int id;

    private Delete(String resource, int id) {
        this.resource = resource;
        this.id = id;
    }

    public static Delete resource(String resource, int id) {
        return new Delete(resource, id);
    }

    @Override
    public void performAs(Actor actor) {
        ApiAbility ability = actor.using(ApiAbility.class);
        ability.perform(client -> client.delete(resource, id));
    }
}
