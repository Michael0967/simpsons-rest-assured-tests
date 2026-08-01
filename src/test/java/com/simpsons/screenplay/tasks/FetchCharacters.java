package com.simpsons.screenplay.tasks;

import com.simpsons.screenplay.Actor;
import com.simpsons.screenplay.Task;
import com.simpsons.screenplay.abilities.ApiAbility;

/**
 * Fetches a page of characters and stores the response in the actor's
 * {@link ApiAbility}.
 */
public class FetchCharacters implements Task {

    private final int page;

    private FetchCharacters(int page) {
        this.page = page;
    }

    public static FetchCharacters onPage(int page) {
        return new FetchCharacters(page);
    }

    @Override
    public void performAs(Actor actor) {
        actor.using(ApiAbility.class).perform(client -> client.getCharacters(page));
    }
}
