package com.jacobtread.blaze.logging

interface BlazeLoggingHandler {
    fun debug(text: String)

    fun warn(text: String)
    fun warn(text: String, cause: Throwable)

    fun error(text: String)
    fun error(text: String, cause: Throwable)

    fun getComponentNames(): Map<Int, String>
    fun getCommandNames(): Map<Int, String>
    fun getNotifyNames(): Map<Int, String>
}