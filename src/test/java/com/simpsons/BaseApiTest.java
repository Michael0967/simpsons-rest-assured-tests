package com.simpsons;

import com.simpsons.client.SimpsonsApiClient;

/**
 * Clase base de todos los tests: expone el cliente de API y el validador
 * de contratos sin duplicarlos en cada suite.
 */
public abstract class BaseApiTest {

    protected static final SimpsonsApiClient client = new SimpsonsApiClient();
}
