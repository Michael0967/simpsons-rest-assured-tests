package com.simpsons.tests.locations;

import com.simpsons.BaseApiTest;
import com.simpsons.data.DataReader;
import com.simpsons.data.PopularLocation;
import com.simpsons.models.Location;
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
class LocationsApiTest extends BaseApiTest {

    @Test
    @DisplayName("GET /locations/1 devuelve la casa de los Simpson")
    void getFirstLocationReturnsExpectedData() {
        client.getLocation(1)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("742 Evergreen Terrace"))
                .body("town", equalTo("Springfield"))
                .body("use", equalTo("Residential"));
    }

    @Test
    @DisplayName("La ubicación se deserializa en POJO")
    void getLocationDeserializesIntoPojo() {
        Location location = client.getLocation(1).as(Location.class);

        assertThat(location.getName()).isEqualTo("742 Evergreen Terrace");
        assertThat(location.getTown()).isEqualTo("Springfield");
        assertThat(location.getUse()).isEqualTo("Residential");
        assertThat(location.getImagePath()).startsWith("/");
    }

    @ParameterizedTest(name = "Lugar {0}")
    @MethodSource("popularLocations")
    @DisplayName("Ubicaciones icónicas resuelven en Springfield")
    void popularLocationsResolve(PopularLocation expected) {
        Location actual = client.getLocation(expected.id()).as(Location.class);

        assertThat(actual.getName()).isEqualTo(expected.name());
        assertThat(actual.getTown()).isEqualTo(expected.town());
    }

    @Test
    @DisplayName("El listado de ubicaciones pagina 20 items")
    void listIsPaginated() {
        PaginatedResponse<Location> page = client.getLocations(1).as(new TypeRef<>() {
        });

        assertThat(page.getCount()).isEqualTo(477);
        assertThat(page.getPages()).isEqualTo(24);
        assertThat(page.getResults()).hasSize(20);
    }

    @Test
    @Tag("contract")
    @DisplayName("La ubicación cumple su JSON Schema")
    void locationMatchesContract() {
        ApiSchemaValidator.validate(client.getLocation(1), "location.json").statusCode(200);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> popularLocations() {
        return DataReader.readList("/data/popular-locations.json", PopularLocation.class)
                .stream()
                .map(org.junit.jupiter.params.provider.Arguments::of);
    }
}
