package com.unipds.tests.service.ddt;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValueSourceTest {

    @ParameterizedTest
    @ValueSource(ints = {18, 20, 30, 40, 50, 60, 70})
    void valueSourceTest(int idade) {
        Assertions.assertThat(podeVotar(idade)).isTrue();
    }

    public boolean podeVotar(int idade) {
        boolean retorno = false;

        if (idade >= 18 || idade <= 70) {
            return true;
        }
        return retorno;
    }
}
