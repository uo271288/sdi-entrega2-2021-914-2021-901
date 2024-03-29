/**
 * Módulo que gestiona las rutas de la entidad oferta
 *
 * @param app
 * @param swig
 * @param gestorBD
 * @param logger
 */
module.exports = function (app, swig, gestorBD, logger) {

    /**
     * Función que sirve para restar una cierta cantidad de dinero al saldo de un usuario
     *
     * @param importe
     * @param req
     * @param res
     */
    function restar(importe, req, res) {
        let criterio = {"_id": gestorBD.mongo.ObjectID(req.session.usuario._id)};
        req.session.usuario.balance = req.session.usuario.balance - importe;
        let usuario = {
            email: req.session.usuario.email,
            name: req.session.usuario.name,
            surname: req.session.usuario.surname,
            password: req.session.usuario.password,
            balance: req.session.usuario.balance,
            role: req.session.usuario.role
        }
        gestorBD.modificarUsuario(criterio, usuario, function (result) {
            if (result == null) {
                logger.error("Error 500: Error con la base de datos")
                let respuesta = swig.renderFile('views/error.html',
                    {
                        usuario: req.session.usuario,
                        numeroError: 500,
                        mensaje: "Error al modificar usuario de la base de datos"
                    });
                res.send(respuesta);
            }
        });
    }

    /**
     * Muestra las ofertas para un usuario. Divide el listado en páginas
     */
    app.get("/ofertas", function (req, res) {
        let criterio = {"autor.email": {$ne: req.session.usuario.email}};
        if (req.query.busqueda != null) {
            req.session.busqueda = req.query.busqueda;
            criterio = {"titulo": {$regex: ".*" + req.session.busqueda.toLowerCase() + ".*"}};
        }
        let pg = parseInt(req.query.pg); // Es String !!!
        if (req.query.pg == null) {
            pg = 1;
        }
        gestorBD.obtenerOfertasPg(criterio, pg, function (ofertas, total) {
            if (ofertas == null) {
                logger.error("Error 500: Error con la base de datos")
                let respuesta = swig.renderFile('views/error.html',
                    {
                        usuario: req.session.usuario,
                        numeroError: 500,
                        mensaje: "Error al listar ofertas de la base de datos"
                    });
                res.send(respuesta);
            } else {
                let ultimaPg = total / 5;
                if (total % 5 > 0) { // Sobran decimales
                    ultimaPg = ultimaPg + 1;
                }
                if (pg > ultimaPg && ofertas.length > 0) {
                    let respuesta = swig.renderFile('views/error.html',
                        {
                            usuario: req.session.usuario,
                            numeroError: 404,
                            mensaje: "La pagina " + pg + "no existe para la busqueda " + req.session.busqueda
                        });
                    logger.error("Error 404: La pagina " + pg + "no existe para la busqueda " + req.session.busqueda);
                    res.send(respuesta);
                } else {
                    let paginas = []; // paginas mostrar
                    for (let i = pg - 2; i <= pg + 2; i++) {
                        if (i > 0 && i <= ultimaPg) {
                            paginas.push(i);
                        }
                    }
                    if (typeof (req.session.errores) != "undefined") {
                        let respuesta = swig.renderFile('views/ofertas.html',
                            {
                                tipo: req.session.errores.tipo,
                                mensaje: req.session.errores.mensaje,
                                ofertas: ofertas,
                                usuario: req.session.usuario,
                                paginas: paginas,
                                actual: pg,
                                busqueda: req.session.busqueda
                            });
                        logger.info("El usuario " + req.session.usuario.email + " ha accedido a la vista de ofertas a la pagina " + pg);
                        delete req.session.errores;
                        res.send(respuesta);
                    } else {
                        let respuesta = swig.renderFile('views/ofertas.html',
                            {
                                ofertas: ofertas,
                                usuario: req.session.usuario,
                                paginas: paginas,
                                actual: pg,
                                busqueda: req.session.busqueda
                            });
                        logger.info("El usuario " + req.session.usuario.email + " ha accedido a la vista de ofertas a la pagina " + pg);
                        res.send(respuesta);
                    }
                }
            }
        });
    });

    /**
     * Muestra el formulario para agregar una oferta
     */
    app.get('/ofertas/agregar', function (req, res) {
        if (typeof (req.session.errores) != "undefined") {
            let respuesta = swig.renderFile('views/agregar.html', {
                tipo: req.session.errores.tipo,
                mensaje: req.session.errores.mensaje,
                usuario: req.session.usuario
            });
            logger.info("El usuario " + req.session.usuario.email + " ha accedido a la vista de agregar ofertas");
            res.send(respuesta);
        } else {
            let respuesta = swig.renderFile('views/agregar.html', {
                usuario: req.session.usuario
            });
            logger.info("El usuario " + req.session.usuario.email + " ha accedido a la vista de agregar ofertas");
            res.send(respuesta);
        }

    });

    /**
     * Permite comprar una oferta
     */
    app.get("/ofertas/comprar/:id", function (req, res) {
        let ofertaId = gestorBD.mongo.ObjectID(req.params.id);
        let compra = {
            usuario: req.session.usuario,
            ofertaId: ofertaId
        }
        gestorBD.insertarCompra(compra, function (idCompra) {
            if (idCompra == null) {
                logger.error("Error 500: Error con la base de datos")
                let respuesta = swig.renderFile('views/error.html',
                    {
                        usuario: req.session.usuario,
                        numeroError: 500,
                        mensaje: "Error al insertar la compra de la base de datos"
                    });
                res.send(respuesta);
            } else {
                let criterio = {"_id": gestorBD.mongo.ObjectID(ofertaId)};
                gestorBD.obtenerOfertas(criterio, function (ofertas) {
                    if (ofertas == null) {
                        logger.error("Error 500: Error con la base de datos")
                        let respuesta = swig.renderFile('views/error.html',
                            {
                                usuario: req.session.usuario,
                                numeroError: 500,
                                mensaje: "Error al listar ofertas de la base de datos"
                            });
                        res.send(respuesta);
                    } else {

                        let oferta = {
                            titulo: ofertas[0].titulo,
                            detalles: ofertas[0].detalles,
                            precio: ofertas[0].precio,
                            autor: ofertas[0].autor,
                            hora: ofertas[0].hora,
                            destacada: ofertas[0].destacada,
                            comprador: req.session.usuario
                        }
                        if (req.session.usuario.balance - oferta.precio >= 0) {

                            gestorBD.modificarOferta(criterio, oferta, function (result) {
                                if (result == null) {
                                    logger.error("Error 500: Error con la base de datos")
                                    let respuesta = swig.renderFile('views/error.html',
                                        {
                                            usuario: req.session.usuario,
                                            numeroError: 500,
                                            mensaje: "Error al modificar ofertas de la base de datos"
                                        });
                                    res.send(respuesta);
                                } else {
                                    let criterio = {"_id": gestorBD.mongo.ObjectID(req.session.usuario._id)};
                                    req.session.usuario.balance = req.session.usuario.balance - oferta.precio;
                                    let usuario = {
                                        email: req.session.usuario.email,
                                        name: req.session.usuario.name,
                                        surname: req.session.usuario.surname,
                                        password: req.session.usuario.password,
                                        balance: req.session.usuario.balance,
                                        role: req.session.usuario.role
                                    }
                                    gestorBD.modificarUsuario(criterio, usuario, function (result) {
                                        if (result == null) {
                                            logger.error("Error 500: Error con la base de datos")
                                            let respuesta = swig.renderFile('views/error.html',
                                                {
                                                    usuario: req.session.usuario,
                                                    numeroError: 500,
                                                    mensaje: "Error al modificar usuario de la base de datos"
                                                });
                                            res.send(respuesta);
                                        } else {
                                            logger.info("El usuario " + req.session.usuario.email + " ha comprado la oferta " + ofertas[0]._id.toString())
                                            res.redirect("/ofertas");
                                        }
                                    });

                                }
                            });
                        } else {
                            req.session.errores = {
                                mensaje: "Saldo insuficiente",
                                tipo: "alert alert-danger"
                            }
                            logger.error("Saldo insuficiente para comprar una oferta.");
                            res.redirect("/ofertas");
                        }

                    }
                });
            }
        });
    });

    /**
     * Permite agregar una oferta
     */
    app.post("/oferta", function (req, res) {
        let destacada = false;
        if (req.body.destacada)
            destacada = true;
        let oferta = {
            titulo: req.body.titulo.toLowerCase(),
            detalles: req.body.detalles,
            precio: req.body.precio,
            autor: req.session.usuario,
            hora: new Date(Date.now()),
            destacada: destacada,
            comprador: null
        }

        if (oferta.titulo.length === 0 || oferta.detalles.length === 0) {
            req.session.errores = {
                mensaje: "El titulo o la descripcion no pueden estar vacios",
                tipo: "alert alert-danger"
            }
            logger.error("Error, el titulo o la descripcion no pueden estar vacios");
            res.redirect("/ofertas/agregar");
        } else {
            if (oferta.precio < 0) {
                req.session.errores = {
                    mensaje: "El precio no puede ser negativo",
                    tipo: "alert alert-danger"
                }
                logger.error("Error, el precio no puede ser negativo");
                res.redirect("/ofertas/agregar");
            } else {
                // Conectarse
                gestorBD.insertarOferta(oferta, function (id) {
                    if (id == null) {
                        logger.error("Error 500: Error con la base de datos")
                        let respuesta = swig.renderFile('views/error.html',
                            {
                                usuario: req.session.usuario,
                                numeroError: 500,
                                mensaje: "Error al modificar usuario de la base de datos"
                            });
                        res.send(respuesta);
                    } else {
                        if (destacada) {
                            restar(20, req, res)
                        }
                        logger.info("Agregada la Oferta ID: " + id);
                        res.redirect("/ofertas/misofertas");
                    }
                });
            }
        }

    });

    /**
     * Muestra la lista de ofertas compradas
     */
    app.get("/compras", function (req, res) {
        let criterio = {"usuario._id": req.session.usuario._id};

        gestorBD.obtenerCompras(criterio, function (compras) {
            if (compras == null) {
                logger.error("Error 500: Error con la base de datos")
                let respuesta = swig.renderFile('views/error.html',
                    {
                        usuario: req.session.usuario,
                        numeroError: 500,
                        mensaje: "Error al listar compras de la base de datos"
                    });
                res.send(respuesta);
            } else {
                let ofertasCompradasIds = [];

                for (let i = 0; i < compras.length; i++) {
                    ofertasCompradasIds.push(compras[i].ofertaId);
                }
                let criterio = {"_id": {$in: ofertasCompradasIds}}
                gestorBD.obtenerOfertas(criterio, function (ofertas) {
                    if (ofertas == null) {
                        logger.error("Error 500: Error con la base de datos")
                        let respuesta = swig.renderFile('views/error.html',
                            {
                                usuario: req.session.usuario,
                                numeroError: 500,
                                mensaje: "Error al listar ofertas de la base de datos"
                            });
                        res.send(respuesta);
                    } else {
                        let respuesta = swig.renderFile('views/compras.html', {
                            ofertas: ofertas,
                            usuario: req.session.usuario
                        });
                        logger.info("El usuario " + req.session.usuario.email + " ha accedido a la vista de compras");
                        res.send(respuesta);
                    }
                })
            }
        })
    });

    /**
     * Muestra la lista de las ofertas del usuario
     */
    app.get("/ofertas/misofertas", function (req, res) {
            let criterio = {"autor._id": req.session.usuario._id};

            gestorBD.obtenerOfertas(criterio, function (ofertas) {
                if (ofertas == null) {
                    logger.error("Error 500: Error con la base de datos")
                    let respuesta = swig.renderFile('views/error.html',
                        {
                            usuario: req.session.usuario,
                            numeroError: 500,
                            mensaje: "Error al listar ofertas de la base de datos"
                        });
                    res.send(respuesta);
                } else {
                    if (typeof (req.session.errores) != "undefined") {
                        let respuesta = swig.renderFile('views/misofertas.html',
                            {
                                mensaje: req.session.errores.mensaje,
                                tipo: req.session.errores.tipo,
                                ofertas: ofertas,
                                usuario: req.session.usuario
                            });

                        delete req.session.errores;
                        res.send(respuesta);
                    } else {
                        let respuesta = swig.renderFile('views/misofertas.html', {
                            ofertas: ofertas,
                            usuario: req.session.usuario
                        });
                        logger.info("El usuario " + req.session.usuario.email + "ha accedido a la vista de Mis Ofertas");
                        res.send(respuesta);
                    }
                }
            });
        }
    )
    ;

    /**
     * Permite eliminar una oferta
     */
    app.get('/ofertas/eliminar/:id', function (req, res) {
        let criterio = {"_id": gestorBD.mongo.ObjectID(req.params.id)};
        gestorBD.eliminarOferta(criterio, function (ofertas) {
            if (ofertas == null) {
                logger.error("Error al eliminar la oferta")
                let respuesta = swig.renderFile('views/error.html',
                    {
                        usuario: req.session.usuario,
                        numeroError: 500,
                        mensaje: "Error al eliminar la oferta de la base de datos"
                    });
                res.send(respuesta);
            } else {
                logger.info("El usuario " + req.session.usuario.email + " ha sido redireccionado a la vista de Mis Ofertas");
                res.redirect("/ofertas/misofertas");
            }
        });
    });

    /**
     * Permite destacar una oferta
     */
    app.get('/ofertas/destacar/:id', function (req, res) {
        let criterio = {"_id": gestorBD.mongo.ObjectID(req.params.id)};
        if (req.session.usuario.balance - 20 >= 0) {
            // En caso de que el usuario tenga saldo suficiente
            gestorBD.obtenerOfertas(criterio, function (ofertas) {
                if (ofertas == null) {
                    logger.error("Error al obtener las ofertas")
                    let respuesta = swig.renderFile('views/error.html',
                        {
                            usuario: req.session.usuario,
                            numeroError: 500,
                            mensaje: "Error al listar ofertas de la base de datos"
                        });
                    res.send(respuesta);
                } else {
                    logger.info("El usuario " + req.session.usuario.email + " ha accedido a la vista de compras");
                }
                let oferta = {
                    titulo: ofertas[0].titulo,
                    detalles: ofertas[0].detalles,
                    precio: ofertas[0].precio,
                    autor: ofertas[0].autor,
                    hora: ofertas[0].hora,
                    destacada: true,
                    comprador: ofertas[0].comprador
                }
                gestorBD.modificarOferta(criterio, oferta, function (result) {
                    if (result == null) {
                        logger.error("Error 500: Error con la base de datos")
                        let respuesta = swig.renderFile('views/error.html',
                            {
                                usuario: req.session.usuario,
                                numeroError: 500,
                                mensaje: "Error al modificar ofertas de la base de datos"
                            });
                        res.send(respuesta);
                    } else {
                        restar(20, req, res)
                        logger.info("Oferta " + ofertas[0]._id.toString() + " destacada")
                        res.redirect("/ofertas/misofertas");
                    }
                });
            })
        } else {
            // En caso contrario se le muestra un error
            req.session.errores = {
                mensaje: "Saldo insuficiente",
                tipo: "alert alert-danger"
            }
            logger.error("Saldo insuficiente para comprar una oferta.");
            res.redirect("/ofertas/misofertas");
        }
    });
}
;
