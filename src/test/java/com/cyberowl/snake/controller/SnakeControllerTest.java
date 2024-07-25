package com.cyberowl.snake.controller;

import com.cyberowl.snake.constants.SnakeConstants;
import com.cyberowl.snake.dto.NewGameResponse;
import com.cyberowl.snake.dto.ValidateMoveRequest;
import com.cyberowl.snake.dto.ValidateMoveResponse;
import com.cyberowl.snake.exception.FruitNotReachedException;
import com.cyberowl.snake.exception.GameOverException;
import com.cyberowl.snake.model.*;
import com.cyberowl.snake.service.SnakeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SnakeController.class)
class SnakeControllerTest {

    private static final String NEW_GAME_ENDPOINT = "/api/v1/snake/new";
    private static final String VALIDATE_MOVE_ENDPOINT = "/api/v1/snake/validate";

    private static final String correlationId = UUID.randomUUID().toString();
    private static final int GRID_WIDTH = 5;
    private static final int GRID_HEIGHT = 5;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SnakeService snakeService;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @CsvSource({"1,1", "5,10", "15,15", "50,75", "100,100", "1000,1000"})
    void newGame_shouldReturnNewGameResponse_whenValidDimensions(int width, int height) throws Exception {
        NewGameResponse expectedResponse = createExpectedNewGameResponse(width, height);

        when(snakeService.newGame(width, height)).thenReturn(expectedResponse);

        mockMvc.perform(get(NEW_GAME_ENDPOINT)
                        .param("w", String.valueOf(width))
                        .param("h", String.valueOf(height))
                        .header(SnakeConstants.CORRELATION_ID_HEADER, correlationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state.gameId").value(expectedResponse.state().getGameId()))
                .andExpect(jsonPath("$.state.width").value(width))
                .andExpect(jsonPath("$.state.height").value(height))
                .andExpect(jsonPath("$.state.score").value(0))
                .andExpect(jsonPath("$.state.fruit.coordinate.x").value(greaterThan(0)))
                .andExpect(jsonPath("$.state.fruit.coordinate.x").value(greaterThan(0)))
                .andExpect(jsonPath("$.state.snake.coordinate.x").value(0))
                .andExpect(jsonPath("$.state.snake.coordinate.y").value(0))
                .andExpect(jsonPath("$.state.snake.velocity.velX").value(1))
                .andExpect(jsonPath("$.state.snake.velocity.velY").value(0))
                .andExpect(header().string(SnakeConstants.CORRELATION_ID_HEADER, correlationId));
    }

    @Test
    void newGame_shouldHandleVeryLargeDimensions() throws Exception {
        // Use MAX_VALUE - 1 to prevent overflow when generating random position
        int width = Integer.MAX_VALUE - 1;
        int height = Integer.MAX_VALUE - 1;
        NewGameResponse expectedResponse = createExpectedNewGameResponse(width, height);

        when(snakeService.newGame(width, height)).thenReturn(expectedResponse);

        mockMvc.perform(get(NEW_GAME_ENDPOINT)
                        .param("w", String.valueOf(width))
                        .param("h", String.valueOf(height))
                        .header(SnakeConstants.CORRELATION_ID_HEADER, correlationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state.width").value(width))
                .andExpect(jsonPath("$.state.height").value(height));
    }

    @Test
    void newGame_shouldGenerateCorrelationId_whenNotProvided() throws Exception {
        NewGameResponse expectedResponse = createExpectedNewGameResponse(GRID_WIDTH, GRID_HEIGHT);
        when(snakeService.newGame(GRID_WIDTH, GRID_HEIGHT)).thenReturn(expectedResponse);

        mockMvc.perform(get(NEW_GAME_ENDPOINT)
                        .param("w", String.valueOf(GRID_WIDTH))
                        .param("h", String.valueOf(GRID_HEIGHT)))
                .andExpect(status().isOk())
                .andExpect(header().exists(SnakeConstants.CORRELATION_ID_HEADER))
                .andExpect(header().string(SnakeConstants.CORRELATION_ID_HEADER, matchesPattern("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}")));
    }

    @ParameterizedTest
    @CsvSource({"-1,-1", "-1,0", "0,-1", "0,0", "1,0", "0,1"})
    void newGame_shouldReturnBadRequest_whenInvalidDimensions(int width, int height) throws Exception {
        mockMvc.perform(get(NEW_GAME_ENDPOINT)
                        .param("w", String.valueOf(width))
                        .param("h", String.valueOf(height))
                        .header(SnakeConstants.CORRELATION_ID_HEADER, correlationId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(SnakeConstants.VALIDATION_ERROR))
                .andExpect(result -> {
                    if (width < 1) {
                        jsonPath("$.details['newGame.width']").value("Width must be at least 1").match(result);
                    }
                    if (height < 1) {
                        jsonPath("$.details['newGame.height']").value("Height must be at least 1").match(result);
                    }
                });
    }

    @Test
    void newGame_shouldReturnBadRequest_whenNonNumericDimensions() throws Exception {
        mockMvc.perform(get(NEW_GAME_ENDPOINT)
                        .param("w", "abc")
                        .param("h", "def")
                        .header(SnakeConstants.CORRELATION_ID_HEADER, correlationId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(SnakeConstants.VALIDATION_ERROR))
                .andExpect(jsonPath("$.details.w").value("Invalid value for w"))
                .andExpect(jsonPath("$.details.h").value("Invalid value for h"));
    }

    @Test
    void newGame_shouldReturnBadRequest_whenMissingDimensions() throws Exception {
        mockMvc.perform(get(NEW_GAME_ENDPOINT)
                        .header(SnakeConstants.CORRELATION_ID_HEADER, correlationId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(SnakeConstants.VALIDATION_ERROR))
                .andExpect(jsonPath("$.details.w").value("Missing required parameter"))
                .andExpect(jsonPath("$.details.h").value("Missing required parameter"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE", "PATCH"})
    void newGame_shouldReturnMethodNotAllowed_whenInvalidHttpMethod(String httpMethod) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(httpMethod), NEW_GAME_ENDPOINT)
                        .param("w", String.valueOf(GRID_WIDTH))
                        .param("h", String.valueOf(GRID_HEIGHT))
                        .header(SnakeConstants.CORRELATION_ID_HEADER, correlationId))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void newGame_shouldReturnInternalServerError_whenServiceThrowsException() throws Exception {
        when(snakeService.newGame(anyInt(), anyInt())).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get(NEW_GAME_ENDPOINT)
                        .param("w", String.valueOf(GRID_WIDTH))
                        .param("h", String.valueOf(GRID_HEIGHT))
                        .header(SnakeConstants.CORRELATION_ID_HEADER, correlationId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(SnakeConstants.INTERNAL_SERVER_ERROR));
    }

    private NewGameResponse createExpectedNewGameResponse(int width, int height) {
        String gameId = UUID.randomUUID().toString();
        GridSize gridSize = new GridSize(width, height);
        Fruit fruit = Fruit.generateRandomPosition(gridSize, 1);
        Snake snake = new Snake(new Coordinate(0, 0), new Velocity(1, 0));

        State expectedState = State.builder()
                .gameId(gameId)
                .width(width)
                .height(height)
                .score(SnakeConstants.INITIAL_SCORE)
                .fruit(fruit)
                .snake(snake)
                .build();

        return new NewGameResponse(expectedState);
    }

    @Test
    void validateMove_shouldReturnValidMoveResponse_whenValidRequest() throws Exception {
        ValidateMoveRequest request = createMoveRequest();
        ValidateMoveResponse expectedResponse = createExpectedMoveResponse();

        when(snakeService.validateMove(argThat(this::isValidRequest))).thenReturn(expectedResponse);

        mockMvc.perform(post(VALIDATE_MOVE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header(SnakeConstants.CORRELATION_ID_HEADER, correlationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state.gameId").value(expectedResponse.state().getGameId()))
                .andExpect(jsonPath("$.state.width").value(expectedResponse.state().getWidth()))
                .andExpect(jsonPath("$.state.height").value(expectedResponse.state().getHeight()))
                .andExpect(jsonPath("$.state.score").value(expectedResponse.state().getScore()))
                .andExpect(jsonPath("$.state.fruit.coordinate.x").value(expectedResponse.state().getFruit().getCoordinate().x()))
                .andExpect(jsonPath("$.state.fruit.coordinate.y").value(expectedResponse.state().getFruit().getCoordinate().y()))
                .andExpect(jsonPath("$.state.snake.coordinate.x").value(expectedResponse.state().getSnake().getCoordinate().x()))
                .andExpect(jsonPath("$.state.snake.coordinate.y").value(expectedResponse.state().getSnake().getCoordinate().y()))
                .andExpect(jsonPath("$.state.snake.velocity.velX").value(expectedResponse.state().getSnake().getVelocity().velX()))
                .andExpect(jsonPath("$.state.snake.velocity.velY").value(expectedResponse.state().getSnake().getVelocity().velY()))
                .andExpect(jsonPath("$.ticks").isArray())
                .andExpect(jsonPath("$.ticks", hasSize(2)))
                .andExpect(jsonPath("$.ticks[0].velocity.velX").value(1))
                .andExpect(jsonPath("$.ticks[0].velocity.velY").value(0))
                .andExpect(jsonPath("$.ticks[1].velocity.velX").value(0))
                .andExpect(jsonPath("$.ticks[1].velocity.velY").value(-1))
                .andExpect(header().string(SnakeConstants.CORRELATION_ID_HEADER, correlationId));

        verify(snakeService).validateMove(argThat(this::isValidRequest));
    }

    @Nested
    class ValidateMoveInvalidInputTests {

        @ParameterizedTest
        @MethodSource("invalidRequestScenarios")
        void shouldReturnBadRequest_whenInvalidRequest(String testName, ValidateMoveRequest request, String expectedErrorField, String expectedErrorMessage) throws Exception {
            MvcResult mvcResult = mockMvc.perform(post(VALIDATE_MOVE_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .header(SnakeConstants.CORRELATION_ID_HEADER, correlationId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(SnakeConstants.VALIDATION_ERROR))
                    .andReturn();

            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println(testName + " - Response Body: " + responseBody);

            assertThat(responseBody)
                    .contains(expectedErrorField)
                    .contains(expectedErrorMessage);
        }

        private static Stream<Arguments> invalidRequestScenarios() {
            return Stream.of(
                    Arguments.of("Null State", new ValidateMoveRequest(null, createValidTicks()), "state", "State cannot be null"),
                    Arguments.of("Null Ticks", new ValidateMoveRequest(createValidState(), null), "ticks", "Ticks cannot be empty"),
                    Arguments.of("Null Game ID", new ValidateMoveRequest(createStateWithNullGameId(), createValidTicks()), "state.gameId", "Game ID is required"),
                    Arguments.of("Invalid Width", new ValidateMoveRequest(createStateWithInvalidWidth(), createValidTicks()), "state.width", "Width must be at least 1"),
                    Arguments.of("Invalid Height", new ValidateMoveRequest(createStateWithInvalidHeight(), createValidTicks()), "state.height", "Height must be at least 1"),
                    Arguments.of("Negative Score", new ValidateMoveRequest(createStateWithNegativeScore(), createValidTicks()), "state.score", "Score must be non-negative"),
                    Arguments.of("Null Fruit", new ValidateMoveRequest(createStateWithNullFruit(), createValidTicks()), "state.fruit", "Fruit cannot be null"),
                    Arguments.of("Invalid Fruit Coordinate", new ValidateMoveRequest(createStateWithInvalidFruitCoordinate(), createValidTicks()), "state.fruit.coordinate", SnakeConstants.INVALID_COORDINATE),
                    Arguments.of("Null Snake", new ValidateMoveRequest(createStateWithNullSnake(), createValidTicks()), "state.snake", "Snake cannot be null"),
                    Arguments.of("Invalid Snake Coordinate", new ValidateMoveRequest(createStateWithInvalidSnakeCoordinate(), createValidTicks()), "state.snake.coordinate", SnakeConstants.INVALID_COORDINATE),
                    Arguments.of("Invalid Snake Velocity", new ValidateMoveRequest(createStateWithInvalidSnakeVelocity(), createValidTicks()), "state.snake.velocity", SnakeConstants.INVALID_VELOCITY),
                    Arguments.of("Empty Ticks", new ValidateMoveRequest(createValidState(), Collections.emptyList()), "ticks", "Ticks cannot be empty"),
                    Arguments.of("Null Tick Velocity", new ValidateMoveRequest(createValidState(), createTicksWithNullVelocity()), "ticks[0].velocity", SnakeConstants.INVALID_VELOCITY),
                    Arguments.of("Invalid Tick Velocity", new ValidateMoveRequest(createValidState(), createTicksWithInvalidVelocity()), "ticks", SnakeConstants.INVALID_VELOCITY)
            );
        }
    }

    @Test
    void validateMove_shouldReturnNotFound_whenFruitNotReached() throws Exception {
        ValidateMoveRequest request = createFruitNotReachedMoveRequest();

        when(snakeService.validateMove(ArgumentMatchers.any(ValidateMoveRequest.class)))
                .thenThrow(new FruitNotReachedException(SnakeConstants.FRUIT_NOT_REACHED));

        mockMvc.perform(post(VALIDATE_MOVE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header(SnakeConstants.CORRELATION_ID_HEADER, correlationId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(SnakeConstants.FRUIT_NOT_REACHED))
                .andExpect(header().string(SnakeConstants.CORRELATION_ID_HEADER, correlationId));

        verify(snakeService).validateMove(ArgumentMatchers.any(ValidateMoveRequest.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE", "PATCH"})
    void validateMove_shouldReturnMethodNotAllowed_whenInvalidHttpMethod(String httpMethod) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(httpMethod), VALIDATE_MOVE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header(SnakeConstants.CORRELATION_ID_HEADER, correlationId))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void validateMove_shouldReturnTeapot_whenGameIsOver() throws Exception {
        ValidateMoveRequest request = createGameOverMoveRequest();

        when(snakeService.validateMove(ArgumentMatchers.any(ValidateMoveRequest.class)))
                .thenThrow(new GameOverException(SnakeConstants.GAME_OVER));

        mockMvc.perform(post(VALIDATE_MOVE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header(SnakeConstants.CORRELATION_ID_HEADER, correlationId))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.message").value(SnakeConstants.GAME_OVER))
                .andExpect(header().string(SnakeConstants.CORRELATION_ID_HEADER, correlationId));

        verify(snakeService).validateMove(ArgumentMatchers.any(ValidateMoveRequest.class));
    }

    @Test
    void validateMove_shouldReturnInternalServerError_whenServiceThrowsException() throws Exception {
        ValidateMoveRequest request = createMoveRequest();

        when(snakeService.validateMove(ArgumentMatchers.any(ValidateMoveRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post(VALIDATE_MOVE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header(SnakeConstants.CORRELATION_ID_HEADER, correlationId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(SnakeConstants.INTERNAL_SERVER_ERROR))
                .andExpect(header().string(SnakeConstants.CORRELATION_ID_HEADER, correlationId));

        verify(snakeService).validateMove(ArgumentMatchers.any(ValidateMoveRequest.class));
    }

    private ValidateMoveRequest createMoveRequest() {
        State initialState = State.builder()
                .gameId(SnakeControllerTest.correlationId)
                .width(GRID_WIDTH)
                .height(GRID_HEIGHT)
                .score(0)
                .fruit(Fruit.generateFixedPosition(1, 1))
                .snake(new Snake(new Coordinate(0, 0), new Velocity(1, 0)))
                .build();
        List<Tick> ticks = List.of(
                new Tick(new Velocity(1, 0)),
                new Tick(new Velocity(0, -1))
        );
        return new ValidateMoveRequest(initialState, ticks);
    }

    private ValidateMoveRequest createGameOverMoveRequest() {
        State initialState = State.builder()
                .gameId(SnakeControllerTest.correlationId)
                .width(GRID_WIDTH)
                .height(GRID_HEIGHT)
                .score(0)
                .fruit(Fruit.generateFixedPosition(1, 1))
                .snake(new Snake(new Coordinate(0, 0), new Velocity(1, 0)))
                .build();
        List<Tick> ticks = List.of(
                new Tick(new Velocity(1, 0)),
                new Tick(new Velocity(-1, 0))
        );
        return new ValidateMoveRequest(initialState, ticks);
    }

    private ValidateMoveRequest createFruitNotReachedMoveRequest() {
        State initialState = State.builder()
                .gameId(SnakeControllerTest.correlationId)
                .width(GRID_WIDTH)
                .height(GRID_HEIGHT)
                .score(0)
                .fruit(Fruit.generateFixedPosition(1, 1))
                .snake(new Snake(new Coordinate(0, 0), new Velocity(1, 0)))
                .build();
        List<Tick> ticks = List.of(
                new Tick(new Velocity(1, 0))
        );
        return new ValidateMoveRequest(initialState, ticks);
    }

    private ValidateMoveResponse createExpectedMoveResponse() {
        State finalState = State.builder()
                .gameId(SnakeControllerTest.correlationId)
                .width(GRID_WIDTH)
                .height(GRID_HEIGHT)
                .score(1)
                .fruit(Fruit.generateRandomPosition(GRID_WIDTH, GRID_HEIGHT, 0))
                .snake(new Snake(new Coordinate(1, 1), new Velocity(0, -1)))
                .build();
        List<Tick> ticks = List.of(
                new Tick(new Velocity(1, 0)),
                new Tick(new Velocity(0, -1))
        );
        return new ValidateMoveResponse(finalState, ticks);
    }

    // Basic sanity check
    private boolean isValidRequest(ValidateMoveRequest req) {
        return req.getState().getWidth() == GRID_WIDTH &&
                req.getState().getHeight() == GRID_HEIGHT &&
                req.getTicks().size() == 2;
    }

    // Helper methods to create various invalid states and ticks
    private static State createValidState() {
        return State.builder()
                .gameId(correlationId)
                .width(GRID_WIDTH)
                .height(GRID_HEIGHT)
                .score(0)
                .fruit(Fruit.generateFixedPosition(1, 1))
                .snake(new Snake(new Coordinate(0, 0), new Velocity(1, 0)))
                .build();
    }

    private static State createStateWithNullGameId() {
        return State.builder()
                .gameId(null)
                .width(GRID_WIDTH)
                .height(GRID_HEIGHT)
                .score(0)
                .fruit(Fruit.generateFixedPosition(1, 1))
                .snake(new Snake(new Coordinate(0, 0), new Velocity(1, 0)))
                .build();
    }

    private static State createStateWithInvalidWidth() {
        return State.builder()
                .gameId(correlationId)
                .width(0)
                .height(GRID_HEIGHT)
                .score(0)
                .fruit(Fruit.generateFixedPosition(1, 1))
                .snake(new Snake(new Coordinate(0, 0), new Velocity(1, 0)))
                .build();
    }

    private static State createStateWithInvalidHeight() {
        return State.builder()
                .gameId(correlationId)
                .width(GRID_WIDTH)
                .height(-1)
                .score(0)
                .fruit(Fruit.generateFixedPosition(1, 1))
                .snake(new Snake(new Coordinate(0, 0), new Velocity(1, 0)))
                .build();
    }

    private static State createStateWithNegativeScore() {
        return State.builder()
                .gameId(correlationId)
                .width(GRID_WIDTH)
                .height(GRID_HEIGHT)
                .score(-1)
                .fruit(Fruit.generateFixedPosition(1, 1))
                .snake(new Snake(new Coordinate(0, 0), new Velocity(1, 0)))
                .build();
    }

    private static State createStateWithNullFruit() {
        return State.builder()
                .gameId(correlationId)
                .width(GRID_WIDTH)
                .height(GRID_HEIGHT)
                .score(0)
                .fruit(null)
                .snake(new Snake(new Coordinate(0, 0), new Velocity(1, 0)))
                .build();
    }

    private static State createStateWithInvalidFruitCoordinate() {
        return State.builder()
                .gameId(correlationId)
                .width(GRID_WIDTH)
                .height(GRID_HEIGHT)
                .score(0)
                .fruit(Fruit.generateFixedPosition(-1, -1))
                .snake(new Snake(new Coordinate(0, 0), new Velocity(1, 0)))
                .build();
    }

    private static State createStateWithNullSnake() {
        return State.builder()
                .gameId(correlationId)
                .width(GRID_WIDTH)
                .height(GRID_HEIGHT)
                .score(0)
                .fruit(Fruit.generateFixedPosition(1, 1))
                .snake(null)
                .build();
    }

    private static State createStateWithInvalidSnakeCoordinate() {
        return State.builder()
                .gameId(correlationId)
                .width(GRID_WIDTH)
                .height(GRID_HEIGHT)
                .score(0)
                .fruit(Fruit.generateFixedPosition(1, 1))
                .snake(new Snake(new Coordinate(-1, -1), new Velocity(1, 0)))
                .build();
    }

    private static State createStateWithInvalidSnakeVelocity() {
        return State.builder()
                .gameId(correlationId)
                .width(GRID_WIDTH)
                .height(GRID_HEIGHT)
                .score(0)
                .fruit(Fruit.generateFixedPosition(1, 1))
                .snake(new Snake(new Coordinate(0, 0), new Velocity(2, -2)))
                .build();
    }

    private static List<Tick> createValidTicks() {
        return List.of(
                new Tick(new Velocity(1, 0)),
                new Tick(new Velocity(0, -1))
        );
    }

    private static List<Tick> createTicksWithNullVelocity() {
        return List.of(
                new Tick(null),
                new Tick(new Velocity(0, -1))
        );
    }

    private static List<Tick> createTicksWithInvalidVelocity() {
        return List.of(
                new Tick(new Velocity(1, 0)),
                new Tick(new Velocity(2, -2))
        );
    }
}
