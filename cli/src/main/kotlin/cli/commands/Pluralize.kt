package cli.commands

/** Elige la forma singular o plural según la cantidad, para que los reportes se lean bien. */
internal fun pluralize(
    count: Int,
    singular: String,
    plural: String,
): String = if (count == 1) singular else plural
