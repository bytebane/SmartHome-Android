import 'dart:async';
import 'package:flutter/services.dart';
import 'package:flutter/material.dart';
import 'package:esptouch_flutter/esptouch_flutter.dart';

class ConnectionPage extends StatefulWidget {
  const ConnectionPage({Key? key, required this.task}) : super(key: key);

  final ESPTouchTask task;

  @override
  State<StatefulWidget> createState() => ConnectionPageState();
}

class ConnectionPageState extends State<ConnectionPage> {
  late final Stream<ESPTouchResult> stream;
  late final StreamSubscription<ESPTouchResult> streamSubscription;
  late final Timer timer;

  final List<ESPTouchResult> results = [];

  @override
  void initState() {
    stream = widget.task.execute();
    streamSubscription = stream.listen(results.add);
    final receiving = widget.task.taskParameter.waitUdpReceiving;
    final sending = widget.task.taskParameter.waitUdpSending;
    final cancelLatestAfter = receiving + sending;
    timer = Timer(
      cancelLatestAfter,
      () {
        streamSubscription.cancel();
        if (results.isEmpty && mounted) {
          showDialog(
            context: context,
            builder: (context) {
              return AlertDialog(
                title: const Text('No devices found'),
                actions: <Widget>[
                  TextButton(
                    onPressed: () => Navigator.of(context)
                      ..pop()
                      ..pop(),
                    child: const Text('OK'),
                  ),
                ],
              );
            },
          );
        }
      },
    );
    super.initState();
  }

  @override
  dispose() {
    timer.cancel();
    streamSubscription.cancel();
    super.dispose();
  }

  Widget waitingState(BuildContext context) {
    return Center(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        mainAxisAlignment: MainAxisAlignment.center,
        children: const [
          CircularProgressIndicator(),
          SizedBox(height: 16),
          Text('Waiting for results'),
        ],
      ),
    );
  }

  Widget error(BuildContext context, String s) {
    return Center(child: Text(s, style: const TextStyle(color: Colors.red)));
  }

  copyValue(BuildContext context, String label, String v) {
    return () {
      Clipboard.setData(ClipboardData(text: v));
      final snackBar = SnackBar(content: Text('Copied $label to clipboard $v'));
      ScaffoldMessenger.of(context).showSnackBar(snackBar);
    };
  }

  Widget noneState(BuildContext context) {
    return const Text('None');
  }

  Widget resultList(BuildContext context) {
    return ListView.builder(
      shrinkWrap: true,
      itemCount: results.length,
      itemBuilder: (_, index) {
        final result = results[index];
        final textTheme = Theme.of(context).textTheme;
        return Padding(
          padding: const EdgeInsets.all(8.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: <Widget>[
              GestureDetector(
                onLongPress: copyValue(context, 'BSSID', result.bssid),
                child: Row(
                  children: [
                    Text('BSSID: ', style: textTheme.bodyText1),
                    Text(result.bssid,
                        style: const TextStyle(fontFamily: 'monospace')),
                  ],
                ),
              ),
              const SizedBox(height: 20),
              GestureDetector(
                onLongPress: copyValue(context, 'IP', result.ip),
                child: Row(
                  children: <Widget>[
                    Text('IP: ', style: textTheme.bodyText1),
                    Text(result.ip,
                        style: const TextStyle(fontFamily: 'monospace')),
                  ],
                ),
              )
            ],
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Task'),
      ),
      body: StreamBuilder<ESPTouchResult>(
        builder: (context, AsyncSnapshot<ESPTouchResult> snapshot) {
          if (snapshot.hasError) {
            return error(context, 'Error in StreamBuilder');
          }
          if (!snapshot.hasData) {
            return Center(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                mainAxisAlignment: MainAxisAlignment.center,
                children: const [
                  CircularProgressIndicator(),
                  SizedBox(height: 16),
                  Text('BroadCasting Wifi Details'),
                ],
              ),
            );
          }
          switch (snapshot.connectionState) {
            case ConnectionState.active:
              return resultList(context);
            case ConnectionState.none:
              return noneState(context);
            case ConnectionState.done:
              return resultList(context);
            case ConnectionState.waiting:
              return waitingState(context);
          }
        },
        stream: stream,
      ),
    );
  }
}
