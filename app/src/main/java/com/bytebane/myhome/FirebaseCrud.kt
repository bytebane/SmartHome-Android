package com.bytebane.myhome

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await


object FirebaseCrud {
    private val database = FirebaseDatabase.getInstance()
    private val dbHomeRef = database.getReference("myHome")
    private val dbDeviceStatusRef = database.getReference("myHome/deviceStats")
    private val dbFanRef = database.getReference("myHome/fans")
    private val dbSwitchRef = database.getReference("myHome/switches")

    private val liveDataFans = MutableLiveData<DataSnapshot>()
    private val liveDataSwitches = MutableLiveData<DataSnapshot>()
    private val liveDeviceStatus = MutableLiveData<DataSnapshot>()

    init {
        dbFanRef.addChildEventListener(object : ChildEventListener {
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle the child change event
                liveDataFans.value = snapshot
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseChildListener - ${error.code}", error.message)
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        })
        dbSwitchRef.addChildEventListener(object : ChildEventListener {
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle the child change event
                liveDataSwitches.value = snapshot
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseChildListener - ${error.code}", error.message)
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        })
        dbDeviceStatusRef.addChildEventListener(object : ChildEventListener {
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle the child change event
                liveDeviceStatus.value = snapshot
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseChildListener - ${error.code}", error.message)
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        })
    }


    fun getFansData(): LiveData<DataSnapshot> {
        return liveDataFans
    }

    fun getSwitchesData(): LiveData<DataSnapshot> {
        return liveDataSwitches
    }

    //    Function To Get All Available Data from RTDB/myHome
    suspend fun getData(): DataSnapshot {
        return dbHomeRef.get().await()
    }

    //    Get Device Status
    fun getDeviceStatus(): LiveData<DataSnapshot> {
//        return dbDeviceStatusRef.get().await()
        return liveDeviceStatus
    }

    //    Function to update the fan speed --> takes the speed as an argument
    suspend fun setFanSpeed(speed: Int) {
        dbFanRef.child("fanSpeed").setValue(speed).await()
    }

    //    Function to update the switch --> takes the switchName and status as an argument
    suspend fun setSwitchStatus(switch: String, status: Boolean) {
        dbSwitchRef.child(switch).setValue(status).await()
    }

}
