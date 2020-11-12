package io.github.kiranscaria.imagecompositor

import android.content.Intent
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavArgument
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.random.Random

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class CompositionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_composite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val foregroundImages: TypedArray = resources.obtainTypedArray(R.array.foreground_images)
        val selectedForeground: Int = Random.nextInt(foregroundImages.length())
        val backgroundImages: TypedArray = resources.obtainTypedArray(R.array.background_images)
        val selectedBackground: Int = Random.nextInt(backgroundImages.length())

        view.findViewById<ImageView>(R.id.foregroundImageView)
            .setImageResource(foregroundImages.getResourceId(selectedForeground, R.drawable.foreground_0))
        view.findViewById<ImageView>(R.id.backgroundImageView)
            .setImageResource(backgroundImages.getResourceId(selectedBackground, R.drawable.background_0))

        // make the loading bar visible
        val progressBarCyclic = requireActivity().findViewById<ProgressBar>(R.id.progressBarCyclic)


        view.findViewById<Button>(R.id.compositeButton).setOnClickListener {
            progressBarCyclic.visibility = View.VISIBLE

            val bundle = bundleOf(
                "selectedForeground" to selectedForeground,
                "selectedBackground" to selectedBackground
            )

            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
        }

    }
}
