import 'package:flutter/material.dart';
import 'package:smarthome/utils/constants.dart';

ThemeData darkTheme = ThemeData(
  brightness: Brightness.dark,
  primaryColor: myPrimaryColor,
  primaryColorLight: myPrimaryLightColor,
  primaryColorDark: myPrimaryDarkColor,
  errorColor: colorRed,
  scaffoldBackgroundColor: colorBlack,
  indicatorColor: colorBlack,
  buttonTheme: const ButtonThemeData(
    buttonColor: myPrimaryColor,
    disabledColor: colorGrey,
  ),
);

ThemeData lightTheme = ThemeData(
  brightness: Brightness.light,
  primaryColor: myPrimaryColor,
  primaryColorLight: myPrimaryLightColor,
  primaryColorDark: myPrimaryDarkColor,
  scaffoldBackgroundColor: colorWhite,
  indicatorColor: colorWhite,
  buttonTheme: const ButtonThemeData(
    buttonColor: myPrimaryColor,
    disabledColor: colorGrey,
  ),
);
