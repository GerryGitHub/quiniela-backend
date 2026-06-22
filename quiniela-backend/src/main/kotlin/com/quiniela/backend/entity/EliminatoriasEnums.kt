package com.quiniela.backend.entity

enum class Ronda(val ronda: String) {
    R32("R32"),
    R16("R16"),
    QF("QF"),
    SF("SF"),
    THIRD("3RD"),
    FINAL("FINAL");

    companion object {
        val ALL = entries.toList()
        val NEXT_RONDA_MAP = ALL.zipWithNext().toMap()

        fun from(value: String): Ronda =
            entries.firstOrNull { it.ronda == value }
                ?: throw IllegalArgumentException("Unknown ronda: $value")
    }
}

enum class SlotTipo(val tipo: String) {
    GRUPO_1("GRUPO_1"),
    GRUPO_2("GRUPO_2"),
    GRUPO_3("GRUPO_3"),
    WINNER("WINNER"),
    LOSER("LOSER");

    companion object {
        val GRUPOS = setOf(GRUPO_1, GRUPO_2, GRUPO_3)
        val CRUZAS = setOf(WINNER, LOSER)

        fun from(value: String): SlotTipo =
            entries.firstOrNull { it.tipo == value }
                ?: throw IllegalArgumentException("Unknown slot tipo: $value")
    }
}

enum class EstadoQuiniela(val estado: String) {
    ACTIVA("ACTIVA"),
    FINALIZADA("FINALIZADA"),
    GRUPOS("GRUPOS");

    companion object {
        fun from(value: String): EstadoQuiniela =
            entries.firstOrNull { it.estado == value }
                ?: throw IllegalArgumentException("Unknown estado: $value")
    }
}

enum class SortField(val column: String) {
    NOMBRE("nombre"),
    CREADOR("creador"),
    FECHA("fecha");

    companion object {
        fun from(value: String?): SortField? =
            if (value == null) null
            else entries.firstOrNull { it.column == value }
    }
}

enum class SortDirection(val direction: String) {
    ASC("asc"),
    DESC("desc");

    companion object {
        fun from(value: String?): SortDirection? =
            if (value == null) null
            else entries.firstOrNull { it.direction == value }
    }
}

enum class RolUsuario(val rol: String, val authority: String) {
    USER("USER", "ROLE_USER"),
    ADMIN("ADMIN", "ROLE_ADMIN");

    companion object {
        fun from(value: String): RolUsuario? = entries.firstOrNull { it.rol == value }
    }
}

enum class HealthStatus(val status: String) {
    ONLINE("ONLINE"),
    OFFLINE("OFFLINE");
}

object Constantes {
    const val PREFIJO_WINNER = "W"
    const val PREFIJO_LOSER = "L"
    const val TOKEN_TYPE_BEARER = "Bearer"

    const val RESPONSE_KEY_MESSAGE = "message"
    const val RESPONSE_KEY_ERROR = "error"
    const val RESPONSE_KEY_PARTIDOS_COUNT = "partidosCount"
}
