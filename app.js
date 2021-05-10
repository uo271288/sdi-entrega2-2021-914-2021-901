//Módulos
let express = require('express');
let app = express();

let mongo = require('mongodb');
let swig = require('swig');
let bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));
let expressSession = require('express-session');
app.use(expressSession({secret: 'abcdefg', resave: true, saveUninitialized: true}));
let crypto = require('crypto');
app.set('clave', 'abcdefg');
app.set('crypto', crypto);
let jwt = require('jsonwebtoken');
app.set('jwt', jwt);

let gestorBD = require("./modules/gestorBD.js");
gestorBD.init(app, mongo);

let routerUsuarioLoggeado = express.Router();
routerUsuarioLoggeado.use(function (req, res, next) {
    logger.info("routerUsuarioLoggeado");
    if (!req.session.usuario) {
        // dejamos correr la petición
        next();
    } else {
        res.redirect("/ofertas")
    }
});
//Aplicar routerUsuariosNoAdmin
app.use("/identificarse", routerUsuarioLoggeado);
app.use("/registrarse", routerUsuarioLoggeado);

/**
 * Router para impedir que usuarios no admin accedan a la lista de usuarios
 *
 * @type {Router}
 */
let routerUsuariosNoAdmin = express.Router();
routerUsuariosNoAdmin.use(function (req, res, next) {
    logger.info("routerUsuariosNoAdmin");
    if (req.session.usuario && req.session.usuario.role === "admin") {
        // dejamos correr la petición
        next();
    } else {
        res.redirect("/ofertas");
    }
});
//Aplicar routerUsuariosNoAdmin
app.use("/usuarios*", routerUsuariosNoAdmin);

// routerUsuarioSession
var routerUsuarioSession = express.Router();
routerUsuarioSession.use(function (req, res, next) {
    logger.info("routerUsuarioSession");
    if (req.session.usuario) {
        if (req.session.usuario.role === "standard") {
            // dejamos correr la petición
            next();
        }
        if (req.session.usuario.role === "admin") {
            res.redirect("/usuarios");
        }
    } else {
        logger.info("va a : " + req.session.destino)
        res.redirect("/identificarse");
    }
});
//Aplicar routerUsuarioSession
app.use("/ofertas", routerUsuarioSession);
app.use("/ofertas/agregar", routerUsuarioSession);
app.use("/ofertas/misofertas", routerUsuarioSession);
app.use("/compras", routerUsuarioSession);

//routerUsuarioAutor
let routerUsuarioAutor = express.Router();
routerUsuarioAutor.use(function (req, res, next) {
    logger.info("routerUsuarioAutor");
    let path = require('path');
    let id = path.basename(req.originalUrl);
// Cuidado porque req.params no funciona
// en el router si los params van en la URL.
    gestorBD.obtenerOfertas(
        {_id: mongo.ObjectID(id)}, function (ofertas) {
            logger.info(ofertas[0]);
            if (ofertas[0] == null) {
                logger.error("Error 404: La oferta " + id + " no esta disponible o no existe")
                let respuesta = swig.renderFile('views/error.html',
                    {
                        numeroError: 404,
                        mensaje: "La oferta " + id + " no esta disponible o no existe"
                    });
                res.send(respuesta);
            } else {
                if (ofertas[0].autor._id === req.session.usuario._id) {
                    next();
                } else {
                    res.redirect("/ofertas");
                }
            }
        })
});
//Aplicar routerUsuarioAutor
app.use("/ofertas/eliminar", routerUsuarioAutor);

let routerUsuarioNoAutor = express.Router();
routerUsuarioNoAutor.use(function (req, res, next) {
    logger.info("routerUsuarioNoAutor");
    let path = require('path');
    let id = path.basename(req.originalUrl);
// Cuidado porque req.params no funciona
// en el router si los params van en la URL.
    gestorBD.obtenerOfertas(
        {_id: mongo.ObjectID(id)}, function (ofertas) {
            logger.info(ofertas[0]);
            if (ofertas[0] == null) {
                logger.error("Error 404: La oferta " + id + " no esta disponible o no existe")
                let respuesta = swig.renderFile('views/error.html',
                    {
                        numeroError: 404,
                        mensaje: "La oferta " + id + " no esta disponible o no existe"
                    });
                res.send(respuesta);
            } else {
                if (ofertas[0].autor._id !== req.session.usuario._id) {
                    next();
                } else {
                    res.redirect("/ofertas");
                }
            }
        })
});
//Aplicar routerUsuarioAutor
app.use("/ofertas/comprar", routerUsuarioNoAutor);

// routerUsuarioToken
let routerUsuarioToken = express.Router();
routerUsuarioToken.use(function (req, res, next) {
    // obtener el token, vía headers (opcionalmente GET y/o POST)
    let token = req.headers['token'] || req.body.token || req.query.token;
    if (token != null) {
        // verificar el token
        jwt.verify(token, 'secreto', function (err, infoToken) {
            if (err || (Date.now() / 1000 - infoToken.tiempo) > 240) {
                res.status(403); // Forbidden
                res.json({acceso: false, error: 'Token invalido o caducado'});
                // También podríamos comprobar que intoToken.usuario existe
                return;
            } else {
                // dejamos correr la petición
                res.usuario = infoToken.usuario;
                next();
            }
        });
    } else {
        res.status(403); // Forbidden
        res.json({acceso: false, mensaje: 'No hay Token'});
    }
});
// Aplicar routerUsuarioToken
app.use('/api/oferta', routerUsuarioToken);

let logger = require("./modules/logger.js");

app.use(express.static('public'));

// Variables
app.set('port', 8081);
app.set('db', 'mongodb://admin:sdi@tiendamusica-shard-00-00.r7syo.mongodb.net:27017,tiendamusica-shard-00-01.r7syo.mongodb.net:27017,tiendamusica-shard-00-02.r7syo.mongodb.net:27017/mywallapop?ssl=true&replicaSet=atlas-b8hkqq-shard-0&authSource=admin&retryWrites=true&w=majority');
//Rutas/controladores por lógica
require("./routes/rusuarios.js")(app, swig, gestorBD, logger); // (app, param1, param2, etc.)
require("./routes/rofertas.js")(app, swig, gestorBD, logger); // (app, param1, param2, etc.)
require("./routes/rapiofertas.js")(app, gestorBD, logger); // (app, param1, param2, etc.)

app.get('/', function (req, res) {
    res.redirect('/ofertas');
})

app.use(function (err, req, res, next) {
    console.log("Error producido: " + err);
    if (!res.headersSent) {
        let respuesta = swig.renderFile('views/error.html',
            {
                numeroError: 400,
                mensaje: "Recurso no disponible"
            });
        res.send(respuesta);
    }
});

app.listen(app.get('port'), function () {
    logger.info('Servidor activo');
});