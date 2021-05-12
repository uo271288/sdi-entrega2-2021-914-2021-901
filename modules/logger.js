/**
 * Importa el paquete winston que permite crear un logger
 */
const {createLogger, format, transports} = require('winston');

/**
 * Especifíca ciertas características del logger
 *
 * @type {winston.Logger}
 */
module.exports = createLogger({
    format: format.combine(
        format.simple(), // Muestra el texto en formato simple
        format.timestamp(), // Muestra la fecha y hora del momento de la llamada
        format.printf(info => `[${info.timestamp}] ${info.level}: ${info.message}`)
        // Da el siguiente formato al mensaje del logger: [yyyy-mm-ddThh:mm:ss.sssZ] <tipo>: <mensaje>
    ),
    transports: [
        // Esctibe en un archivo el mensaje del logger
        new transports.File({
            maxsize: 5120000, // Tamaño máximo del archivo, cuando se supera se genera otro archivo
            maxFiles: 5, // Número máximo de archivos que va a generar, cuando se supera se sobreescribe el
                         // primer fichero de log creado
            filename: `${__dirname}/../logs/log-app.log` // Fichero destino
        }),
        // Imprime en la consola el mensaje del logger
        new transports.Console({
            format: format.colorize({all: true}) // Estabelce el color para los mensajes de log de la consola
        })
    ]
});