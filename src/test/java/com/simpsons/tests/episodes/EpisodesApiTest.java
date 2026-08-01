package com.simpsons.tests.episodes;

import com.simpsons.BaseApiTest;
import com.simpsons.data.DataReader;
import com.simpsons.data.PopularEpisode;
import com.simpsons.models.Episode;
import com.simpsons.models.PaginatedResponse;
import com.simpsons.screenplay.tasks.FetchEpisode;
import com.simpsons.screenplay.tasks.FetchEpisodes;
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
class EpisodesApiTest extends BaseApiTest {

    @Test
    @DisplayName("GET /episodes/1 returns the series pilot")
    void getFirstEpisodeReturnsExpectedData() {
        actor.attemptsTo(FetchEpisode.withId(1));

        actor.should(
                thatTheStatusCode().isEqualTo(200),
                that(bodyField("id")).isEqualTo(1),
                that(bodyFieldAsString("name")).isEqualTo("Simpsons Roasting on an Open Fire"),
                that(bodyField("season")).isEqualTo(1),
                that(bodyField("episode_number")).isEqualTo(1),
                that(bodyFieldAsString("airdate")).isEqualTo("1989-12-17")
        );
    }

    @Test
    @DisplayName("The episode deserializes into a POJO")
    void getEpisodeDeserializesIntoPojo() {
        actor.attemptsTo(FetchEpisode.withId(1));

        Episode episode = actor.asksFor(as(Episode.class));

        assertThat(episode.getName()).isEqualTo("Simpsons Roasting on an Open Fire");
        assertThat(episode.getSeason()).isEqualTo(1);
        assertThat(episode.getSynopsis()).isNotBlank();
        assertThat(episode.getImagePath()).startsWith("/");
    }

    @ParameterizedTest(name = "Episode {0} (season {2})")
    @MethodSource("popularEpisodes")
    @DisplayName("Representative episodes resolve with their season")
    void popularEpisodesResolve(PopularEpisode expected) {
        actor.attemptsTo(FetchEpisode.withId(expected.id()));

        actor.should(
                that(bodyFieldAsString("name")).isEqualTo(expected.name()),
                that(bodyField("season")).isEqualTo(expected.season()),
                that(bodyFieldAsString("airdate")).isNotNull()
        );
    }

    @Test
    @DisplayName("The episode listing pages 20 items")
    void listIsPaginated() {
        actor.attemptsTo(FetchEpisodes.onPage(1));

        PaginatedResponse<Episode> page = actor.asksFor(as(new TypeRef<PaginatedResponse<Episode>>() {
        }));

        assertThat(page.getCount()).isEqualTo(768);
        assertThat(page.getPages()).isEqualTo(39);
        assertThat(page.getResults()).hasSize(20);
    }

    @Test
    @Tag("contract")
    @DisplayName("The episode matches its JSON schema")
    void episodeMatchesContract() {
        actor.attemptsTo(FetchEpisode.withId(1));
        ApiSchemaValidator.validate(actor.asksFor(status()), "episode.json").statusCode(200);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> popularEpisodes() {
        return DataReader.readList("/data/popular-episodes.json", PopularEpisode.class)
                .stream()
                .map(org.junit.jupiter.params.provider.Arguments::of);
    }
}
