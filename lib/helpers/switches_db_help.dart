import 'package:get/get.dart';
import 'package:firebase_database/firebase_database.dart';

class SwitchesDBHelp extends GetxController {
  final DatabaseReference _mySwitchesRef =
      FirebaseDatabase.instance.ref('myHome').child('/switches');

  RxBool switch1 = false.obs;
  RxBool switch2 = false.obs;
  RxBool switch3 = false.obs;
  RxBool switch4 = false.obs;

  Future<void> setAllSwitches(bool isOn) async {
    await _mySwitchesRef.update({
      'switch1': isOn,
      'switch2': isOn,
      'switch3': isOn,
      'switch4': isOn,
    }).whenComplete(() {
      switch1.value = isOn;
      switch2.value = isOn;
      switch3.value = isOn;
      switch4.value = isOn;
    }).onError((error, stackTrace) {});
  }

  Future<void> setSwitches(bool l1, bool l2, bool l3, bool l4) async {
    await _mySwitchesRef.update({
      'switch1': l1,
      'switch2': l2,
      'switch3': l3,
      'switch4': l4,
    }).whenComplete(() {
      switch1.value = l1;
      switch2.value = l2;
      switch3.value = l3;
      switch4.value = l4;
    }).onError((error, stackTrace) {});
  }

  Future<void> setswitch1(bool l1) async {
    await _mySwitchesRef
        .update({'switch1': l1})
        .whenComplete(() => switch1.value = l1)
        .onError((error, stackTrace) {});
  }

  Future<void> setswitch2(bool l2) async {
    await _mySwitchesRef
        .update({'switch2': l2})
        .whenComplete(() => switch2.value = l2)
        .onError((error, stackTrace) {});
  }

  Future<void> setswitch3(bool l3) async {
    await _mySwitchesRef
        .update({'switch3': l3})
        .whenComplete(() => switch3.value = l3)
        .onError((error, stackTrace) {});
  }

  Future<void> setswitch4(bool l4) async {
    await _mySwitchesRef
        .update({'switch4': l4})
        .whenComplete(() => switch4.value = l4)
        .onError((error, stackTrace) {});
  }

  getSwitchesStats() async {
    await _mySwitchesRef
        .child('/switch1')
        .get()
        .then((value) => switch1.value = value.value as bool);
    await _mySwitchesRef
        .child('/switch2')
        .get()
        .then((value) => switch2.value = value.value as bool);
    await _mySwitchesRef
        .child('/switch3')
        .get()
        .then((value) => switch3.value = value.value as bool);
    await _mySwitchesRef
        .child('/switch4')
        .get()
        .then((value) => switch4.value = value.value as bool);
  }
}
