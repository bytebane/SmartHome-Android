package com.r3.smarthome.smarthome


import io.flutter.embedding.android.FlutterActivity

import android.util.Log
import android.content.Intent
import android.content.Context
import android.app.PendingIntent
import android.widget.RemoteViews
import android.content.ComponentName
import com.google.firebase.database.*
import android.content.ContentValues.TAG
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import com.r3.smarthome.smarthome.R.layout.home_widget

class MainActivity: FlutterActivity() {
}

val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("myHome")

var light1_isOn: Boolean = false
var light2_isOn: Boolean = false
var light3_isOn: Boolean = false
var light4_isOn: Boolean = false
var lights_areOn: Boolean = false

var textLight1: String = ""
var textLight2: String = ""
var textLight3: String = ""
var textLight4: String = ""
var textLightsAll: String = ""


/**
 * Implementation of App Widget functionality.
 */
class HomeWidget : AppWidgetProvider() {

    private val actionLight1 = "ACTION_WIDGET1"
    private val actionLight2 = "ACTION_WIDGET2"
    private val actionLight3 = "ACTION_WIDGET3"
    private val actionLight4 = "ACTION_WIDGET4"
    private val actionLightsAll = "ACTION_WIDGET5"


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        // Construct the RemoteViews object
        val views = RemoteViews(context!!.packageName, home_widget)
        val appWidget = ComponentName(context, HomeWidget::class.java)
        val appWidgetManager = AppWidgetManager.getInstance(context)
//--1
        if (actionLight1 == intent!!.action) {
            light1_isOn = !light1_isOn
            dbRef.child("light1").setValue(light1_isOn).addOnFailureListener { err ->
                Log.e(TAG, "Error ${err.message}")
            }
            setTexts(views)
            appWidgetManager.updateAppWidget(appWidget, views)
        }
//--2
        if (actionLight2 == intent!!.action) {
            light2_isOn = !light2_isOn
            dbRef.child("light2").setValue(light2_isOn).addOnFailureListener { err ->
                Log.e(TAG, "Error ${err.message}")
            }
            setTexts(views)
            appWidgetManager.updateAppWidget(appWidget, views)
        }
//--3
        if (actionLight3 == intent!!.action) {
            light3_isOn = !light3_isOn
            dbRef.child("light3").setValue(light3_isOn).addOnFailureListener { err ->
                Log.e(TAG, "Error ${err.message}")
            }
            setTexts(views)
            appWidgetManager.updateAppWidget(appWidget, views)
        }
//--4
        if (actionLight4 == intent!!.action) {
            light4_isOn = !light4_isOn
            dbRef.child("light4").setValue(light4_isOn).addOnFailureListener { err ->
                Log.e(TAG, "Error ${err.message}")
            }
            setTexts(views)
            appWidgetManager.updateAppWidget(appWidget, views)
        }
//--5
        if (actionLightsAll == intent!!.action) {
            lights_areOn = !lights_areOn
            light1_isOn = lights_areOn
            light2_isOn = lights_areOn
            light3_isOn = lights_areOn
            light4_isOn = lights_areOn
            textLightsAll = getOnOffText(lights_areOn)
            val pushVal: MutableMap<String, Boolean> = hashMapOf(
                "light1" to light1_isOn,
                "light2" to light2_isOn,
                "light3" to light3_isOn,
                "light4" to light4_isOn
            )

            dbRef.setValue(pushVal).addOnFailureListener { err ->
                Log.e(TAG, "Error ${err.message}")
            }
            setTexts(views)
            appWidgetManager.updateAppWidget(appWidget, views)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int

    ) {
        val views = RemoteViews(context.packageName, home_widget)

        val intent1 = Intent(context, HomeWidget::class.java)
        val intent2 = Intent(context, HomeWidget::class.java)
        val intent3 = Intent(context, HomeWidget::class.java)
        val intent4 = Intent(context, HomeWidget::class.java)
        val intents = Intent(context, HomeWidget::class.java)

        intent1.action = actionLight1
        intent2.action = actionLight2
        intent3.action = actionLight3
        intent4.action = actionLight4
        intents.action = actionLightsAll

        val pendingIntent1 = PendingIntent.getBroadcast(
            context, 0, intent1,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pendingIntent2 = PendingIntent.getBroadcast(
            context, 0, intent2,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pendingIntent3 = PendingIntent.getBroadcast(
            context, 0, intent3,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pendingIntent4 = PendingIntent.getBroadcast(
            context, 0, intent4,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pendingIntents = PendingIntent.getBroadcast(
            context, 0, intents,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val dbListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get DB object and use the values
                val post = dataSnapshot.value as Map<String, Boolean>
                light1_isOn = post["light1"]!!
                light2_isOn = post["light2"]!!
                light3_isOn = post["light3"]!!
                light4_isOn = post["light4"]!!

                lights_areOn = light1_isOn && light2_isOn && light3_isOn && light4_isOn
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "DB:onCancelled", databaseError.toException())
            }
        }
        dbRef.addValueEventListener(dbListener)

        setTexts(views)

        views.setOnClickPendingIntent(R.id.Button_light1, pendingIntent1)
        views.setOnClickPendingIntent(R.id.Button_light2, pendingIntent2)
        views.setOnClickPendingIntent(R.id.Button_light3, pendingIntent3)
        views.setOnClickPendingIntent(R.id.Button_light4, pendingIntent4)
        views.setOnClickPendingIntent(R.id.Button_lights, pendingIntents)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

private fun textsLoad() {
    lights_areOn = light1_isOn && light2_isOn && light3_isOn && light4_isOn
    textLight1 = getOnOffText(light1_isOn)
    textLight2 = getOnOffText(light2_isOn)
    textLight3 = getOnOffText(light3_isOn)
    textLight4 = getOnOffText(light4_isOn)
    textLightsAll = getOnOffText(lights_areOn)
}

private fun getOnOffText(isOn: Boolean): String {
    return if (isOn) {
        "ON"
    } else {
        "OFF"
    }
}

private fun setTexts(views: RemoteViews) {
    textsLoad()
    views.setTextViewText(R.id.Button_light1, textLight1)
    views.setTextViewText(R.id.Button_light2, textLight2)
    views.setTextViewText(R.id.Button_light3, textLight3)
    views.setTextViewText(R.id.Button_light4, textLight4)
    views.setTextViewText(R.id.Button_lights, textLightsAll)
}