# Printscript2026
Proyecto Printscript - Ingeniería de Sistemas.

## Módulos

| Módulo | Responsabilidad |
| --- | --- |
| `commons` | Tipos compartidos: `Token`, el AST y `PrintScriptException` (el error con su ubicación en el fuente). |
| `lexer` | Convierte el fuente en un flujo de tokens. |
| `parser` | Agrupa los tokens en sentencias y las convierte en nodos del AST. |
| `interpreter` | Evalúa el AST. |
| `formatter` | Reescribe el fuente según un archivo de reglas. |
| `linter` | Verifica convenciones y malas prácticas sobre el AST. |
| `cli` | Interfaz de línea de comandos que expone las cuatro operaciones. |

Todo el recorrido —lexer, parser, formatter, linter e interpreter— es perezoso: el fuente se
procesa a medida que se lee, así que un archivo que no cabe en memoria se puede recorrer de
punta a punta.

## CLI

```bash
./gradlew :cli:installDist
./cli/build/install/printscript/bin/printscript <operación> <archivo> [opciones]
```

| Argumento | Descripción |
| --- | --- |
| `<operación>` | `validation`, `execution`, `formatting` o `analyzing`. |
| `<archivo>` | El fuente a procesar. |
| `--version` | Versión del lenguaje. Opcional, por defecto `1.0`. También admite `1.1`. |
| `--config` | Archivo de configuración. Obligatorio para `formatting` (YAML o JSON) y `analyzing` (JSON). |
| `--output` | Archivo donde escribir el código formateado. Opcional; por defecto va a `stdout`. |

El resultado de la operación sale por `stdout` y todo lo demás —avance del parseo, avisos y
errores— por `stderr`, de modo que la salida del `formatting` se puede redirigir sin mezclas.

Ejemplos:

```bash
printscript validation programa.ps
printscript execution programa.ps --version 1.1
printscript formatting programa.ps --config reglas.yaml --output formateado.ps
printscript analyzing programa.ps --config reglasLinter.json
```

### Códigos de salida

| Código | Significado |
| --- | --- |
| `0` | La operación terminó bien. |
| `1` | Error en el fuente (léxico, sintáctico o de ejecución), violaciones encontradas por el `analyzing`, o error de entrada/salida. |
| `2` | Error en la invocación o en el archivo de configuración. |

Los errores del fuente se informan con su ubicación exacta, incluyendo fila y columna de inicio
y de fin:

```
Error: las sentencias deben finalizar con ";", "}" o "{" (desde línea 1, columna 1 hasta línea 1, columna 11)
```

### Configuración

Reglas del `formatter` (YAML; el mismo contenido en JSON también se admite):

```yaml
spaceBeforeColon: false
spaceAfterColon: true
spaceAroundEquals: true
lineBreakPrintln: 1
conditionalIndentation: 4   # sólo en 1.1
```

Reglas del `linter` (JSON):

```json
{
  "identifier_format": "camelcase",
  "enable_print_only": true,
  "enable_input_only": true
}
```

`enable_input_only` sólo está disponible en la versión 1.1, porque `readInput` no existe en 1.0.

## Verificación

```bash
./gradlew check
```

Corre Ktlint, Detekt, los tests y la verificación de cobertura (mínimo 80% por módulo).
