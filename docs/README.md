# Documentación

Explicación del sistema módulo por módulo, pensada para leer y para consultar durante la
presentación. Son páginas HTML autocontenidas: se abren con doble clic, sin servidor ni conexión.

Empezar por [`index.html`](index.html), que enlaza al resto.

| Archivo | Qué contiene |
| --- | --- |
| `index.html` | Índice de toda la documentación. |
| `overview.html` | Diagrama de componentes, flujo de datos, el Ejemplo 2 recorrido de punta a punta y las decisiones de diseño con su costo. |
| `commons.html` | `Token`, el AST y `PrintScriptException`. |
| `lexer.html` | Del texto a los tokens: la expresión regular combinada, 1.0 vs 1.1 y la pereza medida. |
| `parser.html` | `StatementSplitter` y la cadena de factories, con los árboles que producen. |
| `interpreter.html` | El ciclo de evaluadores, la traza del Ejemplo 2 y el tipado de `readInput`. |
| `formatter.html` | El formateo por huecos entre tokens, con un antes y después por regla. |
| `linter.html` | De la configuración a las reglas, y cómo se reportan las violaciones. |
| `cli.html` | Las cuatro operaciones y un runbook de demo con la salida real de cada comando. |
| `github.html` | CI, CD y cómo se publica una versión nueva de los paquetes. |

Para abrir el índice desde la terminal, en la raíz del repositorio:

```bash
open docs/index.html
```

## Notas

- El texto de las fichas está en inglés; este índice y este README, en español.
- Las salidas, trazas y mediciones que aparecen se obtuvieron ejecutando el código de la
  versión **1.0.0**, y las referencias `Archivo.kt:línea` corresponden a esa versión. Si el código
  cambia, los fragmentos citados pueden quedar corridos unas líneas.
- Las fichas cargan tipografías desde Google Fonts. Sin conexión se ven igual, con las
  tipografías del sistema.
- Cada ficha tiene un autotest cuyas marcas se guardan sólo en el navegador que las abre.
