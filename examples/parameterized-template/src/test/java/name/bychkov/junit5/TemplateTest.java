package name.bychkov.junit5;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import name.bychkov.junit5.params.ParameterizedTemplate;
import name.bychkov.junit5.params.provider.ValueSource;

@ParameterizedTemplate
@ValueSource(strings = { "test-value-1", "test-value-2" })
public abstract class TemplateTest {
    private String constructorParameter;

    public TemplateTest(String constructorParameter) {
        this.constructorParameter = constructorParameter;
    }

    @Test
    public void test1() {
        Assertions.assertNotNull(constructorParameter);
    }

    @Test
    public void test2() {
        Assertions.assertEquals("test-value-1", constructorParameter);
    }
}