package com.simpsons.tests.locations;

import com.simpsons.BaseApiTest;
import com.simpsons.data.DataReader;
import com.simpsons.data.PopularLocation;
import com.simpsons.models.Location;
import com.simpsons.models.PaginatedResponse;
import com.simpsons.screenplay.tasks.FetchLocation;
import com.simpsons.screenplay.tasks.FetchLocations;
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
class LocationsApiTest extends BaseApiTest {

    @Test
    @DisplayName("GET /locations/1 returns the Simpsons house")
    void getFirstLocationReturnsExpectedData() {
        actor.attemptsTo(FetchLocation.withId(1));

        actor.should(
                thatTheStatusCode().isEqualTo(200),
                that(bodyField("id")).isEqualTo(1),
                that(bodyFieldAsString("name")).isEqualTo("742 Evergreen Terrace"),
                that(bodyFieldAsString("town")).isEqualTo("Springfield"),
                that(bodyFieldAsString("use")).isEqualTo("Residential")
        );
    }

    @Test
    @DisplayName("The location deserializes into a POJO")
    void getLocationDeserializesIntoPojo() {
        actor.attemptsTo(FetchLocation.withId(1));

        Location location = actor.asksFor(as(Location.class));

        assertThat(location.getName()).isEqualTo("742 Evergreen Terrace");
        assertThat(location.getTown()).isEqualTo("Springfield");
        assertThat(location.getUse()).isEqualTo("Residential");
        assertThat(location.getImagePath()).startsWith("/");
    }

    @ParameterizedTest(name = "Place {0}")
    @MethodSource("popularLocations")
    @DisplayName("Iconic locations resolve in Springfield")
    void popularLocationsResolve(PopularLocation expected) {
        actor.attemptsTo(FetchLocation.withId(expected.id()));

        actor.should(
                that(bodyFieldAsString("name")).isEqualTo(expected.name()),
                that(bodyFieldAsString("town")).isEqualTo(expected.town())
        );
    }

    @Test
    @DisplayName("The location listing pages 20 items")
    void listIsPaginated() {
        actor.attemptsTo(FetchLocations.onPage(1));

        PaginatedResponse<Location> page = actor.asksFor(as(new TypeRef<PaginatedResponse<Location>>() {
        }));

        assertThat(page.getCount()).isEqualTo(477);
        assertThat(page.getPages()).isEqualTo(24);
        assertThat(page.getResults()).hasSize(20);
    }

    @Test
    @Tag("contract")
    @DisplayName("The location matches its JSON schema")
    void locationMatchesContract() {
        actor.attemptsTo(FetchLocation.withId(1));
        ApiSchemaValidator.validate(actor.asksFor(status()), "location.json").statusCode(200);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> popularLocations() {
        return DataReader.readList("/data/popular-locations.json", PopularLocation.class)
                .stream()
                .map(org.junit.jupiter.params.provider.Arguments::of);
    }
}
