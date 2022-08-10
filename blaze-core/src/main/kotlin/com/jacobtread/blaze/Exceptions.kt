package com.jacobtread.blaze

open class TdfContainerException(message: String) : RuntimeException(message)

class MissingTdfException(label: String) : TdfContainerException("Missing tdf $label")
class InvalidTdfException(label: String, expected: Class<*>, got: Class<*>) : TdfContainerException("Expected $label to be of type ${expected.simpleName} but got ${got.simpleName} instead")
class TdfReadException(label: String, type: UByte, cause: Throwable) : RuntimeException("Invalid tdf read $label, $type", cause)

/**
 * NotAuthenticatedException Exception thrown when the player gets accessed from
 * a player session that is not authenticated
 *
 * @constructor Create empty NotAuthenticatedException
 */
class NotAuthenticatedException : RuntimeException("Not authenticated")