package com.jacobtread.blaze.debug

interface DebugNaming {
    fun getComponentNames(): Map<Int, String>
    fun getCommandNames(): Map<Int, String>
    fun getNotifyNames(): Map<Int, String>
}