package io.github.kiranscaria.imagecompositor

import android.app.AlertDialog
import android.app.ProgressDialog
import android.app.ProgressDialog.show
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ResultFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // perform model inference
        val modelInference = tflite_inference(requireContext(), useGpu = true);

        val selectedForeground = arguments?.getInt("selectedForeground")
        val selectedBackground = arguments?.getInt("selectedBackground")

        val foregroundImages: TypedArray = resources.obtainTypedArray(R.array.foreground_images)
        val backgroundImages: TypedArray = resources.obtainTypedArray(R.array.background_images)

        var foregroundImage = BitmapFactory. decodeResource(
            resources, foregroundImages.getResourceId(
                selectedForeground!!, R.drawable.foreground_0))

        var backgroundImage = BitmapFactory.decodeResource(
            resources, backgroundImages.getResourceId(
                selectedBackground!!, R.drawable.background_0))

        foregroundImage = Bitmap.createScaledBitmap(foregroundImage, 512, 512, true);
        backgroundImage = Bitmap.createScaledBitmap(backgroundImage, 512, 512, true);

        val modelExecutionResult = modelInference.execute(
            foregroundImage,
            backgroundImage, requireContext());
        val compositeImage = modelExecutionResult["compositeImage"];

        // Display the composited image
        view.findViewById<ImageView>(R.id.resultImageView).setImageBitmap(compositeImage as Bitmap?);

        // loading has stopped
        val progressBarCyclic = requireActivity().findViewById<ProgressBar>(R.id.progressBarCyclic)
        progressBarCyclic.visibility = View.INVISIBLE

        view.findViewById<Button>(R.id.backButton).setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }
}