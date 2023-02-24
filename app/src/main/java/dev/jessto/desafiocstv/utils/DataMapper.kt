package dev.jessto.desafiocstv.utils

abstract class DataMapper<in R, out D> {

    abstract fun toDomain(data: R): D

    fun mapList(data: Iterable<R>) = data.map { toDomain(it) }
}
