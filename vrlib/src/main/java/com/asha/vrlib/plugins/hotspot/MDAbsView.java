package com.asha.vrlib.plugins.hotspot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDViewBuilder;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.asha.vrlib.texture.MD360Texture;

import static com.asha.vrlib.common.VRUtil.checkMainThread;
import static com.asha.vrlib.common.VRUtil.notNull;

/**
 * Created by hzqiujiadi on 2017/4/12.
 * hzqiujiadi ashqalcn@gmail.com
 */

public abstract class MDAbsView extends MDAbsHotspot {

    private boolean mInvalidate;

    private MD360Texture texture;

    private View mAttachedView;

    private MDLayoutParams mLayoutParams;

    private Canvas mCanvas;

    private Bitmap mBitmap;

    public MDAbsView(MDViewBuilder builder) {
        super(builder.builderDelegate);
        this.mAttachedView = builder.attachedView;
        this.mLayoutParams = builder.layoutParams;
        this.mAttachedView.setLayoutParams(this.mLayoutParams);

        this.mBitmap = Bitmap.createBitmap(mLayoutParams.width, mLayoutParams.height, Bitmap.Config.ARGB_8888);
        this.mCanvas = new Canvas(mBitmap);

        requestLayout();
    }

    public void invalidate(){
        checkMainThread("invalidate must called in main thread.");
        notNull(mLayoutParams, "layout params can't be null");
        notNull(mAttachedView, "attached view can't be null");

        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mAttachedView.draw(mCanvas);
        mInvalidate = true;
    }

    public void requestLayout(){
        checkMainThread("invalidate must called in main thread.");
        notNull(mLayoutParams, "layout params can't be null");
        notNull(mAttachedView, "attached view can't be null");

        mAttachedView.measure(
                View.MeasureSpec.makeMeasureSpec(mLayoutParams.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(mLayoutParams.height, View.MeasureSpec.EXACTLY)
        );
        mAttachedView.layout(0, 0, mAttachedView.getMeasuredWidth(), mAttachedView.getMeasuredHeight());

        invalidate();
    }

    @Override
    protected void initInGL(Context context) {
        super.initInGL(context);

        texture = new MD360BitmapTexture(new MDVRLibrary.IBitmapProvider() {
            @Override
            public void onProvideBitmap(MD360BitmapTexture.Callback callback) {
                callback.texture(mBitmap);
            }
        });
        texture.create();
    }

    @Override
    public void renderer(int index, int width, int height, MD360Director director) {
        if (texture == null){
            return;
        }

        if (mInvalidate){
            mInvalidate = false;
            texture.notifyChanged();
        }

        texture.texture(program);

        if (texture.isReady()){
            super.renderer(index, width, height, director);
        }
    }

    public View getAttachedView() {
        return mAttachedView;
    }
}
