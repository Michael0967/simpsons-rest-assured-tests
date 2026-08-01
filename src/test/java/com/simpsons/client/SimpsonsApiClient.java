package com.simpsons.client;

import com.simpsons.core.RestClient;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

/**
 * Fachada de alto nivel sobre los endpoints de The Simpsons API.
 *
 * Los tests no interactúan con Rest Assured directamente: llaman a métodos
 * tipados de aquí. Si el endpoint cambia, solo cambia esta clase.
 */
public class SimpsonsApiClient {

    private final RequestSpecification spec = new RestClient().spec();

    // ------------------------------------------------------------------
    // Characters
    // ------------------------------------------------------------------

    public Response getCharacters() {
        return get("/characters");
    }

    public Response getCharacters(int page) {
        return getWithPage("/characters", page);
    }

    public Response getCharacter(int id) {
        return get("/characters/{id}", id);
    }

    // ------------------------------------------------------------------
    // Episodes
    // ------------------------------------------------------------------

    public Response getEpisodes() {
        return get("/episodes");
    }

    public Response getEpisodes(int page) {
        return getWithPage("/episodes", page);
    }

    public Response getEpisode(int id) {
        return get("/episodes/{id}", id);
    }

    // ------------------------------------------------------------------
    // Locations
    // ------------------------------------------------------------------

    public Response getLocations() {
        return get("/locations");
    }

    public Response getLocations(int page) {
        return getWithPage("/locations", page);
    }

    public Response getLocation(int id) {
        return get("/locations/{id}", id);
    }

    // ------------------------------------------------------------------
    // Casos de error / genéricos
    // ------------------------------------------------------------------

    public Response getResourcePage(String resource, int page) {
        return getWithPage(resource, page);
    }

    public Response get(String resource) {
        return get(resource, new Object[0]);
    }

    public Response get(String resource, Object... pathParams) {
        return io.restassured.RestAssured.given()
                .spec(spec)
                .when()
                .get(resource, pathParams);
    }

    public Response post(String resource) {
        return io.restassured.RestAssured.given()
                .spec(spec)
                .when()
                .post(resource);
    }

    public Response request(String method, String resource) {
        return io.restassured.RestAssured.given()
                .spec(spec)
                .when()
                .request(method, resource);
    }

    public Response getWithHeaders(String resource, Map<String, String> headers) {
        return io.restassured.RestAssured.given()
                .spec(spec)
                .headers(headers)
                .when()
                .get(resource);
    }

    public Response delete(String resource, int id) {
        return io.restassured.RestAssured.given()
                .spec(spec)
                .when()
                .delete(resource + "/{id}", id);
    }

    private Response getWithPage(String resource, int page) {
        return io.restassured.RestAssured.given()
                .spec(spec)
                .queryParam("page", page)
                .when()
                .get(resource);
    }
}
