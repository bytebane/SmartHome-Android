import 'package:firebase_database/firebase_database.dart';
import 'package:get/get.dart';

class FanDBHelp extends GetxController {
  final DatabaseReference _mySwitchesRef =
      FirebaseDatabase.instance.ref('myHome').child('/fans');

  RxBool fan1 = false.obs;
  RxInt fan1Speed = 0.obs;

  Future<void> setFanStats(bool fanStats, int fanSpeed) async {
    await _mySwitchesRef
        .set({'fan1Stats': fanStats, 'fan1Speed': fanSpeed}).whenComplete(() {
      fan1.value = fanStats;
      fan1Speed.value = fanSpeed;
    }).onError((error, stackTrace) {});
  }

  getFanStats() async {
    await _mySwitchesRef
        .child('/fan1Stats')
        .get()
        .then((fStats) => fan1.value = fStats.value as bool);
    await _mySwitchesRef
        .child('/fan1Speed')
        .get()
        .then((fSpeed) => fan1Speed.value = fSpeed.value as int);
  }
}
