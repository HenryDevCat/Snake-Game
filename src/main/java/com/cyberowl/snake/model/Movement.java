package com.cyberowl.snake.model;

public interface Movement {
    Velocity getVelocity();

    void updateVelocity(Velocity velocity);
}
