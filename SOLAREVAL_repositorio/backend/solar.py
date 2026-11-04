"""Potencial solar del techo: Google Solar API o modo demostración."""
import json
import os
import urllib.error
import urllib.parse
import urllib.request

URL = "https://solar.googleapis.com/v1/buildingInsights:findClosest"


class SolarError(Exception):
    def __init__(self, mensaje: str, codigo: int = 502):
        super().__init__(mensaje)
        self.codigo = codigo


def parsear_respuesta(datos: dict) -> dict:
    """Extrae de la respuesta de buildingInsights lo que necesita el cálculo."""
    sp = datos.get("solarPotential") or {}
    configs = sp.get("solarPanelConfigs") or []
    panel_w = sp.get("panelCapacityWatts")
    if not configs or not panel_w:
        raise SolarError("La Solar API no devolvió configuraciones de paneles para este techo", 404)
    limpias = [{"panelsCount": c["panelsCount"], "yearlyEnergyDcKwh": c["yearlyEnergyDcKwh"]} for c in configs]
    return {"panel_w": float(panel_w), "configs": limpias, "fuente": "solar_api"}


def consultar_solar_api(lat: float, lon: float, llave: str) -> dict:
    query = urllib.parse.urlencode({
        "location.latitude": lat, "location.longitude": lon,
        "requiredQuality": "BASE", "key": llave,
    })
    try:
        with urllib.request.urlopen(f"{URL}?{query}", timeout=15) as r:
            return parsear_respuesta(json.loads(r.read().decode()))
    except urllib.error.HTTPError as e:
        if e.code == 404:
            raise SolarError("No hay datos solares para esta ubicación. Ajusta el pin del mapa o intenta de nuevo.", 404)
        if e.code in (400, 403):
            raise SolarError("La llave de la Solar API es inválida o no tiene permisos", 502)
        raise SolarError(f"La Solar API respondió con error {e.code}", 502)
    except (urllib.error.URLError, TimeoutError):
        raise SolarError("No se pudo conectar con la Solar API. Intenta de nuevo.", 502)


def potencial_demo(p: dict) -> dict:
    """Modo demostración: configuraciones simuladas con parámetros editables (no son datos reales)."""
    panel_w = p["panel_w_demo"]
    por_panel_kwh = panel_w / 1000 * p["rendimiento_demo"]
    configs = [{"panelsCount": n, "yearlyEnergyDcKwh": n * por_panel_kwh}
               for n in range(1, int(p["paneles_max_demo"]) + 1)]
    return {"panel_w": float(panel_w), "configs": configs, "fuente": "demo"}


def obtener_potencial(lat: float, lon: float, parametros: dict) -> dict:
    llave = os.getenv("SOLAR_API_KEY", "").strip()
    if os.getenv("MODO_DEMO", "false").lower() == "true" or not llave:
        return potencial_demo(parametros)
    return consultar_solar_api(lat, lon, llave)
