package dog.catfood.utils

import kotlin.reflect.KType
import kotlin.reflect.typeOf

class Converter<T : Any>(
    private val convertFn: (String) -> T?
) {
    fun convert(value: String): T? {
        return try {
            convertFn(value)
        } catch (e: Throwable) {
            throw ConversionException("Error converting value=$value", e)
        }
    }
}

class ConverterFactory(
    private val converters: Map<KType, Converter<*>>
) {
    fun get(type: KType): Converter<*> {
        return converters[type] ?: throw ConversionException("No converter for type=$type")
    }
}

class ConversionException(message: String, e: Throwable? = null): Throwable(message, e)

val DEFAULT_CONVERTERS = mapOf(
    typeOf<String>() to Converter{ it },
    typeOf<Int>() to Converter { it.toInt() },
    typeOf<Long>() to Converter { it.toLong() },
    typeOf<Short>() to Converter { it.toShort() },
    typeOf<Boolean>() to Converter { it.toBoolean() },
    typeOf<Float>() to Converter { it.toFloat() },
    typeOf<Double>() to Converter { it.toDouble() },
    typeOf<String?>() to Converter{ it },
    typeOf<Int?>() to Converter { it.takeUnless { it == "" }?.toInt() },
    typeOf<Long?>() to Converter { it.takeUnless { it == "" }?.toLong() },
    typeOf<Short?>() to Converter { it.takeUnless { it == "" }?.toShort() },
    typeOf<Boolean?>() to Converter { it.takeUnless { it == "" }?.toBoolean() },
    typeOf<Float?>() to Converter { it.takeUnless { it == "" }?.toFloat() },
    typeOf<Double?>() to Converter { it.takeUnless { it == "" }?.toDouble() }
)