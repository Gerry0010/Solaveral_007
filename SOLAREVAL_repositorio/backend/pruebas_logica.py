"""Pruebas de la lógica sin base de datos: python pruebas_logica.py"""
import time
import jwt
import calculos, solar, tokens

# --- cálculo: el ejemplo de las pantallas (1,850 kWh bimestral)
p = solar.potencial_demo({"panel_w_demo": 400, "rendimiento_demo": 1600, "paneles_max_demo": 40})
consumo = calculos.consumo_anual(1850, "bimestral")
assert consumo == 11100
cfg = calculos.elegir_configuracion(p["configs"], consumo, 0.85)
gen = cfg["yearlyEnergyDcKwh"] * 0.85
assert gen <= consumo and cfg["panelsCount"] == 20, cfg           # 20 paneles de 400 W
r = calculos.calcular_reporte(consumo_kwh=1850, periodo="bimestral", precio_kwh=4.0,
    potencia_kw=cfg["panelsCount"] * 0.4, generacion_anual_kwh=gen, costo_por_kw=20000,
    umbral_ahora=4, umbral_condiciones=7, cobertura_minima=0.5)
print(r)
assert r["ahorro_estimado"] == round(gen * 4.0, 2)
assert r["recomendacion"] in ("conviene_ahora", "conviene_con_condiciones", "no_conviene_todavia")

# --- RN-03: la generación no puede pasar del consumo
r2 = calculos.calcular_reporte(consumo_kwh=100, periodo="mensual", precio_kwh=4.0, potencia_kw=5,
    generacion_anual_kwh=99999, costo_por_kw=20000, umbral_ahora=4, umbral_condiciones=7, cobertura_minima=0.5)
assert r2["cobertura"] == 1.0 and r2["ahorro_estimado"] == 100 * 12 * 4.0
# --- si ni la configuración más pequeña cabe, usa la más pequeña
assert calculos.elegir_configuracion([{"panelsCount": 5, "yearlyEnergyDcKwh": 9000},
    {"panelsCount": 2, "yearlyEnergyDcKwh": 4000}], 1000, 0.85)["panelsCount"] == 2
# --- clasificación
assert calculos.clasificar(3, 0.9, 4, 7, 0.5) == "conviene_ahora"
assert calculos.clasificar(3, 0.2, 4, 7, 0.5) == "conviene_con_condiciones"
assert calculos.clasificar(5, 0.9, 4, 7, 0.5) == "conviene_con_condiciones"
assert calculos.clasificar(9, 0.9, 4, 7, 0.5) == "no_conviene_todavia"
assert calculos.clasificar(None, 0, 4, 7, 0.5) == "no_conviene_todavia"

# --- Solar API: parseo de una respuesta con la forma de buildingInsights
ok = solar.parsear_respuesta({"solarPotential": {"panelCapacityWatts": 400, "solarPanelConfigs": [
    {"panelsCount": 4, "yearlyEnergyDcKwh": 1800.5, "roofSegmentSummaries": []}]}})
assert ok["panel_w"] == 400 and ok["configs"][0]["panelsCount"] == 4
try:
    solar.parsear_respuesta({"solarPotential": {}}); raise SystemExit("debió fallar")
except solar.SolarError as e:
    assert e.codigo == 404

# --- contraseñas y tokens
h = tokens.hash_password("Demo1234")
assert tokens.verificar_password("Demo1234", h) and not tokens.verificar_password("otra", h)
assert not tokens.verificar_password("x", "basura")
t = tokens.crear_token(7, "pyme")
assert tokens.leer_token(t) == {"id": 7, "rol": "pyme"}
vencido = jwt.encode({"sub": "1", "rol": "pyme", "exp": int(time.time()) - 5}, tokens.SECRET_KEY, algorithm="HS256")
try:
    tokens.leer_token(vencido); raise SystemExit("debió fallar")
except jwt.PyJWTError:
    pass
print("TODAS LAS PRUEBAS DE LÓGICA PASARON")
