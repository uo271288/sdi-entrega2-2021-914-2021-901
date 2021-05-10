package com.uniovi.test;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
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

    private String encrypt(String passwordAlex) {
	Mac sha256Hmac;
	final byte[] byteKey = "abcdefg".getBytes(StandardCharsets.UTF_8);
	try {
	    sha256Hmac = Mac.getInstance(HMAC_SHA256);
	    SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA256);
	    sha256Hmac.init(keySpec);
	    byte[] macData = sha256Hmac.doFinal(passwordAlex.getBytes(StandardCharsets.UTF_8));

	    // Can either base64 encode or put it right into hex
	    passwordAlex = bytesToHex(macData);
	} catch (InvalidKeyException | NoSuchAlgorithmException e) {
	    e.printStackTrace();
	}
	return passwordAlex;
    }

    public void initdb() {
	MongoClientURI uri = new MongoClientURI(
		"mongodb+srv://admin:sdi@tiendamusica.r7syo.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
	MongoClient mongoClient = new MongoClient(uri);
	MongoDatabase database = mongoClient.getDatabase("mywallapop");
	Bson filter = Filters.eq("role", "standard");
	database.getCollection("usuarios").deleteMany(filter);

	List<Document> documents = new LinkedList<Document>();
	// Preparing a document
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
	// Inserting the document into the collection
	database.getCollection("usuarios").insertMany(documents);
	mongoClient.close();
	System.out.println("done");
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
	// Comprobamos que entramos en la sección privada
	PO_View.checkElement(driver, "text", "El campo \"Email\" es obligatorio");
	// Rellenamos el formulario.
	PO_RegisterView.fillForm(driver, "p@p.p", "", "Álvarez", "77777", "77777");
	// Comprobamos que entramos en la sección privada
	PO_View.checkElement(driver, "text", "El campo \"Nombre\" es obligatorio");
	// Rellenamos el formulario.
	PO_RegisterView.fillForm(driver, "p@p.p", "Alejandro", "", "77777", "77777");
	// Comprobamos que entramos en la sección privada
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
	// Comprobamos el error de email vacío.
	PO_View.checkElement(driver, "text", "Las contraseñas no coinciden.");
    }

    // [Prueba4] Registro de Usuario con datos inválidos (email existente).
    @Test
    public void PR04() {// Vamos al formulario de registro
	PO_HomeView.clickOption(driver, "registrarse", "free", "/html/body/nav/div/div[2]/ul/li[1]/a");
	// Rellenamos el formulario.
	PO_RegisterView.fillForm(driver, "admin@email.com", "Alejandro", "Álvarez", "77777", "77777");
	// Comprobamos el error de email vacío.
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
	// Comprobamos que entramos en la pagina privada de usuario
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
	// Comprobamos que entramos en la pagina privada de usuario
	PO_View.checkElement(driver, "text", "El campo \"Email\" es obligatorio");
	// Rellenamos el formulario
	PO_LoginView.fillForm(driver, "a@b.c", "");
	// Comprobamos que entramos en la pagina privada de usuario
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
	// Comprobamos que entramos en la pagina privada de usuario
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
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "alex@uniovi.sdi");
	// Le damos al botón de logout
	PO_PrivateView.logout(driver);
    }

    // [Prueba10] Comprobar que el botón cerrar sesión no está visible si el usuario
    // no está autenticado.
    @Test
    public void PR10() {
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
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "admin@email.com");
	// Pinchamos en la opción de menu de Ofertas: //li[contains(@id,
	// 'offers-menu')]/a
	List<WebElement> elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr",
		PO_View.getTimeout());
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
	// COmprobamos que entramos en la pagina privada del Usuario
	PO_View.checkElement(driver, "text", "admin@email.com");
	// Pinchamos en la opción de menu de Ofertas: //li[contains(@id,
	// 'offers-menu')]/a
	List<WebElement> elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr",
		PO_View.getTimeout());
	driver.findElement(By.id("checkbox")).click();
	By boton = By.id("btn");
	driver.findElement(boton).click();
	elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr", PO_View.getTimeout());
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
	// Pinchamos en la opción de menu de Ofertas: //li[contains(@id,
	// 'offers-menu')]/a
	List<WebElement> elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr",
		PO_View.getTimeout());
	List<WebElement> checkboxes = driver.findElements(By.id("checkbox"));
	checkboxes.get(checkboxes.size() - 1).click();
	By boton = By.id("btn");
	driver.findElement(boton).click();
	elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr", PO_View.getTimeout());
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
	// Pinchamos en la opción de menu de Ofertas: //li[contains(@id,
	// 'offers-menu')]/a
	List<WebElement> elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr",
		PO_View.getTimeout());
	List<WebElement> checkboxes = driver.findElements(By.id("checkbox"));
	checkboxes.get(1).click();
	checkboxes.get(checkboxes.size() - 1).click();
	checkboxes.get(0).click();
	By boton = By.id("btn");
	driver.findElement(boton).click();
	elementos = SeleniumUtils.EsperaCargaPagina(driver, "free", "//tbody/tr", PO_View.getTimeout());
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
}