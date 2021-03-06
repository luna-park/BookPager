package org.lunapark.dev.bookpagerlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.lunapark.dev.bookpagerlib.tools.Vector2D;

import java.lang.ref.WeakReference;
import java.util.List;

import static android.graphics.Color.LTGRAY;


public class PageCurlView extends View {

    private BookPage bookPage;
    private List<String> book;
    private boolean showPageNumber = false;
    //    private int backgroundPageColor = Color.rgb(235, 235, 205);
    private int backgroundPageColor = Color.rgb(235, 235, 235);
    private ChangePageListener changePageListener;

    /*
     * Our Log tag
     */
    private final static String TAG = "PageCurlView";

    // Debug text paint stuff
    private Paint mTextPaint;
    private TextPaint mTextPaintShadow;

    /*
     * Px / Draw call
     */
    private int mCurlSpeed = 62;

    /*
     * Fixed update time used to create a smooth curl animation
     */
    private int mUpdateRate;

    /*
     * The initial offset for x and y axis movements
     */
    private int mInitialEdgeOffset;

    /*
     * The mode we will use
     */
    private int mCurlMode;

    /*
     * Simple curl mode. Curl target will move only in one axis.
     */
    public static final int CURLMODE_SIMPLE = 0;

    /*
     * Dynamic curl mode. Curl target will move on both X and Y axis.
     */
    public static final int CURLMODE_DYNAMIC = 1;

    /*
     * Enable/Disable debug mode
     */
    private boolean bEnableDebugMode = false;

    /*
     * The context which owns us
     */
    private WeakReference<Context> mContext;

    /*
     * Handler used to auto flip time based
     */
    private FlipAnimationHandler mAnimationHandler;

    /*
     * Maximum radius a page can be flipped, by default it's the width of the view
     */
    private float mFlipRadius;

    /*
     * Point used to move
     */
    private Vector2D mMovement;

    /*
     * The finger position
     */
    private Vector2D mFinger;

    /*
     * Movement point form the last frame
     */
    private Vector2D mOldMovement;

    /*
     * Page curl edge
     */
    private Paint mCurlEdgePaint;

    /*
     * Our points used to define the current clipping paths in our draw call
     */
    private Vector2D mA, mB, mC, mD, mE, mF, mOldF, mOrigin;

    /*
     * Left and top offset to be applied when drawing
     */
//    private int mCurrentLeft, mCurrentTop;

    /*
     * If false no draw call has been done
     */
    private boolean bViewDrawn;

    /*
     * Defines the flip direction that is currently considered
     */
    private boolean bFlipRight;

    /*
     * If TRUE we are currently auto-flipping
     */
    private boolean bFlipping;

    /*
     * TRUE if the user moves the pages
     */
    private boolean bUserMoves;

    /*
     * Used to control touch input blocking
     */
    private boolean bBlockTouchInput = false;

    /*
     * Enable input after the next draw event
     */
    private boolean bEnableInputAfterDraw = false;

    /*
     * The current foreground
     */
    private Bitmap mForeground;

    /*
     * The current background
     */
    private Bitmap mBackground;

    /*
     * Current selected page
     */
    private int mIndex = 0;

    ///
    private Rect rect = new Rect();
    private Paint paint = new Paint();

    public void setOnChangePageListener(ChangePageListener changePageListener) {
        this.changePageListener = changePageListener;
    }

