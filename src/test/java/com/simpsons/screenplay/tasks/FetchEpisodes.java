package com.simpsons.screenplay.tasks;

import com.simpsons.screenplay.Actor;
import com.simpsons.screenplay.Task;
import com.simpsons.screenplay.abilities.ApiAbility;

/**
 * Fetches a page of episodes and stores the response in the actor's
 * {@link ApiAbility}.
 */
public class FetchEpisodes implements Task {

    private final int page;

    private FetchEpisodes(int page) {
        this.page = page;
    }

    public static FetchEpisodes onPage(int page) {
        return new FetchEpisodes(page);
    }

    @Override
    public void performAs(Actor actor) {
        actor.using(ApiAbility.class).perform(client -> client.getEpisodes(page));
    }
}
