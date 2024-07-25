package com.cyberowl.snake.service.impl;

import com.cyberowl.snake.constants.SnakeConstants;
import com.cyberowl.snake.dto.NewGameResponse;
import com.cyberowl.snake.dto.ValidateMoveRequest;
import com.cyberowl.snake.dto.ValidateMoveResponse;
import com.cyberowl.snake.exception.FruitNotReachedException;
import com.cyberowl.snake.exception.GameOverException;
import com.cyberowl.snake.model.*;
import com.cyberowl.snake.service.SnakeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class SnakeServiceImpl implements SnakeService {

    @Override
    public NewGameResponse newGame(int width, int height) {
        log.info("Starting new game with width: {} and height: {}", width, height);
        GridSize gridSize = new GridSize(width, height);
        State state = State.builder()
                .gameId(UUID.randomUUID().toString())
                .width(width)
                .height(height)
                .score(SnakeConstants.INITIAL_SCORE)
                .fruit(Fruit.generateRandomPosition(gridSize, 1))
                .snake(new Snake(SnakeConstants.INITIAL_SNAKE_POSITION, SnakeConstants.INITIAL_SNAKE_VELOCITY))
                .build();
        return new NewGameResponse(state);
    }

    @Override
    public ValidateMoveResponse validateMove(ValidateMoveRequest validateMoveRequest) {
        State state = validateMoveRequest.getState();
        List<Tick> ticks = validateMoveRequest.getTicks();

        for (Tick tick : ticks) {
            if (!isValidMove(state, tick)) {
                log.warn("Invalid move detected. Game over.");
                throw new GameOverException(SnakeConstants.GAME_OVER);
            }
            applyMove(state, tick);
            if (hasReachedFruit(state)) {
                updateGameState(state);
                log.info("Fruit reached. Updated game state.");
                return new ValidateMoveResponse(state, ticks);
            }
        }

        log.info("All ticks processed. Fruit not reached.");
        throw new FruitNotReachedException(SnakeConstants.FRUIT_NOT_REACHED);
    }

    private boolean isValidMove(State state, Tick tick) {
        Snake snake = state.getSnake();
        Velocity snakeVelocity = snake.getVelocity();
        Velocity tickVelocity = tick.getVelocity();

        if (isReverseDirection(snakeVelocity, tickVelocity)) {
            log.warn(SnakeConstants.REVERSE_DIRECTION);
            return false;
        }

        int nextXCoordinate = snake.getCoordinate().x() + tickVelocity.velX();
        int nextYCoordinate = snake.getCoordinate().y() + -(tickVelocity.velY()); // Invert y velocity: positive y is downward in grid coordinate system

        if (isOutOfBounds(state, nextXCoordinate, nextYCoordinate)) {
            log.warn(SnakeConstants.OUT_OF_BOUNDS);
            return false;
        }

        return true;
    }

    // Negates tickVelocity to check if it's the opposite of snake velocity, preventing reversal
    private boolean isReverseDirection(Velocity snakeVelocity, Velocity tickVelocity) {
        int snakeVelX = snakeVelocity.velX();
        int snakeVelY = snakeVelocity.velY();
        int tickVelX = tickVelocity.velX();
        int tickVelY = tickVelocity.velY();

        // Check for L-movement
        if ((snakeVelX == -1 && snakeVelY == 1 && tickVelX == 1 && tickVelY == -1) ||
                (snakeVelX == 1 && snakeVelY == -1 && tickVelX == -1 && tickVelY == 1) ||
                (snakeVelX == -1 && snakeVelY == -1 && tickVelX == 1 && tickVelY == 1) ||
                (snakeVelX == 1 && snakeVelY == 1 && tickVelX == -1 && tickVelY == -1)) {
            return false;
        }

        // Check for reverse direction
        return snakeVelX == -(tickVelX) && snakeVelY == -(tickVelY);
    }

    private boolean isOutOfBounds(State state, int x, int y) {
        return x < 0 || x > state.getWidth() ||
                y < 0 || y > state.getHeight();
    }

    private void applyMove(State state, Tick tick) {
        state.getSnake().move(tick);
    }

    private boolean hasReachedFruit(State state) {
        return state.getSnake().getCoordinate().equals(state.getFruit().getCoordinate());
    }

    private void updateGameState(State state) {
        state.setScore(state.getScore() + SnakeConstants.POINTS_PER_FRUIT);
        state.setFruit(Fruit.generateRandomPosition(state.getWidth(), state.getHeight(), 0));
    }
}
