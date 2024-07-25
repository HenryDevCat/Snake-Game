package com.cyberowl.snake.model;

import com.cyberowl.snake.util.ValidCoordinate;
import com.cyberowl.snake.util.ValidVelocity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

@ToString
public class Snake implements Position, Movement {
    @ValidCoordinate
    private Coordinate coordinate;

    @ValidVelocity
    private Velocity velocity;  // X and Y velocity of the snake (one of -1, 0, 1)

    public Snake(@ValidCoordinate @JsonProperty("coordinate") Coordinate coordinate,
                 @ValidVelocity @JsonProperty("velocity") Velocity velocity) {
        this.coordinate = coordinate;
        this.velocity = velocity;
    }

    @Override
    public Coordinate getCoordinate() {
        return coordinate;
    }

    @Override
    public void updateCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public Velocity getVelocity() {
        return velocity;
    }

    @Override
    public void updateVelocity(Velocity velocity) {
        this.velocity = velocity;
    }

    public void move(Tick tick) {
        updateCoordinate(new Coordinate(
                coordinate.x() + tick.getVelocity().velX(),
                coordinate.y() + -(tick.getVelocity().velY()) // Invert y velocity: positive y is downward in grid coordinate system
        ));
        updateVelocity(tick.getVelocity());
    }
}
