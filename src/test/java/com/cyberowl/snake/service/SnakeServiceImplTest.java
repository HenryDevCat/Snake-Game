package com.cyberowl.snake.service;

import com.cyberowl.snake.constants.SnakeConstants;
import com.cyberowl.snake.dto.NewGameResponse;
import com.cyberowl.snake.dto.ValidateMoveRequest;
import com.cyberowl.snake.dto.ValidateMoveResponse;
import com.cyberowl.snake.exception.FruitNotReachedException;
import com.cyberowl.snake.exception.GameOverException;
import com.cyberowl.snake.model.*;
import com.cyberowl.snake.service.impl.SnakeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class SnakeServiceImplTest {

    private static final String correlationId = UUID.randomUUID().toString();
    private static final int GRID_WIDTH = 5;
    private static final int GRID_HEIGHT = 5;

    private SnakeServiceImpl snakeServiceImpl;

    @BeforeEach
    void setUp() {
        snakeServiceImpl = new SnakeServiceImpl();
    }

    @Nested
    class NewGameTests {
        @ParameterizedTest
        @CsvSource({"1,1", "5,10", "15,15", "50,75", "100,100", "1000,1000"})
        void newGame_shouldInitializeCorrectly(int width, int height) {
            NewGameResponse response = snakeServiceImpl.newGame(width, height);

            assertNotNull(response.state().getGameId());
            assertEquals(width, response.state().getWidth());
            assertEquals(height, response.state().getHeight());
            assertEquals(SnakeConstants.INITIAL_SCORE, response.state().getScore());
            assertNotNull(response.state().getFruit());
            assertNotNull(response.state().getSnake());
            assertEquals(SnakeConstants.INITIAL_SNAKE_POSITION, response.state().getSnake().getCoordinate());
            assertEquals(SnakeConstants.INITIAL_SNAKE_VELOCITY, response.state().getSnake().getVelocity());
        }

        @Test
        void newGame_shouldGenerateFruitWithinBoundaries() {
            int width = GRID_WIDTH, height = GRID_HEIGHT;
            NewGameResponse response = snakeServiceImpl.newGame(width, height);

            Coordinate fruitCoordinate = response.state().getFruit().getCoordinate();
            assertTrue(fruitCoordinate.x() >= 1 && fruitCoordinate.x() <= width);
            assertTrue(fruitCoordinate.y() >= 1 && fruitCoordinate.y() <= height);
        }

        @Test
        void newGame_shouldGenerateUniqueFruitPositions() {
            int iterations = 1000;
            Set<Coordinate> fruitPositions = new HashSet<>();

            for (int i = 0; i < iterations; i++) {
                NewGameResponse response = snakeServiceImpl.newGame(GRID_WIDTH, GRID_HEIGHT);
                fruitPositions.add(response.state().getFruit().getCoordinate());
            }

            int uniquePositions = fruitPositions.size();
            int totalPositions = GRID_WIDTH * GRID_HEIGHT;
            double coveragePercentage = (uniquePositions * 100.0) / totalPositions;

            log.info("Unique fruit positions: {}/{} ({}%)",
                    uniquePositions, totalPositions, String.format("%.2f", coveragePercentage));

            assertTrue(uniquePositions > 1, "Multiple unique fruit positions should be generated");
            assertTrue(coveragePercentage > 50, "At least 50% of possible positions should be covered");
        }
    }

    @Nested
    class ValidateMoveTests {
        @Test
        void validateMove_shouldHandleValidMoves_Minimum1x1GridFruitReached() {
            State initialState = createInitialState(GRID_WIDTH, GRID_HEIGHT, 1, 1);
            List<Tick> ticks = List.of(
                    new Tick(new Velocity(1, 0)),
                    new Tick(new Velocity(0, -1))
            );
            ValidateMoveRequest request = new ValidateMoveRequest(initialState, ticks);
            ValidateMoveResponse response = snakeServiceImpl.validateMove(request);

            assertEquals(SnakeConstants.POINTS_PER_FRUIT, response.state().getScore());
            assertEquals(new Coordinate(1, 1), response.state().getSnake().getCoordinate());
            assertEquals(new Velocity(0, -1), response.state().getSnake().getVelocity());
        }

        @Test
        void validateMove_shouldHandleValidMoves_5x5GridOffCenterFruitReached() {
            State initialState = createInitialState(GRID_WIDTH, GRID_HEIGHT, 2, 3);
            List<Tick> ticks = List.of(
                    new Tick(new Velocity(1, 0)),
                    new Tick(new Velocity(0, -1)),
                    new Tick(new Velocity(0, -1)),
                    new Tick(new Velocity(1, -1))
            );
            ValidateMoveRequest request = new ValidateMoveRequest(initialState, ticks);
            ValidateMoveResponse response = snakeServiceImpl.validateMove(request);

            assertEquals(SnakeConstants.POINTS_PER_FRUIT, response.state().getScore());
            assertEquals(new Coordinate(2, 3), response.state().getSnake().getCoordinate());
            assertEquals(new Velocity(1, -1), response.state().getSnake().getVelocity());
        }

        @Test
        void validateMove_shouldHandleValidMoves_5x5GridFurthestFruitReached() {
            State initialState = createInitialState(GRID_WIDTH, GRID_HEIGHT, 5, 5);
            List<Tick> ticks = List.of(
                    // Wrong moves to fruit
                    new Tick(new Velocity(1, 0)),
                    new Tick(new Velocity(1, -1)),
                    new Tick(new Velocity(0, -1)),
                    new Tick(new Velocity(-1, 0)),
                    new Tick(new Velocity(0, 1)),
                    new Tick(new Velocity(-1, 1)),
                    // Wrong moves, then L-movement to correct moves

                    // Right moves to fruit
                    new Tick(new Velocity(1, -1)),
                    new Tick(new Velocity(1, 0)),
                    new Tick(new Velocity(0, -1)),
                    // L-movement
                    new Tick(new Velocity(1, 1)),
                    new Tick(new Velocity(-1, -1)),
                    new Tick(new Velocity(1, -1)),
                    new Tick(new Velocity(-1, 1)),
                    new Tick(new Velocity(-1, -1)),
                    new Tick(new Velocity(1, 1)),
                    // Normal movement
                    new Tick(new Velocity(1, -1)),
                    new Tick(new Velocity(0, -1)),
                    new Tick(new Velocity(1, 0)),
                    new Tick(new Velocity(1, -1))
            );
            ValidateMoveRequest request = new ValidateMoveRequest(initialState, ticks);
            ValidateMoveResponse response = snakeServiceImpl.validateMove(request);

            assertEquals(SnakeConstants.POINTS_PER_FRUIT, response.state().getScore());
            assertEquals(new Coordinate(5, 5), response.state().getSnake().getCoordinate());
            assertEquals(new Velocity(1, -1), response.state().getSnake().getVelocity());
        }

        @Test
        void validateMove_shouldThrowGameOverException_whenReverseDirection() {
            State initialState = createInitialState(GRID_WIDTH, GRID_HEIGHT, 1, 2);
            List<Tick> ticks = List.of(
                    new Tick(new Velocity(1, 0)),
                    new Tick(new Velocity(-1, 0))
            );
            ValidateMoveRequest request = new ValidateMoveRequest(initialState, ticks);

            assertThrows(GameOverException.class, () -> snakeServiceImpl.validateMove(request));
        }

        @Test
        void validateMove_shouldThrowGameOverException_whenMovingOutOfLeftBound() {
            State initialState = createInitialState(GRID_WIDTH, GRID_HEIGHT, 2, 2);
            List<Tick> ticks = List.of(
                    new Tick(new Velocity(1, 0)),
                    new Tick(new Velocity(0, -1)),
                    new Tick(new Velocity(-1, 1)),
                    new Tick(new Velocity(-1, 0))
            );
            ValidateMoveRequest request = new ValidateMoveRequest(initialState, ticks);

            assertThrows(GameOverException.class, () -> snakeServiceImpl.validateMove(request));
        }

        @Test
        void validateMove_shouldThrowGameOverException_whenMovingOutOfRightBound() {
            State initialState = createInitialState(GRID_WIDTH, GRID_HEIGHT, 2, 2);
            List<Tick> ticks = List.of(
                    new Tick(new Velocity(1, -1)),
                    new Tick(new Velocity(1, 0)),
                    new Tick(new Velocity(1, 1)),
                    new Tick(new Velocity(1, 0)),
                    new Tick(new Velocity(1, -1)),
                    new Tick(new Velocity(1, 0))
            );
            ValidateMoveRequest request = new ValidateMoveRequest(initialState, ticks);

            assertThrows(GameOverException.class, () -> snakeServiceImpl.validateMove(request));
        }

        @Test
        void validateMove_shouldThrowGameOverException_whenMovingOutOfUpperBound() {
            State initialState = createInitialState(GRID_WIDTH, GRID_HEIGHT, 2, 2);
            List<Tick> ticks = List.of(
                    new Tick(new Velocity(1, -1)),
                    new Tick(new Velocity(0, -1)),
                    new Tick(new Velocity(-1, -1)),
                    new Tick(new Velocity(0, -1)),
                    new Tick(new Velocity(1, -1)),
                    new Tick(new Velocity(0, -1))
            );
            ValidateMoveRequest request = new ValidateMoveRequest(initialState, ticks);

            assertThrows(GameOverException.class, () -> snakeServiceImpl.validateMove(request));
        }

        @Test
        void validateMove_shouldThrowGameOverException_whenMovingOutOfLowerBound() {
            State initialState = createInitialState(GRID_WIDTH, GRID_HEIGHT, 2, 2);
            List<Tick> ticks = List.of(
                    new Tick(new Velocity(0, 1)),
                    new Tick(new Velocity(1, 0))
            );
            ValidateMoveRequest request = new ValidateMoveRequest(initialState, ticks);

            assertThrows(GameOverException.class, () -> snakeServiceImpl.validateMove(request));
        }

        @Test
        void validateMove_shouldThrowFruitNotReachedException_whenFruitNotReached() {
            State initialState = createInitialState(GRID_WIDTH, GRID_HEIGHT, 2, 1);
            List<Tick> ticks = List.of(
                    new Tick(new Velocity(1, 0)),
                    new Tick(new Velocity(0, -1)),
                    new Tick(new Velocity(0, -1))
            );
            ValidateMoveRequest request = new ValidateMoveRequest(initialState, ticks);

            assertThrows(FruitNotReachedException.class, () -> snakeServiceImpl.validateMove(request));
        }
    }

    @Nested
    class EdgeCaseTests {
        @Test
        void validateMove_shouldHandleLongSequenceOfMoves() {
            State initialState = createInitialState(1000, 1000, 1000, 1000);
            List<Tick> ticks = IntStream.range(0, 1000)
                    .mapToObj(i -> new Tick(new Velocity(1, -1)))
                    .toList();
            ValidateMoveRequest request = new ValidateMoveRequest(initialState, ticks);
            ValidateMoveResponse response = snakeServiceImpl.validateMove(request);

            assertEquals(new Coordinate(1000, 1000), response.state().getSnake().getCoordinate());
        }
    }

    private State createInitialState(int width, int height,
                                     int fruitX, int fruitY) {
        return State.builder()
                .gameId(correlationId)
                .width(width)
                .height(height)
                .score(0)
                .fruit(Fruit.generateFixedPosition(fruitX, fruitY))
                .snake(new Snake(new Coordinate(0, 0), new Velocity(1, 0)))
                .build();
    }
}
