module.exports = function(app, swig) {
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
};
