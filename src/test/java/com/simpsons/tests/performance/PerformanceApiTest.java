package com.simpsons.tests.performance;

import com.simpsons.BaseApiTest;
import com.simpsons.client.SimpsonsApiClient;
import com.simpsons.core.ApiConfig;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de performance: SLA de latencia por endpoint y estabilidad
 * bajo carga concurrente. Los umbrales son configurables vía
 * config.properties (performance.latency.*, performance.concurrency.*).
 * Correr: mvn test -Pperformance
 */
@Tag("performance")
class PerformanceApiTest extends BaseApiTest {

    private record LatencySample(long elapsedMs, int status) {
    }

    @FunctionalInterface
    private interface RequestTask {
        Response call(SimpsonsApiClient client) throws Exception;
    }

    @Test
    @DisplayName("El detalle de personaje cumple el SLA de latencia")
    void characterDetailMeetsLatencyTarget() throws Exception {
        var samples = fireConcurrently(1, measureIterations(), c -> c.getCharacter(1));
        assertLatency(samples, "GET /characters/1");
    }

    @Test
    @DisplayName("Los listados de los tres recursos cumplen el SLA de latencia")
    void listEndpointsMeetLatencyTarget() throws Exception {
        for (String resource : List.of("/characters", "/episodes", "/locations")) {
            var samples = fireConcurrently(1, measureIterations(), c -> c.get(resource));
            assertLatency(samples, "GET " + resource);
        }
    }

    @Test
    @DisplayName("Bajo carga concurrente la API se mantiene estable y sin errores")
    void concurrentLoadStaysStable() throws Exception {
        var samples = fireConcurrently(concurrencyThreads(), concurrencyTotal(), c -> c.getCharacter(1));

        long serverErrors = samples.stream().filter(s -> s.status() >= 500).count();
        long clientErrors = samples.stream().filter(s -> s.status() >= 400 && s.status() < 500).count();

        assertThat(serverErrors).as("errores 5xx bajo carga").isZero();
        assertThat(clientErrors).as("errores 4xx bajo carga").isZero();
        assertThat(samples).allMatch(s -> s.status() == 200).as("todas las respuestas 200");

        assertLatency(samples, "GET /characters/1 (concurrente)");
    }

    private static List<LatencySample> fireConcurrently(int threads, int totalRequests, RequestTask task)
            throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<LatencySample>> futures = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                SimpsonsApiClient client = new SimpsonsApiClient();
                long t0 = System.nanoTime();
                Response response = task.call(client);
                return new LatencySample(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0),
                        response.getStatusCode());
            }));
        }

        start.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(5, TimeUnit.MINUTES)).as("todos los hilos terminaron").isTrue();

        List<LatencySample> samples = new ArrayList<>();
        for (Future<LatencySample> future : futures) {
            samples.add(future.get());
        }
        return samples;
    }

    private static void assertLatency(List<LatencySample> samples, String label) {
        double avg = averageMs(samples);
        double p95 = percentileMs(samples, 95);

        assertThat(avg).as("latencia media de %s (ms)", label)
                .isLessThanOrEqualTo(avgThresholdMs());
        assertThat(p95).as("p95 de %s (ms)", label)
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
