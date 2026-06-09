package com.nova.assistant.home;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

public class OrbView extends View {

    public enum OrbState { IDLE, LISTENING, PROCESSING, SPEAKING }

    private OrbState currentState = OrbState.IDLE;

    // Paint objects
    private Paint corePaint;
    private Paint ring1Paint;
    private Paint ring2Paint;
    private Paint ring3Paint;
    private Paint glowPaint;

    // Animated values
    private float pulseScale   = 1.0f;
    private float rotationAngle = 0f;
    private float glowAlpha   = 0.3f;
    private float breathe     = 1.0f;

    // Animators
    private ValueAnimator pulseAnim;
    private ValueAnimator rotAnim;
    private ValueAnimator glowAnim;
    private ValueAnimator breatheAnim;

    // Colors per state
    private int coreColor    = Color.parseColor("#00EEFF");
    private int ring1Color   = Color.parseColor("#0088BB");
    private int glowColor    = Color.parseColor("#00D4FF");

    public OrbView(Context context) {
        super(context);
        init();
    }

    public OrbView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OrbView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        corePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ring1Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ring2Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ring3Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        ring1Paint.setStyle(Paint.Style.STROKE);
        ring1Paint.setStrokeWidth(2f);
        ring2Paint.setStyle(Paint.Style.STROKE);
        ring2Paint.setStrokeWidth(1.5f);
        ring3Paint.setStyle(Paint.Style.STROKE);
        ring3Paint.setStrokeWidth(1f);

