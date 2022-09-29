package com.github.h4de5ing.gsoncommon

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object JsonUtils {

    //gson
    fun <T> gParser(json: String, clazz: Class<T>): T = getJsonParser().fromJson(json, clazz)

    //处理json数据里面包含null的情况
    //https://blog.csdn.net/yangshare/article/details/77482156
    fun getJsonParser(): Gson {
        return GsonBuilder().serializeNulls()
            .registerTypeAdapterFactory(NullStringToEmptyAdapterFactory<Any>())
            //.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            //.setPrettyPrinting()
            .setExclusionStrategies(AnnotationExclusion()).create()
    }

//    fun <T> gParserType(json: String, type: Type): T = getJsonParser().fromJson(json, type)

//    fun <T> toJsonClass(clazz: Class<T>): String = getJsonParser().toJson(clazz)
//    inline fun <reified T> genericType() = object : TypeToken<T>() {}.type
    fun Any?.toJson(): String = getJsonParser().toJson(this)

    fun Any?.isJson(): Boolean {
        return try {
            this.toString().startsWith("{")
        } catch (e: Exception) {
            false
        }
    }
}