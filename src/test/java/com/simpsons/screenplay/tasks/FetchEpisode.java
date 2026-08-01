package com.simpsons.screenplay.tasks;

import com.simpsons.screenplay.Actor;
import com.simpsons.screenplay.Task;
import com.simpsons.screenplay.abilities.ApiAbility;

/**
 * Fetches a single episode by id and stores the response in the actor's
 * {@link ApiAbility}.
 */
public class FetchEpisode implements Task {

    private final int id;

    private FetchEpisode(int id) {
        this.id = id;
    }

    public static FetchEpisode withId(int id) {
        return new FetchEpisode(id);
    }

    @Override
    public void performAs(Actor actor) {
        actor.using(ApiAbility.class).perform(client -> client.getEpisode(id));
    }
}
