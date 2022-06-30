import 'package:get/get.dart';
import 'package:flutter/material.dart';
import 'package:toggle_switch/toggle_switch.dart';
import 'package:esptouch_flutter/esptouch_flutter.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

import 'connection_page.dart';
import '../utils/private.dart';
import '../utils/constants.dart';
import '../helpers/lights_db_help.dart';

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final _lights = LightsDBHelp();

  final _selectedIndex = 1.obs;

  final List<bool> _isSelected = [true, false];

  final _myAnimationDuration = const Duration(milliseconds: 300);

  checkLights() async {
    await _lights.getLights();
    if (_lights.light1.value == true &&
        _lights.light2.value == true &&
        _lights.light3.value == true &&
        _lights.light4.value == true) {
      _selectedIndex.value = 0;
    } else {
      _selectedIndex.value = 1;
    }
  }

  isLightsON() async {
    if (_lights.light1.value == true &&
        _lights.light2.value == true &&
        _lights.light3.value == true &&
        _lights.light4.value == true) {
      _selectedIndex.value = 0;
    } else {
      _selectedIndex.value = 1;
    }
  }

  @override
  void initState() {
    checkLights();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    isLightsON();
    return Scaffold(
      backgroundColor: colorWhite,
      // appBar: AppBar(
      //   centerTitle: true,
      //   backgroundColor: colorWhite,
      //   elevation: 0,
      //   title: const Text(
      //     'My HOME',
      //     style: TextStyle(color: myPrimaryColor, fontSize: 42),
      //   ),
      // ),
      body: FutureBuilder(
          future: FirebaseDatabase.instance.ref('myHome').get(),
          builder: (context, snapshot) {
            if (snapshot.connectionState == ConnectionState.done) {
              return Obx(
                () => Column(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        //* Switch 2
                        AnimatedContainer(
                          duration: _myAnimationDuration,
                          curve: Curves.easeIn,
                          decoration: BoxDecoration(
                            borderRadius: BorderRadius.circular(100),
                            color: _lights.light2.value
                                ? myPrimaryColor
                                : Colors.black45,
                          ),
                          child: IconButton(
                            padding: const EdgeInsets.all(25),
                            icon: const FaIcon(
                              FontAwesomeIcons.solidLightbulb,
                            ),
                            iconSize: 80,
                            color: colorWhite,
                            splashColor: _lights.light2.value
                                ? myPrimaryColor
                                : Colors.black26,
                            highlightColor: _lights.light2.value
                                ? myPrimaryLightColor
                                : colorGrey,
                            onPressed: () async {
                              _lights
                                  .setLight2(!_lights.light2.value)
                                  .then((_) => {isLightsON()});
                            },
                          ),
                        ),
                        //* Switch 1
                        AnimatedContainer(
                          duration: _myAnimationDuration,
                          curve: Curves.easeIn,
                          decoration: BoxDecoration(
                            borderRadius: BorderRadius.circular(100),
                            color: _lights.light1.value
                                ? myPrimaryColor
                                : Colors.black45,
                          ),
                          child: IconButton(
                            padding: const EdgeInsets.all(25),
                            icon: const FaIcon(
                              FontAwesomeIcons.solidLightbulb,
                            ),
                            iconSize: 80,
                            color: colorWhite,
                            splashColor: _lights.light1.value
                                ? myPrimaryColor
                                : Colors.black26,
                            highlightColor: _lights.light1.value
                                ? myPrimaryLightColor
                                : colorGrey,
                            onPressed: () {
                              _lights
                                  .setLight1(!_lights.light1.value)
                                  .then((_) => {isLightsON()});
                            },
                          ),
                        ),
                      ],
                    ),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        //* Switch 4
                        AnimatedContainer(
                          duration: _myAnimationDuration,
                          curve: Curves.easeIn,
                          decoration: BoxDecoration(
                            borderRadius: BorderRadius.circular(100),
                            color: _lights.light4.value
                                ? myPrimaryColor
                                : Colors.black45,
                          ),
                          child: IconButton(
                            padding: const EdgeInsets.all(25),
                            icon: const FaIcon(
                              FontAwesomeIcons.solidLightbulb,
                            ),
                            iconSize: 80,
                            color: colorWhite,
                            splashColor: _lights.light4.value
                                ? myPrimaryColor
                                : Colors.black26,
                            highlightColor: _lights.light4.value
                                ? myPrimaryLightColor
                                : colorGrey,
                            onPressed: () async {
                              _lights
                                  .setLight4(!_lights.light4.value)
                                  .then((_) => {isLightsON()});
                            },
                          ),
                        ),
                        //* Switch 3
                        AnimatedContainer(
                          duration: _myAnimationDuration,
                          curve: Curves.easeIn,
                          decoration: BoxDecoration(
                            borderRadius: BorderRadius.circular(100),
                            color: _lights.light3.value
                                ? myPrimaryColor
                                : Colors.black45,
                          ),
                          child: IconButton(
                            padding: const EdgeInsets.all(25),
                            icon: const FaIcon(
                              FontAwesomeIcons.solidLightbulb,
                            ),
                            iconSize: 80,
                            color: colorWhite,
                            splashColor: _lights.light3.value
                                ? myPrimaryColor
                                : Colors.black26,
                            highlightColor: _lights.light3.value
                                ? myPrimaryLightColor
                                : colorGrey,
                            onPressed: () {
                              _lights
                                  .setLight3(!_lights.light3.value)
                                  .then((_) => {isLightsON()});
                            },
                          ),
                        ),
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
                      activeFgColor: colorWhite,
                      animate: true,
                      animationDuration: 300,
                      radiusStyle: true,
                      inactiveBgColor: colorGrey,
                      inactiveFgColor: colorWhite,
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
                          _lights.setAllLights(true);
                        } else if (index == 1) {
                          _lights.setAllLights(false);
                        }

                        for (int i = 0; i < _isSelected.length; i++) {
                          _selectedIndex.value = index!;
                        }
                      },
                    ),
                  ],
                ),
              );
            }
            if (snapshot.connectionState == ConnectionState.waiting) {
              return Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: const [
                  SizedBox(width: double.maxFinite),
                  CircularProgressIndicator(),
                  SizedBox(height: 15),
                  Text('Retrieving Data...', style: TextStyle(fontSize: 16)),
                  SizedBox(height: 5),
                  Text('Please Wait!!!', style: TextStyle(fontSize: 14))
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
      //* Connct to Wifi Button
      floatingActionButtonLocation: FloatingActionButtonLocation.centerFloat,
      floatingActionButton: Obx(() => FloatingActionButton(
            onPressed: connectWifi,
            tooltip: 'SmartConnect Wifi',
            backgroundColor:
                _selectedIndex.value == 1 ? Colors.black45 : myPrimaryColor,
            child: const FaIcon(
              FontAwesomeIcons.plus,
              color: colorWhite,
            ),
          )),
    );
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
