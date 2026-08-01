package com.simpsons.tests.characters;

import com.simpsons.BaseApiTest;
import com.simpsons.data.DataReader;
import com.simpsons.data.PopularCharacter;
import com.simpsons.models.Character;
import com.simpsons.models.Episode;
import com.simpsons.models.PaginatedResponse;
import com.simpsons.screenplay.tasks.FetchCharacter;
import com.simpsons.screenplay.tasks.FetchCharacters;
import com.simpsons.screenplay.tasks.FetchEpisode;
import com.simpsons.validation.ApiSchemaValidator;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.simpsons.screenplay.Ensure.*;
import static com.simpsons.screenplay.questions.TheResponse.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("regression")
class CharactersApiTest extends BaseApiTest {

    @Test
    @DisplayName("GET /characters/1 returns Homer with his phrases")
    void getHomerReturnsExpectedData() {
        actor.attemptsTo(FetchCharacter.withId(1));

        actor.should(
                thatTheStatusCode().isEqualTo(200),
                that(bodyField("id")).isEqualTo(1),
                that(bodyFieldAsString("name")).isEqualTo("Homer Simpson"),
                that(bodyFieldAsString("gender")).isEqualTo("Male"),
                that(bodyFieldAsString("status")).isEqualTo("Alive"),
                that(bodyFieldAsString("portrait_path")).isEqualTo("/character/1.webp"),
                that(bodyField("phrases")).contains("Doh!"),
                that(bodyField("phrases"), phrases ->
                        assertThat((java.util.Collection<?>) phrases).hasSizeGreaterThanOrEqualTo(1))
        );
    }

    @Test
    @DisplayName("The detail deserializes into a POJO including nested models")
    void getCharacterDeserializesIntoPojo() {
        actor.attemptsTo(FetchCharacter.withId(1));

        Character homer = actor.asksFor(as(Character.class));

        assertThat(homer.getName()).isEqualTo("Homer Simpson");
        assertThat(homer.getAge()).isEqualTo(39);
        assertThat(homer.getPhrases()).contains("Doh!");
        assertThat(homer.getFirstAppearanceEp())
                .isNotNull()
                .extracting(Episode::getName)
                .isEqualTo("Simpsons Roasting on an Open Fire");
        assertThat(homer.getFirstAppearanceSh())
                .isNotNull()
                .extracting(Episode::getName)
                .isEqualTo("Good Night");
    }

    @ParameterizedTest(name = "Character {0} ({1})")
    @MethodSource("popularCharacters")
    @DisplayName("Popular characters resolve with their data")
    void popularCharactersResolve(PopularCharacter expected) {
        actor.attemptsTo(FetchCharacter.withId(expected.id()));

        actor.should(
                that(bodyFieldAsString("name")).isEqualTo(expected.name()),
                that(bodyFieldAsString("gender")).isEqualTo(expected.gender()),
                that(bodyFieldAsString("status")).isEqualTo(expected.status()),
                that(bodyFieldAsString("portrait_path")).startsWith("/")
        );
    }

    @Test
    @DisplayName("The listing pages 20 items and keeps the total")
    void listIsPaginatedConsistently() {
        actor.attemptsTo(FetchCharacters.onPage(1));

        PaginatedResponse<Character> page = actor.asksFor(as(new TypeRef<PaginatedResponse<Character>>() {
        }));

        assertThat(page.getCount()).isEqualTo(1_182);
        assertThat(page.getPages()).isEqualTo(60);
        assertThat(page.getResults()).hasSize(20);
        assertThat(page.getPrev()).isNull();
        assertThat(page.getNext()).contains("page=2");
    }

    @Test
    @DisplayName("Ids are contiguous: 1182 exists and 1183 does not")
    void characterIdsAreContiguous() {
        actor.attemptsTo(FetchCharacter.withId(1_182));
        actor.should(
                thatTheStatusCode().isEqualTo(200),
                that(bodyField("id")).isEqualTo(1_182)
        );

        actor.attemptsTo(FetchCharacter.withId(1_183));
        actor.should(thatTheStatusCode().isEqualTo(404));
    }

    @Test
    @DisplayName("A character's first appearance matches the episode resource")
    void firstAppearanceMatchesEpisodeResource() {
        actor.attemptsTo(FetchCharacter.withId(1));
        Character homer = actor.asksFor(as(Character.class));

        actor.attemptsTo(FetchEpisode.withId(homer.getFirstAppearanceEpId()));
        Episode episode = actor.asksFor(as(Episode.class));

        assertThat(episode.getName()).isEqualTo(homer.getFirstAppearanceEp().getName());
        assertThat(episode.getId()).isEqualTo(homer.getFirstAppearanceEp().getId());
    }

    @Test
    @Tag("contract")
    @DisplayName("Character detail matches its JSON schema")
    void characterDetailMatchesContract() {
        actor.attemptsTo(FetchCharacter.withId(1));
        ApiSchemaValidator.validate(actor.asksFor(status()), "character-detail.json").statusCode(200);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> popularCharacters() {
        return DataReader.readList("/data/popular-characters.json", PopularCharacter.class)
                .stream()
                .map(org.junit.jupiter.params.provider.Arguments::of);
    }
}
