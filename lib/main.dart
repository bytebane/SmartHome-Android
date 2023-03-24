import 'package:get/get.dart';
import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';

import 'pages/home.dart';
import 'utils/themes.dart';
import 'utils/constants.dart';
import 'helpers/theme_controller.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return GetMaterialApp(
      title: appName,
      // debugShowCheckedModeBanner: false,
      theme: lightTheme,
      darkTheme: darkTheme,
      initialBinding: ThemeBinding(),
      themeMode: ThemeMode.system,
      home: const HomePage(),
    );
  }
}
