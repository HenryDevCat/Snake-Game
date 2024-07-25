package com.cyberowl.snake.controller;

import com.cyberowl.snake.constants.SnakeConstants;
import com.cyberowl.snake.dto.NewGameResponse;
import com.cyberowl.snake.dto.ValidateMoveRequest;
import com.cyberowl.snake.dto.ValidateMoveResponse;
import com.cyberowl.snake.service.SnakeService;
import com.cyberowl.snake.util.CorrelationIdUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/snake")
@Validated
@RequiredArgsConstructor
@Tag(name = "Snake Game", description = "APIs for managing Snake game")
@Slf4j
public class SnakeController {

    private final SnakeService snakeService;

    @Operation(summary = "Start a new game", description = "Creates a new Snake game with specified width and height")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game created successfully",
                    content = @Content(schema = @Schema(implementation = NewGameResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request."),
    })
    @GetMapping(value = "/new", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NewGameResponse> newGame(
            @Parameter(description = "Width of the game board", example = "10")
            @RequestParam("w") @Min(value = 1, message = "Width must be at least 1") int width,
            @Parameter(description = "Height of the game board", example = "15")
            @RequestParam("h") @Min(value = 1, message = "Height must be at least 1") int height,
            @RequestHeader(value = SnakeConstants.CORRELATION_ID_HEADER, required = false) String correlationId) {

        String requestId = CorrelationIdUtil.getOrGenerateCorrelationId(correlationId);
        log.info("Creating new game. Width: {}, Height: {}. Request ID: {}", width, height, requestId);
        NewGameResponse response = snakeService.newGame(width, height);
        log.info("New game created successfully. Request ID: {}", requestId);
        return ResponseEntity.ok()
                .header(SnakeConstants.CORRELATION_ID_HEADER, requestId)
                .body(response);
    }

    @Operation(summary = "Validate move", description = "Validates a move in the Snake game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid state & ticks.",
                    content = @Content(schema = @Schema(implementation = ValidateMoveResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request."),
            @ApiResponse(responseCode = "404", description = "Fruit not found, the ticks do not lead the snake to the fruit position."),
            @ApiResponse(responseCode = "418", description = "Game is over, snake went out of bounds or made an invalid move."),
    })
    @PostMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ValidateMoveResponse> validateMove(
            @Valid @RequestBody ValidateMoveRequest validateMoveRequest,
            @RequestHeader(value = SnakeConstants.CORRELATION_ID_HEADER, required = false) String correlationId) {

        String requestId = CorrelationIdUtil.getOrGenerateCorrelationId(correlationId);
        log.info("Validating move. Request: {}. Request ID: {}", validateMoveRequest, requestId);
        ValidateMoveResponse response = snakeService.validateMove(validateMoveRequest);
        log.info("Move validated successfully. Request ID: {}", requestId);
        return ResponseEntity.ok()
                .header(SnakeConstants.CORRELATION_ID_HEADER, requestId)
                .body(response);
    }
}
