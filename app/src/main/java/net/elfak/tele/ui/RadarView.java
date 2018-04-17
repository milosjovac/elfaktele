package net.elfak.tele.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RadarView extends View {

    final String DEGREE_SYMBOL = "\u00b0";

    // Utils attributes
    private Paint paintTransparent, gridPaintGreen, gridPaintGreenText, gridPaintWhite, gridPaintYellow, gridPaintYellowStrong, paintParticles;
    private int width, height, x0, y0;
    private int paddingLeft, paddingRight, paddingTop, paddingBottom, radiusMax, limitRadius, radius;
    private float scaleFactor;
    private RectF gridRectF1;
    private Rect gridRect1;
    private Map<Integer, Map> points = new ConcurrentHashMap<Integer, Map>() {{
        put(0, new HashMap() {{
            put("distance", 0);
            put("time", System.currentTimeMillis());
        }});
    }};
    private String angleText = "", dataText = "";

    public RadarView(Context context) {
        super(context);
        initAttributes(null);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttributes(attrs);
    }

    private void initAttributes(AttributeSet a) {
        paintTransparent = new Paint();
        paintTransparent.setColor(Color.parseColor("#222222"));
        paintParticles = new Paint();
        gridPaintWhite = new Paint();
        gridPaintWhite.setColor(Color.WHITE);
        gridPaintWhite.setAlpha(100);
        gridPaintWhite.setStyle(Paint.Style.STROKE);
        gridPaintWhite.setStrokeWidth(1);
        gridPaintGreen = new Paint();
        gridPaintGreen.setColor(Color.GREEN);
        gridPaintGreen.setStyle(Paint.Style.STROKE);
        gridPaintGreen.setStrokeWidth(2);
        gridPaintGreenText = new Paint();
        gridPaintGreenText.setColor(Color.GREEN);
        gridPaintGreenText.setTextSize(45);
        gridPaintYellow = new Paint();
        gridPaintYellow.setColor(Color.YELLOW);
        gridPaintYellow.setStyle(Paint.Style.STROKE);
        gridPaintYellow.setTextSize(30);
        gridPaintYellowStrong = new Paint();
        gridPaintYellowStrong.setColor(Color.YELLOW);
        gridPaintYellowStrong.setStyle(Paint.Style.STROKE);
        gridPaintYellowStrong.setStrokeWidth(6);

        gridRectF1 = new RectF();
        gridRect1 = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        paddingLeft = getPaddingLeft();
        paddingRight = getPaddingRight();
        paddingTop = getPaddingTop();
        paddingBottom = getPaddingBottom();

        // Try for a width based on our minimum
        int minw = paddingLeft + paddingRight + getSuggestedMinimumWidth();
        width = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let slider get as big as it can
        int minh = MeasureSpec.getSize(width) - paddingBottom + paddingTop;
        height = resolveSizeAndState(MeasureSpec.getSize(minh), heightMeasureSpec, 0);

        // Calculate utils attributes to avoid computations in onDraw()
        x0 = width / 2;
        y0 = height / 2;
        radius = (int) (0.4 * width);
        radiusMax = (int) (1.1 * radius);
        limitRadius = radiusMax;
        scaleFactor = (1501.f / width);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPaint(paintTransparent);
        paintGrid(canvas);
        drawDataPoints(canvas);
    }

    public void paintGrid(Canvas canvas) {

        // outer limit
        gridRectF1.bottom = y0 + radiusMax;
        gridRectF1.top = y0 - radiusMax;
        gridRectF1.left = x0 - radiusMax;
        gridRectF1.right = x0 + radiusMax;
        canvas.drawOval(gridRectF1, gridPaintGreen);

        // radial grid
        for (int i = 0; i < radiusMax; i += 80) {
            int radius = (int) (i / scaleFactor);
            gridRectF1.left = x0 - radius;
            gridRectF1.top = y0 - radius;
            gridRectF1.right = x0 + radius;
            gridRectF1.bottom = y0 + radius;
            canvas.drawOval(gridRectF1, gridPaintWhite);
        }

        // angular grid (fine ticks)
        int angularGridDivision = 12;
        int angularGridDivisionFine = 3;
        for (double d = -Math.PI; d < Math.PI; d += Math.PI / angularGridDivision / angularGridDivisionFine) {
            double cos = Math.cos(d);
            double sin = Math.sin(d);
            canvas.drawLine(x0 + (int) (Math.round(cos * (limitRadius - 5))),
                    y0 - (int) (Math.round(sin * (limitRadius - 5))),
                    x0 + (int) (Math.round(cos * (limitRadius + 5))),
                    y0 - (int) (Math.round(sin * (limitRadius + 5))),
                    gridPaintYellowStrong);
        }

        // angular grid (coarse)
        for (double d = -Math.PI; d < Math.PI; d += Math.PI / angularGridDivision) {
            canvas.drawLine(x0, y0, x0 + (int) (Math.round(Math.cos(d) * limitRadius)), y0 - (int) (Math.round(Math.sin(d) * limitRadius)), gridPaintWhite);
        }

        // angular grid labels
        for (double d = 0; d < Math.PI * (2. - 1. / angularGridDivision); d += Math.PI / angularGridDivision) {
            int degree = (int) (Math.round(d / Math.PI * 180.));
            String text = degree + DEGREE_SYMBOL;
            gridPaintYellow.getTextBounds(text, 0, text.length(), gridRect1);
            int labelRadius = radiusMax + gridRect1.height() * 2;
            canvas.drawText(text, x0 + (int) (Math.cos(d) * labelRadius) - text.length() / 2 - gridRect1.width() / 2, y0 - (int) (Math.sin(d) * labelRadius) + gridRect1.height() / 2, gridPaintYellow);
        }
    }

    public void setDataPoints(int angle, HashMap currentItem) {
        points.put(angle, currentItem);
        invalidate();
    }

    public void drawDataPoints(Canvas canvas) {
        for (Map.Entry<Integer, Map> entry : points.entrySet()) {
            double angle = entry.getKey().doubleValue();
            Map data = entry.getValue();
            int distance = (int) data.get("distance");
            long updated = (long) data.get("time");
            long timeDifference = System.currentTimeMillis() - updated;

            int x = (int) (Math.round(Math.cos(angle / 308. * Math.PI) * distance / scaleFactor / 3.3));
            int y = (int) (Math.round(Math.sin(angle / 308. * Math.PI) * distance / scaleFactor / 3.3));
            int r = 4 + (int) (distance / 400. / scaleFactor * 2f * Math.exp(-timeDifference / 10000.));

            // set the drawing color
            int colorRed = (int) (.8f * (1.01f - (float) (Math.pow(Math.exp(-timeDifference / 5000.), 2))) * 255);
            int colorGreen = (int) (.8f * (float) Math.exp(-timeDifference / 12000.) * 255);
            paintParticles.setColor(getIntFromColor(colorRed, colorGreen, 0));
            canvas.drawCircle(x0 + x, y0 + y, r, paintParticles);

            // radijalni zraci
            if (timeDifference < 300) {

                // clear old text
                paintParticles.setColor(paintTransparent.getColor());
                canvas.drawRect(0, 0, width, 180, paintParticles);

                // set the drawing color
                paintParticles.setColor(Color.GREEN);
                canvas.drawLine(x0, y0, x0 + x, y0 + y, paintParticles);

                // text
                angleText = "Receiving data, current angle: " + angle;
                canvas.drawText(angleText, 40, 80, gridPaintGreenText);
                dataText = data.toString();
                canvas.drawText(dataText, 40, 150, gridPaintGreenText);
            }
        }
        canvas.drawText(angleText, 40, 80, gridPaintGreenText);
        canvas.drawText(dataText, 40, 150, gridPaintGreenText);
    }

    public int getIntFromColor(int Red, int Green, int Blue) {
        Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        Blue = Blue & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }
}