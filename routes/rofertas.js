module.exports = function(app, swig,gestorBD) {
    app.get("/ofertas", function(req, res) {
        let ofertas = [{
            "nombre" : "Coche de Golf",
            "precio" : "100"
        }]

        let respuesta = swig.renderFile('views/ofertas.html',{
            vendedor: 'Tienda de ofertas',
            ofertas: ofertas
        });

        res.send(respuesta);
    });

    app.get('/ofertas/agregar', function (req, res) {
        let respuesta = swig.renderFile('views/agregar.html', {

        });
        res.send(respuesta);
    });

    app.post("/oferta", function(req, res) {
        let oferta = {
            titulo : req.body.titulo,
            detalles : req.body.detalles,
            precio : req.body.precio
        }
        // Conectarse
        gestorBD.insertarOferta(oferta, function(id){
            if (id == null) {
                res.send("Error al insertar la oferta");
            } else {
                res.send("Agregada la Oferta ID: " + id);
            }
        });

    });
};
