package com.jacobtread.blaze.data

data class VarTriple(val a: ULong, val b: ULong, val c: ULong) {
    constructor(a: UInt, b: UInt, c: UInt) : this(a.toULong(), b.toULong(), c.toULong())
}