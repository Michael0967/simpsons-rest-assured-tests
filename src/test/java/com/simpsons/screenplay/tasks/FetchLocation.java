package com.simpsons.screenplay.tasks;

import com.simpsons.screenplay.Actor;
import com.simpsons.screenplay.Task;
import com.simpsons.screenplay.abilities.ApiAbility;

/**
 * Fetches a single location by id and stores the response in the actor's
 * {@link ApiAbility}.
 */
public class FetchLocation implements Task {

    private final int id;

    private FetchLocation(int id) {
        this.id = id;
    }

    public static FetchLocation withId(int id) {
        return new FetchLocation(id);
    }

    @Override
    public void performAs(Actor actor) {
        actor.using(ApiAbility.class).perform(client -> client.getLocation(id));
    }
}
