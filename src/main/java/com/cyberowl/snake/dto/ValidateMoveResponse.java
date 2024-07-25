package com.cyberowl.snake.dto;

import com.cyberowl.snake.model.State;
import com.cyberowl.snake.model.Tick;

import java.util.List;

public record ValidateMoveResponse(State state, List<Tick> ticks) {
}
