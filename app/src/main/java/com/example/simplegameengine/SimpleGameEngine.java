package com.example.simplegameengine;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SimpleGameEngine extends Activity {
    GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(gameView);
    }

    class GameView extends SurfaceView implements Runnable {
        Thread gameThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playing;

        Canvas canvas;
        Paint paint;
        Display display;

        long fps;
        private long timeThisFrame;

        Bitmap bitmapBob;

        boolean isMoving = false;
        float walkSpeedPerSecond = 150;
        float marioXPosition = 10;

        int xMaxPosition;
        int xMinPosition = 0;
        int width, height;

        boolean rightMax = false;
        boolean leftMax = false;

        public GameView(Context context){
            super(context);

            display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            ourHolder = getHolder();
            paint = new Paint();

            bitmapBob = BitmapFactory.decodeResource(this.getResources(), R.drawable.bob);

            xMaxPosition = getResources().getDisplayMetrics().widthPixels - bitmapBob.getWidth();

            playing = true;
        }

        @Override
        public void run(){
            while (playing){
                long startFrameTime = System.currentTimeMillis();

                update();

                draw();

                timeThisFrame = System.currentTimeMillis() - startFrameTime;

                if(timeThisFrame > 0){
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        public void draw() {
            if(ourHolder.getSurface().isValid()){
                canvas = ourHolder.lockCanvas();

                canvas.drawColor(Color.argb(255, 26, 128, 182));

                paint.setColor(Color.argb(255, 249, 129, 0));
                paint.setTextSize(45);

                canvas.drawText("FPS : " + fps, 20, 40, paint);
                canvas.drawBitmap(bitmapBob, marioXPosition, 200, paint);

                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void update() {
            if(isMoving){
                if ((marioXPosition < xMaxPosition) & !rightMax){
                    marioXPosition = marioXPosition + (walkSpeedPerSecond / fps);
                    if (marioXPosition >= xMaxPosition){
                        rightMax = true;
                        leftMax = false;
                    }
                }

                if (rightMax & (marioXPosition > xMinPosition) & !leftMax){
                    marioXPosition = marioXPosition - (walkSpeedPerSecond / fps);
                    if (marioXPosition <= xMinPosition){
                        leftMax = true;
                        rightMax = false;
                    }
                }
            }
        }

        public void pause(){
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e){
                Log.e("Error:", "joining thread");
            }
        }

        public void resume(){
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        //Override class View
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent){
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                    isMoving = true;
                    break;
                case MotionEvent.ACTION_UP:
                    isMoving = false;
                    break;
            }

            return true;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        gameView.resume();
    }

    @Override
    protected void onPause(){
        super.onPause();

        gameView.pause();
    }
}