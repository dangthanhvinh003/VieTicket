package com.example.VieTicketSystem;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.bonigarcia.wdm.WebDriverManager;

@SpringBootTest
class VieTicketSystemApplicationTests {

	private WebDriver driver;

	@BeforeEach
	public void setUp() {
		// Set up ChromeDriver using WebDriverManager or specify the path
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
	}

	@AfterEach
	public void tearDown() {
		// Close the browser after each test
		if (driver != null) {
			driver.quit();
		}
	}

	@Test
	public void testEventCreation() {
		// Open the login page
		driver.get("http://localhost:8080/auth/login");

		// Fill in username and password
		WebElement username = driver.findElement(By.id("username"));
		username.sendKeys("o1");

		WebElement password = driver.findElement(By.id("password"));
		password.sendKeys("123");

		// Click login button
		WebElement loginButton = driver.findElement(By.xpath("//button[text()='Login']"));
		loginButton.click();

		// Wait for the dashboard or landing page to load
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		WebElement createEventLink = wait.until(
				ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.btn.btn-outline-success[href='/createEvent']")));
		createEventLink.click();

		// Upload Banner
		WebElement bannerUpload = driver.findElement(By.name("banner"));
		bannerUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png"); // Replace with actual path

		// Upload Poster
		WebElement posterUpload = driver.findElement(By.name("poster"));
		posterUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png"); // Replace with actual path

		// Fill in Event details
		WebElement nameInput = driver.findElement(By.id("name"));
		nameInput.sendKeys("Random Event Name");

		WebElement locationInput = driver.findElement(By.id("location"));
		locationInput.sendKeys("Random Location");

		WebElement startDateInput = driver.findElement(By.id("start_date"));
		startDateInput.clear();
		startDateInput.sendKeys("07202024\t130000");

		WebElement endDateInput = driver.findElement(By.id("end_date"));
		endDateInput.clear();
		endDateInput.sendKeys("07202024\t190000");

		Select typeSelect = new Select(driver.findElement(By.id("type")));
		typeSelect.selectByValue("music");

		WebElement ticketSaleDateInput = driver.findElement(By.id("ticket_sale_date"));
		ticketSaleDateInput.clear();
		ticketSaleDateInput.sendKeys("07202024150000");

		WebElement ckEditor = driver.findElement(By.className("ck-editor__editable"));
		// Send keys to the CKEditor
		ckEditor.sendKeys("randomKeys");

		// Optionally, you can simulate special key actions
		ckEditor.sendKeys(Keys.ENTER); // Example: Press Enter key

		WebElement formElement = driver.findElement(By.cssSelector("form.form"));

		formElement.submit();
		// Assert the title or other elements to confirm event creation

		WebElement createEventV = wait.until(
				ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.btnsd-outline-success[href='/createEvent']")));
		createEventV.click();

		String title = driver.getTitle();
		assertTrue(title.contains("Expected Title")); // Replace with expected title

		// Optionally, assert other elements or perform further validation
	}
}
