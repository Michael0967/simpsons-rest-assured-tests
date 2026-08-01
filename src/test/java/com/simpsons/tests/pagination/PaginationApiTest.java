package com.simpsons.tests.pagination;

import com.simpsons.BaseApiTest;
import com.simpsons.screenplay.tasks.FetchPage;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.simpsons.screenplay.questions.TheResponse.jsonPath;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pagination invariants: page size, next/prev links, partial last page and
 * behaviour on out-of-range pages.
 */
@Tag("regression")
class PaginationApiTest extends BaseApiTest {

    private static final int PAGE_SIZE = 20;

    static Stream<Arguments> resources() {
        return Stream.of(
                Arguments.of("/characters", 1_182, 60, 2),
                Arguments.of("/episodes", 768, 39, 8),
                Arguments.of("/locations", 477, 24, 17)
        );
    }

    @ParameterizedTest(name = "{0}: count={1}, pages={2}, last={3}")
    @MethodSource("resources")
    @DisplayName("Pagination invariants hold per resource")
    void paginationInvariants(String resource, int count, int pages, int lastPageSize) {
        JsonPath first = page(resource, 1);
        assertThat(first.getInt("count")).as("count of %s", resource).isEqualTo(count);
        assertThat(first.getInt("pages")).as("pages of %s", resource).isEqualTo(pages);
        assertThat(first.getList("results")).as("first page of %s", resource).hasSize(PAGE_SIZE);
        assertThat(first.getString("prev")).isNull();
        assertThat(first.getString("next")).contains("page=2");

        JsonPath last = page(resource, pages);
        assertThat(last.getList("results")).as("last page of %s", resource).hasSize(lastPageSize);
        assertThat(last.getString("next")).isNull();
        assertThat(last.getString("prev")).contains("page=" + (pages - 1));

        JsonPath outOfRange = page(resource, pages + 1);
        assertThat(outOfRange.getList("results")).as("out of range on %s", resource).isEmpty();

        assertThat(count).as("consistency count = 20*(pages-1)+last, on %s", resource)
                .isEqualTo(PAGE_SIZE * (pages - 1) + lastPageSize);
    }

    @Test
    @DisplayName("Consecutive character pages neither overlap nor skip items")
    void consecutivePagesDoNotOverlap() {
        List<Integer> firstPage = page("/characters", 1).getList("results.id", Integer.class);
        List<Integer> secondPage = page("/characters", 2).getList("results.id", Integer.class);

        assertThat(firstPage).hasSize(PAGE_SIZE);
        assertThat(secondPage).hasSize(PAGE_SIZE);
        assertThat(firstPage.getLast()).isEqualTo(20);
        assertThat(secondPage.getFirst()).isEqualTo(21);
        assertThat(secondPage).doesNotContainAnyElementsOf(firstPage);
    }

    @Test
    @DisplayName("page=0 falls back to the first page")
    void pageZeroFallsBackToFirstPage() {
        List<Integer> ids = page("/characters", 0).getList("results.id", Integer.class);
        assertThat(ids.getFirst()).isEqualTo(1);
    }

    private JsonPath page(String resource, int pageNumber) {
        actor.attemptsTo(FetchPage.of(resource).number(pageNumber));
        return actor.asksFor(jsonPath());
    }
}
