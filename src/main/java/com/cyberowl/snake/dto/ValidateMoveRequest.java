package com.cyberowl.snake.dto;

import com.cyberowl.snake.model.State;
import com.cyberowl.snake.model.Tick;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
public class ValidateMoveRequest {
    @JsonProperty("state")
    @NotNull(message = "State cannot be null")
    @Valid
    private State state;

    @JsonProperty("ticks")
    @NotEmpty(message = "Ticks cannot be empty")
    @Valid
    private List<Tick> ticks;
}
