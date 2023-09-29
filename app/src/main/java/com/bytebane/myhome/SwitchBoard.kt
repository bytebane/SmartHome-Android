package com.bytebane.myhome

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Implementation of App Widget functionality.
 */
private val coroutineScope =
    CoroutineScope(Job() + Dispatchers.Main + CoroutineName("FireBaseRTDB"))
private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
    Log.e(
        "FireBaseRTDB",
        exception.message.toString()
    )
}

private val switches = Array(5) { i -> "switch${i + 1}" }
private var switch1_isOn: Boolean = false
private var switch2_isOn: Boolean = false
private var switch3_isOn: Boolean = false
private var switch4_isOn: Boolean = false
private var switch5_isOn: Boolean = false
private var switches_areOn: Boolean = false

private var textSwitch1: String = ""
private var textSwitch2: String = ""
private var textSwitch3: String = ""
private var textSwitch4: String = ""
private var textSwitch5: String = ""
private var textMainSwitch: String = ""

private const val ACTION_SWITCH1 = "Light1"
private const val ACTION_SWITCH2 = "Light2"
private const val ACTION_SWITCH3 = "Light3"
private const val ACTION_SWITCH4 = "Light4"
private const val ACTION_SWITCH5 = "Fan"
private const val ACTION_MAIN_SWITCH = "MainSwitch"


class SwitchBoard : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
//        Stream data from database & update widgets
        coroutineScope.launch(exceptionHandler) {
            FirebaseCrud.getSwitchesData().observeForever { snapshot ->
                when (snapshot.key) {
                    "switch1" -> {
                        switch1_isOn = snapshot.value as Boolean
                    }

                    "switch2" -> {
                        switch2_isOn = snapshot.value as Boolean
                    }

                    "switch3" -> {
                        switch3_isOn = snapshot.value as Boolean
                    }

                    "switch4" -> {
                        switch4_isOn = snapshot.value as Boolean
                    }

                    "switch5" -> {
                        switch5_isOn = snapshot.value as Boolean
                    }
                }
                updateWidgets(context)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

//        This is where we will receive the intent broadcast
        when (intent.action) {
            ACTION_SWITCH1 -> {
                switch1_isOn = !switch1_isOn
                setSwitchData("switch1", switch1_isOn)
            }

            ACTION_SWITCH2 -> {
                switch2_isOn = !switch2_isOn
                setSwitchData("switch2", switch2_isOn)
            }

            ACTION_SWITCH3 -> {
                switch3_isOn = !switch3_isOn
                setSwitchData("switch3", switch3_isOn)
            }

            ACTION_SWITCH4 -> {
                switch4_isOn = !switch4_isOn
                setSwitchData("switch4", switch4_isOn)
            }

            ACTION_SWITCH5 -> {
                switch5_isOn = !switch5_isOn
                setSwitchData("switch5", switch5_isOn)
            }

            ACTION_MAIN_SWITCH -> {
                switches_areOn = !switches_areOn
                setSwitchData("mainSwitch", switches_areOn)
                switch1_isOn = switches_areOn
                switch2_isOn = switches_areOn
                switch3_isOn = switches_areOn
                switch4_isOn = switches_areOn
                switch5_isOn = switches_areOn
            }
        }
        updateWidgets(context)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews & set onClick listener
    val views = RemoteViews(context.packageName, R.layout.switch_board)
    views.setOnClickPendingIntent(R.id.widgetSwitch1, pendingIntents(context, ACTION_SWITCH1))
    views.setOnClickPendingIntent(R.id.widgetSwitch2, pendingIntents(context, ACTION_SWITCH2))
    views.setOnClickPendingIntent(R.id.widgetSwitch3, pendingIntents(context, ACTION_SWITCH3))
    views.setOnClickPendingIntent(R.id.widgetSwitch4, pendingIntents(context, ACTION_SWITCH4))
    views.setOnClickPendingIntent(R.id.widgetSwitch5, pendingIntents(context, ACTION_SWITCH5))
    views.setOnClickPendingIntent(
        R.id.widgetMainSwitch,
        pendingIntents(context, ACTION_MAIN_SWITCH)
    )
//    On Click on empty area in widget open application
    views.setOnClickPendingIntent(
        R.id.widgetRoot, PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )
    )

//    First read from database and update widgets
    coroutineScope.launch(exceptionHandler) {
        val homeData = FirebaseCrud.getData()
        switch1_isOn = homeData.child("/switches/switch1").value as Boolean
        switch2_isOn = homeData.child("/switches/switch2").value as Boolean
        switch3_isOn = homeData.child("/switches/switch3").value as Boolean
        switch4_isOn = homeData.child("/switches/switch4").value as Boolean
        switch5_isOn = homeData.child("switches/switch5").value as Boolean

    }
    setTexts(views)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

private fun updateWidgets(context: Context) {
    val manager = AppWidgetManager.getInstance(context)
    val ids = manager.getAppWidgetIds(ComponentName(context, SwitchBoard::class.java))
    ids.forEach { id ->
        updateAppWidget(context, manager, id)
    }
}

// Create the intents for the widget
private fun pendingIntents(context: Context, action: String): PendingIntent {
    val intent = Intent(context, SwitchBoard::class.java)
    intent.action = action
    return PendingIntent.getBroadcast(
        context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )
}

// Toggle MainSwitch
private fun toggleMainSwitch() {
    switches_areOn = switch1_isOn && switch2_isOn && switch3_isOn && switch4_isOn && switch5_isOn
}

// Set Text in Views
private fun setTexts(views: RemoteViews) {
    textsLoad()
    views.setTextViewText(R.id.widgetSwitch1, textSwitch1)
    views.setTextViewText(R.id.widgetSwitch2, textSwitch2)
    views.setTextViewText(R.id.widgetSwitch3, textSwitch3)
    views.setTextViewText(R.id.widgetSwitch4, textSwitch4)
    views.setTextViewText(R.id.widgetSwitch5, textSwitch5)
    views.setTextViewText(R.id.widgetMainSwitch, textMainSwitch)
}

// Boolean to text ON/OFF
private fun getOnOffText(isOn: Boolean): String {
    return if (isOn) "ON"
    else "OFF"
}

// Load Texts from getOnOffText
private fun textsLoad() {
    toggleMainSwitch()
    textSwitch1 = getOnOffText(switch1_isOn)
    textSwitch2 = getOnOffText(switch2_isOn)
    textSwitch3 = getOnOffText(switch3_isOn)
    textSwitch4 = getOnOffText(switch4_isOn)
    textSwitch5 = getOnOffText(switch5_isOn)
    textMainSwitch = getOnOffText(switches_areOn)
}

// Post switch state to database
private fun setSwitchData(switchName: String, isOn: Boolean) {
    if (switchName == "mainSwitch")
        switches.forEach { switch ->
            coroutineScope.launch(exceptionHandler) {
                FirebaseCrud.setSwitchStatus(switch, isOn)
            }
        }
    else {
        coroutineScope.launch(exceptionHandler) {
            FirebaseCrud.setSwitchStatus(switchName, isOn)
        }
    }
}
