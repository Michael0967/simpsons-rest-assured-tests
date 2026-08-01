package com.simpsons.tests.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpsons.BaseApiTest;
import com.simpsons.screenplay.tasks.FetchCharacter;
import com.simpsons.screenplay.tasks.FetchCharacters;
import com.simpsons.screenplay.tasks.FetchEpisode;
import com.simpsons.screenplay.tasks.FetchLocation;
import com.simpsons.validation.ApiSchemaValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.simpsons.screenplay.questions.TheResponse.bodyField;
import static com.simpsons.screenplay.questions.TheResponse.status;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Structural contract validation against JSON Schema.
 * Run: mvn test -Pcontract
 */
@Tag("contract")
class ContractApiTest extends BaseApiTest {

    @Test
    @DisplayName("Character detail matches its schema")
    void characterDetailMatchesContract() {
        actor.attemptsTo(FetchCharacter.withId(1));
        ApiSchemaValidator.validate(actor.asksFor(status()), "character-detail.json").statusCode(200);
    }

    @Test
    @DisplayName("Character list items match their schema")
    void characterListItemMatchesContract() throws Exception {
        actor.attemptsTo(FetchCharacters.onPage(1));
        Object item = actor.asksFor(bodyField("results[0]"));
        String itemJson = new ObjectMapper().writeValueAsString(item);

        assertThat(itemJson,
                matchesJsonSchemaInClasspath("schemas/character-list-item.json"));
    }

    @Test
    @DisplayName("Episode matches its schema")
    void episodeMatchesContract() {
        actor.attemptsTo(FetchEpisode.withId(1));
        ApiSchemaValidator.validate(actor.asksFor(status()), "episode.json").statusCode(200);
    }

    @Test
    @DisplayName("Location matches its schema")
    void locationMatchesContract() {
        actor.attemptsTo(FetchLocation.withId(1));
        ApiSchemaValidator.validate(actor.asksFor(status()), "location.json").statusCode(200);
    }

    @Test
    @DisplayName("The paged response wrapper matches its schema")
    void pagedResponseMatchesContract() {
        actor.attemptsTo(FetchCharacters.onPage(1));
        ApiSchemaValidator.validate(actor.asksFor(status()), "paged-response.json").statusCode(200);
    }

    @Test
    @DisplayName("Error responses match their schema")
    void errorResponseMatchesContract() {
        actor.attemptsTo(FetchCharacter.withId(999_999));
        ApiSchemaValidator.validate(actor.asksFor(status()), "error.json").statusCode(404);
    }
}
