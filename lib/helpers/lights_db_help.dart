import 'package:get/get.dart';
import 'package:firebase_database/firebase_database.dart';

class LightsDBHelp extends GetxController {
  final DatabaseReference _myHomeRef =
      FirebaseDatabase.instance.ref().child('myHome');

  RxBool light1 = false.obs;
  RxBool light2 = false.obs;
  RxBool light3 = false.obs;
  RxBool light4 = false.obs;

  Future<void> setAllLights(bool isOn) async {
    await _myHomeRef.update({
      'light1': isOn,
      'light2': isOn,
      'light3': isOn,
      'light4': isOn,
    }).whenComplete(() {
      light1.value = isOn;
      light2.value = isOn;
      light3.value = isOn;
      light4.value = isOn;
    }).onError((error, stackTrace) {});
  }

  Future<void> setLights(bool l1, bool l2, bool l3, bool l4) async {
    await _myHomeRef.update({
      'light1': l1,
      'light2': l2,
      'light3': l3,
      'light4': l4,
    }).whenComplete(() {
      light1.value = l1;
      light2.value = l2;
      light3.value = l3;
      light4.value = l4;
    }).onError((error, stackTrace) {});
  }

  Future<void> setLight1(bool l1) async {
    await _myHomeRef
        .update({'light1': l1})
        .whenComplete(() => light1.value = l1)
        .onError((error, stackTrace) {});
  }

  Future<void> setLight2(bool l2) async {
    await _myHomeRef
        .update({'light2': l2})
        .whenComplete(() => light2.value = l2)
        .onError((error, stackTrace) {});
  }

  Future<void> setLight3(bool l3) async {
    await _myHomeRef
        .update({'light3': l3})
        .whenComplete(() => light3.value = l3)
        .onError((error, stackTrace) {});
  }

  Future<void> setLight4(bool l4) async {
    await _myHomeRef
        .update({'light4': l4})
        .whenComplete(() => light4.value = l4)
        .onError((error, stackTrace) {});
  }

  getLights() async {
    await _myHomeRef
        .child('/light1')
        .get()
        .then((value) => light1.value = value.value as bool);
    await _myHomeRef
        .child('/light2')
        .get()
        .then((value) => light2.value = value.value as bool);
    await _myHomeRef
        .child('/light3')
        .get()
        .then((value) => light3.value = value.value as bool);
    await _myHomeRef
        .child('/light4')
        .get()
        .then((value) => light4.value = value.value as bool);
  }
}
