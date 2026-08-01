package com.simpsons.screenplay.tasks;

import com.simpsons.screenplay.Actor;
import com.simpsons.screenplay.Task;
import com.simpsons.screenplay.abilities.ApiAbility;

/**
 * Fetches a page of locations and stores the response in the actor's
 * {@link ApiAbility}.
 */
public class FetchLocations implements Task {

    private final int page;

    private FetchLocations(int page) {
        this.page = page;
    }

    public static FetchLocations onPage(int page) {
        return new FetchLocations(page);
    }

    @Override
    public void performAs(Actor actor) {
        actor.using(ApiAbility.class).perform(client -> client.getLocations(page));
    }
}
