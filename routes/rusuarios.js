/**
 * Módulo que gestiona las rutas de la entidad usuario
 *
 * @param app
 * @param swig
 * @param gestorBD Gestor de la base de datos
 * @param logger Logger de la aplicación
 */
module.exports = function (app, swig, gestorBD, logger) {

    /**
     * Muestra una vista que contiene la lista de usuarios
     */
    app.get("/usuarios", function (req, res) {
        let criterio = {email: {$ne: "admin@email.com"}}; // $ne sirve para seleccionar todos aquellos elementos que no
                                                          // coinciden con un elemento dado
        gestorBD.obtenerUsuarios(criterio, function (usuarios) {
            if (typeof (req.session.errores) != "undefined") {
                // En caso de que exista algún error se le pasa a la vista su mensaje y tipo, además de los
                // usuarios y el usuario que está loggeado
                let respuesta = swig.renderFile('views/usuarios.html', {
                    mensaje: req.session.errores.mensaje,
                    tipo: req.session.errores.tipo,
                    usuarios: usuarios,
                    usuario: req.session.usuario
                });
                // Se borran los errores de la sesión para que no se repitan en otras vistas o al recargar la página
                delete req.session.errores;
                res.send(respuesta);
            } else {
                // En caso contrario se pasan los usuarios y el usuario loggeado
                let respuesta = swig.renderFile('views/usuarios.html', {
                    usuarios: usuarios,
                    usuario: req.session.usuario
                });
                logger.info("Admin ha accedido a la lista de usuarios del sistema");
                res.send(respuesta);
            }
        });
    });

    /**
     * Recoge los datos de la lista de usuarios y borra aquellos que cuyo combobox ha sido seleccionado
     */
    app.post('/usuarios/eliminar', function (req, res) {
        let criterio;
        let usuarios = req.body.usuariosEliminar;
        if (typeof (usuarios) != 'undefined') {
            if (typeof (usuarios) == "string") {
                // Si existe un único usuario se le pasa este para borrarlo
                criterio = {email: usuarios};
            } else {
                // En caso de que halla más $in sirve para saber si un elemento se encuentra dentro de un array
                criterio = {email: {$in: usuarios}};
            }
            gestorBD.eliminarUsuarios(criterio, function (result) {
                if (result == null) {
                    let respuesta = swig.renderFile('views/error.html',
                        {
                            usuario: req.session.usuario,
                            numeroError: 500,
                            mensaje: "Error al eliminar usuarios"
                        });
                    logger.error("Error al eliminar usuarios");
                    res.send(respuesta);
                } else {
                    let criterio;
                    if (typeof (usuarios) == "string") {
                        criterio = {"autor.email": usuarios};
                    } else {
                        criterio = {"autor.email": {$in: usuarios}};
                    }
                    gestorBD.eliminarOferta(criterio, function (result) {
                        if (result == null) {
                            let respuesta = swig.renderFile('views/error.html',
                                {
                                    usuario: req.session.usuario,
                                    numeroError: 500,
                                    mensaje: "Error al eliminar usuarios"
                                });
                            logger.error("Error al eliminar usuarios");
                            res.send(respuesta);
                        }
                    })

                    logger.info("Se han eliminado los usuarios " + usuarios + " correctamente");
                    req.session.errores = {
                        mensaje: "Se han eliminado los usuarios correctamente",
                        tipo: "alert alert-info"
                    }
                    res.redirect("/usuarios");
                }
            });
        } else {
            logger.warn("No se han seleccionado usuarios para eliminar");
            req.session.errores = {
                mensaje: "Seleccione al menos un usuario para eliminar.",
                tipo: "alert alert-warning"
            }
            res.redirect("/usuarios");
        }
    });

    /**
     * Muestra la vista de registro
     */
    app.get("/registrarse", function (req, res) {
        let respuesta;
        if (typeof (req.session.errores) != "undefined") {
            // En caso de que haya errores envía a la vista el mensaje y el tipo
            respuesta = swig.renderFile('views/registro.html', {
                mensaje: req.session.errores.mensaje,
                tipo: req.session.errores.tipo
            });
            // Se borran los errores de la sesión para que no se repitan en otras vistas o al recargar la página
            delete req.session.errores;
        } else {
            respuesta = swig.renderFile('views/registro.html', {});
        }
        res.send(respuesta);
    });


    /**
     * Recoge los datos del formulario de registro y registra al usuario en caso de que todos los campos sean correctos.
     */
    app.post('/registrarse', function (req, res) {
        if (req.body.email == null || req.body.email === "") {
            req.session.errores = {
                mensaje: 'El campo "Email" es obligatorio',
                tipo: "alert alert-danger"
            }
            res.redirect("/registrarse");
        } else if (req.body.name == null || req.body.name === "") {
            req.session.errores = {
                mensaje: 'El campo "Nombre" es obligatorio',
                tipo: "alert alert-danger"
            }
            res.redirect("/registrarse");
        } else if (req.body.surname == null || req.body.surname === "") {
            req.session.errores = {
                mensaje: 'El campo "Apellidos" es obligatorio',
                tipo: "alert alert-danger"
            }
            res.redirect("/registrarse");
        } else if (req.body.password == null || req.body.password === "") {
            req.session.errores = {
                mensaje: 'El campo "Contraseña" es obligatorio',
                tipo: "alert alert-danger"
            }
            res.redirect("/registrarse");
        } else if (req.body.passwordRepeat == null || req.body.passwordRepeat === "") {
            req.session.errores = {
                mensaje: 'El campo "Repita la contraseña" es obligatorio',
                tipo: "alert alert-danger"
            }
            logger.error("Error de registro.");
            res.redirect("/registrarse");
        } else {
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
                        // En caso de que todos los campos sean correctos encripta la contraseña
                        let seguro = app.get("crypto").createHmac('sha256', app.get('clave'))
                            .update(req.body.password).digest('hex');
                        // y guarda los datos del usuario en una variable para guardarlo en la base de datos
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
                                // En caso de que surja algún error al intentar registrar al usuario se le muestra un error
                                let respuesta = swig.renderFile('views/error.html',
                                    {
                                        usuario: req.session.usuario,
                                        numeroError: 500,
                                        mensaje: "Error de registro."
                                    });
                                logger.error("Error de registro.");
                                res.redirect(respuesta);
                            } else {
                                req.session.usuario = usuario;
                                logger.info("El usuario " + usuario.email + " ha iniciado sesión");
                                res.redirect("/ofertas");
                            }
                        });
                    }
                }
            })
        }
    });

    /**
     * Muestra el formulario de inicio de sesión
     */
    app.get("/identificarse", function (req, res) {
        let respuesta;
        if (typeof (req.session.errores) != "undefined") {
            // En caso de que haya errores envía a la vista el mensaje y el tipo
            respuesta = swig.renderFile('views/identificacion.html', {
                mensaje: req.session.errores.mensaje,
                tipo: req.session.errores.tipo
            });
            // Se borran los errores de la sesión para que no se repitan en otras vistas o al recargar la página
            delete req.session.errores;
        } else
            respuesta = swig.renderFile('views/identificacion.html', {});
        res.send(respuesta);

    });

    /**
     * Recoge los datos del formulario de inicio de sesión y en caso de que no haya errores le muestra al usuario
     * una vista específica en función de su rol
     */
    app.post("/identificarse", function (req, res) {
        if (req.body.email == null || req.body.email === "") {
            req.session.errores = {
                mensaje: 'El campo "Email" es obligatorio',
                tipo: "alert alert-danger"
            }
            res.redirect("/identificarse");
        } else if (req.body.password == null || req.body.password === "") {
            req.session.errores = {
                mensaje: 'El campo "Contraseña" es obligatorio',
                tipo: "alert alert-danger"
            }
            res.redirect("/identificarse");
        } else {
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
                        mensaje: "Email o contraseña incorrecto",
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
        }
    });


    /**
     * Cierra la sesión del usuario
     */
    app.get('/desconectarse', function (req, res) {
        logger.info(req.session.usuario.email + " desconectado");
        req.session.usuario = null;
        res.redirect("/identificarse");
    });
};