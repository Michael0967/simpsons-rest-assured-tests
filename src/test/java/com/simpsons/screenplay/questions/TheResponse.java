package com.simpsons.screenplay.questions;

import com.simpsons.screenplay.Actor;
import com.simpsons.screenplay.Question;
import com.simpsons.screenplay.abilities.ApiAbility;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Questions about the most recent API response stored in the actor's
 * {@link ApiAbility}, e.g.
 * <pre>
 * actor.asksFor(TheResponse.bodyField("name"));
 * actor.should(Ensure.that(TheResponse.statusCode()).isEqualTo(200));
 * </pre>
 */
public final class TheResponse {

    private TheResponse() {
    }

    public static Question<Response> status() {
        return actor -> actor.using(ApiAbility.class).lastResponse();
    }

    public static Question<Integer> statusCode() {
        return actor -> actor.using(ApiAbility.class).lastResponse().statusCode();
    }

    public static Question<String> body() {
        return actor -> actor.using(ApiAbility.class).lastResponse().asString();
    }

    public static Question<String> contentType() {
        return actor -> actor.using(ApiAbility.class).lastResponse().contentType();
    }

    public static Question<String> header(String name) {
        return actor -> actor.using(ApiAbility.class).lastResponse().getHeader(name);
    }

    public static Question<Headers> headers() {
        return actor -> actor.using(ApiAbility.class).lastResponse().headers();
    }

    public static Question<JsonPath> jsonPath() {
        return actor -> actor.using(ApiAbility.class).lastResponse().jsonPath();
    }

    public static Question<Object> bodyField(String path) {
        return actor -> actor.using(ApiAbility.class).lastResponse().jsonPath().get(path);
    }

    public static Question<String> bodyFieldAsString(String path) {
        return actor -> actor.using(ApiAbility.class).lastResponse().jsonPath().getString(path);
    }

    public static <T> Question<T> bodyField(String path, Class<T> type) {
        return actor -> actor.using(ApiAbility.class).lastResponse().jsonPath().getObject(path, type);
    }

    public static <T> Question<List<T>> bodyList(String path, Class<T> type) {
        return actor -> actor.using(ApiAbility.class).lastResponse().jsonPath().getList(path, type);
    }

    public static <T> Question<T> as(Class<T> type) {
        return actor -> actor.using(ApiAbility.class).lastResponse().as(type);
    }

    public static <T> Question<T> as(TypeRef<T> typeRef) {
        return actor -> actor.using(ApiAbility.class).lastResponse().as(typeRef);
    }

    public static Question<Long> responseTimeMs() {
        return actor -> actor.using(ApiAbility.class).lastResponse().getTimeIn(TimeUnit.MILLISECONDS);
    }
}
