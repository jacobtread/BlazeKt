package com.jacobtread.blaze.tdf

import io.netty.buffer.ByteBuf

interface TdfReadable<T> {

    fun read(label: String, input: ByteBuf): T

}