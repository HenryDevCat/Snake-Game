package com.cyberowl.snake.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class State {
    @JsonProperty("gameId")
    @NotBlank(message = "Game ID is required")
    private String gameId;

    @JsonProperty("width")
    @Min(value = 1, message = "Width must be at least 1")
    private int width;

    @JsonProperty("height")
    @Min(value = 1, message = "Height must be at least 1")
    private int height;

    @JsonProperty("score")
    @PositiveOrZero(message = "Score must be non-negative")
    private int score;

    @JsonProperty("fruit")
    @NotNull(message = "Fruit cannot be null")
    @Valid
    private Fruit fruit;

    @JsonProperty("snake")
    @NotNull(message = "Snake cannot be null")
    @Valid
    private Snake snake;
}
