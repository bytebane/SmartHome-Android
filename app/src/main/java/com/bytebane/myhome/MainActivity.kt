package com.bytebane.myhome

import android.animation.ValueAnimator
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import com.google.android.material.color.DynamicColors
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock


class MainActivity : ComponentActivity() {

    private lateinit var deviceStatus: TextView
    private lateinit var switch1: ToggleButton
    private lateinit var switch2: ToggleButton
    private lateinit var switch3: ToggleButton
    private lateinit var switch4: ToggleButton
    private lateinit var switch5: ToggleButton
    private lateinit var mainSwitch: ToggleButton
    private lateinit var fanSpeed: SeekBar
    private val fanAnimator = ValueAnimator.ofFloat(0f, 360f)

    private lateinit var inAnimation: AlphaAnimation
    private lateinit var outAnimation: AlphaAnimation
    private lateinit var progressBarHolder: FrameLayout
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var noInternetView: ConstraintLayout
    private lateinit var menuBtn: ImageButton

    private val coroutineScope =
        CoroutineScope(Job() + Dispatchers.Main + CoroutineName("FireBaseRTDB"))
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e(
            "FireBaseRTDB",
            exception.message.toString()
        )
    }

    private val dataLiveData = MutableLiveData<DataSnapshot>()

    private lateinit var switchMap: Map<String, ToggleButton>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DynamicColors.applyToActivitiesIfAvailable(this);

//        Views Initialization & Settings
        progressBarHolder = findViewById(R.id.progressBarHolder)
        noInternetView = findViewById(R.id.noInternetView)

        deviceStatus = findViewById(R.id.deviceStatus)
        switch1 = findViewById(R.id.switch1)
        switch2 = findViewById(R.id.switch2)
        switch3 = findViewById(R.id.switch3)
        switch4 = findViewById(R.id.switch4)
        switch5 = findViewById(R.id.switch5)
        fanSpeed = findViewById(R.id.fanSpeed)
        mainSwitch = findViewById(R.id.mainSwitch)
        menuBtn = findViewById(R.id.menuBtn)


//        Create a map of switches for convenience
        switchMap = mapOf(
            "switch1" to switch1,
            "switch2" to switch2,
            "switch3" to switch3,
            "switch4" to switch4,
            "switch5" to switch5
        )

//        Show the loader View
        inAnimation = AlphaAnimation(0f, 1f)
        inAnimation.duration = 200
        progressBarHolder.animation = inAnimation
        progressBarHolder.visibility = View.VISIBLE

        if (!checkForInternet(this)) {
            noInternetView.animation = inAnimation
            noInternetView.visibility = View.VISIBLE
            progressBarHolder.visibility = View.GONE
        }

//        ViewAnimator Setup
        fanAnimator.repeatCount = ValueAnimator.INFINITE
        fanAnimator.interpolator = LinearInterpolator()
        fanAnimator.addUpdateListener { animation ->
            switch5.rotation = animation.animatedValue as Float
        }


//        Coroutine First Time Data Loading
        coroutineScope.launch(coroutineExceptionHandler) {
            dataLiveData.value = FirebaseCrud.getData()

            fanSpeed.progress =
                dataLiveData.value!!.child("/fans/fanSpeed").value.toString().toInt()
            switch1.isChecked = dataLiveData.value!!.child("/switches/switch1").value as Boolean
            switch2.isChecked = dataLiveData.value!!.child("/switches/switch2").value as Boolean
            switch3.isChecked = dataLiveData.value!!.child("/switches/switch3").value as Boolean
            switch4.isChecked = dataLiveData.value!!.child("/switches/switch4").value as Boolean
            switch5.isChecked = dataLiveData.value!!.child("switches/switch5").value as Boolean

//            val deviceDateTime = dataLiveData.value!!.child("deviceStats/dateTime").value as String

//            Log.i("TAG-DevStats", deviceDateTime)
//            val elapsedTime = Duration.between(LocalDateTime.now(), LocalDateTime.parse(deviceDateTime)).seconds

//            val currentTimeStamp: Long = Clock.System.now().toEpochMilliseconds()
//            val deviceTimeStamp = kotlinx.datetime.
//            Log.i("TAG-Dev-now", currentTimeStamp.toString())
//            Log.i("TAG-Dev-device", deviceTimeStamp.toString())
//            val elapsedTime = DateTime.now().difference(DateTime.parse(mydbEvent.snapshot.value.toString()))
//            if (elapsedTime.inSeconds > 10) {
//                _isDeviceOnline.value = false
//            } else {
//                _isDeviceOnline.value = true
//            }

            updateFanSpeed()
        }

        menuBtn.setOnClickListener{view ->
            showPopup(view)
        }

        // Observe the dataLiveData object
        dataLiveData.observe(this) { snapshot ->
            // If the data retrieval is successful, hide the progress bar
            if (snapshot != null) {
                outAnimation = AlphaAnimation(1f, 0f)
                outAnimation.duration = 200
                progressBarHolder.animation = outAnimation
                progressBarHolder.visibility = View.GONE
                if (!checkForInternet(this)) {
                    noInternetView.animation = outAnimation
                    noInternetView.visibility = View.GONE
                }
            }
        }

