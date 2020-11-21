package ru.larin.wifipowercontroller.lib;

public class BuildInfo {
/*    [{
        "outputType": {
            "type": "APK"
        },
        "apkInfo": {
            "type": "MAIN",
                    "splits": [],
            "versionCode": 1,
                    "versionName": "1.0",
                    "enabled": true,
                    "outputFile": "app-debug.apk",
                    "fullName": "debug",
                    "baseName": "debug"
        },
        "path": "app-debug.apk",
                "properties": {}
    }
]*/

    private ApkInfo apkInfo;

    public ApkInfo getApkInfo() {
        return apkInfo;
    }

    public void setApkInfo(ApkInfo apkInfo) {
        this.apkInfo = apkInfo;
    }
}
