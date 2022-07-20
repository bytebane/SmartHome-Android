import 'dart:async';
import 'package:get/get.dart';
import 'package:flutter/material.dart';
import 'package:toggle_switch/toggle_switch.dart';
import 'package:esptouch_flutter/esptouch_flutter.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

import 'connection_page.dart';
import '../utils/private.dart';
import '../utils/constants.dart';
import 'package:smarthome/utils/my_switch.dart';
import 'package:smarthome/helpers/theme_controller.dart';
import 'package:smarthome/helpers/switches_db_help.dart';

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final _mySwitches = SwitchesDBHelp();
  final _tc = Get.find<ThemeController>();
  final RxBool _isDeviceOnline = false.obs;

  final List<bool> _isSelected = [true, false];
  final List<bool> _isSelectedTheme = [true, false];
  final _selectedIndex = 0.obs, _selectedIndexTheme = 1.obs;
  bool _initLoad = false;

  late StreamSubscription _sub;
  final Stream _myStream = Stream.periodic(const Duration(seconds: 5), (_) {
    return FirebaseDatabase.instance
        .ref('myHome/deviceStats')
        .child('dateTime')
        .once();
  });

  @override
  void initState() {
    _tc.firstLoadCheckTheme();
    initialSwitchCheck();
    checkDeviceStats();

    super.initState();
  }

  @override
  void dispose() {
    _sub.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return RefreshIndicator(
      strokeWidth: 3,
      displacement: 250,
      color: _tc.isDarkTheme.value ? colorBlack : colorWhite,
      backgroundColor: _tc.isDarkTheme.value ? Colors.white38 : Colors.black38,
      triggerMode: RefreshIndicatorTriggerMode.onEdge,
      onRefresh: () async {
        // await Future.delayed(Duration(milliseconds: 1000));
        setState(() {});
      },
      child: Scaffold(
        // appBar: AppBar(
        //   centerTitle: true,
        //   backgroundColor: colorWhite,
        //   elevation: 0,
        //   title: const Text(
        //     appName,
        //     style: TextStyle(color: myPrimaryColor, fontSize: 42),
        //   ),
        // ),
        body: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          child: Container(
            height: MediaQuery.of(context).size.height,
            padding: const EdgeInsets.only(top: 25),
            child: FutureBuilder(
                future: FirebaseDatabase.instance.ref('myHome/switches').get(),
                builder: (context, snapshot) {
                  if (snapshot.connectionState == ConnectionState.done) {
                    return Obx(
                      () => Stack(
                        alignment: AlignmentDirectional.center,
                        children: [
                          //* Device Status
                          FutureBuilder(
                              future: FirebaseDatabase.instance
                                  .ref('myHome/deviceStats')
                                  .child('dateTime')
                                  .get(),
                              builder: (context, snapshot) {
                                if (_initLoad == true &&
                                    snapshot.hasData &&
                                    !snapshot.hasError) {
                                  return Positioned(
                                    top: 65,
                                    child: Row(
                                      children: [
                                        CircleAvatar(
                                          backgroundColor:
                                              _isDeviceOnline.value == true
                                                  ? colorGreen
                                                  : colorRed,
                                        ),
                                        const SizedBox(width: 10),
                                        Text(
                                          _isDeviceOnline.value == true
                                              ? 'Device Online'
                                              : 'Device Offline',
                                          style: TextStyle(
                                              fontSize: 28,
                                              fontWeight: FontWeight.w600,
                                              color:
                                                  _isDeviceOnline.value == true
                                                      ? colorGreen
                                                      : colorRed),
                                        )
                                      ],
                                    ),
                                  );
                                } else if (_initLoad == false ||
                                    snapshot.connectionState ==
                                        ConnectionState.waiting) {
                                  return Positioned(
                                    top: 65,
                                    child: Row(
                                      children: const [
                                        CircleAvatar(
                                          backgroundColor: colorGrey,
                                        ),
                                        SizedBox(width: 10),
                                        Text(
                                          'Checking Device Status...',
                                          style: TextStyle(
                                              fontSize: 24,
                                              fontWeight: FontWeight.w500,
                                              color: colorGrey),
                                        )
                                      ],
                                    ),
                                  );
                                } else {
                                  return const SizedBox();
                                }
                              }),
                          Positioned(
                            top: 18,
                            right: 15,
                            child: ToggleSwitch(
                              minWidth: 55.0,
                              minHeight: 35.0,
                              cornerRadius: 25.0,
                              activeFgColor: colorWhite,
                              inactiveBgColor: _tc.isDarkTheme.value
                                  ? Colors.white24
                                  : Colors.black38,
                              inactiveFgColor: Colors.white38,
                              initialLabelIndex: _tc.isDarkTheme.value ? 1 : 0,
                              totalSwitches: 2,
                              icons: const [
                                FontAwesomeIcons.sun,
                                FontAwesomeIcons.moon,
                              ],
                              activeBgColors: const [
                                [Colors.black45, Colors.black26],
                                [colorBlack, myPrimaryColor]
                              ],
                              animate: true,
                              curve: Curves.bounceInOut,
                              onToggle: (index) {
                                if (index == 0) {
                                  _tc.isDarkTheme.value = false;
                                  Get.changeThemeMode(_tc.isDarkTheme.value
                                      ? ThemeMode.dark
                                      : ThemeMode.light);
                                  _tc.saveThemeStatus();
                                } else if (index == 1) {
                                  _tc.isDarkTheme.value = true;
                                  Get.changeThemeMode(
                                    _tc.isDarkTheme.value
                                        ? ThemeMode.dark
                                        : ThemeMode.light,
                                  );
                                  _tc.saveThemeStatus();
                                }

                                for (int i = 0;
                                    i < _isSelectedTheme.length;
                                    i++) {
                                  _selectedIndexTheme.value = index!;
                                }
                              },
                            ),
                          ),

                          Container(
                            padding: const EdgeInsets.only(top: 45),
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                              children: [
                                Row(
                                  mainAxisAlignment:
                                      MainAxisAlignment.spaceEvenly,
                                  children: [
                                    //* Switch 2
                                    mySwitch(_mySwitches.switch2,
                                        _tc.isDarkTheme.value, () async {
                                      _mySwitches
                                          .setswitch2(
                                              !_mySwitches.switch2.value)
                                          .then((_) => {areSwitchesON()});
                                    }),

                                    //* Switch 1
                                    mySwitch(_mySwitches.switch1,
                                        _tc.isDarkTheme.value, () async {
                                      _mySwitches
                                          .setswitch1(
                                              !_mySwitches.switch1.value)
                                          .then((_) => {areSwitchesON()});
                                    }),
                                  ],
                                ),
                                Row(
                                  mainAxisAlignment:
                                      MainAxisAlignment.spaceEvenly,
                                  children: [
                                    //* Switch 4
                                    mySwitch(_mySwitches.switch4,
                                        _tc.isDarkTheme.value, () async {
                                      _mySwitches
                                          .setswitch4(
                                              !_mySwitches.switch4.value)
                                          .then((_) => {areSwitchesON()});
                                    }),

                                    //* Switch 3
                                    mySwitch(_mySwitches.switch3,
                                        _tc.isDarkTheme.value, () async {
                                      _mySwitches
                                          .setswitch3(
                                              !_mySwitches.switch3.value)
                                          .then((_) => {areSwitchesON()});
                                    }),
                                  ],
                                ),
                                //* Switch Mains
                                ToggleSwitch(
                                  minHeight: 65,
                                  minWidth: 170.0,
                                  cornerRadius: 50.0,
                                  totalSwitches: 2,
                                  fontSize: 18,
                                  iconSize: 32,
                                  activeFgColor: _tc.isDarkTheme.value
                                      ? colorWhite
                                      : colorBlack,
                                  animate: true,
                                  animationDuration: 300,
                                  radiusStyle: true,
                                  inactiveBgColor: _tc.isDarkTheme.value
                                      ? Colors.white24
                                      : Colors.black26,
                                  inactiveFgColor: _tc.isDarkTheme.value
                                      ? colorWhite
                                      : colorBlack,
                                  initialLabelIndex: _selectedIndex.value,
                                  labels: const ['Lights ON', 'Lights OFF'],
                                  icons: const [
                                    FontAwesomeIcons.solidLightbulb,
                                    FontAwesomeIcons.solidLightbulb
                                  ],
                                  activeBgColors: const [
                                    [myPrimaryColor],
                                    [Colors.black26]
                                  ],
                                  onToggle: (index) {
                                    if (index == 0) {
                                      _mySwitches
                                          .setAllSwitches(true)
                                          .then((_) => {areSwitchesON()});
                                    } else if (index == 1) {
                                      _mySwitches
                                          .setAllSwitches(false)
                                          .then((_) => {areSwitchesON()});
                                    }

                                    for (int i = 0;
                                        i < _isSelected.length;
                                        i++) {
                                      _selectedIndex.value = index!;
                                    }
                                  },
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    );
                  }
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        const SizedBox(width: double.maxFinite),
                        CircularProgressIndicator(
                            color:
                                _tc.isDarkTheme.value ? colorWhite : colorGrey),
                        const SizedBox(height: 15),
                        const Text('Retrieving Data...',
                            style: TextStyle(fontSize: 16)),
                        const SizedBox(height: 5),
                        const Text('Please Wait!!!',
                            style: TextStyle(fontSize: 14))
                      ],
                    );
                  }
                  if (snapshot.hasError) {
                    return Center(
                      child: Text('Something Went Wrong! \n${snapshot.error}',
                          style: const TextStyle(fontSize: 16)),
                    );
                  } else {
                    return const Center(
                      child: Text(
                          'Something Went Wrong! \nCheck your Network connection & Try Reloading...',
                          style: TextStyle(fontSize: 16)),
                    );
                  }
                }),
          ),
        ),
        //* Connct to Wifi Button
        floatingActionButtonLocation: FloatingActionButtonLocation.centerFloat,
        floatingActionButton: Obx(() => FloatingActionButton(
              onPressed: connectWifi,
              tooltip: 'SmartConnect Wifi',
              backgroundColor: _selectedIndex.value == 0
                  ? myPrimaryColor
                  : _tc.isDarkTheme.value
                      ? Colors.white24
                      : Colors.black26,
              splashColor: myPrimaryLightColor,
              child: FaIcon(
                FontAwesomeIcons.plus,
                color: _tc.isDarkTheme.value ? colorWhite : colorBlack,
              ),
            )),
      ),
    );
  }

  //* Check Device Status
  checkDeviceStats() async {
    _sub = _myStream.listen((event) async {
      DatabaseEvent mydbEvent = await event;
      Duration elapsedTime;
      elapsedTime = DateTime.now()
          .difference(DateTime.parse(mydbEvent.snapshot.value.toString()));
      if (elapsedTime.inSeconds > 10) {
        _isDeviceOnline.value = false;
      } else {
        _isDeviceOnline.value = true;
      }
    });
    await Future.delayed(const Duration(seconds: 5));
    setState(() {
      _initLoad = true;
    });
  }

  //* Check Switch stats First Time
  initialSwitchCheck() async {
    await _mySwitches.getSwitchesStats();
    areSwitchesON();
  }

  //* Check All Switch Stats for Toggle Button
  areSwitchesON() async {
    if (_mySwitches.switch1.value == true &&
        _mySwitches.switch2.value == true &&
        _mySwitches.switch3.value == true &&
        _mySwitches.switch4.value == true) {
      _selectedIndex.value = 0;
    } else {
      _selectedIndex.value = 1;
    }
  }

  //* Connct to Wifi Func
  connectWifi() {
    final GlobalKey<FormState> formKey = GlobalKey<FormState>();

    final TextEditingController ssid = TextEditingController(text: mySSID),
        bssid = TextEditingController(text: myBSSID),
        password = TextEditingController(text: myPASS);

    showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
            title: const Center(
                child: Text(
              'Enter Wifi Credentials',
              style: TextStyle(fontSize: 24, color: myPrimaryColor),
            )),
            content: Form(
              key: formKey,
              child: Wrap(
                // shrinkWrap: true,
                // padding: const EdgeInsets.symmetric(horizontal: 16),
                children: [
                  TextFormField(
                    controller: ssid,
                    decoration: const InputDecoration(
                      labelText: 'SSID',
                      hintText: 'Android_AP',
                    ),
                    validator: (value) {
                      if (value != null && value.isEmpty) {
                        return 'Cannot be Empty';
                      }
                      return null;
                    },
                  ),
                  TextFormField(
                    controller: bssid,
                    decoration: const InputDecoration(
                      labelText: 'BSSID',
                      hintText: '00:a0:c9:14:c8:29',
                    ),
                    validator: (value) {
                      if (value != null && value.isEmpty) {
                        return 'Cannot be Empty';
                      }
                      return null;
                    },
                  ),
                  TextFormField(
                    controller: password,
                    obscureText: true,
                    decoration: const InputDecoration(
                      labelText: 'Password',
                      hintText: r'V3Ry.S4F3-P@$$w0rD',
                    ),
                    validator: (value) {
                      if (value != null && value.isEmpty) {
                        return 'Cannot be Empty';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: 50),
                ],
              ),
            ),
            actions: [
              ElevatedButton(
                onPressed: () async {
                  if (formKey.currentState!.validate()) {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => ConnectionPage(
                            task: ESPTouchTask(
                                ssid: ssid.text,
                                bssid: bssid.text,
                                password: password.text,
                                packet: ESPTouchPacket.broadcast,
                                taskParameter: const ESPTouchTaskParameter())),
                      ),
                    );
                  }
                },
                child: const Text('  BroadCast  '),
              ),
            ],
          );
        });
  }
}
