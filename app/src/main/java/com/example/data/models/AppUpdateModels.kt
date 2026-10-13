package com.example.data.models

data class AppUpdateInfo(
    val latestVersionCode: Int = 1,
    val latestVersionName: String = "1.0.0",
    val releaseNotes: String = "Bug fixes and performance improvements",
    val downloadUrl: String = "https://storage.googleapis.com/gen-lang-client-0639805966.appspot.com/builds/latest.apk",
    val isMandatory: Boolean = false
)
