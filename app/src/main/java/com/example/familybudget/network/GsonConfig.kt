package com.example.familybudget.network

import com.google.gson.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import java.lang.reflect.Type

class LocalDateTimeAdapter : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_DATE_TIME

    override fun serialize(
        src: LocalDateTime?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return if (src == null) JsonNull.INSTANCE else JsonPrimitive(formatter.format(src))
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDateTime? {
        if (json == null || json.isJsonNull) {
            return null
        }
        
        val dateStr = json.asString
        return try {
            LocalDateTime.parse(dateStr, formatter)
        } catch (e: DateTimeParseException) {
            try {
                // Попробуем альтернативный формат, если основной не сработал
                LocalDateTime.parse(dateStr)
            } catch (e: DateTimeParseException) {
                throw JsonParseException("Could not parse date: $dateStr", e)
            }
        }
    }
}

fun createGson(): Gson {
    return GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()
} 