package com.rin2401.r3ach;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.view.Surface;
import java.nio.ByteBuffer;

public class ImageTransmogrifier implements OnImageAvailableListener {
    private final int height;
    private final ImageReader imageReader;
    private Bitmap latestBitmap = null;
    private final BubbleService svc;
    private final int width;

    ImageTransmogrifier(BubbleService bubbleService) {
        this.svc = bubbleService;
        this.width = Constans.WIDTH;
        this.height = Constans.HEIGHT;
        this.imageReader = ImageReader.newInstance(this.width, this.height, 1, 1);
        this.imageReader.setOnImageAvailableListener(this, bubbleService.getHandler());
    }

    public void onImageAvailable(ImageReader imageReader) {
        try {
            Image acquireLatestImage = imageReader.acquireLatestImage();
            if (acquireLatestImage != null) {
                Plane[] planes = acquireLatestImage.getPlanes();
                ByteBuffer buffer = planes[0].getBuffer();
                int pixelStride = planes[0].getPixelStride();
                Bitmap createBitmap = Bitmap.createBitmap(this.width + ((planes[0].getRowStride() - (this.width * pixelStride)) / pixelStride), this.height, Config.ARGB_8888);
                createBitmap.copyPixelsFromBuffer(buffer);
                this.svc.processImage(createBitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public Surface getSurface() {
        return this.imageReader.getSurface();
    }

    /* Access modifiers changed, original: 0000 */
    public int getWidth() {
        return this.width;
    }

    /* Access modifiers changed, original: 0000 */
    public int getHeight() {
        return this.height;
    }

    /* Access modifiers changed, original: 0000 */
    public void close() {
        this.imageReader.close();
    }
}
