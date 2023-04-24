package data

import kotlinx.serialization.Serializable

/**
 * Data class, represents the coordinates
 *
 * @param x the x coordinate, should be <= 552
 * @param y the y coordinate
 */
@Serializable
data class Coordinates(
    val x: Float,
    val y: Double
)