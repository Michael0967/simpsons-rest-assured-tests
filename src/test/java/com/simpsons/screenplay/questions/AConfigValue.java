package com.simpsons.screenplay.questions;

import com.simpsons.core.ApiConfig;
import com.simpsons.screenplay.Actor;
import com.simpsons.screenplay.Question;

/**
 * Questions that read configuration values, so scenarios can verify behaviour
 * driven by {@code config.properties} (e.g. the base URI scheme).
 */
public final class AConfigValue {

    private AConfigValue() {
    }

    public static Question<String> string(String key) {
        return actor -> ApiConfig.value(key);
    }

    public static Question<Integer> intValue(String key) {
        return actor -> ApiConfig.intValue(key, 0);
    }
}
