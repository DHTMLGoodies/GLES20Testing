package com.puzzleall.opengltesting;

public class Square extends AbstractShape {

    private static final float[] sVerticeData = {
            // X, Y, Z,
            // R, G, B, A
            -0.5f, -0.25f, 0.0f,
            1.0f, 0.0f, 0.0f, 1.0f,

            0.5f, -0.25f, 0.0f,
            0.0f, 0.0f, 1.0f, 1.0f,

            0.0f, 0.559016994f, 0.0f,
            0.0f, 1.0f, 0.0f, 1.0f};

    @Override
    protected float[] getVerticeData() {
        return sVerticeData;
    }

    @Override
    protected int getVertexCount() {
        return 3;
    }
}
