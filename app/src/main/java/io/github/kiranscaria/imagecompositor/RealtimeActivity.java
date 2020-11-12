package io.github.kiranscaria.imagecompositor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

import io.github.kiranscaria.imagecompositor.camera.CameraActivity;
import io.github.kiranscaria.imagecompositor.camera.views.OverlayView;

public class RealtimeActivity extends CameraActivity {
    private tflite_inference inference;
    private Bitmap rgbCameraFrameBitmap = null;
    private Bitmap segmentedFrameBitmap = null;
    private Bitmap backgroundFrameBitmap = null;
    private Context context;
    private ImageView outputview;
    Bitmap compositImage;
    private TextView runtimeTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setFrontCamera(true);
        super.onCreate(savedInstanceState);
        context=this;
    }


    @Override
    protected void processImage() {
        Log.d(TAG,"Proccessing image");
        rgbCameraFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        Bitmap rgbCameraFrameBitmapi=Bitmap.createScaledBitmap(rgbCameraFrameBitmap,512,512,true);
        backgroundFrameBitmap=Bitmap.createScaledBitmap(backgroundFrameBitmap,512,512,true);
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        long startTime = SystemClock.uptimeMillis();
                        Map<?,?> modelExecutionResult = inference.execute(rgbCameraFrameBitmapi,backgroundFrameBitmap,context);//imageSegmenter.predictSegmentation(rotCamFrame, coloredMaskClasses);

                        Bitmap compositImagei = (Bitmap) modelExecutionResult.get("compositeImage");
                        Matrix matrix = new Matrix();

                        matrix.postRotate(270);
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(compositImagei, previewWidth, previewHeight, true);
                        compositImage = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                outputview.setImageBitmap(compositImage);
                                runtimeTextview.setText("Runtime : "+(inference.getRuntime()));
                            }
                        });
                        readyForNextImage();
                    }
                });
    }

    @Override
    protected void onPreviewSizeChosen(Size size, int rotation, boolean isFrontCam) {
            inference=new tflite_inference(this,true);
            rgbCameraFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
            backgroundFrameBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.background_0);
            outputview = findViewById(R.id.segmentation_overlay);
            runtimeTextview=findViewById(R.id.runtime);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_preview_fragment;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return new Size(1920, 1080);
    }
}