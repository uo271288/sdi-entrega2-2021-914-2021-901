module.exports = function(app, gestorBD,logger) {

    app.get("/api/oferta", function(req, res) {
        let criterio = {"autor.email": {$ne: res.usuario}};
        gestorBD.obtenerOfertas(criterio, function(ofertas) {
            if (ofertas == null) {
                res.status(500);
                res.json({
                    error : "se ha producido un error"
                })
            } else {
                res.status(200);
                res.send( JSON.stringify(ofertas) );
            }
        });
    });

    app.post("/api/autenticar/", function (req, res) {
        let seguro = app.get("crypto")
            .createHmac('sha256', app.get('clave'))
            .update(req.body.password).digest('hex');
        let criterio = {
            email: req.body.email,
            password: seguro
        }
        gestorBD.obtenerUsuarios(criterio, function (usuarios) {
            if (usuarios == null || usuarios.length == 0) {
                res.status(401); //Unauthorized
                res.json({autenticado: false})
            } else {
                let token = app.get('jwt').sign({usuario: criterio.email, tiempo: Date.now() / 1000}, "secreto");
                res.status(200);
                res.json({
                    autenticado: true,
                    token: token
                })
            }
        })
    });

    app.get("/api/conversacion/:id", function(req, res) {
        let criterio = {"email": res.usuario};
        gestorBD.obtenerUsuarios(criterio, function (usuarios) {
            if (usuarios == null || usuarios.length == 0) {
                res.status(500);
                res.json({
                    error : "se ha producido un error"
                })
            } else {
                criterio = { "_id" : gestorBD.mongo.ObjectID(req.params.id)}
                gestorBD.obtenerOfertas(criterio, function(ofertas) {
                    if (ofertas == null) {
                        res.status(500);
                        res.json({
                            error : "se ha producido un error"
                        })
                    } else {
                        logger.info(usuarios[0]._id.toString());
                        logger.info("oferta:" + req.params.id.toString());
                        criterio = {$or: [
                                {
                                    propietario : usuarios[0]._id.toString(),
                                    oferta : req.params.id.toString()
                                },{
                                    interesado : usuarios[0]._id.toString(),
                                    oferta : req.params.id.toString()
                                }
                            ]
                        };
                        gestorBD.obtenerConversacion(criterio, function(conversacion) {
                            if (conversacion == null) {
                                res.status(500);
                                res.json({
                                    error : "se ha producido un error"
                                })
                            } else {
                                res.status(200);
                                logger.info(conversacion.length);
                                res.send( JSON.stringify(conversacion) );
                            }
                        });
                    }
                });

            }
        })
    });

    app.post("/api/conversacion", function(req, res) {
        let criterio = {"email": res.usuario};
        gestorBD.obtenerUsuarios(criterio, function (usuarios) {
            if (usuarios == null || usuarios.length == 0) {
                res.status(500);
                res.json({
                    error : "se ha producido un error"
                })
            } else {
                criterio = { "_id" : gestorBD.mongo.ObjectID(req.body.idOferta)}
                gestorBD.obtenerOfertas(criterio, function(ofertas) {
                    if (ofertas == null) {
                        res.status(500);
                        res.json({
                            error : "se ha producido un error"
                        })
                    } else {
                        logger.info(req.body.idOferta);
                        criterio = {$or: [
                                {
                                    propietario : usuarios[0]._id.toString(),
                                    oferta : req.body.idOferta
                                },{
                                    interesado : usuarios[0]._id.toString(),
                                    oferta : req.body.idOferta
                                }
                            ]
                        };
                        gestorBD.obtenerConversacion(criterio, function(conversacion) {
                            if (conversacion == null) {
                                res.status(500);
                                res.json({
                                    error : "se ha producido un error"
                                })
                            } else {

                                if(conversacion.length>0){
                                    let conversacionNueva = {
                                        propietario : conversacion[0].propietario,
                                        interesado : conversacion[0].interesado,
                                        oferta: req.body.idOferta,
                                        mensaje:
                                            {
                                                autor: res.usuario,
                                                hora: new Date(Date.now()),
                                                mensaje: req.body.mensaje,
                                                leido: false
                                            }
                                    };
                                    gestorBD.insertarConversacion(conversacionNueva ,function(idConversacion) {
                                        if (idConversacion == null) {
                                            res.status(500);
                                            logger.info("se ha producido un error");
                                            res.json({
                                                error: "se ha producido un error"
                                            })
                                        } else {
                                            res.status(201);
                                            logger.info("conversacion insertada");
                                            res.json({
                                                mensaje: "conversacion insertada",
                                                _id: idConversacion
                                            })
                                        }
                                    })
                                }else {
                                    logger.info(ofertas[0].autor._id);
                                    logger.info(usuarios[0]._id.toString());

                                    if(ofertas[0].autor._id !== usuarios[0]._id.toString()) {

                                        let conversacionNueva = {
                                            propietario: ofertas[0].autor._id,
                                            interesado: usuarios[0]._id.toString(),
                                            oferta: req.body.idOferta,
                                            mensaje:
                                                {
                                                    autor: res.usuario,
                                                    hora: new Date(Date.now()),
                                                    mensaje: req.body.mensaje,
                                                    leido: false
                                                }
                                        };
                                        gestorBD.insertarConversacion(conversacionNueva, function (idConversacion) {
                                            if (idConversacion == null) {
                                                res.status(500);
                                                logger.info("se ha producido un error");
                                                res.json({
                                                    error: "se ha producido un error"
                                                })
                                            } else {
                                                res.status(201);
                                                logger.info("conversacion insertada");
                                                res.json({
                                                    mensaje: "conversacion insertada",
                                                    _id: idConversacion
                                                })
                                            }
                                        })
                                    }else{
                                        res.status(500);
                                        logger.info("El usuario que inicia la conversacion no puede ser el autor de la oferta");
                                        res.json({
                                            error: "El usuario que inicia la conversacion no puede ser el autor de la oferta"
                                        })
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
    });
}
