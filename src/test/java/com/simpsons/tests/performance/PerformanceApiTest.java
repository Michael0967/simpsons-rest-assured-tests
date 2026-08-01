package com.simpsons.tests.performance;

import com.simpsons.BaseApiTest;
import com.simpsons.core.ApiConfig;
import com.simpsons.screenplay.Actor;
import com.simpsons.screenplay.abilities.ApiAbility;
import com.simpsons.screenplay.tasks.FetchCharacter;
import com.simpsons.screenplay.tasks.FetchResource;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.simpsons.screenplay.questions.TheResponse.status;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance tests: latency SLA per endpoint and stability under concurrent
 * load. Thresholds are configurable via config.properties
 * (performance.latency.*, performance.concurrency.*).
 * Run: mvn test -Pperformance
 */
@Tag("performance")
class PerformanceApiTest extends BaseApiTest {

    private record LatencySample(long elapsedMs, int status) {
    }

    @FunctionalInterface
    private interface RequestTask {
        Response call(Actor actor);
    }

    @Test
    @DisplayName("Character detail meets the latency SLA")
    void characterDetailMeetsLatencyTarget() throws Exception {
        var samples = fireConcurrently(1, measureIterations(), actor -> {
            actor.attemptsTo(FetchCharacter.withId(1));
            return actor.asksFor(status());
        });
        assertLatency(samples, "GET /characters/1");
    }

    @Test
    @DisplayName("The three list endpoints meet the latency SLA")
    void listEndpointsMeetLatencyTarget() throws Exception {
        for (String resource : List.of("/characters", "/episodes", "/locations")) {
            String target = resource;
            var samples = fireConcurrently(1, measureIterations(), actor -> {
                actor.attemptsTo(FetchResource.named(target));
                return actor.asksFor(status());
            });
            assertLatency(samples, "GET " + resource);
        }
    }

    @Test
    @DisplayName("Under concurrent load the API stays stable and error-free")
    void concurrentLoadStaysStable() throws Exception {
        var samples = fireConcurrently(concurrencyThreads(), concurrencyTotal(), actor -> {
            actor.attemptsTo(FetchCharacter.withId(1));
            return actor.asksFor(status());
        });

        long serverErrors = samples.stream().filter(s -> s.status() >= 500).count();
        long clientErrors = samples.stream().filter(s -> s.status() >= 400 && s.status() < 500).count();

        assertThat(serverErrors).as("5xx errors under load").isZero();
        assertThat(clientErrors).as("4xx errors under load").isZero();
        assertThat(samples).allMatch(s -> s.status() == 200).as("all responses 200");

        assertLatency(samples, "GET /characters/1 (concurrent)");
    }

    private static List<LatencySample> fireConcurrently(int threads, int totalRequests, RequestTask task)
            throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<LatencySample>> futures = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                Actor worker = Actor.named("Load tester").whoCan(ApiAbility.callingTheSimpsonsApi());
                long t0 = System.nanoTime();
                Response response = task.call(worker);
                return new LatencySample(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0),
                        response.getStatusCode());
            }));
        }

        start.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(5, TimeUnit.MINUTES)).as("all threads finished").isTrue();

        List<LatencySample> samples = new ArrayList<>();
        for (Future<LatencySample> future : futures) {
            samples.add(future.get());
        }
        return samples;
    }

    private static void assertLatency(List<LatencySample> samples, String label) {
        double avg = averageMs(samples);
        double p95 = percentileMs(samples, 95);

        assertThat(avg).as("average latency of %s (ms)", label)
                .isLessThanOrEqualTo(avgThresholdMs());
        assertThat(p95).as("p95 of %s (ms)", label)
                .isLessThanOrEqualTo(p95ThresholdMs());
    }

    private static double percentileMs(List<LatencySample> samples, double p) {
        var sorted = samples.stream().map(LatencySample::elapsedMs).sorted().toList();
        int index = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(index, 0));
    }

    private static double averageMs(List<LatencySample> samples) {
        return samples.stream().mapToLong(LatencySample::elapsedMs).average().orElseThrow();
    }

    private static int measureIterations() {
        return ApiConfig.intValue("performance.measure.iterations", 5);
    }

    private static int concurrencyThreads() {
        return ApiConfig.intValue("performance.concurrency.threads", 10);
    }

    private static int concurrencyTotal() {
        return ApiConfig.intValue("performance.concurrency.total", 30);
    }

    private static double avgThresholdMs() {
        return ApiConfig.intValue("performance.latency.avg.ms", 3000);
    }

    private static double p95ThresholdMs() {
        return ApiConfig.intValue("performance.latency.p95.ms", 5000);
    }
}
