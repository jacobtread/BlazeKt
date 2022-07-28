package com.jacobtread.blaze.data

data class VarPair(val a: ULong, val b: ULong) {
    constructor(a: UInt, b: UInt) : this(a.toULong(), b.toULong())
}