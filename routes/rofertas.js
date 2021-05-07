module.exports = function(app, swig,gestorBD, logger) {
    app.get("/ofertas", function(req, res) {
        let criterio = {};
        if( req.query.busqueda != null ){
            req.session.busqueda = req.query.busqueda;
            criterio = { "titulo" : {$regex : ".*"+req.session.busqueda+".*"}  };
        }
        let pg = parseInt(req.query.pg); // Es String !!!
        if ( req.query.pg == null){
            pg = 1;
        }
        gestorBD.obtenerOfertasPg(criterio, pg , function(ofertas, total ) {
            if (ofertas == null) {
                res.send("Error al listar ofertas");
            } else {
                let ultimaPg = total/5;
                if (total % 5 > 0 ){ // Sobran decimales
                    ultimaPg = ultimaPg+1;
                }
                let paginas = []; // paginas mostrar
                for(let i = pg-2 ; i <= pg+2 ; i++){
                    if ( i > 0 && i <= ultimaPg){
                        paginas.push(i);
                    }
                }
                let respuesta = swig.renderFile('views/ofertas.html',
                    {
                        ofertas : ofertas,
                        usuario: req.session.usuario,
                        paginas : paginas,
                        actual : pg,
                        busqueda: req.session.busqueda
                    });
                res.send(respuesta);
            }
        });
    });

    app.get('/ofertas/agregar', function (req, res) {
        let respuesta = swig.renderFile('views/agregar.html', {
            usuario: req.session.usuario
        });
        res.send(respuesta);
    });

    app.get("/ofertas/comprar/:id", function(req, res) {
        let ofertaId = gestorBD.mongo.ObjectID(req.params.id);
               let compra = {
                  usuario : req.session.usuario,
                  ofertaId : ofertaId
               }
                gestorBD.insertarCompra(compra ,function(idCompra){
                if ( idCompra == null ){
                    res.send(respuesta);
                } else {
                    let criterio = { "_id" : gestorBD.mongo.ObjectID(ofertaId) };
                    gestorBD.obtenerOfertas(criterio,function(ofertas) {
                        if (ofertas == null) {
                            res.send("Error al listar ofertas ");
                        } else {
                            let oferta = {
                                titulo : ofertas[0].titulo,
                                detalles : ofertas[0].detalles,
                                precio : ofertas[0].precio,
                                autor: ofertas[0].autor,
                                hora : ofertas[0].hora,
                                comprador: req.session.usuario
                            }
                            gestorBD.modificarOferta(criterio, oferta, function(result) {
                                if (result == null) {
                                    res.send("Error al modificar ");
                                } else {
                                    let criterio = { "_id" : gestorBD.mongo.ObjectID(req.session.usuario._id)};
                                    if(req.session.usuario.balance - oferta.precio >= 0){
                                        req.session.usuario.balance = req.session.usuario.balance - oferta.precio;
                                    }else{

                                    }
                                    let usuario = {
                                        email:req.session.usuario.email,
                                        name:req.session.usuario.name,
                                        surname:req.session.usuario.surname,
                                        password:req.session.usuario.password,
                                        balance:req.session.usuario.balance,
                                        role:req.session.usuario.role
                                    }
                                    gestorBD.modificarUsuario(criterio,usuario, function(result) {
                                        if (result == null) {
                                            res.send("Error al modificar ");
                                        } else {
                                            logger.info(criterio._id+"");
                                            res.redirect("/ofertas");
                                        }
                                    });
                                }
                            });


                        }
                    });
                }
        });
    });


    app.post("/oferta", function(req, res) {
        let oferta = {
            titulo : req.body.titulo,
            detalles : req.body.detalles,
            precio : req.body.precio,
            autor: req.session.usuario,
            hora : new Date(Date.now()),
            comprador: null
        }
        // Conectarse
        gestorBD.insertarOferta(oferta, function(id){
            if (id == null) {
                logger.info("Error al insertar oferta");
                res.send("Error al insertar la oferta");
            } else {
                logger.info("Agregada la Oferta ID: " + id);
                res.redirect("/ofertas/misofertas");
            }
        });

    });

    app.get("/compras", function (req,res){
       let criterio = {"usuario" : req.session.usuario};

       gestorBD.obtenerCompras(criterio,function (compras) {
           if(compras == null){
               res.send("Error al listar");
           }else{
               let ofertasCompradasIds=[];
               for(i=0; i<compras.length;i++){
                   ofertasCompradasIds.push(compras[i].ofertaId);
               }

               let criterio = {"_id": {$in: ofertasCompradasIds}}
               gestorBD.obtenerOfertas(criterio, function(ofertas){
                   let respuesta = swig.renderFile('views/compras.html',{
                     ofertas : ofertas,
                       usuario: req.session.usuario
                   });
                   res.send(respuesta);
               })
           }
       })
    });

    app.get("/ofertas/misofertas", function(req, res) {
        let criterio = {"autor" : req.session.usuario};

        gestorBD.obtenerOfertas( criterio,function(ofertas) {
            if (ofertas == null) {
                res.send("Error al listar ofertas ");
            } else {
                let respuesta = swig.renderFile('views/misofertas.html',
                    {
                        ofertas : ofertas,
                        usuario: req.session.usuario
                    });
                res.send(respuesta);
            }
        });
    });
    app.get('/ofertas/eliminar/:id', function (req, res) {
        let criterio = {"_id" : gestorBD.mongo.ObjectID(req.params.id) };
        gestorBD.eliminarOferta(criterio,function(ofertas){
            if ( ofertas == null ){
                res.send(respuesta);
            } else {
                res.redirect("/ofertas/misofertas");
            }
        });
    });

};
