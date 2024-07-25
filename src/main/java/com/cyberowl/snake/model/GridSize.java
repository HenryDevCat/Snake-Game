package com.cyberowl.snake.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GridSize {

    private int width;
    private int height;

    public GridSize(int width, int height) {
        setDimensions(width, height);
    }

    private void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
