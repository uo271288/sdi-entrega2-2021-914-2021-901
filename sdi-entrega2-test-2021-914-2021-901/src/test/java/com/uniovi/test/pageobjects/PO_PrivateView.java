package com.uniovi.test.pageobjects;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.uniovi.test.util.SeleniumUtils;

public class PO_PrivateView extends PO_NavView {
    static public void fillFormAddOffer(WebDriver driver, String titulop, String descriptionp, String pricep,
	    boolean destacadap) {
	// Esperamos 5 segundo a que carge el DOM porque en algunos equipos falla
	SeleniumUtils.esperarSegundos(driver, 5);
	// Rellenemos el campo de titulo
	WebElement title = driver.findElement(By.name("titulo"));
	title.clear();
	title.sendKeys(titulop);
	// Rellenamos el campo de descripcion
	WebElement description = driver.findElement(By.name("detalles"));
	description.clear();
	description.sendKeys(descriptionp);
	// Rellenamos el campo de precio
	WebElement price = driver.findElement(By.name("precio"));
	price.click();
	price.clear();
	price.sendKeys(pricep);
	if (destacadap) {
	    WebElement destacada = driver.findElement(By.name("destacada"));
	    destacada.click();
	}
	By boton = By.className("btn");
	driver.findElement(boton).click();
    }

    static public void fillSearch(WebDriver driver, String searchp) {
	// Esperamos 5 segundo a que carge el DOM porque en algunos equipos falla
	SeleniumUtils.esperarSegundos(driver, 5);
	// Rellenemos el campo search
	WebElement title = driver.findElement(By.name("busqueda"));
	title.clear();
	title.sendKeys(searchp);
	By boton = By.className("btn");
	driver.findElement(boton).click();
    }

    static public void logout(WebDriver driver) {
	PO_PrivateView.clickOption(driver, "desconectarse", "free", "/html/body/nav/div/div[2]/ul/li[2]/a");
	PO_View.checkElement(driver, "text", "Identificación de usuario");
    }

    static public void goToPage(WebDriver driver, int num) {
	List<WebElement> elementos = PO_View.checkElement(driver, "free", "//a[contains(@class, 'page-link')]");
	elementos.get(num).click();
    }
}
