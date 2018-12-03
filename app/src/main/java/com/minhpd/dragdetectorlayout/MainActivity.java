package com.minhpd.dragdetectorlayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    int mDirection = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DragDetectorLayout dragDetectorLayout
                = (DragDetectorLayout) findViewById(R.id.dragdetectorlayout);
        dragDetectorLayout.setOnDragListener(onDragListener);
    }

    DragDetectorLayout.OnDragListener onDragListener = new DragDetectorLayout.OnDragListener() {
        @Override
        public void onStartDragging(float startX, float startY, int direction) {
            mDirection = direction;
        }

        @Override
        public void onDragging(float translationX, float translationY, float dx, float dy) {

        }

        @Override
        public void onReleaseDragging(float x, float y, float vx, float vy, boolean cancelOpen, boolean cancelClose) {
            if (mDirection == DragDetectorLayout.BOTTOM_TO_TOP) {
                Toast.makeText(getApplicationContext(), "Drag from bottom to top",
                        Toast.LENGTH_SHORT).show();
            } else if (mDirection == DragDetectorLayout.TOP_TO_BOTTOM) {
                Toast.makeText(getApplicationContext(), "Drag from top to bottom",
                        Toast.LENGTH_SHORT).show();
            } else if (mDirection == DragDetectorLayout.LEFT_TO_RIGHT) {
                Toast.makeText(getApplicationContext(), "Drag from left to right",
                        Toast.LENGTH_SHORT).show();
            } else if (mDirection == DragDetectorLayout.RIGHT_TO_LEFT) {
                Toast.makeText(getApplicationContext(), "Drag from right to left",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };
}