        startIdleAnimation();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateGradients(w, h);
    }

    private void updateGradients(int w, int h) {
        if (w == 0 || h == 0) return;
        float cx = w / 2f;
        float cy = h / 2f;
        float coreR = Math.min(w, h) * 0.22f;

        // Core radial gradient
        RadialGradient coreGrad = new RadialGradient(cx, cy, coreR,
            new int[]{Color.WHITE, coreColor, Color.parseColor("#004466")},
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP);
        corePaint.setShader(coreGrad);

        // Outer glow gradient
        RadialGradient glowGrad = new RadialGradient(cx, cy, Math.min(w, h) * 0.48f,
            new int[]{adjustAlpha(glowColor, 0.4f), adjustAlpha(glowColor, 0.15f), Color.TRANSPARENT},
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP);
        glowPaint.setShader(glowGrad);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;
        float baseR = Math.min(w, h) * 0.42f;
        float coreR = Math.min(w, h) * 0.22f;

        canvas.save();
        canvas.rotate(rotationAngle, cx, cy);

        // Outer glow ring (pulsing)
        glowPaint.setAlpha((int)(glowAlpha * 255));
        canvas.drawCircle(cx, cy, baseR * pulseScale * breathe, glowPaint);

        // Ring 3 (outermost, dashed feel via alpha)
        ring3Paint.setColor(adjustAlpha(glowColor, 0.12f * breathe));
        canvas.drawCircle(cx, cy, baseR * 0.95f * pulseScale, ring3Paint);

        // Ring 2
        ring2Paint.setColor(adjustAlpha(glowColor, 0.25f * breathe));
        canvas.drawCircle(cx, cy, baseR * 0.70f * pulseScale, ring2Paint);

        // Ring 1 (closest)
        ring1Paint.setColor(adjustAlpha(glowColor, 0.5f));
        canvas.drawCircle(cx, cy, baseR * 0.48f * pulseScale, ring1Paint);

        canvas.restore();

        // Core orb (no rotation)
        updateGradients(w, h);
        canvas.drawCircle(cx, cy, coreR * breathe, corePaint);

        // Inner bright spot
        Paint spotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        spotPaint.setColor(Color.WHITE);
        spotPaint.setAlpha(60);
        canvas.drawCircle(cx - coreR * 0.25f, cy - coreR * 0.25f, coreR * 0.3f, spotPaint);
    }

    // ===================== STATE MANAGEMENT =====================

    public void setState(OrbState state) {
        if (currentState == state) return;
        currentState = state;
        stopAllAnimators();

        switch (state) {
            case IDLE:
                setStateColors("#00EEFF", "#0088BB", "#00D4FF");
                startIdleAnimation();
                break;
            case LISTENING:
                setStateColors("#00FF88", "#00AA55", "#00FF88");
                startListeningAnimation();
                break;
            case PROCESSING:
                setStateColors("#FFB300", "#CC8800", "#FFB300");
                startProcessingAnimation();
                break;
            case SPEAKING:
                setStateColors("#00D4FF", "#0099CC", "#00D4FF");
                startSpeakingAnimation();
                break;
        }
        invalidate();
    }

    private void setStateColors(String core, String ring, String glow) {
        coreColor  = Color.parseColor(core);
        ring1Color = Color.parseColor(ring);
        glowColor  = Color.parseColor(glow);
        updateGradients(getWidth(), getHeight());
    }

    // ===================== ANIMATIONS =====================

    private void startIdleAnimation() {
        // Gentle breathe
        breatheAnim = ValueAnimator.ofFloat(0.92f, 1.05f);
        breatheAnim.setDuration(3200);
        breatheAnim.setRepeatMode(ValueAnimator.REVERSE);
        breatheAnim.setRepeatCount(ValueAnimator.INFINITE);
        breatheAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        breatheAnim.addUpdateListener(a -> { breathe = (float) a.getAnimatedValue(); invalidate(); });
        breatheAnim.start();

        // Slow glow pulse
        glowAnim = ValueAnimator.ofFloat(0.15f, 0.35f);
        glowAnim.setDuration(3200);
        glowAnim.setRepeatMode(ValueAnimator.REVERSE);
        glowAnim.setRepeatCount(ValueAnimator.INFINITE);
        glowAnim.addUpdateListener(a -> { glowAlpha = (float) a.getAnimatedValue(); });
        glowAnim.start();

        // Slow rotation
        rotAnim = ValueAnimator.ofFloat(0f, 360f);
        rotAnim.setDuration(20000);
        rotAnim.setRepeatCount(ValueAnimator.INFINITE);
        rotAnim.setInterpolator(new LinearInterpolator());
        rotAnim.addUpdateListener(a -> { rotationAngle = (float) a.getAnimatedValue(); invalidate(); });
        rotAnim.start();
    }

    private void startListeningAnimation() {
        // Fast pulse - reacting to sound
        pulseAnim = ValueAnimator.ofFloat(0.95f, 1.15f);
        pulseAnim.setDuration(500);
        pulseAnim.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnim.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnim.addUpdateListener(a -> { pulseScale = (float) a.getAnimatedValue(); invalidate(); });
        pulseAnim.start();

        glowAnim = ValueAnimator.ofFloat(0.4f, 0.7f);
        glowAnim.setDuration(500);
        glowAnim.setRepeatMode(ValueAnimator.REVERSE);
        glowAnim.setRepeatCount(ValueAnimator.INFINITE);
        glowAnim.addUpdateListener(a -> { glowAlpha = (float) a.getAnimatedValue(); });
        glowAnim.start();

        rotAnim = ValueAnimator.ofFloat(0f, 360f);
        rotAnim.setDuration(4000);
        rotAnim.setRepeatCount(ValueAnimator.INFINITE);
        rotAnim.setInterpolator(new LinearInterpolator());
        rotAnim.addUpdateListener(a -> { rotationAngle = (float) a.getAnimatedValue(); });
        rotAnim.start();

        breathe = 1.0f;
    }

    private void startProcessingAnimation() {
        // Spin fast - thinking
        rotAnim = ValueAnimator.ofFloat(0f, 360f);
        rotAnim.setDuration(1200);
        rotAnim.setRepeatCount(ValueAnimator.INFINITE);
        rotAnim.setInterpolator(new LinearInterpolator());
        rotAnim.addUpdateListener(a -> { rotationAngle = (float) a.getAnimatedValue(); invalidate(); });
        rotAnim.start();

        pulseAnim = ValueAnimator.ofFloat(1.0f, 1.08f);
        pulseAnim.setDuration(600);
        pulseAnim.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnim.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnim.addUpdateListener(a -> { pulseScale = (float) a.getAnimatedValue(); invalidate(); });
        pulseAnim.start();

        glowAlpha = 0.5f;
        breathe   = 1.0f;
    }

    private void startSpeakingAnimation() {
        // Wave-like expansion and contraction
        breatheAnim = ValueAnimator.ofFloat(0.9f, 1.12f);
        breatheAnim.setDuration(400);
        breatheAnim.setRepeatMode(ValueAnimator.REVERSE);
        breatheAnim.setRepeatCount(ValueAnimator.INFINITE);
        breatheAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        breatheAnim.addUpdateListener(a -> { breathe = (float) a.getAnimatedValue(); invalidate(); });
        breatheAnim.start();

        glowAnim = ValueAnimator.ofFloat(0.45f, 0.75f);
        glowAnim.setDuration(400);
        glowAnim.setRepeatMode(ValueAnimator.REVERSE);
        glowAnim.setRepeatCount(ValueAnimator.INFINITE);
        glowAnim.addUpdateListener(a -> { glowAlpha = (float) a.getAnimatedValue(); });
        glowAnim.start();

        rotAnim = ValueAnimator.ofFloat(0f, 360f);
        rotAnim.setDuration(8000);
        rotAnim.setRepeatCount(ValueAnimator.INFINITE);
        rotAnim.setInterpolator(new LinearInterpolator());
        rotAnim.addUpdateListener(a -> { rotationAngle = (float) a.getAnimatedValue(); });
        rotAnim.start();

        pulseScale = 1.0f;
    }

    private void stopAllAnimators() {
        if (pulseAnim   != null) pulseAnim.cancel();
        if (rotAnim     != null) rotAnim.cancel();
        if (glowAnim    != null) glowAnim.cancel();
        if (breatheAnim != null) breatheAnim.cancel();
        pulseScale    = 1.0f;
        rotationAngle = 0f;
        glowAlpha     = 0.3f;
        breathe       = 1.0f;
    }

    // ===================== HELPERS =====================

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.min(255, Math.max(0, (int)(Color.alpha(color) * factor)));
        if (alpha == 0 && factor > 0) alpha = (int)(255 * factor);
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAllAnimators();
    }

    public OrbState getCurrentState() { return currentState; }
}
