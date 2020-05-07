package io.stephub.provider.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode
public class StepResponse<VALUE> {
    public enum StepStatus {
        PASSED, FAILED;

        @Override
        @JsonValue
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    private StepStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Duration duration;
    @Singular
    private Map<String, VALUE> outputs = new HashMap<>();
}