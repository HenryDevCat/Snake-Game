package com.cyberowl.snake.service;

import com.cyberowl.snake.dto.NewGameResponse;
import com.cyberowl.snake.dto.ValidateMoveRequest;
import com.cyberowl.snake.dto.ValidateMoveResponse;

public interface SnakeService {
    NewGameResponse newGame(int width, int height);

    ValidateMoveResponse validateMove(ValidateMoveRequest validateMoveRequest);
}
