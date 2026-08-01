package com.simpsons.screenplay;

import java.util.HashMap;
import java.util.Map;

/**
 * The persona that performs tasks, asks questions and verifies consequences in
 * a Screenplay scenario. Each actor owns its abilities and a private memory
 * (see {@link #remember(String, Object)} / {@link #recall(String)}).
 *
 * <p>Instances are cheap and intentionally not shared between tests: one actor
 * per test method keeps state isolated under parallel execution.</p>
 */
public final class Actor {

    private final String name;
    private final Map<Class<? extends Ability>, Ability> abilities = new HashMap<>();
    private final Map<String, Object> memory = new HashMap<>();

    private Actor(String name) {
        this.name = name;
    }

    public static Actor named(String name) {
        return new Actor(name);
    }

    public Actor whoCan(Ability... grantedAbilities) {
        for (Ability ability : grantedAbilities) {
            abilities.put(ability.getClass(), ability);
        }
        return this;
    }

    public <T extends Ability> T using(Class<T> abilityType) {
        Ability ability = abilities.get(abilityType);
        if (ability == null) {
            throw new ScreenplayException(
                    "Actor '" + name + "' cannot use " + abilityType.getSimpleName()
                            + "; grant it with whoCan(...)", null);
        }
        return abilityType.cast(ability);
    }

    public void attemptsTo(Performable... performables) {
        for (Performable performable : performables) {
            try {
                performable.performAs(this);
            } catch (Exception e) {
                throw new ScreenplayException(
                        "Step failed: " + performable.getClass().getSimpleName(), e);
            }
        }
    }

    public <T> T asksFor(Question<T> question) {
        try {
            return question.answerAs(this);
        } catch (Exception e) {
            throw new ScreenplayException(
                    "Question failed: " + question.getClass().getSimpleName(), e);
        }
    }

    public void should(Consequence... consequences) {
        for (Consequence consequence : consequences) {
            try {
                consequence.verifyAs(this);
            } catch (AssertionError e) {
                throw e;
            } catch (Exception e) {
                throw new ScreenplayException(
                        "Consequence failed: " + consequence.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * Stores a value in the actor's memory so later steps can reuse it.
     */
    public void remember(String key, Object value) {
        memory.put(key, value);
    }

    /**
     * Retrieves a previously remembered value by key.
     */
    @SuppressWarnings("unchecked")
    public <T> T recall(String key) {
        return (T) memory.get(key);
    }

    public String name() {
        return name;
    }
}
