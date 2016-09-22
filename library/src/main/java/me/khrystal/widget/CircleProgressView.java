package me.khrystal.widget;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;


public class CircleProgressView extends View {

    private static final int    DEFAULT_HEIGHT           =   250;
    private static final int    DEFAULT_WIDTH            =   250;
    private static final int    DEFAULT_PADDING          =   20;
    private static final float  DEFAULT_START_ANGLE      =   0f;
    private final static float  DEFAULT_END_ANGLE        =   360f;
    private final static int  DEFAULT_CENTER_TEXTSIZE    =   60;
    private final static int  DEFAULT_UNIT_TEXTSIZE      =   36;
    private final static int  DEFAULT_STORKE_WIDTH       =   3;
    private final static int  DEFAULT_POINT_RADIUS       =   2;
    private final static int DEFAULT_ANIM_DURATION       =   3000;


    private int mWidth, mHeight;

    private Paint mProgressPaint, mRingPaint;

    private RectF mRingRect, mProgressRect;

    private float mCurrentAngle;

    private float mTotalAngle;

    private Bitmap bitmap;

    private float[] pos;

    private float[] tan;

    private Matrix matrix;

    private Paint mBitmapPaint, mCenterTextPaint, mUnitTextPaint;

    private float mStartAngle, mEndAngle;

    private String mUnit, mCenterText;
    private int mUnitSize, mCenterTextSize;
    private int mStorkeWidth;
    private int mPointRadius;
    private int mAnimDuration;
    private int mAnimRepeatCount;
    private int mStrokeAlpha, mBlurWidth;
    private boolean isStarted;

    private @ColorInt int mStartColor, mEndColor, mCurrentColor, mStrokeColor;

    private ValueAnimator mAngleAnim;

    private OnProgressListener mOnProgressListener;


    public CircleProgressView(Context context) {
        this(context, null);
    }

