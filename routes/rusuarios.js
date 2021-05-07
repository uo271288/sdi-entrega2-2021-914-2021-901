/**
 * Módulo que gestiona las rutas de la entidad usuario
 *
 * @param app
 * @param swig
 * @param gestorBD
 * @param logger
 */
module.exports = function (app, swig, gestorBD, logger) {
    app.get("/usuarios", function (req, res) {
        let criterio = {email: {$ne: "admin@email.com"}};
        gestorBD.obtenerUsuarios(criterio, function (usuarios) {
            if (typeof (req.session.errores) != "undefined") {
                let respuesta = swig.renderFile('views/usuarios.html', {
                    mensaje: req.session.errores.mensaje,
                    tipo: req.session.errores.tipo,
                    usuarios: usuarios,
                    usuario: req.session.usuario
                });
                delete req.session.errores;
                res.send(respuesta);
            } else {
                let respuesta = swig.renderFile('views/usuarios.html', {
                    usuarios: usuarios,
                    usuario: req.session.usuario
                });
                logger.info("Admin ha accedido a la lista de usuarios del sistema");
                res.send(respuesta);
            }
        });
    });

    app.post('/usuarios/eliminar', function (req, res) {
        let criterio;
        let usuarios = req.body.usuariosEliminar;
        console.log(usuarios);
        if (typeof (usuarios) != 'undefined') {
            if (typeof (usuarios) == "string") {
                criterio = {email: usuarios};
            } else {
                criterio = {email: {$in: usuarios}};
            }
            gestorBD.eliminarUsuarios(criterio, function (result) {
                if (result == null) {
                    logger.error("Error al eliminar usuarios");
                    res.send("Error al eliminar usuarios");
                } else {
                    logger.info("Se han eliminado los usuarios " + usuarios + " correctamente");
                    res.redirect("/usuarios");
                }
            });
        } else {
            logger.error("No se han seleccionado usuarios para eliminar");
            req.session.errores = {
                mensaje: "Seleccione al menos un usuario para eliminar.",
                tipo: "alert alert-danger"
            }
            res.redirect("/usuarios");
        }
    });

    app.get("/registrarse", function (req, res) {
        let respuesta;
        if (typeof (req.session.errores) != "undefined") {
            respuesta = swig.renderFile('views/registro.html', {
                mensaje: req.session.errores.mensaje,
                tipo: req.session.errores.tipo
            });
            delete req.session.errores;
        } else {
            respuesta = swig.renderFile('views/registro.html', {});
        }
        res.send(respuesta);
    });

    app.post('/registrarse', function (req, res) {
        let criterio = {email: req.body.email};
        gestorBD.obtenerUsuarios(criterio, function (usuarios) {
            if (usuarios.length !== 0) {
                logger.error("Error de registro. El usuario " + req.body.email + " ya está registrado");
                req.session.errores = {
                    mensaje: "El usuario ya está registrado.",
                    tipo: "alert alert-danger"
                }
                res.redirect("/registrarse");
            } else {
                if (req.body.passwordRepeat !== req.body.password) {
                    req.session.errores = {
                        mensaje: "Las contraseñas no coinciden.",
                        tipo: "alert alert-danger"
                    }
                    logger.error("Error de registro. Las contraseñas no coinciden.");
                    res.redirect("/registrarse");
                } else {
                    let seguro = app.get("crypto").createHmac('sha256', app.get('clave'))
                        .update(req.body.password).digest('hex');
                    let usuario = {
                        email: req.body.email,
                        name: req.body.name,
                        surname: req.body.surname,
                        password: seguro,
                        balance: 100.0,
                        role: "standard"
                    }

                    gestorBD.insertarUsuario(usuario, function (id) {
                        if (id == null) {
                            req.session.errores = {
                                mensaje: "Error al registrar usuario",
                                tipo: "alert alert-danger"
                            }
                            logger.error("Error de registro.");
                            res.redirect("/registrarse");
                        } else {
                            req.session.errores = {
                                mensaje: "Nuevo usuario registrado",
                                tipo: "alert alert-success"
                            }
                            logger.info("Usuario " + usuario.email + " registrado");
                            res.redirect("/registrarse");
                        }
                    });
                }
            }
        })
    });

    app.get("/identificarse", function (req, res) {
        let respuesta;
        if (typeof (req.session.errores) != "undefined") {

            respuesta = swig.renderFile('views/identificacion.html', {
                mensaje: req.session.errores.mensaje,
                tipo: req.session.errores.tipo
            });
            delete req.session.errores;
        } else
            respuesta = swig.renderFile('views/identificacion.html', {});
        res.send(respuesta);
    });

    app.post("/identificarse", function (req, res) {
        let seguro = app.get("crypto").createHmac('sha256', app.get('clave'))
            .update(req.body.password).digest('hex');
        let criterio = {
            email: req.body.email,
            password: seguro
        }
        gestorBD.obtenerUsuarios(criterio, function (usuarios) {
            if (usuarios == null || usuarios.length === 0) {
                req.session.usuario = null;
                req.session.errores = {
                    mensaje: "Email o password incorrecto",
                    tipo: "alert alert-danger"
                }
                logger.error("Error de inicio de sesión.");
                res.redirect("/identificarse");
            } else {
                req.session.usuario = usuarios[0];
                if (usuarios[0].role === "admin") {
                    logger.info("El administrador ha iniciado sesión");
                    res.redirect("/usuarios");
                } else {
                    logger.info("El usuario " + usuarios[0].email + " ha iniciado sesión");
                    res.redirect("/ofertas");
                }
            }
        });
    });

    app.get('/desconectarse', function (req, res) {
        logger.info(req.session.usuario.email + " desconectado");
        req.session.usuario = null;
        res.redirect("/identificarse");
    });
};