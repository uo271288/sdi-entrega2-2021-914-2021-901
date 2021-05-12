package com.uniovi.test;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.uniovi.test.pageobjects.PO_HomeView;
import com.uniovi.test.pageobjects.PO_LoginView;
import com.uniovi.test.pageobjects.PO_PrivateView;
import com.uniovi.test.pageobjects.PO_RegisterView;
import com.uniovi.test.pageobjects.PO_View;
import com.uniovi.test.util.SeleniumUtils;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class SdiEntrega2Test20219142021901ApplicationTests {

    // En Windows (Debe ser la versión 65.0.1 y desactivar las actualizacioens
    // automáticas)):
    static String PathFirefox86 = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
    static String Geckdriver024 = "C:\\Users\\ivany\\Desktop\\Alex\\PL-SDI-Sesión5-material\\PL-SDI-Sesión5-material\\geckodriver022win64.exe";
    // En MACOSX (Debe ser la versión 65.0.1 y desactivar las actualizacioens
    // automáticas):
    // static String PathFirefox65 =
    // "/Applications/Firefox.app/Contents/MacOS/firefox-bin";
    // static String Geckdriver024 = "/Users/delacal/selenium/geckodriver024mac";
    // Común a Windows y a MACOSX

    static WebDriver driver = getDriver(PathFirefox86, Geckdriver024);
    static String URL = "http://localhost:8081";

    /**
     * https://stackoverflow.com/questions/39355241/compute-hmac-sha512-with-secret-key-in-java
     * https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
     * 
     * Tanto estas constantes como los métodos String bytesToHex(byte[] bytes) y
     * String encrypt(String passwordAlex) las hemos encontrado en los anteriores
     * post de StackOverflow y los hemos modificado para que cumplan la función de
     * encriptado de las contraseñas. Lo hemos hecho así para que no hubiera
     * conflicto entre el encriptado usado en NodeJS y este
     */
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    public static WebDriver getDriver(String PathFirefox, String Geckdriver) {
	System.setProperty("webdriver.firefox.bin", PathFirefox);
	System.setProperty("webdriver.gecko.driver", Geckdriver);
	WebDriver driver = new FirefoxDriver();
	return driver;
    }

    // Antes de cada prueba se navega al URL home de la aplicación
    @Before
    public void setUp() {
	initdb();
	driver.navigate().to(URL);
    }

    // Después de cada prueba se borran las cookies del navegador
    @After
    public void tearDown() {
	driver.manage().deleteAllCookies();
    }

    // Antes de la primera prueba
    @BeforeClass
    static public void begin() {
    }

    // Al finalizar la última prueba
    @AfterClass
    static public void end() {
	// Cerramos el navegador al finalizar las pruebas
	driver.quit();
    }

    private static String bytesToHex(byte[] bytes) {
	char[] hexChars = new char[bytes.length * 2];
	for (int j = 0; j < bytes.length; j++) {
	    int v = bytes[j] & 0xFF;
	    hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	    hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	}
	return new String(hexChars);
    }

    private String encrypt(String passwordToEncrypt) {
	Mac sha256Hmac;
	final byte[] byteKey = "abcdefg".getBytes(StandardCharsets.UTF_8);
	try {
	    sha256Hmac = Mac.getInstance(HMAC_SHA256);
	    SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA256);
	    sha256Hmac.init(keySpec);
	    byte[] macData = sha256Hmac.doFinal(passwordToEncrypt.getBytes(StandardCharsets.UTF_8));
	    passwordToEncrypt = bytesToHex(macData);
	} catch (InvalidKeyException | NoSuchAlgorithmException e) {
	    e.printStackTrace();
	}
	return passwordToEncrypt;
    }

    /**
     * Inicializa la BBDD antes de cada test
     */
    public void initdb() {
	MongoClientURI uri = new MongoClientURI(
		"mongodb+srv://admin:sdi@tiendamusica.r7syo.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
	MongoClient mongoClient = new MongoClient(uri);
	MongoDatabase database = mongoClient.getDatabase("mywallapop");

	Bson filter = Filters.eq("role", "standard");
	database.getCollection("usuarios").deleteMany(filter);

	BasicDBObject document = new BasicDBObject();
	database.getCollection("ofertas").deleteMany(document);
	database.getCollection("conversaciones").deleteMany(document);
	database.getCollection("compras").deleteMany(document);

	List<Document> documents = new LinkedList<Document>();
	String passwordAlex = encrypt("alex");
	Document alex = new Document();
	alex.append("email", "alex@uniovi.sdi");
	alex.append("name", "alex");
	alex.append("surname", "alex");
	alex.append("password", passwordAlex);
	alex.append("balance", 100.0);
	alex.append("role", "standard");
	documents.add(alex);

	String password1 = encrypt("prueba1");
	Document prueba1 = new Document();
	prueba1.append("email", "prueba1@sdi.uo");
	prueba1.append("name", "prueba1");
	prueba1.append("surname", "prueba1");
	prueba1.append("password", password1);
	prueba1.append("balance", 100.0);
	prueba1.append("role", "standard");
	documents.add(prueba1);

	String password2 = encrypt("prueba2");
	Document prueba2 = new Document();
	prueba2.append("email", "prueba2@sdi.uo");
	prueba2.append("name", "prueba2");
	prueba2.append("surname", "prueba2");
	prueba2.append("password", password2);
	prueba2.append("balance", 100.0);
	prueba2.append("role", "standard");
	documents.add(prueba2);

	String password3 = encrypt("prueba3");
	Document prueba3 = new Document();
	prueba3.append("email", "prueba3@sdi.uo");
	prueba3.append("name", "prueba3");
	prueba3.append("surname", "prueba3");
	prueba3.append("password", password3);
	prueba3.append("balance", 100.0);
	prueba3.append("role", "standard");
	documents.add(prueba3);

	String password4 = encrypt("prueba4");
	Document prueba4 = new Document();
	prueba4.append("email", "prueba4@sdi.uo");
	prueba4.append("name", "prueba4");
	prueba4.append("surname", "prueba4");
	prueba4.append("password", password4);
	prueba4.append("balance", 100.0);
	prueba4.append("role", "standard");
	documents.add(prueba4);

	String password5 = encrypt("prueba5");
	Document prueba5 = new Document();
	prueba5.append("email", "prueba5@sdi.uo");
	prueba5.append("name", "prueba5");
	prueba5.append("surname", "prueba5");
	prueba5.append("password", password5);
	prueba5.append("balance", 100.0);
	prueba5.append("role", "standard");
	documents.add(prueba5);

	database.getCollection("usuarios").insertMany(documents);

	filter = Filters.eq("email", "prueba1@sdi.uo");
	FindIterable<Document> usuario1 = database.getCollection("usuarios").find(filter);
	prueba1.append("_id", usuario1.first().get("_id").toString());

	List<Document> documentsOfertas = new LinkedList<Document>();
	Document oferta1 = new Document();
	oferta1.append("titulo", "pelota");
	oferta1.append("detalles", "Pelota de Baloncesto");
	oferta1.append("precio", "9");
	oferta1.append("autor", prueba1);
	oferta1.append("hora", new Date(System.currentTimeMillis()));
	oferta1.append("comprador", null);
	documentsOfertas.add(oferta1);

	Document oferta2 = new Document();
	oferta2.append("titulo", "pantalones");
	oferta2.append("detalles", "Pantalones vaqueros");
	oferta2.append("precio", "120");
	oferta2.append("autor", prueba1);
	oferta2.append("hora", new Date(System.currentTimeMillis()));
	oferta2.append("comprador", null);
	documentsOfertas.add(oferta2);

	Document oferta3 = new Document();
	oferta3.append("titulo", "patines");
	oferta3.append("detalles", "Patines en linea");
	oferta3.append("precio", "100");
	oferta3.append("autor", prueba1);
	oferta3.append("hora", new Date(System.currentTimeMillis()));
	oferta3.append("comprador", null);
	documentsOfertas.add(oferta3);

	database.getCollection("ofertas").insertMany(documentsOfertas);

	mongoClient.close();
    }

    // [Prueba1] Registro de Usuario con datos válidos.
    @Test
    public void PR01() {
	// Vamos al formulario de registro
	PO_HomeView.clickOption(driver, "registrarse", "free", "/html/body/nav/div/div[2]/ul/li[1]/a");
	// Rellenamos el formulario.
	PO_RegisterView.fillForm(driver, "prueba6@sdi.uo", "prueba6", "prueba6", "prueba6", "prueba6");
	// Comprobamos que entramos en la sección privada
	PO_View.checkElement(driver, "free", "/html/body/div[1]/h2[text()='Ofertas']");
    }

    // [Prueba2] Registro de Usuario con datos inválidos (email, nombre y apellidos
    // vacíos).
    @Test
    public void PR02() {
	// Vamos al formulario de registro
	PO_HomeView.clickOption(driver, "registrarse", "free", "/html/body/nav/div/div[2]/ul/li[1]/a");
	// Rellenamos el formulario.
	PO_RegisterView.fillForm(driver, "", "Alejandro", "Álvarez", "77777", "77777");
	// Comprobamos que nos muestra un error
	PO_View.checkElement(driver, "text", "El campo \"Email\" es obligatorio");
	// Rellenamos el formulario.
	PO_RegisterView.fillForm(driver, "p@p.p", "", "Álvarez", "77777", "77777");
	// Comprobamos que nos muestra un error
	PO_View.checkElement(driver, "text", "El campo \"Nombre\" es obligatorio");
	// Rellenamos el formulario.
	PO_RegisterView.fillForm(driver, "p@p.p", "Alejandro", "", "77777", "77777");
	// Comprobamos que nos muestra un error
	PO_View.checkElement(driver, "text", "El campo \"Apellidos\" es obligatorio");
    }

    // [Prueba3] Registro de Usuario con datos inválidos (repetición de contraseña
    // inválida).
    @Test
    public void PR03() {
	// Vamos al formulario de registro
	PO_HomeView.clickOption(driver, "registrarse", "free", "/html/body/nav/div/div[2]/ul/li[1]/a");
	// Rellenamos el formulario.
	PO_RegisterView.fillForm(driver, "p@p.p", "Alejandro", "Álvarez", "77777", "1111");
	// Comprobamos que nos muestra un error
	PO_View.checkElement(driver, "text", "Las contraseñas no coinciden.");
    }

    // [Prueba4] Registro de Usuario con datos inválidos (email existente).
    @Test
    public void PR04() {
	// Vamos al formulario de registro
	PO_HomeView.clickOption(driver, "registrarse", "free", "/html/body/nav/div/div[2]/ul/li[1]/a");
	// Rellenamos el formulario.
	PO_RegisterView.fillForm(driver, "admin@email.com", "Alejandro", "Álvarez", "77777", "77777");
	// Comprobamos que nos muestra un error
	PO_View.checkElement(driver, "text", "El usuario ya está registrado.");
    }

    // [Prueba5] Inicio de sesión con datos válidos.
    @Test
    public void PR05() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "alex@uniovi.sdi", "alex");
	// Comprobamos que entramos en la pagina privada de admin
	PO_View.checkElement(driver, "free", "/html/body/div[1]/h2[text()='Ofertas']");
    }

    // [Prueba6] Inicio de sesión con datos inválidos (email existente, pero
    // contraseña incorrecta).
    @Test
    public void PR06() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "alex@uniovi.sdi", "1111");
	// Comprobamos que nos muestra un error
	PO_View.checkElement(driver, "text", "Email o contraseña incorrecto");
    }

    // [Prueba7] Inicio de sesión con datos inválidos (campo email o contraseña
    // vacíos).
    @Test
    public void PR07() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "", "1");
	// Comprobamos que nos muestra un error
	PO_View.checkElement(driver, "text", "El campo \"Email\" es obligatorio");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "a@b.c", "");
	// Comprobamos que nos muestra un error
	PO_View.checkElement(driver, "text", "El campo \"Contraseña\" es obligatorio");
    }

    // [Prueba8] Inicio de sesión con datos inválidos (email no existente en la
    // aplicación).

    @Test
    public void PR08() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "alex@uniovi.sdi", "1111");
	// Comprobamos que nos muestra un error
	PO_View.checkElement(driver, "text", "Email o contraseña incorrecto");
    }

    // [Prueba9] Hacer click en la opción de salir de sesión y comprobar que se
    // redirige a la página de inicio de sesión (Login).
    @Test
    public void PR09() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "alex@uniovi.sdi", "alex");
	// Comprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "alex@uniovi.sdi");
	// Le damos al botón de logout
	PO_PrivateView.logout(driver);
    }

    // [Prueba10] Comprobar que el botón cerrar sesión no está visible si el usuario
    // no está autenticado.
    @Test
    public void PR10() {
	// Comprobamos que la opción Cerrar Sesión no existe
	SeleniumUtils.EsperaCargaPaginaNoTexto(driver, "Cerrar Sesión", PO_View.getTimeout());
    }

    // [Prueba11] Mostrar el listado de usuarios y comprobar que se muestran todos
    // los que existen en el sistema.
    @Test
    public void PR11() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "admin@email.com", "admin");
	// Comprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "admin@email.com");
	List<WebElement> elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr",
		PO_View.getTimeout());
	// Comprobamos que están todos los usuarios del sistema
	assertTrue(elementos.size() == 6);
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
    }

    // [Prueba12] Ir a la lista de usuarios, borrar el primer usuario de la lista,
    // comprobar que la lista se actualiza y dicho usuario desaparece.
    @Test
    public void PR12() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "admin@email.com", "admin");
	// Comprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "admin@email.com");
	List<WebElement> elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr",
		PO_View.getTimeout());
	// Seleccionamos el primer usuario
	driver.findElement(By.id("checkbox")).click();
	By boton = By.id("btn");
	// Lo borramos
	driver.findElement(boton).click();
	elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr", PO_View.getTimeout());
	// Comprobamos que están todos los usuarios del sistema menos el borrado
	assertTrue(elementos.size() == 5);
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "alex@uniovi.sdi", "alex");
	// Comprobamos que el usuario ya no existe
	PO_View.checkElement(driver, "text", "Email o contraseña incorrecto");
    }

    // [Prueba13] Ir a la lista de usuarios, borrar el último usuario de la lista,
    // comprobar que la lista se actualiza y dicho usuario desaparece.
    @Test
    public void PR13() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "admin@email.com", "admin");
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "admin@email.com");
	List<WebElement> elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr",
		PO_View.getTimeout());
	List<WebElement> checkboxes = driver.findElements(By.id("checkbox"));
	// Seleccionamos el último usuario
	checkboxes.get(checkboxes.size() - 1).click();
	By boton = By.id("btn");
	// Lo borramos
	driver.findElement(boton).click();
	elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr", PO_View.getTimeout());
	// Comprobamos que están todos los usuarios del sistema menos el borrado
	assertTrue(elementos.size() == 5);
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba5@sdi.uo", "prueba5");
	// Comprobamos que el usuario ya no existe
	PO_View.checkElement(driver, "text", "Email o contraseña incorrecto");
    }

    // [Prueba14] Ir a la lista de usuarios, borrar 3 usuarios, comprobar que la
    // lista se actualiza y dichos usuarios desaparecen.
    @Test
    public void PR14() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "admin@email.com", "admin");
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "admin@email.com");
	List<WebElement> elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr",
		PO_View.getTimeout());
	// Seleccionamos los usuarios que queremos borrar
	List<WebElement> checkboxes = driver.findElements(By.id("checkbox"));
	checkboxes.get(1).click();
	checkboxes.get(checkboxes.size() - 1).click();
	checkboxes.get(0).click();
	By boton = By.id("btn");
	// Los borramos
	driver.findElement(boton).click();
	elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr", PO_View.getTimeout());
	// Comprobamos que están todos los usuarios del sistema menos los borrados
	assertTrue(elementos.size() == 3);
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "alex@uniovi.sdi", "alex");
	// Comprobamos que el usuario ya no existe
	PO_View.checkElement(driver, "text", "Email o contraseña incorrecto");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba1@sdi.uo", "prueba1");
	// Comprobamos que el usuario ya no existe
	PO_View.checkElement(driver, "text", "Email o contraseña incorrecto");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba5@sdi.uo", "prueba5");
	// Comprobamos que el usuario ya no existe
	PO_View.checkElement(driver, "text", "Email o contraseña incorrecto");
    }

    // [Prueba15] Ir al formulario de alta de oferta, rellenarla con datos válidos y
    // pulsar el botón Submit. Comprobar que la oferta sale en el listado de ofertas
    // de dicho usuario.
    @Test
    public void PR15() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba1@sdi.uo", "prueba1");
	// Comprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "prueba1@sdi.uo");
	// Vamos a la vista de mis ofertas.
	PO_HomeView.clickOption(driver, "misofertas", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Le damos al boton Agregar Oferta
	By boton = By.className("btn-info");
	driver.findElement(boton).click();
	// Ahora vamos a rellenar la oferta.
	PO_PrivateView.fillFormAddOffer(driver, "Oferta de prueba", "Esta es la descripcion de una oferta", "9", false);
	// Comprobamos que aparece la oferta en la pagina
	List<WebElement> elementos = PO_View.checkElement(driver, "text", "oferta de prueba");
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
    }

    // [Prueba16] Ir al formulario de alta de oferta, rellenarla con datos inválidos
    // (campo título vacío y precio en negativo) y pulsar el botón Submit. Comprobar
    // que se muestra el mensaje de campo obligatorio.
    @Test
    public void PR16() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba1@sdi.uo", "prueba1");
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "prueba1@sdi.uo");
	// Vamos a la vista de mis ofertas.
	PO_HomeView.clickOption(driver, "misofertas", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Le damos al boton Agregar Oferta
	By boton = By.className("btn-info");
	driver.findElement(boton).click();
	// Ahora vamos a rellenar la oferta.
	PO_PrivateView.fillFormAddOffer(driver, "", "Esta es la descripcion de una oferta", "9", false);
	// Comprobamos que aparece la oferta en la pagina
	List<WebElement> elementos = PO_View.checkElement(driver, "text",
		"El titulo o la descripcion no pueden estar vacios");
	// Ahora vamos a rellenar la oferta.
	PO_PrivateView.fillFormAddOffer(driver, "Oferta de prueba", "Esta es la descripcion de una oferta", "-7",
		false);
	// Comprobamos que aparece la oferta en la pagina
	elementos = PO_View.checkElement(driver, "text", "El precio no puede ser negativo");
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
    }

    // [Prueba17] Mostrar el listado de ofertas para dicho usuario y comprobar que
    // se muestran todas las que existen para este usuario.
    @Test
    public void PR17() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba1@sdi.uo", "prueba1");
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "prueba1@sdi.uo");
	// Vamos a la vista de mis ofertas.
	PO_HomeView.clickOption(driver, "misofertas", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	List<WebElement> elementos = PO_View.checkElement(driver, "text", "Mis Ofertas");
	// Comprobamos que estan todas las ofetas para ese usuario
	elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr", PO_View.getTimeout());
	assertTrue(elementos.size() == 3);
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
    }

    // [Prueba18] Ir a la lista de ofertas, borrar la primera oferta de la lista,
    // comprobar que la lista se actualiza y que la oferta desaparece.
    @Test
    public void PR18() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba1@sdi.uo", "prueba1");
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "prueba1@sdi.uo");
	// Vamos a la vista de mis ofertas.
	PO_HomeView.clickOption(driver, "misofertas", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	List<WebElement> elementos = PO_View.checkElement(driver, "text", "Mis Ofertas");
	// Pulsamos en elimnar el la primera oferta
	elementos = PO_View.checkElement(driver, "free", "//a[contains(text(), 'Eliminar')]");
	elementos.get(0).click();
	// Y esperamos a que NO aparezca la uprimera "Botas de montaña"
	SeleniumUtils.EsperaCargaPaginaNoTexto(driver, "pelota", PO_View.getTimeout());
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
    }

    // [Prueba19] Ir a la lista de ofertas, borrar la última oferta de la lista,
    // comprobar que la lista se actualiza y que la oferta desaparece.
    @Test
    public void PR19() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba1@sdi.uo", "prueba1");
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "prueba1@sdi.uo");
	// Vamos a la vista de mis ofertas.
	PO_HomeView.clickOption(driver, "misofertas", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	List<WebElement> elementos = PO_View.checkElement(driver, "text", "Mis Ofertas");
	// Pulsamos en elimnar el la primera oferta
	elementos = PO_View.checkElement(driver, "free", "//a[contains(text(), 'Eliminar')]");
	elementos.get(elementos.size() - 1).click();
	// Y esperamos a que NO aparezca la uprimera "Botas de montaña"
	SeleniumUtils.EsperaCargaPaginaNoTexto(driver, "patines", PO_View.getTimeout());
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
    }

    // [Prueba20] Hacer una búsqueda con el campo vacío y comprobar que se muestra
    // la página que corresponde con el listado de las ofertas existentes en el
    // sistema
    @Test
    public void PR20() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba1@sdi.uo", "prueba1");
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "prueba1@sdi.uo");
	// Buscamos un texto vacio
	PO_PrivateView.fillSearch(driver, "");
	// Comprobamos que estan todas las ofetas para esa pagina
	List<WebElement> elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr",
		PO_View.getTimeout());
	assertTrue(elementos.size() == 3);
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
    }

    // [Prueba21] Hacer una búsqueda escribiendo en el campo un texto que no exista
    // y comprobar que se muestra la página que corresponde, con la lista de ofertas
    // vacía.
    @Test
    public void PR21() {
	PO_View.setTimeout(6);
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba1@sdi.uo", "prueba1");
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "prueba1@sdi.uo");
	// Buscamos un texto vacio
	PO_PrivateView.fillSearch(driver, "texto no existente");
	// Comprobamos que estan todas las ofetas para esa pagina
	try {
	    List<WebElement> elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr",
		    PO_View.getTimeout());
	} catch (TimeoutException e) {
	    System.out.println("No se han encontrado ofertas. Es correcto");
	}
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
    }

    // [Prueba22] Hacer una búsqueda escribiendo en el campo un texto en minúscula o
    // mayúscula y comprobar que se muestra la página que corresponde, con la lista
    // de ofertas que contengan dicho texto, independientemente que el título esté
    // almacenado en minúsculas o mayúscula.
    @Test
    public void PR22() {
	PO_View.setTimeout(6);
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba1@sdi.uo", "prueba1");
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "prueba1@sdi.uo");
	// Buscamos un texto vacio
	PO_PrivateView.fillSearch(driver, "pelota");
	// Comprobamos que estan todas las ofetas para esa pagina
	List<WebElement> elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr",
		PO_View.getTimeout());
	assertTrue(elementos.size() == 1);
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
    }

    // [Prueba23] Sobre una búsqueda determinada (a elección de desarrollador),
    // comprar una oferta que deja un saldo positivo en el contador del comprobador.
    // Y comprobar que el contador se actualiza correctamente en la vista del
    // comprador.
    @Test
    public void PR23() {
	PO_View.setTimeout(8);
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba2@sdi.uo", "prueba2");
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "prueba2@sdi.uo");
	// Buscamos un texto vacio
	PO_PrivateView.fillSearch(driver, "pelota");
	List<WebElement> elementos = PO_View.checkElement(driver, "free", "//a[contains(text(), 'Comprar')]");
	elementos.get(0).click();
	// Comprobamos que el saldo ha cambiado
	PO_View.checkElement(driver, "text", "91");
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
    }

    // [Prueba24] Sobre una búsqueda determinada (a elección de desarrollador),
    // comprar una oferta que deja un saldo 0 en el contador del comprobador. Y
    // comprobar que el contador se actualiza correctamente en la vista del
    // comprador.
    @Test
    public void PR24() {
	PO_View.setTimeout(6);
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba2@sdi.uo", "prueba2");
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "prueba2@sdi.uo");
	// Buscamos un texto vacio
	PO_PrivateView.fillSearch(driver, "patines");
	List<WebElement> elementos = PO_View.checkElement(driver, "free", "//a[contains(text(), 'Comprar')]");
	elementos.get(0).click();
	// Comprobamos que el saldo ha cambiado
	PO_View.checkElement(driver, "text", "0");
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
    }

    // [Prueba25] Sobre una búsqueda determinada (a elección de desarrollador),
    // intentar comprar una oferta que esté por encima de saldo disponible del
    // comprador. Y comprobar que se muestra el mensaje de saldo no suficiente.
    @Test
    public void PR25() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba2@sdi.uo", "prueba2");
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "prueba2@sdi.uo");
	// Buscamos un texto vacio
	PO_PrivateView.fillSearch(driver, "pantalones");
	List<WebElement> elementos = PO_View.checkElement(driver, "free", "//a[contains(text(), 'Comprar')]");
	elementos.get(0).click();
	// Comprobamos que el saldo ha cambiado
	PO_View.checkElement(driver, "text", "100");
	PO_View.checkElement(driver, "text", "Saldo insuficiente");
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
    }

    // [Prueba26] Ir a la opción de ofertas compradas del usuario y mostrar la
    // lista. Comprobar que aparecen las ofertas que deben aparecer.
    @Test
    public void PR26() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba2@sdi.uo", "prueba2");
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "prueba2@sdi.uo");

	PO_PrivateView.fillSearch(driver, "pelota");
	List<WebElement> elementos = PO_View.checkElement(driver, "free", "//a[contains(text(), 'Comprar')]");
	elementos.get(0).click();

	elementos = PO_View.checkElement(driver, "free", "//a[contains(text(), 'Compras')]");
	elementos.get(0).click();
	elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr", PO_View.getTimeout());
	assertTrue(elementos.size() == 1);
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
    }

    // [Prueba27] Al crear una oferta marcar dicha oferta como destacada y a
    // continuación comprobar: i) que aparece en el listado de ofertas destacadas
    // para los usuarios y que el saldo del usuario se actualiza adecuadamente en la
    // vista del ofertante (-20).
    @Test
    public void PR27() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba1@sdi.uo", "prueba1");
	// Comprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "prueba1@sdi.uo");
	// Vamos a la vista de mis ofertas.
	PO_HomeView.clickOption(driver, "misofertas", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Le damos al boton Agregar Oferta
	By boton = By.className("btn-info");
	driver.findElement(boton).click();
	// Ahora vamos a rellenar la oferta.
	PO_PrivateView.fillFormAddOffer(driver, "oferta destacada", "Esta es la descripcion de una oferta", "9", true);
	// Comprobamos que aparece la oferta en la pagina
	List<WebElement> elementos = PO_View.checkElement(driver, "text", "oferta destacada");
	// Comprobamos que se decrementa el saldo
	PO_View.checkElement(driver, "text", "80 €");
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba2@sdi.uo", "prueba2");
	// Comprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "prueba2@sdi.uo");
	// Comprobamos que la nueva oferta aparece en la sección de destacados
	PO_View.checkElement(driver, "free",
		"//*[@id=\"tableDestacadas\"]/tbody/tr/td[contains(text(), 'oferta destacada')]");
    }

    // [Prueba28] Sobre el listado de ofertas de un usuario con más de 20 euros de
    // saldo, pinchar en el enlace Destacada y a continuación comprobar: i) que
    // aparece en el listado de ofertas destacadas para los usuarios y que el saldo
    // del usuario se actualiza adecuadamente en la vista del ofertante (-20).
    @Test
    public void PR28() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba1@sdi.uo", "prueba1");
	// Comprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "prueba1@sdi.uo");
	// Vamos a la vista de mis ofertas.
	PO_HomeView.clickOption(driver, "misofertas", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Destacamos una de las ofertas
	List<WebElement> elementos = PO_View.checkElement(driver, "free", "//a[contains(text(), 'Destacar')]");
	// Destacamos una de la oferta
	elementos.get(0).click();
	// Comprobamos que se decrementa el saldo
	PO_View.checkElement(driver, "text", "80 €");
	// Ahora nos desconectamos
	PO_PrivateView.logout(driver);
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba2@sdi.uo", "prueba2");
	// Comprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "prueba2@sdi.uo");
	// Comprobamos que la nueva oferta aparece en la sección de destacados
	PO_View.checkElement(driver, "free", "//*[@id=\"tableDestacadas\"]/tbody/tr/td[contains(text(), 'pelota')]");
    }

    // [Prueba29] Sobre el listado de ofertas de un usuario con menos de 20 euros de
    // saldo, pinchar en el enlace Destacada y a continuación comprobar que se
    // muestra el mensaje de saldo no suficiente.
    @Test
    public void PR29() {
	// Vamos al formulario de logueo.
	PO_HomeView.clickOption(driver, "identificarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "prueba2@sdi.uo", "prueba2");
	// Comprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "prueba2@sdi.uo");
	// Buscamos un artículo que nos deje el saldo a 0
	PO_PrivateView.fillSearch(driver, "patines");
	List<WebElement> elementos = PO_View.checkElement(driver, "free", "//a[contains(text(), 'Comprar')]");
	// Compramos el artículo
	elementos.get(0).click();
	// Comprobamos que el saldo ha cambiado
	PO_View.checkElement(driver, "text", "0 €");
	// Vamos a la vista de mis ofertas.
	PO_HomeView.clickOption(driver, "misofertas", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	// Le damos al boton Agregar Oferta
	By boton = By.className("btn-info");
	driver.findElement(boton).click();
	// Ahora vamos a rellenar la oferta.
	PO_PrivateView.fillFormAddOffer(driver, "oferta destacada", "Esta es la descripcion de una oferta", "9", false);
	// Comprobamos que aparece la oferta en la pagina
	elementos = PO_View.checkElement(driver, "text", "oferta destacada");
	// COmprobamos que entramos en la pagina privada del Usuario
	elementos = PO_View.checkElement(driver, "free", "//a[contains(text(), 'Destacar')]");
	// Intentamos destacar la oferta
	elementos.get(0).click();
	// Comprobamos que nos muestra un mensaje de error
	PO_View.checkElement(driver, "text", "Saldo insuficiente");
    }

    // [Prueba30] Inicio de sesión con datos válidos.
    @Test
    public void PR30() {
	URL = "http://localhost:8081/cliente.html";
	initdb();
	driver.navigate().to(URL);
	// Rellenamos el formulario de login
	PO_LoginView.fillForm(driver, "prueba2@sdi.uo", "prueba2");
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "Titulo");
    }

    // [Prueba31] Inicio de sesión con datos inválidos (email existente, pero
    // contraseña incorrecta).
    @Test
    public void PR31() {
	PO_View.setTimeout(6);
	URL = "http://localhost:8081/cliente.html";
	initdb();
	driver.navigate().to(URL);
	// Rellenamos el formulario de login
	PO_LoginView.fillForm(driver, "prueba2@sdi.uo", "prueba3");
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "Usuario o contraseña incorrectos");
    }

    // [Prueba32] Inicio de sesión con datos inválidos (campo email o contraseña
    // vacíos).
    @Test
    public void PR32() {
	URL = "http://localhost:8081/cliente.html";
	initdb();
	driver.navigate().to(URL);
	// Rellenamos el formulario de login
	PO_LoginView.fillForm(driver, "", "");
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "Usuario o contraseña incorrectos");
    }

    // [Prueba33] Mostrar el listado de ofertas disponibles y comprobar que se
    // muestran todas las que existen, menos las del usuario identificado.
    @Test
    public void PR33() {
	PO_View.setTimeout(3);
	URL = "http://localhost:8081/cliente.html";
	initdb();
	driver.navigate().to(URL);
	// Rellenamos el formulario de login
	PO_LoginView.fillForm(driver, "prueba2@sdi.uo", "prueba2");
	// COmprobamos que entramos en la pagina privada del Usuario
	List<WebElement> elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr",
		PO_View.getTimeout());
	assertTrue(elementos.size() == 3);
    }

    // [Prueba34] Sobre una búsqueda determinada de ofertas (a elección de
    // desarrollador), enviar un mensaje a una oferta concreta. Se abriría dicha
    // conversación por primera vez. Comprobar que el mensaje aparece en el listado
    // de mensajes.
    @Test
    public void PR34() {

	PO_View.setTimeout(10);
	URL = "http://localhost:8081/cliente.html";
	initdb();
	driver.navigate().to(URL);
	// Rellenamos el formulario de login
	PO_LoginView.fillForm(driver, "prueba2@sdi.uo", "prueba2");
	// COmprobamos que entramos en la pagina privada del Usuario
	List<WebElement> elementos = PO_View.checkElement(driver, "free", "//a[contains(text(), 'Conversacion')]");
	elementos.get(0).click();
	WebElement mensaje = driver.findElement(By.name("mensaje"));
	mensaje.click();
	mensaje.clear();
	mensaje.sendKeys("hola");

	// Pulsar el boton de Alta.
	By boton = By.className("btn");
	driver.findElement(boton).click();

	PO_View.checkElement(driver, "text", "hola");

    }
}