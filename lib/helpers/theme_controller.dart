import 'package:get/get.dart';
import 'package:flutter/material.dart';
import 'package:is_first_run/is_first_run.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ThemeController extends GetxController {
  RxBool isDarkTheme = true.obs;

  final Future<SharedPreferences> _prefs = SharedPreferences.getInstance();

  firstLoadCheckTheme() async {
    bool isFirstRun = await IsFirstRun.isFirstRun();

    if (!isFirstRun) {
      getThemeStatus();
    } else {
      if (Get.isDarkMode) {
        isDarkTheme.value = true;
      } else {
        isDarkTheme.value = false;
      }
      saveThemeStatus();
    }
  }

  saveThemeStatus() async {
    SharedPreferences pref = await _prefs;
    pref.setBool('theme', isDarkTheme.value);
  }

  getThemeStatus() async {
    var isDark = _prefs.then((SharedPreferences prefs) {
      return prefs.getBool('theme') ?? false;
    }).obs;
    isDarkTheme.value = await isDark.value;
    Get.changeThemeMode(isDarkTheme.value ? ThemeMode.dark : ThemeMode.light);
  }
}

class ThemeBinding extends Bindings {
  @override
  void dependencies() {
    Get.lazyPut(() => ThemeController());
  }
}
