import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() => runApp(MyApp());

const CHANNEL = "com.example.nfc_flutter.channel";
const KEY_NATIVE = "showNativeView";

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter NFC',
      initialRoute: '/',
      routes: {
        '/': (context) => MyHomePage(),
        '/reader_page': (context) => MyHomePage(),
      },
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primarySwatch: Colors.green,
      ),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key}) : super(key: key);

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const platform = MethodChannel(CHANNEL);
  String _message = "";

  final edtP1 = TextEditingController();
  final edtP2 = TextEditingController();

  @override
  void initState() {
    // _getMessage().then((String message) {
    //   setState(() {
    //     _message = message;
    //     if (message != null || !message.isEmpty) {
    //       Future.delayed(Duration.zero, () => _showMyDialog(message));
    //     }
    //   });
    // });

    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Flutter NFC"),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              _message == null || _message.isEmpty ? "No data" : _message,
              style: TextStyle(fontSize: 30, color: Colors.blue),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: TextField(
                controller: edtP1,
                decoration: InputDecoration(
                    border: OutlineInputBorder(),
                    labelText: '',
                    hintText: 'Enter Your pin1'),
              ),
            ),
            SizedBox(
              height: 20.0,
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: TextField(
                controller: edtP2,
                decoration:
                    InputDecoration(
                        border: OutlineInputBorder(),
                        labelText: '',
                        hintText: 'Enter Your pin2'),
              ),
            ),
            SizedBox(
              height: 20,
            ),
            // ElevatedButton(
            //   child: Text('Open NFC'),
            //   onPressed: showNativeView,
            // ),
            ElevatedButton(
              child: Text('Scan NFC'),
              onPressed: _getMessage,
            ),
            // ElevatedButton(
            //   child:  Text('Open dialog'),
            //   onPressed: ()=>_showMyDialog("ddd"),
            // ),
          ],
        ),
      ),
    );
  }

  Future<void> _showMyDialog(message) async {
    return showDialog<void>(
      context: context,
      barrierDismissible: false, // user must tap button!
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Notification has data'),
          content: SingleChildScrollView(
            child: ListBody(
              children: <Widget>[
                Text('This is data scan nfc'),
                Text(
                  message,
                  style: TextStyle(
                      fontSize: 20,
                      color: Colors.black,
                      fontWeight: FontWeight.bold),
                ),
              ],
            ),
          ),
          actions: <Widget>[
            TextButton(
              child: const Text('Close  '),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  Future<Null> showNativeView() async {
    await platform.invokeMethod(KEY_NATIVE);
  }

  Future<dynamic> _handleMethod(MethodCall call) async {
    switch (call.method) {
      case "message":
        debugPrint(call.arguments);
        return Future.value("");
    }
  }

  Future<String> _getMessage() async {
    var sendMap = <String, dynamic>{
      'edtP1': edtP1.text,
      'edtP2': edtP2.text};
    String? value;
    try {
      value = await platform.invokeMethod('getPin', sendMap);
    } catch (e) {
      print(e);
    }

    return value!;
  }
}

// class MyHomePage extends StatelessWidget {
//
//
//   MyHomePage({Key? key}) : super(key: key) {
//     platform.setMethodCallHandler(_handleMethod);
//   }
//
//   @override
//   Widget build(BuildContext context) {
//     return Scaffold(
//       appBar:  AppBar(
//         title:  Text("Flutter NFC"),
//       ),
//       body:  Center(
//         child:  Column(
//           mainAxisAlignment: MainAxisAlignment.center,
//           children: <Widget>[
//             RaisedButton(
//               child:  Text('Move to Native World!'),
//               onPressed: showNativeView,
//             ),
//           ],
//         ),
//       ),
//     );
//   }
//
//   Future<Null> showNativeView() async {
//     await platform.invokeMethod(KEY_NATIVE);
//   }
//
//   Future<dynamic> _handleMethod(MethodCall call) async {
//     switch (call.method) {
//       case "message":
//         debugPrint(call.arguments);
//         return Future.value("");
//     }
//   }
//   Future<String> _getMessage() async {
//     var sendMap = <String, dynamic> {
//       'from' : 'Brandon',
//     };
//
//     String? value;
//
//     try {
//       value = await platform.invokeMethod('getMessage', sendMap);
//     } catch (e) {
//       print(e);
//     }
//
//     return value!;
//   }
// }