    /**
     * Inner class used to make a fixed timed animation of the curl effect.
     */
    private class FlipAnimationHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            PageCurlView.this.flipAnimationStep();
        }

        void sleep(long millis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), millis);
        }
    }


    public void setBook(List<String> book) {
        this.book = book;
        bookPage = new BookPage();
    }

    public void setBook(List<String> book, int startPage) {
        this.book = book;
        bookPage = new BookPage();
        mIndex = startPage;
    }

    /*
     * Base
     */
    public PageCurlView(Context context) {
        super(context);
        init(context);
        resetClipEdge();

    }

    /**
     * Construct the object from an XML file. Valid Attributes:
     *
     * @see View#View(Context, AttributeSet)
     */
    public PageCurlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

        // Get the data from the XML AttributeSet
        {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PageCurlView);

            // Get data
            bEnableDebugMode = a.getBoolean(R.styleable.PageCurlView_enableDebugMode, bEnableDebugMode);
            mCurlSpeed = a.getInt(R.styleable.PageCurlView_curlSpeed, mCurlSpeed);
            mUpdateRate = a.getInt(R.styleable.PageCurlView_updateRate, mUpdateRate);
            mInitialEdgeOffset = a.getInt(R.styleable.PageCurlView_initialEdgeOffset, mInitialEdgeOffset);
            mCurlMode = a.getInt(R.styleable.PageCurlView_curlMode, mCurlMode);

            // TODO set page back color
            int colorRef = a.getInt(R.styleable.PageCurlView_pageBackColor, backgroundPageColor);

            // recycle object (so it can be used by others)
            a.recycle();
        }

        resetClipEdge();
    }

    /**
     * Initialize the view
     */
    private void init(Context context) {
        // Foreground text paint
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(16);
        mTextPaint.setColor(0xFF000000);

        // The shadow
        mTextPaintShadow = new TextPaint();
        mTextPaintShadow.setAntiAlias(true);
        mTextPaintShadow.setTextSize(16);
        mTextPaintShadow.setColor(0x00000000);

        // Cache the context
        mContext = new WeakReference<Context>(context);

        // Base padding
        setPadding(3, 3, 3, 3);

        // The focus flags are needed
        setFocusable(true);
        setFocusableInTouchMode(true);

        mMovement = new Vector2D(0, 0);
        mFinger = new Vector2D(0, 0);
        mOldMovement = new Vector2D(0, 0);

        // Create our curl animation handler
        mAnimationHandler = new FlipAnimationHandler();

        // Create our edge paint
        mCurlEdgePaint = new Paint();
        mCurlEdgePaint.setColor(backgroundPageColor);
        mCurlEdgePaint.setAntiAlias(true);
        mCurlEdgePaint.setStyle(Style.FILL);
        mCurlEdgePaint.setShadowLayer(20, -5, 5, Color.LTGRAY);

        // Set the default props, those come from an XML :D

        mUpdateRate = 40;
        mInitialEdgeOffset = 20;
        mCurlMode = 1;

        setLayerType(View.LAYER_TYPE_SOFTWARE, paint);
    }

    /**
     * Reset points to it's initial clip edge state
     */
    public void resetClipEdge() {
        // Set our base movement
        mMovement.x = mInitialEdgeOffset;
        mMovement.y = mInitialEdgeOffset;
        mOldMovement.x = 0;
        mOldMovement.y = 0;

        // Now set the points
        // TODO: OK, those points MUST come from our measures and
        // the actual bounds of the view!
        mA = new Vector2D(mInitialEdgeOffset, 0);
        mB = new Vector2D(this.getWidth(), this.getHeight());
        mC = new Vector2D(this.getWidth(), 0);
        mD = new Vector2D(0, 0);
        mE = new Vector2D(0, 0);
        mF = new Vector2D(0, 0);
        mOldF = new Vector2D(0, 0);

        // The movement origin point
        mOrigin = new Vector2D(this.getWidth(), 0);
    }


    /**
     * Return the context which created use. Can return null if the
     * context has been erased.
     */
    private Context getViewContext() {
        return mContext.get();
    }

    /**
     * See if the current curl mode is dynamic
     *
     * @return TRUE if the mode is CURLMODE_DYNAMIC, FALSE otherwise
     */
    public boolean isCurlModeDynamic() {
        return mCurlMode == CURLMODE_DYNAMIC;
    }

    /**
     * Set the curl speed.
     *
     * @param curlSpeed - New speed in px/frame
     * @throws IllegalArgumentException if curlspeed < 1
     */
    public void setCurlSpeed(int curlSpeed) {
        if (curlSpeed < 1)
            throw new IllegalArgumentException("curlSpeed must be greated than 0");
        mCurlSpeed = curlSpeed;
    }

