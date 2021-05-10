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
        let criterio = {"autor.email": {$ne: res.usuario}};
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
                        criterio = {oferta : gestorBD.mongo.ObjectID(req.params.id)
                        };
                        gestorBD.obtenerConversacion(criterio, function(conversacion) {
                            if (ofertas == null) {
                                res.status(500);
                                res.json({
                                    error : "se ha producido un error"
                                })
                            } else {
                                logger.info(conversacion.length);
                                logger.info(conversacion[0].mensajes[0].mensaje);
                                res.status(200);
                                res.send( JSON.stringify(conversacion) );
                            }
                        });
                    }
                });

            }
        })
    });

    app.post("/api/conversacion", function(req, res) {
        let criterio = {"autor.email": {$ne: res.usuario}};
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
                        logger.info(gestorBD.mongo.ObjectID(req.body.idOferta));
                        let conversacion = {
                            propietario : gestorBD.mongo.ObjectID(ofertas[0].autor._id),
                            interesado : usuarios[0]._id,
                            oferta: gestorBD.mongo.ObjectID(req.body.idOferta),
                            mensajes: [
                                {
                                    autor: usuarios[0]._id,
                                    hora: new Date(Date.now()),
                                    mensaje: req.body.mensaje
                                }
                            ]
                        };
                        gestorBD.insertarConversacion(conversacion ,function(idConversacion) {
                            if (idConversacion == null) {
                                res.status(500);
                                res.json({
                                    error: "se ha producido un error"
                                })
                            } else {
                                res.status(201);
                                res.json({
                                    mensaje: "conversacion insertada",
                                    _id: idConversacion
                                })
                            }
                        })
                    }
                });
            }
        });
    });
}
