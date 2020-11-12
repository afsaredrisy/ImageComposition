package io.github.kiranscaria.imagecompositor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.io.IOException
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

import java.util.*

class tflite_inference(context: Context, private var useGpu: Boolean = true) {
    private var gpuDelegate: GpuDelegate? = null;
    private var numThreads = 4;
    private lateinit var compositeInterpreter: Interpreter;

    private var fullExecutionTime = 0L;
    private var preProcessTime = 0L;
    private var compositePredictionTime = 0L;
    private var postProcessTime = 0L;

    public fun getRuntime(): Long{
        return compositePredictionTime;
    }

    init {
        compositeInterpreter = if (useGpu) {
            getInterpreter(context, COMPOSITION_MODEL, true);
        } else {
            getInterpreter(context, COMPOSITION_MODEL, false);
        }
    }

    companion object {
        private const val TAG = "ImageCompositor";
        private const val BIG_IMAGE_SIZE = 512;
        private const val SMALL_IMAGE_SIZE = 128;
        private const val COMPOSITION_MODEL = "YGC-Combinenet_converted_model.tflite";
    }

    fun execute(foregroundBitmap: Bitmap, backgroundBitmap: Bitmap, context: Context
    ): Map<String, Any> {
        try {
            Log.d(TAG, "Running model...");

            fullExecutionTime = SystemClock.uptimeMillis();
            preProcessTime = SystemClock.uptimeMillis();


            val foregroundSmall = ImageUtils.bitmapToByteBuffer(foregroundBitmap, SMALL_IMAGE_SIZE, SMALL_IMAGE_SIZE);
            val foregroundBig = ImageUtils.bitmapToByteBuffer(foregroundBitmap, BIG_IMAGE_SIZE, BIG_IMAGE_SIZE);
            val backgroundBig = ImageUtils.bitmapToByteBuffer(backgroundBitmap, BIG_IMAGE_SIZE, BIG_IMAGE_SIZE);

            val inputsToModel = arrayOf<Any>(foregroundSmall, foregroundBig, backgroundBig);
            val outputsFromModel = HashMap<Int, Any>()
            val compositePrediction = Array(1) {Array(BIG_IMAGE_SIZE) {Array(BIG_IMAGE_SIZE) {FloatArray(3)} } }
            outputsFromModel[0] = compositePrediction;
            preProcessTime = SystemClock.uptimeMillis() - preProcessTime;

            compositePredictionTime = SystemClock.uptimeMillis();
            compositeInterpreter.runForMultipleInputsOutputs(inputsToModel, outputsFromModel);
            compositePredictionTime = SystemClock.uptimeMillis() - compositePredictionTime;
            Log.d(TAG, "Composite generation time: $compositePredictionTime");

            postProcessTime = SystemClock.uptimeMillis()
            var compositeImage = ImageUtils.convertArrayToBitmap(compositePrediction, BIG_IMAGE_SIZE, BIG_IMAGE_SIZE);
            postProcessTime = SystemClock.uptimeMillis() - postProcessTime;

            fullExecutionTime = SystemClock.uptimeMillis() - fullExecutionTime;
            Log.d(TAG, "Entire pipeline execution: $fullExecutionTime");

            val modelExecutionResult = mapOf<String, Any>(
                "compositeImage" to compositeImage,
                "preProcessTime" to preProcessTime,
                "compositePredictionTime" to compositePredictionTime,
                "postProcessTime" to postProcessTime,
                "fullExecutionTime" to fullExecutionTime
            )

            return modelExecutionResult;

        } catch (e: Exception) {
            val exceptionLog = "Something went wrong: ${e}";
            Log.d(TAG, exceptionLog)

            val emptyBitmap = ImageUtils.createEmptyBitmap(BIG_IMAGE_SIZE, BIG_IMAGE_SIZE);

            return mapOf();
        }
    }


    private fun loadModelFile(context: Context, modelFile: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelFile)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val retFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        fileDescriptor.close()
        return retFile
    }

    private fun getInterpreter(context: Context, modelName: String, useGpu: Boolean = false): Interpreter {
        val tfliteOptions = Interpreter.Options()
        tfliteOptions.setNumThreads(numThreads)

        gpuDelegate = null
        if (useGpu) {
            gpuDelegate = GpuDelegate()
            tfliteOptions.addDelegate(gpuDelegate)
        }

        tfliteOptions.setNumThreads(numThreads)
        return Interpreter(loadModelFile(context, modelName), tfliteOptions)
    }
}
