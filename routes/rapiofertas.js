module.exports = function(app, gestorBD) {

    app.get("/api/oferta", function(req, res) {
        gestorBD.obtenerOfertas( {} , function(ofertas) {
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
}
