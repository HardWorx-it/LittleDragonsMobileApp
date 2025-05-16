package com.example.littledragons.model

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.util.Size
import androidx.core.util.PatternsCompat
import com.google.firebase.Timestamp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.io.ByteArrayOutputStream
import java.io.IOException


fun isEmail(str: String) = PatternsCompat.EMAIL_ADDRESS.matcher(str).matches()

/**
 * Пароль должен содержать
 * - от 6 до 30 символов.
 * - не менее 1 строчной и не менее 1 заглавной буквы.
 * - один специальный символ, например ! или + или - или подобный
 * - не менее 1 цифры
 */
fun isValidPassword(password: String): Boolean {
    if (password.length < 6 || password.length > 30) return false
    if (password.firstOrNull { it.isDigit() } == null) return false
    if (password.filter { it.isLetter() }.firstOrNull { it.isUpperCase() } == null) return false
    if (password.filter { it.isLetter() }.firstOrNull { it.isLowerCase() } == null) return false
    if (password.firstOrNull { !it.isLetterOrDigit() } == null) return false

    return true
}

private val firstAndLastNamePattern = "(\\S+)\\s+(\\S+)".toRegex()

fun splitFirstAndLastName(name: String): Pair<String, String>? {
    if (name.isEmpty()) {
        return null;
    }
    val groups = firstAndLastNamePattern.matchEntire(name)?.groups
    return if ((groups?.size ?: 0) < 3) {
        null
    } else {
        groups!![1]!!.value to groups[2]!!.value
    }
}

fun decodeBase64String(value: String?): ByteArray? = value?.substringAfter("base64,")?.let {
    try {
        if (value.isEmpty()) null else Base64.decode(
            it,
            Base64.DEFAULT
        )
    } catch (e: IllegalArgumentException) {
        Log.e(
            "decodeBase64String",
            "Unable to decode base64",
            e
        )
        null
    }
}

fun encodeBase64Bytes(value: ByteArray?): String? = try {
    if (value?.isEmpty() ?: true) null else Base64.encodeToString(
        value,
        Base64.DEFAULT
    )
} catch (e: IllegalArgumentException) {
    Log.e(
        "decodeBase64String",
        "Unable to decode base64",
        e
    )
    null
}


fun Bitmap.readBitmapToBytes(): ByteArray? {
    val stream = ByteArrayOutputStream()
    compress(
        Bitmap.CompressFormat.PNG,
        90,
        stream
    )
    return try {
        return stream.toByteArray()
    } catch (e: IOException) {
        Log.e(
            "encodeBitmapToBase64",
            "Unable to decode bitmap to base64",
            e
        )
        null
    }
}


@Suppress("DEPRECATION")
fun getPhotoThumbnail(context: Context, photoUri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(
                photoUri,
                Size(
                    640,
                    480
                ),
                null
            )
        } else {
            MediaStore.Images.Thumbnails.getThumbnail(
                context.contentResolver,
                ContentUris.parseId(photoUri),
                MediaStore.Images.Thumbnails.FULL_SCREEN_KIND,
                null
            )
        }
    } catch (e: Exception) {
        Log.e(
            "getPhotoThumbnail",
            "Unable to load thumbnail",
            e
        )
        null
    } catch (e: IOException) {
        Log.e(
            "getPhotoThumbnail",
            "Unable to load thumbnail",
            e
        )
        null
    }
}

private val schoolClassPattern = "^[0-9]{1,2}[А-Я]{1,2}$".toRegex()

fun isValidSchoolClassName(str: String) = str.isNotEmpty() && schoolClassPattern.matches(str)

fun LocalDateTime.toTimestamp(zone: TimeZone = TimeZone.currentSystemDefault()): Timestamp =
    toInstant(zone).run {
        Timestamp(
            epochSeconds,
            nanosecond
        )
    }

fun LocalDate.toTimestamp() =
    this.atTime(LocalTime.fromMillisecondOfDay(0)).toTimestamp(TimeZone.currentSystemDefault())

fun Timestamp.toLocalDateTime(zone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime =
    Instant.fromEpochSeconds(
        seconds,
        nanoseconds
    ).toLocalDateTime(zone)

// Функция для определения, является ли год високосным
fun isLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

fun getFirstAndLastDayOfMonth(year: Int, month: Month) = LocalDate(
    year,
    month.value,
    1
) to LocalDate(
    year,
    month.value,
    month.length(isLeapYear(year))
)