package com.mycompany.thesis1.mvvm.model

sealed class CommonResource(
    val message: String? = null
) {
    class ShowDownload(): CommonResource()
    class Error(message: String?): CommonResource(message)
    class Success(message: String?): CommonResource(message)
    class ShowMessage(message: String?): CommonResource(message)
}