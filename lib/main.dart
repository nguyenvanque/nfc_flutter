import 'dart:collection';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fluttertoast/fluttertoast.dart';

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
  var hashMap;

  final edtP1 = TextEditingController();
  final edtP2 = TextEditingController();


  @override
  void initState() {
    // _getMessage().then((String message) {
    //   setState(() {
    //     _message = message;
    //     if (message!="") {
    //       Future.delayed(Duration.zero, () => _showMyDialog(message));
    //     }
    //   });
    // });
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    getData().then((value) => {
          print("initState" + value),
          if (value != "") {_message = value}
        });
    getMapData().then((value) => {
          if (value.values.length > 0 && value!=null) {
            hashMap = value,
            print(hashMap)
          },
        });

    return Scaffold(
      appBar: AppBar(
        title: Text("Flutter NFC"),
      ),
      body: SingleChildScrollView(
        child: Stack(
          children: [
            Center(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  const  Padding(
                    padding:  EdgeInsets.only(left: 15,top: 30),
                    child: Text("Enter your pin 1",style: TextStyle(fontSize: 18,fontWeight: FontWeight.bold),),
                  ),
                  Container(
                    margin: const EdgeInsets.only(left: 15, right: 15, top: 10),
                    child: TextField(
                      controller: edtP1,
                      keyboardType: TextInputType.number,
                      obscureText: true,
                      maxLength: 4,
                      decoration: const InputDecoration(
                          border: OutlineInputBorder(),
                          labelText: '',
                          hintText: 'Enter Your pin1'),
                    ),
                  ),
                  const SizedBox(
                    height: 10.0,
                  ),
                   const  Padding(
                    padding:  EdgeInsets.only(left: 15,),
                    child: Text("Enter your pin 2",style: TextStyle(fontSize: 18,fontWeight: FontWeight.bold),),
                  ),
                  Container(
                    margin: const EdgeInsets.only(left: 15, right: 15, top: 10),
                    child: TextField(
                      controller: edtP2,
                      keyboardType: TextInputType.number,
                      maxLength: 4,
                      obscureText: true,

                      decoration: const InputDecoration(
                          border: OutlineInputBorder(),
                          labelText: '',
                          hintText: 'Enter Your pin2'),
                    ),
                  ),
                  const SizedBox(
                    height: 10,
                  ),
                  // ElevatedButton(
                  //   child: Text('Open NFC'),
                  //   onPressed: showNativeView,
                  // ),
                  const SizedBox(
                    height: 10,
                  ),
                  Center(
                    child:   ElevatedButton(
                      child: const Text('Read NFC'),
                      onPressed: () {
                        if (edtP1.text == "") {
                          showToast("Please enter your pin 1");
                        } else if (edtP2.text == "") {
                          showToast("Please enter your pin 2");
                        } else if (edtP1.text.length < 4) {
                          showToast("Pin 1 must be 4 characters");
                        } else if (edtP2.text.length < 4) {
                          showToast("Pin 2 must be 4 characters");
                        } else {
                          _getMessage();
                          getMapData();
                          setState(() {
                          });
                        }
                      },
                    ),
                  ),
                  const SizedBox(
                    height: 10,
                  ),
                  Center(
                    child: Text(_message == "" ? "No data" : _message,
                      style: const TextStyle(
                        fontSize: 18,
                      ),
                    ),
                  ),
                  const SizedBox(
                    height: 5,
                  ),
                  const Divider(),
                  const SizedBox(
                    height: 5,
                  ),
                  Center(
                    child: Visibility(
                        visible: true,
                        child: Text(
                          hashMap == null || hashMap.values.length < 0 ?'No data map':'${hashMap}',
                          style: const TextStyle(fontSize: 18),
                        )),
                  )
                ],
              ),
            ),

          ],
        ),
      ),
    );
  }

  void showToast(msg){
    Fluttertoast.showToast(
        msg: msg,
        toastLength: Toast.LENGTH_SHORT,
        gravity: ToastGravity.BOTTOM,
        timeInSecForIosWeb: 1,
        backgroundColor: Colors.grey,
        textColor: Colors.white,
        fontSize: 16.0
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
    var sendMap = <String, dynamic>{'edtP1': edtP1.text, 'edtP2': edtP2.text};
    String? value;
    try {
      value = await platform.invokeMethod('getPin', sendMap);
          print(value);
    } catch (e) {
      print(e);
    }

    return value!;
  }

  Future<String> getData() async {

    String? value;
    try {
      value = await platform.invokeMethod('getData');
    } catch (e) {
      print(e);
    }

    return value!;
  }

  Future getMapData() async {
    var value;
    try {
      value = await platform.invokeMethod('getMapData');
    } catch (e) {
      print(e);
    }
    return value;
  }
}
