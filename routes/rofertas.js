module.exports = function(app, swig,gestorBD, logger) {
    app.get("/ofertas", function(req, res) {
        let criterio = {};
        if( req.query.busqueda != null ){
            criterio = { "titulo" : {$regex : ".*"+req.query.busqueda+".*"}  };
        }

        gestorBD.obtenerOfertas( criterio,function(ofertas) {
            if (ofertas == null) {
                res.send("Error al listar ");
            } else {
                let respuesta = swig.renderFile('views/ofertas.html',
                    {
                        ofertas : ofertas,
                        usuario: req.session.usuario
                    });
                res.send(respuesta);
            }
        });
    });



    app.get('/ofertas/agregar', function (req, res) {
        let respuesta = swig.renderFile('views/agregar.html', {

        });
        res.send(respuesta);
    });

    app.get("/ofertas/comprar/:id", function(req, res) {
        //       let ofertaId = gestorBD.mongo.ObjectID(req.params.id);
        //       let compra = {
        //       ofertaId : ofertaId
        //  }
        // gestorBD.insertarCompra(compra ,function(idCompra){
        //     if ( idCompra == null ){
        //        res.send(respuesta);
        //   } else {
        //        res.redirect("/ofertas.html");
        //   }
        //});
        res.redirect("/ofertas")

    });


    app.post("/oferta", function(req, res) {
        let oferta = {
            titulo : req.body.titulo,
            detalles : req.body.detalles,
            precio : req.body.precio,
            comprador: null
        }
        // Conectarse
        gestorBD.insertarOferta(oferta, function(id){
            if (id == null) {
                logger.info("Error al insertar oferta");
                res.send("Error al insertar la oferta");
            } else {
                logger.info("Agregada la Oferta ID: " + id);
                res.send("Agregada la Oferta ID: " + id);
            }
        });

    });
};
