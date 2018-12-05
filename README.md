# aws-sdk-android-ht-leak

This application demonstrates HandlerThread leak of aws-sdk-android 2.8.4
by having `AWSIotMqttManager` reconnect repeatedly.

The leak can be observed from logcat. The number of active threads is 10 or so at first.

```
12-05 23:41:08.351 24413-24463/com.github.hirofumi.aws_sdk_android_ht_leak E/MainActivity: `ulimit -n` = 32768
12-05 23:41:08.513 24413-24463/com.github.hirofumi.aws_sdk_android_ht_leak E/MainActivity: length of `lsof -p 24413` = 960, number of active threads = 9
12-05 23:41:08.821 24413-24475/com.github.hirofumi.aws_sdk_android_ht_leak W/AWSIotMqttManager: onFailure: connection failed.
```

But it increases instantly.

```
12-05 23:41:38.153 24413-24463/com.github.hirofumi.aws_sdk_android_ht_leak E/MainActivity: length of `lsof -p 24413` = 7569, number of active threads = 2210
```

And OOME occurs.

```
12-05 23:41:39.493 24413-1293/com.github.hirofumi.aws_sdk_android_ht_leak W/AWSIotMqttManager: Reconnect failed 
12-05 23:41:39.497 24413-1294/com.github.hirofumi.aws_sdk_android_ht_leak W/android_ht_lea: Throwing OutOfMemoryError "Could not allocate JNI Env: Failed anonymous mmap(0x0, 8192, 0x3, 0x2, 4720, 0): Permission denied. See process maps in the log."
12-05 23:41:39.501 24413-1294/com.github.hirofumi.aws_sdk_android_ht_leak E/AndroidRuntime: FATAL EXCEPTION: Reconnect thread
    Process: com.github.hirofumi.aws_sdk_android_ht_leak, PID: 24413
    java.lang.OutOfMemoryError: Could not allocate JNI Env: Failed anonymous mmap(0x0, 8192, 0x3, 0x2, 4720, 0): Permission denied. See process maps in the log.
        at java.lang.Thread.nativeCreate(Native Method)
```

NB: This also leaks file descriptors because `Looper` requires them.
