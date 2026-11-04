"""Reglas de cálculo del reporte (RN-02 a RN-06). Puro Python, sin base de datos."""

FACTOR_PERIODO = {"mensual": 12, "bimestral": 6}


def consumo_anual(consumo_kwh: float, periodo: str) -> float:
    """RN-02: consumo del periodo del recibo ajustado a 12 meses."""
    return consumo_kwh * FACTOR_PERIODO[periodo]


def elegir_configuracion(configs: list, consumo_anual_kwh: float, derate: float) -> dict:
    """Elige cuántos paneles instalar: la configuración más grande cuya generación
    (ya con el factor DC a AC) no supera el consumo anual (RN-03). Si ni la más
    pequeña cabe, usa la más pequeña."""
    if not configs:
        raise ValueError("No hay configuraciones de paneles")
    ordenadas = sorted(configs, key=lambda c: c["panelsCount"])
    elegida = ordenadas[0]
    for c in ordenadas:
        if c["yearlyEnergyDcKwh"] * derate <= consumo_anual_kwh:
            elegida = c
        else:
            break
    return elegida


def clasificar(retorno, cobertura, umbral_ahora, umbral_condiciones, cobertura_minima):
    """RN-06: recomendación de tres niveles."""
    if retorno is None:
        return "no_conviene_todavia"
    if retorno <= umbral_ahora and cobertura >= cobertura_minima:
        return "conviene_ahora"
    if retorno <= umbral_condiciones:
        return "conviene_con_condiciones"
    return "no_conviene_todavia"


def calcular_reporte(*, consumo_kwh, periodo, precio_kwh, potencia_kw, generacion_anual_kwh,
                     costo_por_kw, umbral_ahora, umbral_condiciones, cobertura_minima):
    """RN-03 a RN-06. generacion_anual_kwh ya debe venir con el factor DC a AC aplicado."""
    consumo = consumo_anual(consumo_kwh, periodo)
    aprovechada = min(generacion_anual_kwh, consumo)           # RN-03
    cobertura = aprovechada / consumo if consumo > 0 else 0.0
    ahorro = aprovechada * precio_kwh                           # RN-04
    inversion = potencia_kw * costo_por_kw
    retorno = inversion / ahorro if ahorro > 0 else None        # RN-05
    return {
        "consumo_anual_kwh": round(consumo, 1),
        "generacion_anual_kwh": round(generacion_anual_kwh, 1),
        "cobertura": round(cobertura, 4),
        "ahorro_estimado": round(ahorro, 2),
        "inversion_referencial": round(inversion, 2),
        "retorno_inversion": round(retorno, 2) if retorno is not None else None,
        "recomendacion": clasificar(retorno, cobertura, umbral_ahora, umbral_condiciones, cobertura_minima),
    }
