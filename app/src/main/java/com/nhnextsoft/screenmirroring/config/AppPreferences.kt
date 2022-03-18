package com.nhnextsoft.screenmirroring.config

class AppPreferences : Preferences(name = "app_preferences") {
    var completedTheFirstTutorial by booleanPref(defaultValue = false)
}