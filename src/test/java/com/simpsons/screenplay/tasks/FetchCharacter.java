package com.simpsons.screenplay.tasks;

import com.simpsons.screenplay.Actor;
import com.simpsons.screenplay.Task;
import com.simpsons.screenplay.abilities.ApiAbility;

/**
 * Fetches a single character by id and stores the response in the actor's
 * {@link ApiAbility}.
 */
public class FetchCharacter implements Task {

    private final int id;

    private FetchCharacter(int id) {
        this.id = id;
    }

    public static FetchCharacter withId(int id) {
        return new FetchCharacter(id);
    }

    @Override
    public void performAs(Actor actor) {
        actor.using(ApiAbility.class).perform(client -> client.getCharacter(id));
    }
}
