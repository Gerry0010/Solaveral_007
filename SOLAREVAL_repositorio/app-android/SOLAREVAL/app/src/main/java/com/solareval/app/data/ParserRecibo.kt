package com.solareval.app.data

data class DatosRecibo(
    val consumoKwh: Double?,
    val tarifa: String?,
    val importe: Double?,
    val periodo: String?,
)

// Busca en el texto que leyó ML Kit los datos del recibo de CFE.
// OJO: es una heurística. Los recibos varían, por eso el usuario SIEMPRE confirma o corrige (RF-07).
object ParserRecibo {
    private val reTarifa = Regex("""\b(PDBT|GDBT|GDMTH|GDMTO|DAC|APBT|APMT|RABT|RAMT)\b""")
    private val reConsumo = Regex("""(\d{1,3}(?:,\d{3})+|\d+)(?:\.\d+)?\s*kwh""", RegexOption.IGNORE_CASE)
    private val reTotalAPagar = Regex("""total\s*a\s*pagar\D{0,15}(\d{1,3}(?:,\d{3})*(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
    private val reTotal = Regex("""\btotal\b\D{0,15}(\d{1,3}(?:,\d{3})*\.\d{2})""", RegexOption.IGNORE_CASE)

    fun analizar(texto: String): DatosRecibo {
        val t = texto.replace("\n", " ")
        val tarifa = reTarifa.find(t.uppercase())?.value
        val consumo = reConsumo.findAll(t)
            .mapNotNull { numero(it.groupValues[1]) }
            .filter { it in 1.0..50000.0 }
            .maxOrNull()
        val importe = (reTotalAPagar.find(t) ?: reTotal.find(t))?.groupValues?.get(1)?.let { numero(it) }
        val periodo = when {
            t.contains("bimestr", ignoreCase = true) -> "bimestral"
            t.contains("mensual", ignoreCase = true) -> "mensual"
            else -> null
        }
        return DatosRecibo(consumo, tarifa, importe, periodo)
    }

    fun numero(s: String): Double? = s.replace(",", "").trim().toDoubleOrNull()
}
