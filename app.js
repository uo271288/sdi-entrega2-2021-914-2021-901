//Módulos
let express = require('express');
let app = express();

let mongo = require('mongodb');
let swig = require('swig');
let bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

let gestorBD = require("./modules/gestorBD.js");
gestorBD.init(app,mongo);

app.use(express.static('public'));

// Variables
app.set('port', 8090);
app.set('db','mongodb://admin:sdi@tiendamusica-shard-00-00.r7syo.mongodb.net:27017,tiendamusica-shard-00-01.r7syo.mongodb.net:27017,tiendamusica-shard-00-02.r7syo.mongodb.net:27017/mywallapop?ssl=true&replicaSet=atlas-b8hkqq-shard-0&authSource=admin&retryWrites=true&w=majority');
//Rutas/controladores por lógica
require("./routes/rusuarios.js")(app,swig,gestorBD); // (app, param1, param2, etc.)
require("./routes/rofertas.js")(app,swig,gestorBD); // (app, param1, param2, etc.)

app.listen(8081,function(){
    console.log('Servidor activo');
});