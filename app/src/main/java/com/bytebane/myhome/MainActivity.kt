package com.bytebane.myhome

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.MenuInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.bytebane.myhome.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding

    private val dataLiveData = MutableLiveData<DataSnapshot>()

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var switchMap: Map<String, ToggleButton>

    private lateinit var deviceDateTimeStr: String

    private lateinit var inAnimation: AlphaAnimation
    private lateinit var outAnimation: AlphaAnimation
    private val fanAnimator = ValueAnimator.ofFloat(0f, 360f)

    private val coroutineScope =
        CoroutineScope(Job() + Dispatchers.Main + CoroutineName("FireBaseRTDB"))
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("FireBaseRTDB", exception.message.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        firebaseAuth.addAuthStateListener {
            if (it.currentUser == null) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        val rootView = binding.root
        setContentView(rootView)

        //        Create a map of switches for convenience
        switchMap = mapOf(
            "switch1" to binding.switch1,
            "switch2" to binding.switch2,
            "switch3" to binding.switch3,
            "switch4" to binding.switch4,
            "switch5" to binding.switch5
        )

        //        Setup Animations
        inAnimation = AlphaAnimation(0f, 1f)
        inAnimation.duration = 200
        outAnimation = AlphaAnimation(1f, 0f)
        outAnimation.duration = 200


        //        Show the loader View
        binding.progressBarHolder.animation = inAnimation
        binding.progressBarHolder.visibility = View.VISIBLE

        showNetworkOverlay(!checkForInternet(this))

        observeNetworkStatus(this, this) { networkStatus ->
            runOnUiThread { showNetworkOverlay(networkStatus == NetworkStatus.DISCONNECTED) }
        }

//        ViewAnimator Setup
        fanAnimator.repeatCount = ValueAnimator.INFINITE
        fanAnimator.interpolator = LinearInterpolator()
        fanAnimator.addUpdateListener { animation ->
            binding.switch5.rotation = animation.animatedValue as Float
        }


//        Coroutine First Time Data Loading
        coroutineScope.launch(coroutineExceptionHandler) {
            dataLiveData.value = FirebaseCrud.getData()

            binding.fanSpeed.progress =
                dataLiveData.value!!.child("/fans/fanSpeed").value.toString().toInt()
            binding.switch1.isChecked =
                dataLiveData.value!!.child("/switches/switch1").value as Boolean
            binding.switch2.isChecked =
                dataLiveData.value!!.child("/switches/switch2").value as Boolean
            binding.switch3.isChecked =
                dataLiveData.value!!.child("/switches/switch3").value as Boolean
            binding.switch4.isChecked =
                dataLiveData.value!!.child("/switches/switch4").value as Boolean
            binding.switch5.isChecked =
                dataLiveData.value!!.child("switches/switch5").value as Boolean
            deviceDateTimeStr = dataLiveData.value!!.child("deviceStats/dateTime").value as String

            checkDeviceStatus()

            updateFanSpeed()
        }

        binding.menuBtn.setOnClickListener { view ->
            showPopup(view)
        }

        // Observe the dataLiveData object
        dataLiveData.observe(this) { snapshot ->
            // If the data retrieval is successful, hide the progress bar
            if (snapshot != null) {
                binding.progressBarHolder.animation = outAnimation
                binding.progressBarHolder.visibility = View.GONE
            }
        }

        FirebaseCrud.getDeviceStatus().observe(this) { snapshot ->
            if (snapshot.key == "dateTime") {
                deviceDateTimeStr = snapshot.value.toString()
                checkDeviceStatus()
            }
        }

//        Listen for changes to the fanData
        FirebaseCrud.getFansData().observe(this) { snapshot ->
            if (snapshot.key == "fanSpeed") {
                binding.fanSpeed.progress = snapshot.value.toString().toInt()
                updateFanSpeed()
            }
        }

//        Listen for changes to the switchData
        FirebaseCrud.getSwitchesData().observe(this) { snapshot ->
            switchMap[snapshot.key]?.isChecked = snapshot.value as Boolean
        }

//        Listen for changes to the Fan Speed Control SeekBar
        binding.fanSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (progress > 0) {
                        fanAnimator.start()
                        binding.switch5.isChecked = true
                    }
                    updateFanSpeed()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

//        Add a listener to the mainSwitch to toggle all the switches
        binding.mainSwitch.setOnClickListener {
            toggleAllSwitches()
        }

//        Add a listener to each switch
        switchMap.values.map { switch ->
            switch.setOnCheckedChangeListener { _, isChecked ->
                // If all switches are on, toggle mainSwitch on
                coroutineScope.launch(coroutineExceptionHandler) {
                    FirebaseCrud.setSwitchStatus(switch.text.toString(), isChecked)
                }
                if (isChecked && binding.switch1.isChecked && binding.switch2.isChecked && binding.switch3.isChecked && binding.switch4.isChecked && binding.switch5.isChecked)
                    binding.mainSwitch.isChecked = true

                if (!isChecked)
                    binding.mainSwitch.isChecked = false

                if (binding.switch5.isChecked && binding.fanSpeed.progress > 0)
                    fanAnimator.start()

                if (!binding.switch5.isChecked) {
                    fanAnimator.cancel()
                }
            }
        }

    }

    //    Check Network Status
    private fun observeNetworkStatus(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        onStatusChanged: (NetworkStatus) -> Unit
    ) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onStatusChanged(NetworkStatus.CONNECTED)
            }

            override fun onLost(network: Network) {
                onStatusChanged(NetworkStatus.DISCONNECTED)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    onStatusChanged(NetworkStatus.CONNECTED_TO_WIFI)
                } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    onStatusChanged(NetworkStatus.CONNECTED_TO_INTERNET)
                } else {
                    onStatusChanged(NetworkStatus.DISCONNECTED)
                }
            }
        }

        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            networkCallback
        )

        lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            }
        })
    }

    private fun checkDeviceStatus() {
        if (this::deviceDateTimeStr.isInitialized) {
            val deviceDT = LocalDateTime.parse(
                deviceDateTimeStr,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            )
            val elapsedDT = Duration.between(deviceDT, LocalDateTime.now())

            if (elapsedDT.seconds > 10) {
                binding.deviceStatus.text = getString(R.string.device_offline)
                binding.deviceStatus.setTextColor(Color.RED)
            } else {
                binding.deviceStatus.text = getString(R.string.device_online)
                binding.deviceStatus.setTextColor(Color.GREEN)
            }
        }
    }

    //    toggle all the switches on and off by main switch
    private fun toggleAllSwitches() {
        switchMap.values.map { switch ->
            switch.isChecked = binding.mainSwitch.isChecked
        }
    }

    //    Function to update the fan speed
    private fun updateFanSpeed() {
        when (binding.fanSpeed.progress) {
            0 -> {
                fanAnimator.duration = 0
                binding.switch5.isChecked = false
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
            when (item.itemId) {
                R.id.deviceConfigBtn -> {
//                    TODO Add ESPTouch SmartConfiguration

                    Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
//                    val intent = Intent(this, DeviceConfigActivity::class.java)
//                    startActivity(intent)
//                    finish()
                }

                R.id.logoutBtn -> {
                    firebaseAuth.signOut()  // sign out
                }

            }
            true
        }
        popup.show()
    }

    //    SHow/Hide network overlay
    private fun showNetworkOverlay(show: Boolean) {
        if (show) {
            binding.progressBarHolder.animation = outAnimation
            binding.progressBarHolder.visibility = View.GONE
            binding.noInternetView.animation = inAnimation
            binding.noInternetView.visibility = View.VISIBLE
        } else {
            binding.noInternetView.animation = outAnimation
            binding.noInternetView.visibility = View.GONE
        }
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
    }
}
