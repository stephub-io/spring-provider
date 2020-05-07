package io.stephub.provider.util.spring;

import io.stephub.provider.api.model.ProviderOptions;
import io.stephub.provider.api.model.StepRequest;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.provider.util.LocalProviderAdapter;
import io.stephub.provider.util.spring.annotation.StepArgument;
import io.stephub.provider.util.spring.annotation.StepMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static java.time.Duration.ofMinutes;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {StepMethodAnnotationProcessor.class, StepMethodAnnotationProcessorTest.SomeBean.class})
class StepMethodAnnotationProcessorTest {

    public static class TestProvider extends SpringBeanProvider<LocalProviderAdapter.SessionState<Object>, Object, Class<?>, Object> {
        private final TestProvider mock = mock(TestProvider.class);

        {
            try {
                when(this.mock.testStepNoArgs()).thenReturn(new StepResponse());
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        private SessionState<Object> state;

        @StepMethod(pattern = "Bla bla")
        public StepResponse testStepNoArgs() throws InterruptedException {
            Thread.sleep(1000);
            return this.mock.testStepNoArgs();
        }

        @StepMethod(pattern = "Bla bla multiple")
        public StepResponse testStepMultipleArgs(final SessionState<Object> someState,
                                                 @StepArgument(name = "enabled") final boolean arg1,
                                                 @StepArgument(name = "data") final String arg2) {
            return this.mock.testStepMultipleArgs(someState, arg1, arg2);
        }

        @Override
        protected SessionState<Object> startState(final String sessionId, final ProviderOptions<Object> options) {
            this.state = mock(SessionState.class);
            return this.state;
        }

        @Override
        protected void stopState(final SessionState<Object> state) {

        }

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public Class<Object> getOptionsSchema() {
            return Object.class;
        }
    }

    @Component
    public static class SomeBean {
        private final SomeBean mock = mock(SomeBean.class);

        @StepMethod(pattern = "Bla bla blub", provider = TestProvider.class)
        public StepResponse testStepExternalNoArgs() {
            return this.mock.testStepExternalNoArgs();
        }
    }

    @SpyBean
    private TestProvider testProvider;

    @Autowired
    private SomeBean externalBean;

    @Test
    public void testStepNoArgs() throws InterruptedException {
        final String sid = this.testProvider.createSession(ProviderOptions.builder().sessionTimeout(ofMinutes(1)).build());
        final StepResponse response = this.testProvider.execute(sid, StepRequest.builder().id("testStepNoArgs").build());
        verify(this.testProvider.mock).testStepNoArgs();
        assertThat(response.getDuration().getSeconds(), greaterThanOrEqualTo(1l));
    }

    @Test
    public void testStepMultipleArgs() {
        final String sid = this.testProvider.createSession(ProviderOptions.builder().sessionTimeout(ofMinutes(1)).build());
        this.testProvider.execute(sid, StepRequest.builder().
                id("testStepMultipleArgs").
                argument("data", "dddd").
                argument("enabled", true).
                build());
        verify(this.testProvider.mock).testStepMultipleArgs(
                this.testProvider.state,
                true,
                "dddd"
        );
    }

    @Test
    public void testExternalStepNoArgs() {
        final String sid = this.testProvider.createSession(ProviderOptions.builder().sessionTimeout(ofMinutes(1)).build());
        this.testProvider.execute(sid, StepRequest.builder().id("testStepExternalNoArgs").build());
        verify(this.externalBean.mock).testStepExternalNoArgs();
    }

}