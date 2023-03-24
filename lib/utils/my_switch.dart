import 'package:get/get.dart';
import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

import 'constants.dart';

Widget mySwitch(RxBool mySwitch, bool isDark, void Function()? onPressed) {
  const animDuration = Duration(milliseconds: 300);
  return Obx(() => AnimatedContainer(
        duration: animDuration,
        curve: Curves.easeIn,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(100),
          color: mySwitch.value
              ? myPrimaryColor
              : isDark
                  ? Colors.white24
                  : Colors.black26,
        ),
        child: IconButton(
          padding: const EdgeInsets.all(25),
          icon: const FaIcon(
            FontAwesomeIcons.solidLightbulb,
          ),
          iconSize: 80,
          color: isDark ? colorWhite : colorBlack,
          splashColor: mySwitch.value
              ? myPrimaryColor
              : isDark
                  ? Colors.white24
                  : Colors.black26,
          highlightColor: mySwitch.value ? myPrimaryLightColor : colorGrey,
          onPressed: onPressed,
        ),
      ));
}
