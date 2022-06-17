package com.aufa.githubstoryapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.aufa.githubstoryapp.databinding.ActivityAddStoryBinding
import com.aufa.githubstoryapp.model.StoryViewModel
import com.aufa.githubstoryapp.model.StoryViewModelFactory
import com.aufa.githubstoryapp.preference.SessionManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

class AddStoryActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityAddStoryBinding
    private var getFile: File? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var locationStatus: Boolean = false

    private lateinit var storyViewModel: StoryViewModel

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    R.string.dont_permission,
                    Toast.LENGTH_SHORT
                ).show()
                onDestroy()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = SessionManager(this)

        val storyViewModelFactory = StoryViewModelFactory(pref.fetchAuthToken().toString())
        storyViewModel = ViewModelProvider(this, storyViewModelFactory)[StoryViewModel::class.java]

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()

        binding.cameraButton.setOnClickListener { startTakePhoto() }
        binding.galleryButton.setOnClickListener { startGallery() }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    // Precise location access granted.
                    getMyLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    // Only approximate location access granted.
                    getMyLastLocation()
                }
                else -> {
                    // No location access granted.
                    createLocationRequest()
                }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLastLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    Toast.makeText(
                        this@AddStoryActivity,
                        R.string.location_on,
                        Toast.LENGTH_SHORT
                    ).show()

                    upload(location.latitude.toString(), location.longitude.toString())
                } else {
                    if (locationStatus) {
                        getMyLastLocation()
                    } else {
                        Toast.makeText(
                            this@AddStoryActivity,
                            R.string.cannot_access_location,
                            Toast.LENGTH_SHORT
                        ).show()

                        upload()
                    }
                }
            }
        } else {

            if (locationStatus) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else {
                Toast.makeText(
                    this@AddStoryActivity,
                    R.string.location_off,
                    Toast.LENGTH_SHORT
                ).show()

                upload()
            }
        }
    }

    private val resolutionLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    Log.i(TAG, "onActivityResult: All location settings are satisfied.")

                    startActivity(Intent(this, AddStoryActivity::class.java))
                    finish()
                }
                RESULT_CANCELED -> {
                    Toast.makeText(
                        this@AddStoryActivity,
                        R.string.location_permission,
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }
            }
        }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(1)
            maxWaitTime = TimeUnit.SECONDS.toMillis(1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                getMyLastLocation()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        resolutionLauncher.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(this@AddStoryActivity, sendEx.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
    }

    private fun upload(lat: String? = "", lon: String? = "") {
        binding.uploadButton.setOnClickListener {
            if (getFile != null) {
                var withLocation = false
                val file = reduceFileImage(getFile as File)

                val descValue = binding.descriptionBox.text.toString()

                if (descValue.isBlank()) {
                    Toast.makeText(
                        this@AddStoryActivity,
                        R.string.description_empty,
                        Toast.LENGTH_SHORT
                    ).show()

                    val i = Intent(this, AddStoryActivity::class.java)
                    startActivity(i)
                    finish()
                }

                val description = descValue.toRequestBody("text/plain".toMediaType())
                val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    file.name,
                    requestImageFile
                )
                if (lat!!.isNotEmpty() && lon!!.isNotEmpty()) {
                    storyViewModel.uploadImageWithLocation(
                        imageMultipart = imageMultipart,
                        description = description,
                        lat = lat.toRequestBody("text/plain".toMediaType()),
                        lon = lon.toRequestBody("text/plain".toMediaType())
                    )
                    withLocation = true
                } else {
                    storyViewModel.uploadImage(
                        imageMultipart = imageMultipart,
                        description = description
                    )
                }

                storyViewModel.uploadStatus.observe(this) { status ->
                    if (status) {
                        if (withLocation) {
                            Toast.makeText(
                                this@AddStoryActivity,
                                R.string.upload_with_location_success,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@AddStoryActivity,
                                R.string.upload_success,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@AddStoryActivity,
                            R.string.upload_failed,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                val i = Intent(this, EmptyActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(i)
                finish()
            } else {
                Toast.makeText(
                    this@AddStoryActivity,
                    R.string.please_input_image,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)
        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@AddStoryActivity,
                "com.aufa.githubstoryapp",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private lateinit var currentPhotoPath: String
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            getFile = myFile

            val result = BitmapFactory.decodeFile(myFile.path)

            binding.previewImageView.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this@AddStoryActivity)
            getFile = myFile
            binding.previewImageView.setImageURI(selectedImg)
        }
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        const val TAG = "AddStoryActivity"

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onMapReady(googleMap: GoogleMap) {

    }

}