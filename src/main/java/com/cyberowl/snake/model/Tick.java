package com.cyberowl.snake.model;

import com.cyberowl.snake.util.ValidVelocity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

@ToString
public class Tick implements Movement {

    @ValidVelocity
    private Velocity velocity;  // X and Y velocity of the tick (one of -1, 0, 1)

    public Tick(@ValidVelocity @JsonProperty("velocity") Velocity velocity) {
        this.velocity = velocity;
    }

    @Override
    public Velocity getVelocity() {
        return velocity;
    }

    @Override
    public void updateVelocity(Velocity velocity) {
        this.velocity = velocity;
    }
}
