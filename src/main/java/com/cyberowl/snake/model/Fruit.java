package com.cyberowl.snake.model;

import com.cyberowl.snake.util.DimensionsValidator;
import com.cyberowl.snake.util.ValidCoordinate;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

import java.util.concurrent.ThreadLocalRandom;

@ToString
public class Fruit implements Position {
    @ValidCoordinate
    private Coordinate coordinate;

    private Fruit(@ValidCoordinate @JsonProperty("coordinate") Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public Coordinate getCoordinate() {
        return coordinate;
    }

    @Override
    public void updateCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public static Fruit generateRandomPosition(GridSize gridSize, int start) {
        return generateRandomPosition(gridSize.getWidth(), gridSize.getHeight(), start);
    }

    /**
     * (Width / height) + 1: nextInt upper bound is exclusive
     * (Width / height) == start:
     * 1) Forces fruit to (1, 1) in 1 x 1 grid (snake starts at 0, 0)
     * 2) Allows unrestricted random generation after first fruit
     */
    public static Fruit generateRandomPosition(int width, int height, int start) {
        DimensionsValidator.validatePositiveDimensions(width, height, start);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int x = (width == start) ? start : random.nextInt(start, width + 1);
        int y = (height == start) ? start : random.nextInt(start, height + 1);
        return new Fruit(new Coordinate(x, y));
    }

    public static Fruit generateFixedPosition(int x, int y) {
        return new Fruit(new Coordinate(x, y));
    }
}
