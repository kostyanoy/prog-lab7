package serialize

import ArgumentType
import data.MusicGenre
import kotlinx.serialization.json.*

//extension functions needed for serializing any value
//using a json methods to cast element to class and catch exceptions

/**
 * Checks if the json element is [String]
 *
 * @return true if the check is successful
 */
fun JsonElement.isString(): Boolean {
    return try {
        if (this.jsonPrimitive.isString)
            return true
        false
    } catch (e: IllegalArgumentException) {
        false
    }
}

/**
 * Checks if the json element is [Int]
 *
 * @return true if the check is successful
 */
fun JsonElement.isInt(): Boolean {
    return try {
        this.jsonPrimitive.int
        true
    } catch (e: IllegalArgumentException) {
        false
    } catch (e: NumberFormatException) {
        false
    }
}

/**
 * Checks if the json element is object with parameters
 *
 * @return true if the check is successful
 */
fun JsonElement.isObject(): Boolean {
    return try {
        this.jsonObject
        true
    } catch (e: IllegalArgumentException) {
        false
    }
}

/**
 * Checks if the json element is collection
 *
 * @return true if the check is successful
 */
fun JsonElement.isArray(): Boolean {
    return try {
        this.jsonArray
        true
    } catch (e: IllegalArgumentException) {
        false
    }
}

/**
 * Checks if the json element is [ArgumentType]
 *
 * @return true if the check is successful
 */
fun JsonElement.isArgumentType(): Boolean {
    return try {
        this.isString() && this.jsonPrimitive.content in ArgumentType.values().map { it.toString() }
    } catch (e: IllegalArgumentException) {
        false
    }
}

/**
 * Checks if the json element is [MusicGenre]
 *
 * @return true if the check is successful
 */
fun JsonElement.isMusicGenre(): Boolean {
    return try {
        this.isString() && this.jsonPrimitive.content in MusicGenre.values().map { it.toString() }
    } catch (e: IllegalArgumentException) {
        false
    }
}

