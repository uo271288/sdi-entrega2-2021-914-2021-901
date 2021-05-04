//Módulos
let express = require('express');
let app = express();

let swig = require('swig');
let bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));


app.use(express.static('public'));

// Variables
app.set('port', 8090);

//Rutas/controladores por lógica
require("./routes/rusuarios.js")(app,swig); // (app, param1, param2, etc.)
require("./routes/rofertas.js")(app,swig); // (app, param1, param2, etc.)

app.listen(8081,function(){
    console.log('Servidor activo');
});