    public CircleProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleProgressView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }


    private void init(AttributeSet attrs) {

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CircleProgressView);
        mStrokeColor = ta.getColor(R.styleable.CircleProgressView_strokeColor, Color.WHITE);
        mStorkeWidth = ta.getDimensionPixelSize(R.styleable.CircleProgressView_strokeWidth, dp2px(DEFAULT_STORKE_WIDTH));
        mAnimDuration = ta.getInt(R.styleable.CircleProgressView_duration, DEFAULT_ANIM_DURATION);
        mStartColor = ta.getColor(R.styleable.CircleProgressView_startColor, Color.WHITE);
        mEndColor = ta.getColor(R.styleable.CircleProgressView_endColor, Color.WHITE);
        mAnimRepeatCount = ta.getInt(R.styleable.CircleProgressView_animRepeatCount, 0);
        mUnitSize = ta.getDimensionPixelSize(R.styleable.CircleProgressView_unitTextSize, sp2px(DEFAULT_UNIT_TEXTSIZE));
        mCenterTextSize = ta.getDimensionPixelSize(R.styleable.CircleProgressView_centerTextSize, sp2px(DEFAULT_CENTER_TEXTSIZE));
        mPointRadius = ta.getDimensionPixelSize(R.styleable.CircleProgressView_pointRadius, dp2px(DEFAULT_POINT_RADIUS));
        mStrokeAlpha = ta.getInt(R.styleable.CircleProgressView_strokeAlpha, 60);
        mBlurWidth = ta.getDimensionPixelSize(R.styleable.CircleProgressView_blurWidth, dp2px(2));
        mUnit = ta.getString(R.styleable.CircleProgressView_unitText);
        mStartAngle = ta.getFloat(R.styleable.CircleProgressView_startAngle, DEFAULT_START_ANGLE);
        mEndAngle = ta.getFloat(R.styleable.CircleProgressView_endAngle, DEFAULT_END_ANGLE);
        mTotalAngle = ta.getFloat(R.styleable.CircleProgressView_totalAngle, DEFAULT_END_ANGLE);
        ta.recycle();

        mRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRingPaint.setStrokeWidth(mStorkeWidth);
        mRingPaint.setColor(mStrokeColor);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setAlpha(mStrokeAlpha);

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStrokeWidth(mStorkeWidth);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);

        mCenterTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterTextPaint.setTextAlign(Paint.Align.CENTER);
        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        mCenterTextPaint.setTypeface(font);

        mUnitTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnitTextPaint.setTextAlign(Paint.Align.CENTER);

        mBitmapPaint = new Paint();
        mBitmapPaint.setStyle(Paint.Style.FILL);
        mBitmapPaint.setAntiAlias(true);

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_circle);
        mBitmapPaint.setMaskFilter(new BlurMaskFilter(mBlurWidth, BlurMaskFilter.Blur.SOLID));

        pos = new float[2];
        tan = new float[2];
        matrix = new Matrix();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidthSize(widthMeasureSpec), measureHeightSize(heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int paddingLeft = getPaddingLeft() == 0 ? DEFAULT_PADDING : getPaddingLeft();
        int paddingRight = getPaddingRight() == 0 ? DEFAULT_PADDING : getPaddingRight();
        int paddingTop = getPaddingTop() == 0 ? DEFAULT_PADDING : getPaddingTop();
        int paddingBottom = getPaddingBottom() == 0 ? DEFAULT_PADDING : getPaddingBottom();
        int width = getWidth();
        int height = getHeight();

        mWidth = w;
        mHeight = h;

        mRingRect = new RectF(
                paddingLeft, paddingTop,
                width - paddingRight, height - paddingBottom);
        mProgressRect = new RectF(
                paddingLeft, paddingTop,
                width - paddingRight, height - paddingBottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawRing(canvas);
        drawProgress(canvas);
        drawNumberText(canvas);
        drawUnitText(canvas);
    }


    private void drawRing(Canvas canvas) {
        canvas.drawArc(mRingRect, mStartAngle, mEndAngle, false, mRingPaint);
    }

    private void drawProgress(Canvas canvas) {

        Path path = new Path();
        path.addArc(mProgressRect, mStartAngle, mCurrentAngle > 360f ? mCurrentAngle - (360f * (int)(mTotalAngle / mCurrentAngle)) : mCurrentAngle);
        PathMeasure pathMeasure = new PathMeasure(path, false);
        pathMeasure.getPosTan(pathMeasure.getLength() * 1, pos, tan);
        matrix.reset();
        matrix.postTranslate(pos[0] - bitmap.getWidth() / 2, pos[1] - bitmap.getHeight() / 2);
        mProgressPaint.setShader(new SweepGradient(mWidth / 2, mHeight / 2, mStartColor, mCurrentColor));

        path.addArc(mProgressRect, mStartAngle, mCurrentAngle);
        canvas.drawPath(path, mProgressPaint);
        if (mCurrentAngle == 0)
            return;
        canvas.drawBitmap(bitmap, matrix, mBitmapPaint);

        mBitmapPaint.setColor(mCurrentColor);
        canvas.drawCircle(pos[0], pos[1], mPointRadius, mBitmapPaint);
    }

    private void drawNumberText(Canvas canvas) {
        mCenterTextPaint.setTextSize(mCenterTextSize);
        if (mAngleAnim != null && mAngleAnim.isRunning()) {
            isStarted = true;
            mCenterTextPaint.setColor(mCurrentColor);
        }
        if (!isStarted)
            mCenterTextPaint.setColor(mStartColor);
        if (!TextUtils.isEmpty(mUnit))
            canvas.drawText(mCenterText, mWidth / 2, mHeight * 4 / 7, mCenterTextPaint);
    }

    private void drawUnitText(Canvas canvas) {
        mUnitTextPaint.setTextSize(mUnitSize);
        if (mAngleAnim != null && mAngleAnim.isRunning())
            mUnitTextPaint.setColor(mCurrentColor);
        if (!isStarted)
            mUnitTextPaint.setColor(mStartColor);
        if (!TextUtils.isEmpty(mUnit))
            canvas.drawText(mUnit, mWidth / 2, mHeight * 4 / 5, mUnitTextPaint);
    }

    private static int getGradientColor(int startColor, int endColor, float percent) {
        int sr = (startColor & 0xff0000) >> 0x10;
        int sg = (startColor & 0xff00) >> 0x8;
        int sb = (startColor & 0xff);

        int er = (endColor & 0xff0000) >> 0x10;
        int eg = (endColor & 0xff00) >> 0x8;
        int eb = (endColor & 0xff);

        int cr = (int) (sr * (1 - percent) + er * percent);
        int cg = (int) (sg * (1 - percent) + eg * percent);
        int cb = (int) (sb * (1 - percent) + eb * percent);
        return Color.argb(0xff, cr, cg, cb);
    }



    private int measureWidthSize(int measureSpec) {
        int defSize = dp2px(DEFAULT_WIDTH);
        int specSize = MeasureSpec.getSize(measureSpec);
        int specMode = MeasureSpec.getMode(measureSpec);

        int result = 0;
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                result = Math.min(defSize, specSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }


    private int measureHeightSize(int measureSpec) {
        int defSize = dp2px(DEFAULT_HEIGHT);
        int specSize = MeasureSpec.getSize(measureSpec);
        int specMode = MeasureSpec.getMode(measureSpec);

        int result = 0;
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                result = Math.min(defSize, specSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    public void startAnim() {
        if (mAngleAnim != null && mAngleAnim.isRunning())
            return;
        mAngleAnim = ValueAnimator.ofFloat(mStartAngle, mTotalAngle);
        mAngleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        mAngleAnim.setDuration(mAnimDuration);
        mAngleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                mCurrentAngle = (float) valueAnimator.getAnimatedValue();
                mCurrentColor = getGradientColor(mStartColor, mEndColor, mCurrentAngle / mTotalAngle);
                if (mOnProgressListener != null) {
                    mOnProgressListener.onProgress(mCurrentAngle / mTotalAngle);
                }
                postInvalidate();
            }
        });
        mAngleAnim.setRepeatCount(mAnimRepeatCount);
        mAngleAnim.start();
    }


    public void stopAnim() {
        if (mAngleAnim != null && mAngleAnim.isRunning()) {
            mAngleAnim.end();
        }
    }

    public CircleProgressView setUnitText(String unit) {
        mUnit = unit;
        return this;
    }

    public CircleProgressView setUnitTextSize(int spTextSize) {
        mUnitSize = sp2px(spTextSize);
        return this;
    }

    public CircleProgressView setCenterText(String centerText) {
        mCenterText = centerText;
        return this;
    }

    public String getCenterText() {
        return mCenterText;
    }

    public CircleProgressView setCenterTextSize(int spTextSize) {
        mCenterTextSize = sp2px(spTextSize);
        return this;
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void pauseAnim() {
        if (mAngleAnim != null) {
            mAngleAnim.pause();
        }
    }

    public void setAnimRepeatCount(int repeatCount) {
        mAnimRepeatCount = repeatCount;
    }


    public CircleProgressView setStorkeWidth(int dpStorkeWidth) {
        mStorkeWidth = dp2px(dpStorkeWidth);
        invalidate();
        return this;
    }

    /**
     * 1 ~ 100
     * @param alpha
     * @return
     */
    public CircleProgressView setStorkeAlpha(int alpha) {
        mStrokeAlpha = alpha;
        invalidate();
        return this;
    }

    public CircleProgressView setStrokeColor(@ColorInt int color) {
        mStrokeColor = color;
        return this;
    }

    public CircleProgressView setStartAngle(float startAngle) {
        mStartAngle = startAngle;
        invalidate();
        return this;
    }

    public CircleProgressView setEndAngle(float endAngle) {
        mEndAngle = endAngle;
        invalidate();
        return this;
    }

    public CircleProgressView setTotalAngle(float totalAngle) {
        mTotalAngle = totalAngle;
        invalidate();
        return this;
    }


    public CircleProgressView setBlurWidth(int dpValue) {
        mBlurWidth = dp2px(dpValue);
        invalidate();
        return this;
    }


    public void setAnimDuration (int duration) {
        if (mAngleAnim != null && mAngleAnim.isRunning())
            return;
        mAnimDuration = duration;
    }

    public CircleProgressView setPointRadius (int dpRadius) {
        mPointRadius = dp2px(dpRadius);
        return this;
    }



    public void setGradinetColor(@ColorInt int startColor, @ColorInt int endColor) {
        mStartColor = startColor;
        mEndColor = endColor;
    }

    public void setOnProgressListener(OnProgressListener listener) {
        mOnProgressListener = listener;
    }

    public int dp2px(int values) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (values * density + 0.5f);
    }

    public int sp2px(float sp) {
        float density = getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * density + 0.5f);

    }

    public interface OnProgressListener {
        /**
         * 0.0f ~ 1.0f
         * @param currentValue
         */
        void onProgress(float currentValue);
    }

}