//        Listen for changes to the fanData
        FirebaseCrud.getFansData().observe(this) { snapshot ->
            if (snapshot.key == "fanSpeed") {
                fanSpeed.progress = snapshot.value.toString().toInt()
                updateFanSpeed()
            }
        }

//        Listen for changes to the switchData
        FirebaseCrud.getSwitchesData().observe(this) { snapshot ->
            switchMap[snapshot.key]?.isChecked = snapshot.value as Boolean
        }

//        Listen for changes to the Fan Speed Control SeekBar
        fanSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (progress > 0) {
                        fanAnimator.start()
                        switch5.isChecked = true
                    }
                    updateFanSpeed()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

//        Add a listener to the mainSwitch to toggle all the switches
        mainSwitch.setOnClickListener {
            toggleAllSwitches()
        }

//        Add a listener to each switch
        switchMap.values.map { switch ->
            switch.setOnCheckedChangeListener { _, isChecked ->
                // If all switches are on, toggle mainSwitch on
                coroutineScope.launch(coroutineExceptionHandler) {
                    FirebaseCrud.setSwitchStatus(switch.text.toString(), isChecked)
                }
                if (isChecked && switch1.isChecked && switch2.isChecked && switch3.isChecked && switch4.isChecked && switch5.isChecked)
                    mainSwitch.isChecked = true

                if (!isChecked)
                    mainSwitch.isChecked = false

                if (switch5.isChecked && fanSpeed.progress > 0)
                    fanAnimator.start()

                if (!switch5.isChecked) {
                    fanAnimator.cancel()
                }
            }
        }
    }

    //    toggle all the switches on and off by main switch
    private fun toggleAllSwitches() {
        switchMap.values.map { switch ->
            switch.isChecked = mainSwitch.isChecked
        }
    }

    //    Function to update the fan speed
    private fun updateFanSpeed() {
        when (fanSpeed.progress) {
            0 -> {
                fanAnimator.duration = 0
                switch5.isChecked = false
            }

            1 -> {
                coroutineScope.launch(coroutineExceptionHandler) {
                    FirebaseCrud.setFanSpeed(1)
                }
                fanAnimator.duration = 1500
            }

            2 -> {
                coroutineScope.launch(coroutineExceptionHandler) {
                    FirebaseCrud.setFanSpeed(2)
                }
                fanAnimator.duration = 1200
            }

            3 -> {
                coroutineScope.launch(coroutineExceptionHandler) {
                    FirebaseCrud.setFanSpeed(3)
                }
                fanAnimator.duration = 900
            }

            4 -> {
                coroutineScope.launch(coroutineExceptionHandler) {
                    FirebaseCrud.setFanSpeed(4)
                }
                fanAnimator.duration = 600
            }

            5 -> {
                coroutineScope.launch(coroutineExceptionHandler) {
                    FirebaseCrud.setFanSpeed(5)
                }
                fanAnimator.duration = 300
            }
        }
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(this, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
            true
        }
        popup.show()
    }

    //    Check for Internet availability
    private fun checkForInternet(context: Context): Boolean {

        // register activity with the connectivity manager service
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Returns a Network object corresponding to
            // the currently active default data network.
            val network = connectivityManager.activeNetwork ?: return false

            // Representation of the capabilities of an active network.
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                // Indicates this network uses a Wi-Fi transport,
                // or WiFi has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                // Indicates this network uses a Cellular transport. or
                // Cellular has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                // else return false
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
}
