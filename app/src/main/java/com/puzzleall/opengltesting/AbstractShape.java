package com.puzzleall.opengltesting;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public abstract class AbstractShape {

    protected final FloatBuffer mVerticesBuffer;
    private static final int BYTES_PER_FLOAT = 4;
    private int mProgram;

    final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;      \n"     // A constant representing the combined model/view/projection matrix.
                    + "attribute vec4 aPosition;     \n"     // Per-vertex position information we will pass in.
                    + "attribute vec4 aColor;        \n"     // Per-vertex color information we will pass in.
                    + "varying vec4 vColor;          \n"     // This will be passed into the fragment shader.
                    + "void main()                    \n"     // The entry point for our vertex shader.
                    + "{                              \n"
                    + "   vColor = aColor;          \n"     // Pass the color through to the fragment shader. It will be interpolated across the triangle.
                    + "   gl_Position = uMVPMatrix   \n"     // gl_Position is a special variable used to store the final position.
                    + "               * aPosition;   \n"     // Multiply the vertex by the matrix to get the final point in
                    + "}                              \n";    // normalized screen coordinates.

    final String fragmentShaderCode =
            "precision mediump float;       \n"     // Set the default precision to medium. We don't need as high of a
                    // precision in the fragment shader.
                    + "varying vec4 vColor;          \n"     // This is the color from the vertex shader interpolated across the triangle per fragment.
                    + "void main()                    \n"     // The entry point for our fragment shader.
                    + "{                              \n"
                    + "   gl_FragColor = vColor;     \n"     // Pass the color directly through the pipeline.
                    + "}                              \n";


    private int mColorHandle;
    private int mPositionHandle;
    private int mMVPMatrixHandle;

    // x,y,z,r,g,b,a
    static final int COORDS_PER_VERTEX = 7;

    /** How many elements per vertex. */
    private final int mStrideBytes = COORDS_PER_VERTEX * BYTES_PER_FLOAT;

    // Position offset in the vertex array
    private final int mPositionOffset = 0;

    /** Size of the position data in elements. */
    private final int mPositionDataSize = 3;

    // Offset of the color data
    private final int mColorOffset = 3;

    /** Size of the color data in elements. */
    private final int mColorDataSize = 4;

    public AbstractShape() {
        final float[] data = getVerticeData();

        Log.d("AbstractShape", "Size: " + data.length);

        mVerticesBuffer = ByteBuffer.allocateDirect(data.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVerticesBuffer.put(data).position(0);


        // prepare shaders and OpenGL program
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        if (mProgram != 0) {
            GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
            GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program

            GLES20.glBindAttribLocation(mProgram, 0, "aPosition");
            GLES20.glBindAttribLocation(mProgram, 1, "aColor");

            GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(mProgram);
                mProgram = 0;
            }
        }

        if (mProgram == 0) {
            throw new RuntimeException("Error creating program.");
        }




        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        GLES20.glUseProgram(mProgram);

        /*

        Log.d("AbstractShape", "program handle " + mProgram);
        Log.d("AbstractShape", "Color handle " + mColorHandle);
        Log.d("AbstractShape", "position handle " + mPositionHandle);
        Log.d("AbstractShape", "Done creating, matrix handle " + mMVPMatrixHandle);
        */


    }

    protected abstract float[] getVerticeData();

    protected abstract int getVertexCount();

    public void draw(float[] mvpMatrix, float[] modelMatrix, float[] projectionMatrix, float[] viewMatrix){
        passInPositionInformation();
        passInColorInformation();

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glGetUniformLocation");

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, getVertexCount());

        // Disable vertex array
        // GLES20.glDisableVertexAttribArray(mPositionHandle);

    }

    private void passInPositionInformation(){
        mVerticesBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, mVerticesBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
    }

    private void passInColorInformation(){
        mVerticesBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, mVerticesBuffer);
        GLES20.glEnableVertexAttribArray(mColorHandle);
    }

    public void setColor(float[] color){
        // glUniform4fv (int location, int count, float[] v, int offset)
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
    }

    /**
     * Utility method for compiling a OpenGL shader.
     * <p/>
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type       - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }
}
