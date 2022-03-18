package com.nhnextsoft.screenmirroring.config

class AppPreferences : Preferences(name = "app_preferences") {
    var completedTheFirstTutorial by booleanPref(defaultValue = false)
    var numberOfDialogRemoveAdsImpressionsPerDay by intPref(defaultValue = 1)
    var numberOfTimesDialogRemoveAdsDisplayed by intPref(defaultValue = 0)
}