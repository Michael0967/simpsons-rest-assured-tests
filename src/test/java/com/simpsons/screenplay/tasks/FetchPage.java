package com.simpsons.screenplay.tasks;

import com.simpsons.screenplay.Actor;
import com.simpsons.screenplay.Task;
import com.simpsons.screenplay.abilities.ApiAbility;

/**
 * Fetches a specific page of any paged resource and stores the response in
 * the actor's {@link ApiAbility}.
 */
public class FetchPage implements Task {

    private final String resource;
    private final Integer page;

    private FetchPage(String resource, Integer page) {
        this.resource = resource;
        this.page = page;
    }

    public static FetchPage of(String resource) {
        return new FetchPage(resource, null);
    }

    public FetchPage number(int page) {
        return new FetchPage(resource, page);
    }

    @Override
    public void performAs(Actor actor) {
        ApiAbility ability = actor.using(ApiAbility.class);
        if (page == null) {
            ability.perform(client -> client.get(resource));
        } else {
            ability.perform(client -> client.getResourcePage(resource, page));
        }
    }
}
