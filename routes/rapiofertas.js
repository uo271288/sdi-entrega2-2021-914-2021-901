/**
 *  Módulo que gestiona las rutas de la API REST
 *
 * @param app
 * @param gestorBD
 * @param logger
 */
module.exports = function (app, gestorBD, logger) {

    /**
     * Muestra todas las ofertas que no pertenecen al usuario identificado
     */
    app.get("/api/oferta", function (req, res) {
        let criterio = {"autor.email": {$ne: res.usuario}};
        gestorBD.obtenerOfertas(criterio, function (ofertas) {
            if (ofertas == null) {
                logger.error("API: se ha producido un error al obtener ofertas")
                res.status(500);
                res.json({
                    error: "Se ha producido un error al obtener ofertas"
                })
            } else {
                logger.info("API: se han cargado las ofertas")
                res.status(200);
                res.send(JSON.stringify(ofertas));
            }
        });
    });

    /**
     * Permite autenticar un usuario
     */
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
                logger.error("API: ha ocurrido un error al autenticar al usuario.")
                res.status(401); //Unauthorized
                res.json({autenticado: false})
            } else {
                let token = app.get('jwt').sign({usuario: criterio.email, tiempo: Date.now() / 1000}, "secreto");
                res.status(200);
                logger.info("API: usuario autenticado")
                res.json({
                    autenticado: true,
                    token: token
                })
            }
        })
    });

    /**
     * Muestra la conversación de una oferta
     */
    app.get("/api/conversacion/:id", function (req, res) {
        let criterio = {"email": res.usuario};
        gestorBD.obtenerUsuarios(criterio, function (usuarios) {
            if (usuarios == null || usuarios.length == 0) {
                res.status(500);
                logger.error("API: se ha producido un error al obtener los usuarios")
                res.json({
                    error: "Se ha producido un error al obtener los usuarios"
                })
            } else {
                criterio = {"_id": gestorBD.mongo.ObjectID(req.params.id)}
                gestorBD.obtenerOfertas(criterio, function (ofertas) {
                    if (ofertas == null) {
                        res.status(500);
                        logger.error("API: se ha producido un error al obtener las ofertas")
                        res.json({
                            error: "Se ha producido un error al obtener las ofertas"
                        })
                    } else {
                        criterio = {
                            $or: [
                                {
                                    propietario: usuarios[0]._id.toString(),
                                    oferta: req.params.id.toString()
                                }, {
                                    interesado: usuarios[0]._id.toString(),
                                    oferta: req.params.id.toString()
                                }
                            ]
                        };
                        gestorBD.obtenerConversacion(criterio, function (conversacion) {
                            if (conversacion == null) {
                                res.status(500);
                                logger.error("API: se ha producido un error al obtener la conversacion")
                                res.json({
                                    error: "Se ha producido un error al obtener la conversacion"
                                })
                            } else {
                                res.status(200);
                                ç
                                logger.info("API: se ha cargado la conversación")
                                res.send(JSON.stringify(conversacion));
                            }
                        });
                    }
                });

            }
        })
    });

    /**
     * Permite crear y enviar mensajes a una conversación
     */
    app.post("/api/conversacion", function (req, res) {
        let criterio = {"email": res.usuario};
        gestorBD.obtenerUsuarios(criterio, function (usuarios) {
            if (usuarios == null || usuarios.length == 0) {
                res.status(500);
                logger.error("API: se ha producido un error al obtener los usuarios")
                res.json({
                    error: "Se ha producido un error al obtener los usuarios"
                })
            } else {
                criterio = {"_id": gestorBD.mongo.ObjectID(req.body.idOferta)}
                gestorBD.obtenerOfertas(criterio, function (ofertas) {
                    if (ofertas == null) {
                        res.status(500);
                        logger.error("API: se ha producido un error al obtener las ofertas")
                        res.json({
                            error: "Se ha producido un error al obtener las ofertas"
                        })
                    } else {
                        criterio = {
                            $or: [
                                {
                                    propietario: usuarios[0]._id.toString(),
                                    oferta: req.body.idOferta
                                }, {
                                    interesado: usuarios[0]._id.toString(),
                                    oferta: req.body.idOferta
                                }
                            ]
                        };
                        gestorBD.obtenerConversacion(criterio, function (conversacion) {
                            if (conversacion == null) {
                                res.status(500);
                                logger.error("API: se ha producido un error al obtener la conversacion")
                                res.json({
                                    error: "Se ha producido un error al obtener la conversacion"
                                })
                            } else {
                                if (conversacion.length > 0) {
                                    let conversacionNueva = {
                                        propietario: conversacion[0].propietario,
                                        interesado: conversacion[0].interesado,
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
                                            logger.error("API: se ha producido un error al insertar la conversacion")
                                            res.json({
                                                error: "Se ha producido un error al insertar la conversacion"
                                            })
                                        } else {
                                            res.status(201);
                                            logger.info("API: se ha insertado la conversación")
                                            res.json({
                                                mensaje: "Se ha insertado la conversación",
                                                _id: idConversacion
                                            })
                                        }
                                    })
                                } else {
                                    if (ofertas[0].autor._id !== usuarios[0]._id.toString()) {

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
                                                logger.error("API: se ha producido un error al insertar la conversacion")
                                                res.json({
                                                    error: "Se ha producido un error al insertar la conversacion"
                                                })
                                            } else {
                                                res.status(201);
                                                logger.info("API: se ha insertado la conversación")
                                                res.json({
                                                    mensaje: "Se ha insertado la conversación",
                                                    _id: idConversacion
                                                })
                                            }
                                        })
                                    } else {
                                        res.status(500);
                                        logger.warn("API: el usuario que inicia la conversacion no puede ser el autor de la oferta");
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