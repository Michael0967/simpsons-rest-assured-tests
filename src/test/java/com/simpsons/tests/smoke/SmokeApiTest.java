package com.simpsons.tests.smoke;

import com.simpsons.BaseApiTest;
import com.simpsons.screenplay.tasks.FetchCharacter;
import com.simpsons.screenplay.tasks.FetchCharacters;
import com.simpsons.screenplay.tasks.FetchEpisode;
import com.simpsons.screenplay.tasks.FetchLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.simpsons.screenplay.Ensure.*;
import static com.simpsons.screenplay.questions.TheResponse.*;

/**
 * Quick verification that the API is alive and responding correctly.
 * This is the first layer executed in CI (mvn test -Psmoke).
 */
@Tag("smoke")
class SmokeApiTest extends BaseApiTest {

    @Test
    @DisplayName("The three resources respond with 200")
    void coreEndpointsAreReachable() {
        actor.attemptsTo(FetchCharacter.withId(1));
        actor.should(thatTheStatusCode().isEqualTo(200));

        actor.attemptsTo(FetchEpisode.withId(1));
        actor.should(thatTheStatusCode().isEqualTo(200));

        actor.attemptsTo(FetchLocation.withId(1));
        actor.should(thatTheStatusCode().isEqualTo(200));

        actor.attemptsTo(FetchCharacters.onPage(1));
        actor.should(thatTheStatusCode().isEqualTo(200));
    }

    @Test
    @DisplayName("Responses have a JSON format")
    void responsesAreJson() {
        actor.attemptsTo(FetchCharacter.withId(1));
        actor.should(that(contentType()).isEqualTo("application/json"));

        actor.attemptsTo(FetchCharacters.onPage(1));
        actor.should(that(contentType()).isEqualTo("application/json"));
    }

    @Test
    @Tag("performance")
    @DisplayName("Character detail responds in under 5 seconds")
    void characterDetailRespondsFastEnough() {
        actor.attemptsTo(FetchCharacter.withId(1));

        actor.should(
                thatTheStatusCode().isEqualTo(200),
                that(bodyFieldAsString("name")).isNotNull(),
                that(responseTimeMs()).isLessThan(5_000L)
        );
    }
}
