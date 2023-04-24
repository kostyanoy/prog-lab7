package utils

import data.MusicBand
import data.MusicGenre

/**
 * Interface for getting validated values
 */
interface Validator {
    /**
     * Get [Int] value
     */
    fun getInt(): Int

    /**
     * Get [String] value
     */
    fun getString(): String

    /**
     * Get [MusicGenre] value
     */
    fun getGenre(): MusicGenre

    /**
     * Get [MusicBand] value
     */
    fun getMusicBand(): MusicBand
}