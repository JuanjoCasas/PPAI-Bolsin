#!/usr/bin/env python3
"""Export PlantUML diagrams to a single PDF for PPAI submission.
Uses PlantUML online server for rendering + headless Chrome for PDF output.
No external Python packages required.
"""

import base64
import os
import subprocess
import tempfile
import urllib.request
import zlib

OUTPUT_FILE = "PPAI2026_3K5_G2_E1_Analisis.pdf"

DOC_DIR = os.path.join(os.path.dirname(__file__), "doc", "CU28_RegistrarRecepcionDeBolsin")

DIAGRAMS = [
    ("Vista de Clases de Análisis", os.path.join(DOC_DIR, "VistaClasesAnalisis.puml")),
    ("Vista de Interacción",        os.path.join(DOC_DIR, "diagramaSecuencia.puml")),
    ("Máquina de Estados",          os.path.join(DOC_DIR, "ME_Documentacion.puml")),
]

CHROME = next(
    (c for c in ("google-chrome", "chromium", "chromium-browser") if subprocess.call(
        ["which", c], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL) == 0),
    None,
)


def plantuml_encode(text: str) -> str:
    compressed = zlib.compress(text.encode("utf-8"), 9)
    data = compressed[2:-4]
    chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_"
    result = []
    i = 0
    while i < len(data):
        if i + 2 < len(data):
            b1, b2, b3 = data[i], data[i + 1], data[i + 2]
            result += [
                chars[(b1 >> 2) & 0x3F],
                chars[((b1 & 0x3) << 4) | ((b2 >> 4) & 0xF)],
                chars[((b2 & 0xF) << 2) | ((b3 >> 6) & 0x3)],
                chars[b3 & 0x3F],
            ]
            i += 3
        elif i + 1 < len(data):
            b1, b2 = data[i], data[i + 1]
            result += [
                chars[(b1 >> 2) & 0x3F],
                chars[((b1 & 0x3) << 4) | ((b2 >> 4) & 0xF)],
                chars[((b2 & 0xF) << 2)],
            ]
            i += 2
        else:
            b1 = data[i]
            result += [chars[(b1 >> 2) & 0x3F], chars[(b1 & 0x3) << 4]]
            i += 1
    return "".join(result)


def fetch_png_b64(puml_text: str) -> str:
    encoded = plantuml_encode(puml_text)
    url = f"https://www.plantuml.com/plantuml/png/{encoded}"
    print(f"    Descargando desde PlantUML server...")
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    with urllib.request.urlopen(req, timeout=30) as resp:
        return base64.b64encode(resp.read()).decode()


def build_html(diagrams_b64: list) -> str:
    pages = ""
    for label, b64 in diagrams_b64:
        pages += f"""
        <div class="page">
            <p class="title">{label}</p>
            <img src="data:image/png;base64,{b64}" />
        </div>
        """
    return f"""<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<style>
  * {{ margin: 0; padding: 0; box-sizing: border-box; }}
  body {{ background: white; }}
  .page {{
    width: 100%;
    height: 100vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    page-break-after: always;
    padding: 15mm;
  }}
  .title {{
    font-family: Arial, sans-serif;
    font-size: 14pt;
    font-weight: bold;
    margin-bottom: 10mm;
    color: #333;
  }}
  img {{
    max-width: 100%;
    max-height: calc(100vh - 40mm);
    object-fit: contain;
  }}
</style>
</head>
<body>
{pages}
</body>
</html>"""


def main():
    if not CHROME:
        print("ERROR: No se encontró Chrome/Chromium instalado.")
        raise SystemExit(1)

    diagrams_b64 = []
    for label, puml_path in DIAGRAMS:
        print(f"  Renderizando: {label}...")
        with open(puml_path, "r", encoding="utf-8") as f:
            content = f.read()
        b64 = fetch_png_b64(content)
        diagrams_b64.append((label, b64))

    html = build_html(diagrams_b64)

    with tempfile.TemporaryDirectory() as tmpdir:
        html_path = os.path.join(tmpdir, "diagrams.html")
        with open(html_path, "w", encoding="utf-8") as f:
            f.write(html)

        output_path = os.path.abspath(OUTPUT_FILE)
        print("  Generando PDF con Chrome...")
        subprocess.check_call([
            CHROME,
            "--headless",
            "--disable-gpu",
            "--no-sandbox",
            "--disable-dev-shm-usage",
            f"--print-to-pdf={output_path}",
            "--print-to-pdf-no-header",
            html_path,
        ], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

    print(f"\nPDF generado: {OUTPUT_FILE}")


if __name__ == "__main__":
    main()
