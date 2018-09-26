package com.kyotob.client;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

/**
 * Created by julian on 13/6/21.
 */
public class CircleTransformForChatView implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        // 背景色を設定
        canvas.drawColor(Color.parseColor("#00000000"));
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);


        float r = size/2f;
        canvas.drawCircle(r, r, r, paint);
        Paint p = new Paint();
        p.setStrokeWidth(30);
        p.setStyle(Paint.Style.STROKE); // 塗りつぶし無し
        p.setColor(Color.parseColor("#4fa3ff")); // 青色
        canvas.drawCircle(r, r, r*0.97f, p); // 丸い円を書く

        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}
