package com.circusscientist.monkeydetector

import ai.fritz.core.Fritz
import ai.fritz.labelmodelfast.ImageLabelOnDeviceModelFast
import ai.fritz.vision.FritzVision
import ai.fritz.vision.FritzVisionImage
import ai.fritz.vision.ImageRotation
import ai.fritz.vision.PredictorStatusListener
import ai.fritz.vision.imagelabeling.FritzVisionLabelPredictor
import ai.fritz.vision.imagelabeling.ImageLabelManagedModelFast
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors
import khttp.get


class MainActivity : AppCompatActivity() {

    private val API_KEY = "place_your_API_Key_here" //see circusscientist.com for details
    private val executor = Executors.newSingleThreadExecutor()
    private var MY_PERMISSIONS_REQUEST_CAMERA = 1
    private lateinit var mp: MediaPlayer
    //global vars idea from: https://stackoverflow.com/questions/52844343/kotlin-set-value-of-global-variable
    companion object {

        var monkey = "dog" //could be anything here "dog" is just a placeholder
        var playing = false
        var teddy_bear = "teddy_bear "

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //moved all startup code to after permissions granted below
        //but app still doesn't work on first run correctly...

        //check camera permissions
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    MY_PERMISSIONS_REQUEST_CAMERA
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            setContentView(R.layout.activity_main)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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
        mp = MediaPlayer.create(applicationContext, R.raw.heyyou4)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    setContentView(R.layout.activity_main)
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                    /*In the MainActivity class, initialize Fritz SDK.
                    */
                    Fritz.configure(this, API_KEY)

                    /*Instead of calling `startCamera()` on the main thread, we use `viewFinder.post { ... }`
                     to make sure that `viewFinder` has already been inflated into the view when `startCamera()` is called.
                     */
                    view_finder.post {
                        startCamera()
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
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

    //asynctask to run http get:
    // Create inner class by extending the AsyncTask
    inner class MyAsyncTask2 : AsyncTask<String, Int, Int>() {
        //Override the doInBackground method
        override fun doInBackground(vararg params: String): Int {
            val count: Int = params.size
            var index = 0
            while (index < count) {
                Log.d(
                    "Kotlin", "In doInBackground Method and Total parameter passed is :$count " +
                            "and processing $index with value: ${params[index]}"
                )
//                 khttp.get from: https://www.kotlinresources.com/library/khttp/
                val urlToGoTo = get("http://192.168.8.13/socket1Off")
                println(urlToGoTo.url)
                println(get("http://192.168.8.13/socket1Off").text)


                Thread.sleep(1000)
                index++
            }
            return 1
        }

        // Override the onProgressUpdate method to post the update on main thread
        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            Log.d("Kotlin", "On ProgressUpdate Method")
        }
        // Setup the intial UI before execution of the background task
        override fun onPreExecute() {
            super.onPreExecute()
            Log.d("Kotlin", "On PreExecute Method")
        }
        // Update the final status by overriding the OnPostExecute method.
        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            Log.d("Kotlin", "On Post Execute and size of String is:$result")
        }
    }

    //asynctask to run http get:
    // Create inner class by extending the AsyncTask
    //I have no idea how to make this work for on and off, so 2x AsyncTasks, on and off....
    inner class MyAsyncTask : AsyncTask<String, Int, Int>() {
        //Override the doInBackground method
        override fun doInBackground(vararg params: String): Int {
            val count: Int = params.size
            var index = 0
            while (index < count) {
                Log.d(
                    "Kotlin", "In doInBackground Method and Total parameter passed is :$count " +
                            "and processing $index with value: ${params[index]}"
                )
//                 khttp.get from: https://www.kotlinresources.com/library/khttp/
                val urlToGoTo = get("http://192.168.8.13/socket1On")
                println(urlToGoTo.url)
                println(get("http://192.168.8.13/socket1On").text)
                Thread.sleep(1000)
                index++
            }
            return 1
        }

        // Override the onProgressUpdate method to post the update on main thread
        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            Log.d("Kotlin", "On ProgressUpdate Method")
        }
        // Setup the intial UI before execution of the background task
        override fun onPreExecute() {
            super.onPreExecute()
            Log.d("Kotlin", "On PreExecute Method")
        }
        // Update the final status by overriding the OnPostExecute method.
        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            Log.d("Kotlin", "On Post Execute and size of String is:$result")
        }
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



            //FritzOnDeviceModel
            val imageLabelOnDeviceModel =  ImageLabelOnDeviceModelFast() //FritzVisionLabelPredictor
            val predictor = FritzVision.ImageLabeling.getPredictor(imageLabelOnDeviceModel)

//below is to load model at runtime. smaller apk but fritz.ai don't want me to do it 10 000 times!:

//            val managedModel = ImageLabelManagedModelFast() //for load model at runtime

//            FritzVision.ImageLabeling.loadPredictor(
//                managedModel,
//                object : PredictorStatusListener<FritzVisionLabelPredictor> {
//                    override fun onPredictorReady(p0: FritzVisionLabelPredictor?) {
//                        Log.d(TAG, "Image Labeling predictor is ready")
//                        predictor = p0
//                    }
//                })

            val labelResult = predictor?.predict(visionImage)

            runOnUiThread {
                labelResult?.resultString?.let {
                    val sname = it.split(":")
                    Log.e(TAG, it)
                    Log.e(TAG, sname[0])
                    Log.e(TAG, "checking for monkey...")
                    tv_name.text = sname[0]
                    val mmm: String = sname[0]
                    monkey = mmm
//                    monkey = sname[0] as String
                    if (monkey.compareTo("monkey ") == 0 || monkey.compareTo("baboon ") == 0) {
                        Log.e(TAG, "monkey monkey monkey it works!")
                        tv_name.text = "MONKEYYYYYYYYYYSSSSSSSS!!!!!!!!!!!"
                        //this plays only once at a time, although monkey is detected many times... works!
                        if (!playing) {
                            //now initialising onCreate!
//                            val mediaPlayer: MediaPlayer? =
//                                MediaPlayer.create(applicationContext, R.raw.siren)
//                            //set playing to false on completion:
                            mp?.setOnCompletionListener {
                                playing = false
                                var task2: MyAsyncTask2 = MyAsyncTask2()
                                task2.execute("http://192.168.8.13/socket1Off")
                            }
                            //now playing - don't play again until complete:
                            playing = true
                            mp?.start() // no need to call prepare(); create() does that for you                            //asynctask http get: - from https://medium.com/nplix/android-asynctask-example-in-kotlin-for-background-processing-of-task-59ed88d8c545
                            // Declare the AsyncTask and start the execution
                            var task: MyAsyncTask = MyAsyncTask()
                            task.execute("http://192.168.8.13/socket1On")
                            "http://192.168.8.13/socket1On"
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
