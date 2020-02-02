package com.lavanya.imagelabeling

import ai.fritz.core.Fritz
import ai.fritz.vision.FritzVision
import ai.fritz.vision.FritzVisionImage
import ai.fritz.vision.ImageRotation
import ai.fritz.vision.PredictorStatusListener
import ai.fritz.vision.imagelabeling.FritzVisionLabelPredictor
import ai.fritz.vision.imagelabeling.ImageLabelManagedModelFast
import android.app.PendingIntent.getActivity
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private val API_KEY = "dcf6227d8ecf4968b4e1a1b5fc1c483b"
    private val executor = Executors.newSingleThreadExecutor()

    //global vars idea from: https://stackoverflow.com/questions/52844343/kotlin-set-value-of-global-variable
    companion object {

        var monkey = "dog"
        var playing = false

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*In the MainActivity class, initialize Fritz SDK.
        */
        Fritz.configure(this, API_KEY)

        /*Instead of calling `startCamera()` on the main thread, we use `viewFinder.post { ... }`
         to make sure that `viewFinder` has already been inflated into the view when `startCamera()` is called.
         */
        view_finder.post {
            startCamera()
        }
    }

    //Function that creates and displays the camera preview
    private fun startCamera() {

        //Specify the configuration for the preview
        val previewConfig = PreviewConfig.Builder()
            .apply {
                //Set the resolution of the captured image
                setTargetResolution(Size(1920, 1080))
            }
            .build()

        //Generate a preview
        val preview = Preview(previewConfig)

        //Add a listener to update preview automatically
        preview.setOnPreviewOutputUpdateListener {

            val parent = view_finder.parent as ViewGroup

            //Remove thr old preview
            parent.removeView(view_finder)

            //Add the new preview
            parent.addView(view_finder, 0)
            view_finder.surfaceTexture = it.surfaceTexture
        }

        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            // In our analysis, we care more about the latest image than
            // analyzing *every* image
            setImageReaderMode(
                ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE
            )
        }.build()

        val imageAnalysis = ImageAnalysis(analyzerConfig).apply {
            setAnalyzer(executor, ImageProcessor())
        }

        /*  Bind use cases to lifecycle. If Android Studio complains about "this"
            being not a LifecycleOwner, try rebuilding the project or updating the appcompat dependency to
            version 1.1.0 or higher.
         */
        CameraX.bindToLifecycle(this, preview, imageAnalysis)
//        Log.e("working?", "monkey working?")
//        monkey = "monkey"
//        if(monkey.compareTo("monkey") == 0){
//            Log.e("MONKEYFFF", "monkey monkey monkey it works!")
//        }
    }

    /* It allows us to define a custom class implementing the ImageAnalysis.Analyzer interface,
       which will be called with incoming camera frames.
    */
    inner class ImageProcessor : ImageAnalysis.Analyzer {
        var predictor: FritzVisionLabelPredictor? = null
        val TAG = javaClass.simpleName


        override fun analyze(image: ImageProxy?, rotationDegrees: Int) {
            Log.e("is it a monkey ?", monkey)
//            monkey = "monkey"
            if (monkey == "monkey ") {
                Log.e("MONKEYFFF", "monkey monkey monkeyYYY it works!")
            } else {
                Log.e("monkeysaysno", "no")
            }
            //Handle all the ML logic here
            val mediaImage = image?.image

            val imageRotation = ImageRotation.getFromValue(rotationDegrees)

            val visionImage = FritzVisionImage.fromMediaImage(mediaImage, imageRotation)

            val managedModel = ImageLabelManagedModelFast()



            FritzVision.ImageLabeling.loadPredictor(
                managedModel,
                object : PredictorStatusListener<FritzVisionLabelPredictor> {
                    override fun onPredictorReady(p0: FritzVisionLabelPredictor?) {
                        Log.d(TAG, "Image Labeling predictor is ready")
                        predictor = p0
                    }
                })

            val labelResult = predictor?.predict(visionImage)

            runOnUiThread {
                labelResult?.resultString?.let {
                    val sname = it.split(":")
                    Log.e(TAG, it)
                    Log.e(TAG, sname[0])
                    Log.e(TAG, "checking for monkey...")
                    tv_name.text = sname[0]
                    val mmm: String = sname[0] as String
                    monkey = mmm
//                    monkey = sname[0] as String
                    if (monkey.compareTo("monkey ") == 0) {
                        Log.e(TAG, "monkey monkey monkey it works!")
                        tv_name.text = "MONKEYYYYYYYYYYSSSSSSSS!!!!!!!!!!!"
                        //this plays only once at a time, although monkey is detected many times... works!
                        if (!playing) {
                            var mediaPlayer: MediaPlayer? =
                                MediaPlayer.create(applicationContext, R.raw.heyyou4)
                            //set playing to false on completion:
                            mediaPlayer?.setOnCompletionListener {
                                playing = false
                            }
                            //now playing - don't play again until complete:
                            playing = true
                            mediaPlayer?.start() // no need to call prepare(); create() does that for you
                        }

//                        mediaPlayer?.stop()

                    }
                } ?: kotlin.run {
                    tv_name.visibility = TextView.INVISIBLE

                }
            }
        }
    }
}
