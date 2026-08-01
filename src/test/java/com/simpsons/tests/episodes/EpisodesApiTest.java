package com.simpsons.tests.episodes;

import com.simpsons.BaseApiTest;
import com.simpsons.data.DataReader;
import com.simpsons.data.PopularEpisode;
import com.simpsons.models.Episode;
import com.simpsons.models.PaginatedResponse;
import com.simpsons.validation.ApiSchemaValidator;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Tag("regression")
class EpisodesApiTest extends BaseApiTest {

    @Test
    @DisplayName("GET /episodes/1 devuelve el piloto de la serie")
    void getFirstEpisodeReturnsExpectedData() {
        client.getEpisode(1)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("Simpsons Roasting on an Open Fire"))
                .body("season", equalTo(1))
                .body("episode_number", equalTo(1))
                .body("airdate", equalTo("1989-12-17"));
    }

    @Test
    @DisplayName("El episodio se deserializa en POJO")
    void getEpisodeDeserializesIntoPojo() {
        Episode episode = client.getEpisode(1).as(Episode.class);

        assertThat(episode.getName()).isEqualTo("Simpsons Roasting on an Open Fire");
        assertThat(episode.getSeason()).isEqualTo(1);
        assertThat(episode.getSynopsis()).isNotBlank();
        assertThat(episode.getImagePath()).startsWith("/");
    }

    @ParameterizedTest(name = "Episodio {0} (temporada {2})")
    @MethodSource("popularEpisodes")
    @DisplayName("Episodios representativos resuelven con su temporada")
    void popularEpisodesResolve(PopularEpisode expected) {
        Episode actual = client.getEpisode(expected.id()).as(Episode.class);

        assertThat(actual.getName()).isEqualTo(expected.name());
        assertThat(actual.getSeason()).isEqualTo(expected.season());
        assertThat(actual.getAirdate()).isNotNull();
    }

    @Test
    @DisplayName("El listado de episodios pagina 20 items")
    void listIsPaginated() {
        PaginatedResponse<Episode> page = client.getEpisodes(1).as(new TypeRef<>() {
        });

        assertThat(page.getCount()).isEqualTo(768);
        assertThat(page.getPages()).isEqualTo(39);
        assertThat(page.getResults()).hasSize(20);
    }

    @Test
    @Tag("contract")
    @DisplayName("El episodio cumple su JSON Schema")
    void episodeMatchesContract() {
        ApiSchemaValidator.validate(client.getEpisode(1), "episode.json").statusCode(200);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> popularEpisodes() {
        return DataReader.readList("/data/popular-episodes.json", PopularEpisode.class)
                .stream()
                .map(org.junit.jupiter.params.provider.Arguments::of);
    }
}
