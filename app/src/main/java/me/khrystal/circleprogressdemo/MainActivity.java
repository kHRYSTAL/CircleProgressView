package me.khrystal.circleprogressdemo;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import me.khrystal.widget.CircleProgressView;

public class MainActivity extends AppCompatActivity implements CircleProgressView.OnProgressListener {

    private CircleProgressView mCircleRingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCircleRingView = (CircleProgressView) findViewById(R.id.circle_view);
        mCircleRingView.setGradinetColor(Color.parseColor("#FF26FF00"), Color.parseColor("#FF126912"));
        mCircleRingView.setCenterTextSize(120);
        mCircleRingView.setCenterText(""+0);
        mCircleRingView.setUnitText("kg");
        mCircleRingView.setUnitTextSize(40);
        mCircleRingView.setOnProgressListener(this);
    }


    public void startAnim(View view) {
        mCircleRingView.startAnim();
    }

    public void stopAnim(View view) {
        mCircleRingView.stopAnim();
    }

    @Override
    public void onProgress(float currentValue) {
        mCircleRingView.setCenterText("" + (int)(200 * currentValue));
    }
}