//    public void setCurlSpeed(int mCurlSpeed) {
//        this.mCurlSpeed = mCurlSpeed;
//    }

    /**
     * Get the current curl speed
     *
     * @return int - Curl speed in px/frame
     */
    public int getCurlSpeed() {
        return mCurlSpeed;
    }

    /**
     * Set the update rate for the curl animation
     *
     * @param updateRate - Fixed animation update rate in fps
     * @throws IllegalArgumentException if updateRate < 1
     */
    public void setUpdateRate(int updateRate) {
        if (updateRate < 1)
            throw new IllegalArgumentException("updateRate must be greated than 0");
        mUpdateRate = updateRate;
    }

    /**
     * Get the current animation update rate
     *
     * @return int - Fixed animation update rate in fps
     */
    public int getUpdateRate() {
        return mUpdateRate;
    }

    /**
     * Set the initial pixel offset for the curl edge
     *
     * @param initialEdgeOffset - px offset for curl edge
     * @throws IllegalArgumentException if initialEdgeOffset < 0
     */
    public void setInitialEdgeOffset(int initialEdgeOffset) {
        if (initialEdgeOffset < 0)
            throw new IllegalArgumentException("initialEdgeOffset can not negative");
        mInitialEdgeOffset = initialEdgeOffset;
    }

    /**
     * Get the initial pixel offset for the curl edge
     *
     * @return int - px
     */
    public int getInitialEdgeOffset() {
        return mInitialEdgeOffset;
    }

    /**
     * Set the curl mode.
     * <p>Can be one of the following values:</p>
     * <table>
     * <colgroup align="left" />
     * <colgroup align="left" />
     * <tr><th>Value</th><th>Description</th></tr>
     * <tr><td><code>{@link #CURLMODE_SIMPLE com.dcg.pagecurl:CURLMODE_SIMPLE}</code></td><td>Curl target will move only in one axis.</td></tr>
     * <tr><td><code>{@link #CURLMODE_DYNAMIC com.dcg.pagecurl:CURLMODE_DYNAMIC}</code></td><td>Curl target will move on both X and Y axis.</td></tr>
     * </table>
     *
     * @param curlMode
     * @throws IllegalArgumentException if curlMode is invalid
     * @see #CURLMODE_SIMPLE
     * @see #CURLMODE_DYNAMIC
     */
    public void setCurlMode(int curlMode) {
        if (curlMode != CURLMODE_SIMPLE &&
                curlMode != CURLMODE_DYNAMIC)
            throw new IllegalArgumentException("Invalid curlMode");
        mCurlMode = curlMode;
    }

    /**
     * Return an integer that represents the current curl mode.
     * <p>Can be one of the following values:</p>
     * <table>
     * <colgroup align="left" />
     * <colgroup align="left" />
     * <tr><th>Value</th><th>Description</th></tr>
     * <tr><td><code>{@link #CURLMODE_SIMPLE com.dcg.pagecurl:CURLMODE_SIMPLE}</code></td><td>Curl target will move only in one axis.</td></tr>
     * <tr><td><code>{@link #CURLMODE_DYNAMIC com.dcg.pagecurl:CURLMODE_DYNAMIC}</code></td><td>Curl target will move on both X and Y axis.</td></tr>
     * </table>
     *
     * @return int - current curl mode
     * @see #CURLMODE_SIMPLE
     * @see #CURLMODE_DYNAMIC
     */
    public int getCurlMode() {
        return mCurlMode;
    }

    /**
     * Enable debug mode. This will draw a lot of data in the view so you can track what is happening
     *
     * @param bFlag - boolean flag
     */
    public void setEnableDebugMode(boolean bFlag) {
        bEnableDebugMode = bFlag;
    }

    /**
     * Check if we are currently in debug mode.
     *
     * @return boolean - If TRUE debug mode is on, FALSE otherwise.
     */
    public boolean isDebugModeEnabled() {
        return bEnableDebugMode;
    }

    /**
     * @see View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int finalWidth, finalHeight;
        finalWidth = measureWidth(widthMeasureSpec);
        finalHeight = measureHeight(heightMeasureSpec);
        setMeasuredDimension(finalWidth, finalHeight);
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = specSize;
        }

        return result;
    }

    /**
     * Determines the height of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = specSize;
        }
        return result;
    }

    /**
     * Render the text
     *
     * @see View#onDraw(Canvas)
     */
    //@Override
    //protected void onDraw(Canvas canvas) {
    //	super.onDraw(canvas);
    //	canvas.drawText(mText, getPaddingLeft(), getPaddingTop() - mAscent, mTextPaint);
    //}

    //---------------------------------------------------------------
    // Curling. This handles touch events, the actual curling
    // implementations and so on.
    //---------------------------------------------------------------
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!bBlockTouchInput) {

            // Get our finger position
            mFinger.x = event.getX();
            mFinger.y = event.getY();
            int width = getWidth();

            // Depending on the action do what we need to
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mOldMovement.x = mFinger.x;
                    mOldMovement.y = mFinger.y;

                    // If we moved over the half of the display flip to next
                    if (mOldMovement.x > (width >> 1)) {
                        mMovement.x = mInitialEdgeOffset;
                        mMovement.y = mInitialEdgeOffset;

                        // Set the right movement flag
                        bFlipRight = true;
                    } else {
                        // Set the left movement flag
                        bFlipRight = false;

                        // go to next previous page
                        previousView();

                        // Set new movement
                        mMovement.x = isCurlModeDynamic() ? width << 1 : width;
                        mMovement.y = mInitialEdgeOffset;
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    bUserMoves = false;
                    bFlipping = true;
                    flipAnimationStep();
                    break;
                case MotionEvent.ACTION_MOVE:
                    bUserMoves = true;

                    // Get movement
                    mMovement.x -= mFinger.x - mOldMovement.x;
                    mMovement.y -= mFinger.y - mOldMovement.y;
                    mMovement = capMovement(mMovement, true);

                    // Make sure the y value get's locked at a nice level
                    if (mMovement.y <= 1)
                        mMovement.y = 1;

                    // Get movement direction
                    bFlipRight = mFinger.x < mOldMovement.x;

                    // Save old movement values
                    mOldMovement.x = mFinger.x;
                    mOldMovement.y = mFinger.y;

                    // Force a new draw call
                    DoPageCurl();
                    this.invalidate();
                    break;
            }

        }

        // TODO: Only consume event if we need to.
        return true;
    }

    /**
     * Make sure we never move too much, and make sure that if we
     * move too much to add a displacement so that the movement will
     * be still in our radius.
     *
     * @param bMaintainMoveDir - Cap movement but do not change the
     *                         current movement direction
     * @return Corrected point
     */
    private Vector2D capMovement(Vector2D point, boolean bMaintainMoveDir) {
        // Make sure we never ever move too much
        if (point.distance(mOrigin) > mFlipRadius) {
            if (bMaintainMoveDir) {
                // Maintain the direction
                point = mOrigin.sum(point.sub(mOrigin).normalize().mult(mFlipRadius));
            } else {
                // Change direction
                if (point.x > (mOrigin.x + mFlipRadius))
                    point.x = (mOrigin.x + mFlipRadius);
                else if (point.x < (mOrigin.x - mFlipRadius))
                    point.x = (mOrigin.x - mFlipRadius);
                point.y = (float) (Math.sin(Math.acos(Math.abs(point.x - mOrigin.x) / mFlipRadius)) * mFlipRadius);
            }
        }
        return point;
    }

    /**
     * Execute a step of the flip animation
     */
    public void flipAnimationStep() {
        if (!bFlipping) {
            return;
        }
        int width = getWidth();

        // No input when flipping
        bBlockTouchInput = true;

        // Handle speed
        float curlSpeed = mCurlSpeed;
        if (!bFlipRight)
            curlSpeed *= -1;

        // Move us
        mMovement.x += curlSpeed;
        mMovement = capMovement(mMovement, false);

        // Create values
        DoPageCurl();

        // Check for endings :D
        if (mA.x < 1 || mA.x > width - 1) {
            bFlipping = false;
            if (bFlipRight) {
                //SwapViews();
                nextView();
            }
            resetClipEdge();

            // Create values
            DoPageCurl();

            try {
                changePageListener.onPageChange(mIndex);
            } catch (NullPointerException e) {
                Log.e(TAG, "ChangePageListener not defined");
            }
            // Enable touch input after the next draw event
            bEnableInputAfterDraw = true;
        } else {
            mAnimationHandler.sleep(mUpdateRate);
        }

        // Force a new draw call
        this.invalidate();
    }

    /**
     * Do the page curl depending on the methods we are using
     */
    private void DoPageCurl() {
        if (bFlipping) {
            if (isCurlModeDynamic())
                doDynamicCurl();
            else
                doSimpleCurl();

        } else {
            if (isCurlModeDynamic())
                doDynamicCurl();
            else
                doSimpleCurl();
        }
    }

    /**
     * Do a simple page curl effect
     */
    private void doSimpleCurl() {
        int width = getWidth();
        int height = getHeight();

        // Calculate point A
        mA.x = width - mMovement.x;
        mA.y = height;

        // Calculate point D
        mD.x = 0;
        mD.y = 0;
        if (mA.x > width / 2) {
            mD.x = width;
            mD.y = height - (width - mA.x) * height / mA.x;
        } else {
            mD.x = 2 * mA.x;
            mD.y = 0;
        }

        // Now calculate E and F taking into account that the line
        // AD is perpendicular to FB and EC. B and C are fixed points.
        double angle = Math.atan((height - mD.y) / (mD.x + mMovement.x - width));
        double _cos = Math.cos(2 * angle);
        double _sin = Math.sin(2 * angle);

        // And get F
        mF.x = (float) (width - mMovement.x + _cos * mMovement.x);
        mF.y = (float) (height - _sin * mMovement.x);

        // If the x position of A is above half of the page we are still not
        // folding the upper-right edge and so E and D are equal.
        if (mA.x > width / 2) {
            mE.x = mD.x;
            mE.y = mD.y;
        } else {
            // So get E
            mE.x = (float) (mD.x + _cos * (width - mD.x));
            mE.y = (float) -(_sin * (width - mD.x));
        }
    }

    /**
     * Calculate the dynamic effect, that one that follows the users finger
     */
    private void doDynamicCurl() {
        int width = getWidth();
        int height = getHeight();

        // F will follow the finger, we add a small displacement
        // So that we can see the edge
        mF.x = width - mMovement.x + 0.1f;
        mF.y = height - mMovement.y + 0.1f;

        // Set min points
        if (mA.x == 0) {
            mF.x = Math.min(mF.x, mOldF.x);
            mF.y = Math.max(mF.y, mOldF.y);
        }

        // Get diffs
        float deltaX = width - mF.x;
        float deltaY = height - mF.y;

        float BH = (float) (Math.sqrt(deltaX * deltaX + deltaY * deltaY) / 2);
        double tangAlpha = deltaY / deltaX;
        double alpha = Math.atan(deltaY / deltaX);
        double _cos = Math.cos(alpha);
        double _sin = Math.sin(alpha);

        mA.x = (float) (width - (BH / _cos));
        mA.y = height;

        mD.y = (float) (height - (BH / _sin));
        mD.x = width;

        mA.x = Math.max(0, mA.x);
        if (mA.x == 0) {
            mOldF.x = mF.x;
            mOldF.y = mF.y;
        }

        // Get W
        mE.x = mD.x;
        mE.y = mD.y;

        // Correct
        if (mD.y < 0) {
            mD.x = width + (float) (tangAlpha * mD.y);
            mE.y = 0;
            mE.x = width + (float) (Math.tan(2 * alpha) * mD.y);
        }
    }

    /**
     * Swap to next view
     */
    private void nextView() {
        int foreIndex = mIndex + 1;
        if (foreIndex >= book.size()) {
            foreIndex = 0;
        }

        int backIndex = foreIndex + 1;
        if (backIndex >= book.size()) {
            backIndex = 0;
        }
        mIndex = foreIndex;
        setViews(foreIndex, backIndex);
    }

    /**
     * Swap to previous view
     */
    private void previousView() {
        int backIndex = mIndex;
        int foreIndex = backIndex - 1;
        if (foreIndex < 0) {
            foreIndex = book.size() - 1;
        }
        mIndex = foreIndex;
        setViews(foreIndex, backIndex);

        Log.e(TAG, String.format("index: %s; fore: %s; back: %s", mIndex, foreIndex, backIndex));
    }

    /**
     * Set current fore and background
     *
     * @param foreground - Foreground view index
     * @param background - Background view index
     */
    private void setViews(int foreground, int background) {
        mForeground = bookPage.getPage(book.get(foreground), true);
        mBackground = bookPage.getPage(book.get(background), false);
    }

    //---------------------------------------------------------------
    // Drawing methods
    //---------------------------------------------------------------

    @Override
    protected void onDraw(Canvas canvas) {
        // Always refresh offsets
//        mCurrentLeft = getLeft();
//        mCurrentTop = getTop();

        // Translate the whole canvas
        //canvas.translate(mCurrentLeft, mCurrentTop);

        // We need to initialize all size data when we first draw the view
        if (!bViewDrawn) {
            bViewDrawn = true;
            onFirstDrawEvent();
        }

        canvas.drawColor(Color.WHITE);


        // Curl pages
        //DoPageCurl();

        // TODO: This just scales the views to the current
        // width and height. We should add some logic for:
        //  1) Maintain aspect ratio
        //  2) Uniform scale
        //  3) ...
//        rect = new Rect();
        rect.left = 0;
        rect.top = 0;
        rect.bottom = getHeight();
        rect.right = getWidth();

        // First Page render
//        paint = new Paint();

        // Draw our elements

        drawForeground(canvas, rect, paint);
        drawBackground(canvas, rect, paint);
        drawCurlEdge(canvas);

        // Draw any debug info once we are done
        if (bEnableDebugMode)
            drawDebug(canvas);

        // Check if we can re-enable input
        if (bEnableInputAfterDraw) {
            bBlockTouchInput = false;
            bEnableInputAfterDraw = false;
        }

        // Restore canvas
        canvas.restore();
    }


    /**
     * Called on the first draw event of the view
     */
    protected void onFirstDrawEvent() {
        mFlipRadius = getWidth();
        bookPage.setViewSize(getWidth(), getHeight());
        updatePages();
        resetClipEdge();
        DoPageCurl();
    }

    /**
     * Draw the foreground
     */
    private void drawForeground(Canvas canvas, Rect rect, Paint paint) {
        canvas.drawBitmap(mForeground, null, rect, paint);

        // Draw the page number (first page is 1 in real life :D
        // there is no page number 0 hehe)
        drawPageNum(canvas, mIndex);
    }

    /**
     * Create a Path used as a mask to draw the background page
     */
    private Path createBackgroundPath() {
        Path path = new Path();
        path.moveTo(mA.x, mA.y);
        path.lineTo(mB.x, mB.y);
        path.lineTo(mC.x, mC.y);
        path.lineTo(mD.x, mD.y);
        path.lineTo(mA.x, mA.y);
        return path;
    }

    /**
     * Draw the background image.
     */
    private void drawBackground(Canvas canvas, Rect rect, Paint paint) {
        Path mask = createBackgroundPath();

        // Save current canvas so we do not mess it up
        canvas.save();
        canvas.clipPath(mask);
        canvas.drawBitmap(mBackground, null, rect, paint);

        // Draw the page number (first page is 1 in real life :D
        // there is no page number 0 hehe)
        drawPageNum(canvas, mIndex);
        canvas.restore();
    }

    /**
     * Creates a path used to draw the curl edge in.
     */
    private Path createCurlEdgePath() {
        Path path = new Path();
        path.moveTo(mA.x, mA.y);
        path.lineTo(mD.x, mD.y);
        path.lineTo(mE.x, mE.y);
        path.lineTo(mF.x, mF.y);
        path.lineTo(mA.x, mA.y);
        return path;
    }

    /**
     * Draw the curl page edge
     */
    private void drawCurlEdge(Canvas canvas) {
        Path path = createCurlEdgePath();
        canvas.drawPath(path, mCurlEdgePaint);
    }

    /**
     * Draw page num (let this be a bit more custom)
     */
    private void drawPageNum(Canvas canvas, int pageNum) {
        if (showPageNumber) {
            mTextPaint.setColor(Color.DKGRAY);
            pageNum++;
            String pageNumText = "- " + pageNum + " -";
            drawCentered(canvas, pageNumText, canvas.getHeight() - mTextPaint.getTextSize() - 5,
                    mTextPaint, mTextPaintShadow);
        }
    }

    //---------------------------------------------------------------
    // Debug draw methods
    //---------------------------------------------------------------

    /**
     * Draw a text with a nice shadow
     */
    public static void drawTextShadowed(Canvas canvas, String text, float x, float y, Paint textPain, Paint shadowPaint) {
        canvas.drawText(text, x - 1, y, shadowPaint);
        canvas.drawText(text, x, y + 1, shadowPaint);
        canvas.drawText(text, x + 1, y, shadowPaint);
        canvas.drawText(text, x, y - 1, shadowPaint);
        canvas.drawText(text, x, y, textPain);
    }

    /**
     * Draw a text with a nice shadow centered in the X axis
     */
    public static void drawCentered(Canvas canvas, String text, float y, Paint textPain, Paint shadowPaint) {
        float posx = (canvas.getWidth() - textPain.measureText(text)) / 2;
        drawTextShadowed(canvas, text, posx, y, textPain, shadowPaint);
    }

    /**
     * Draw debug info
     */
    private void drawDebug(Canvas canvas) {
        float posX = 10;
        float posY = 20;

        Paint paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setStyle(Style.STROKE);

        paint.setColor(Color.BLACK);
        canvas.drawCircle(mOrigin.x, mOrigin.y, getWidth(), paint);

        paint.setStrokeWidth(3);
        paint.setColor(Color.RED);
        canvas.drawCircle(mOrigin.x, mOrigin.y, getWidth(), paint);

        paint.setStrokeWidth(5);
        paint.setColor(Color.BLACK);
        canvas.drawLine(mOrigin.x, mOrigin.y, mMovement.x, mMovement.y, paint);

        paint.setStrokeWidth(3);
        paint.setColor(Color.RED);
        canvas.drawLine(mOrigin.x, mOrigin.y, mMovement.x, mMovement.y, paint);

        posY = debugDrawPoint(canvas, "A", mA, Color.RED, posX, posY);
        posY = debugDrawPoint(canvas, "B", mB, Color.GREEN, posX, posY);
        posY = debugDrawPoint(canvas, "C", mC, Color.BLUE, posX, posY);
        posY = debugDrawPoint(canvas, "D", mD, Color.CYAN, posX, posY);
        posY = debugDrawPoint(canvas, "E", mE, Color.YELLOW, posX, posY);
        posY = debugDrawPoint(canvas, "F", mF, LTGRAY, posX, posY);
        posY = debugDrawPoint(canvas, "Mov", mMovement, Color.DKGRAY, posX, posY);
        posY = debugDrawPoint(canvas, "Origin", mOrigin, Color.MAGENTA, posX, posY);
        posY = debugDrawPoint(canvas, "Finger", mFinger, Color.GREEN, posX, posY);

        // Draw some curl stuff (Just some test)
        /*
        canvas.save();
		Vector2D center = new Vector2D(getWidth()/2,getHeight()/2);
	    //canvas.rotate(315,center.x,center.y);

	    // Test each lines
		//float radius = mA.distance(mD)/2.f;
	    //float radius = mA.distance(mE)/2.f;
	    float radius = mA.distance(mF)/2.f;
		//float radius = 10;
	    float reduction = 4.f;
		RectF oval = new RectF();
		oval.top = center.y-radius/reduction;
		oval.bottom = center.y+radius/reduction;
		oval.left = center.x-radius;
		oval.right = center.x+radius;
		canvas.drawArc(oval, 0, 360, false, paint);
		canvas.restore();
		/**/
    }

    private float debugDrawPoint(Canvas canvas, String name, Vector2D point, int color, float posX, float posY) {
        return debugDrawPoint(canvas, name + " " + point.toString(), point.x, point.y, color, posX, posY);
    }

    private float debugDrawPoint(Canvas canvas, String name, float X, float Y, int color, float posX, float posY) {
        mTextPaint.setColor(color);
        drawTextShadowed(canvas, name, posX, posY, mTextPaint, mTextPaintShadow);
        Paint paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setColor(color);
        canvas.drawPoint(X, Y, paint);
        return posY + 15;
    }

    public boolean isShowPageNumber() {
        return showPageNumber;
    }

    public void setShowPageNumber(boolean showPageNumber) {
        this.showPageNumber = showPageNumber;
    }

    public void goToPage(int pageNum) {
        mIndex = pageNum;
        updatePages();
        invalidate();
    }

    private void updatePages() {
        int indexBackPage = mIndex + 1;
        if (mIndex >= book.size()) {
            mIndex = book.size() - 1;
            indexBackPage = 0;
        }

        if (mIndex < 0) {
            mIndex = 0;
            indexBackPage = 1;
        }

        mForeground = bookPage.getPage(book.get(mIndex), true);
        mBackground = bookPage.getPage(book.get(indexBackPage), false);
    }
}
