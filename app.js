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

let gestorBD = require("./modules/gestorBD.js");
gestorBD.init(app, mongo);

// routerUsuarioSession
var routerUsuarioSession = express.Router();
routerUsuarioSession.use(function(req, res, next) {
    console.log("routerUsuarioSession");
    if ( req.session.usuario ) {
        // dejamos correr la petición
        next();
    } else {
        console.log("va a : "+req.session.destino)
        res.redirect("/identificarse");
    }
});
//Aplicar routerUsuarioSession
app.use("/ofertas",routerUsuarioSession);
app.use("/ofertas/agregar",routerUsuarioSession);
app.use("/ofertas/misofertas",routerUsuarioSession);
app.use("/compras",routerUsuarioSession);

//routerUsuarioAutor
let routerUsuarioAutor = express.Router();
routerUsuarioAutor.use(function(req, res, next) {
    console.log("routerUsuarioAutor");
    let path = require('path');
    let id = path.basename(req.originalUrl);
// Cuidado porque req.params no funciona
// en el router si los params van en la URL.
    gestorBD.obtenerOfertas(
        {_id: mongo.ObjectID(id) }, function (ofertas) {
            console.log(ofertas[0]);
            if(ofertas[0].autor == req.session.usuario ){
                next();
            } else {
                res.redirect("/ofertas");
            }
        })
});
//Aplicar routerUsuarioAutor
app.use("/ofertas/eliminar",routerUsuarioAutor);

let logger = require("./modules/logger.js");

app.use(express.static('public'));

// Variables
app.set('port', 8081);
app.set('db', 'mongodb://admin:sdi@tiendamusica-shard-00-00.r7syo.mongodb.net:27017,tiendamusica-shard-00-01.r7syo.mongodb.net:27017,tiendamusica-shard-00-02.r7syo.mongodb.net:27017/mywallapop?ssl=true&replicaSet=atlas-b8hkqq-shard-0&authSource=admin&retryWrites=true&w=majority');
//Rutas/controladores por lógica
require("./routes/rusuarios.js")(app, swig, gestorBD, logger); // (app, param1, param2, etc.)
require("./routes/rofertas.js")(app, swig, gestorBD, logger); // (app, param1, param2, etc.)

app.get('/', function (req, res) {
    res.redirect('/tienda');
})

app.listen(app.get('port'), function () {
    logger.info('Servidor activo');
});