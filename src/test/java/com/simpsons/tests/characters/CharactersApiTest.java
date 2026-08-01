package com.simpsons.tests.characters;

import com.simpsons.BaseApiTest;
import com.simpsons.data.DataReader;
import com.simpsons.data.PopularCharacter;
import com.simpsons.models.Character;
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
import static org.hamcrest.Matchers.*;

@Tag("regression")
class CharactersApiTest extends BaseApiTest {

    @Test
    @DisplayName("GET /characters/1 devuelve a Homer con sus frases")
    void getHomerReturnsExpectedData() {
        client.getCharacter(1)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("Homer Simpson"))
                .body("gender", equalTo("Male"))
                .body("status", equalTo("Alive"))
                .body("portrait_path", equalTo("/character/1.webp"))
                .body("phrases", hasItem("Doh!"))
                .body("phrases", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("El detalle se deserializa en POJO incluyendo anidados")
    void getCharacterDeserializesIntoPojo() {
        Character homer = client.getCharacter(1).as(Character.class);

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

    @ParameterizedTest(name = "Personaje {0} ({1})")
    @MethodSource("popularCharacters")
    @DisplayName("Personajes populares resuelven con sus datos")
    void popularCharactersResolve(PopularCharacter expected) {
        Character actual = client.getCharacter(expected.id()).as(Character.class);

        assertThat(actual.getName()).isEqualTo(expected.name());
        assertThat(actual.getGender()).isEqualTo(expected.gender());
        assertThat(actual.getStatus()).isEqualTo(expected.status());
        assertThat(actual.getPortraitPath()).startsWith("/");
    }

    @Test
    @DisplayName("El listado pagina 20 items y mantiene el total")
    void listIsPaginatedConsistently() {
        PaginatedResponse<Character> page = client.getCharacters(1).as(new TypeRef<>() {
        });

        assertThat(page.getCount()).isEqualTo(1_182);
        assertThat(page.getPages()).isEqualTo(60);
        assertThat(page.getResults()).hasSize(20);
        assertThat(page.getPrev()).isNull();
        assertThat(page.getNext()).contains("page=2");
    }

    @Test
    @DisplayName("Los IDs son contiguos: el 1182 existe y el 1183 no")
    void characterIdsAreContiguous() {
        client.getCharacter(1_182).then().statusCode(200).body("id", equalTo(1_182));
        client.getCharacter(1_183).then().statusCode(404);
    }

    @Test
    @DisplayName("La primera aparición del personaje coincide con el endpoint de episodios")
    void firstAppearanceMatchesEpisodeResource() {
        Character homer = client.getCharacter(1).as(Character.class);

        Episode episode = client.getEpisode(homer.getFirstAppearanceEpId()).as(Episode.class);
        assertThat(episode.getName()).isEqualTo(homer.getFirstAppearanceEp().getName());
        assertThat(episode.getId()).isEqualTo(homer.getFirstAppearanceEp().getId());
    }

    @Test
    @Tag("contract")
    @DisplayName("El detalle de personaje cumple su JSON Schema")
    void characterDetailMatchesContract() {
        ApiSchemaValidator.validate(client.getCharacter(1), "character-detail.json")
                .statusCode(200);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> popularCharacters() {
        return DataReader.readList("/data/popular-characters.json", PopularCharacter.class)
                .stream()
                .map(org.junit.jupiter.params.provider.Arguments::of);
    }
}
