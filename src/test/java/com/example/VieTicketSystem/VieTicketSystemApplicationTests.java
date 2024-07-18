package com.example.VieTicketSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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

	// @Test
	// public void testEventCreation() {
	// // Open the login page
	// driver.get("http://localhost:8080/auth/login");
	// // Fill in username and password
	// WebElement username = driver.findElement(By.id("username"));
	// username.sendKeys("o1");
	// WebElement password = driver.findElement(By.id("password"));
	// password.sendKeys("123");
	// // Click login button
	// WebElement loginButton =
	// driver.findElement(By.xpath("//button[text()='Login']"));
	// loginButton.click();
	// // Wait for the dashboard or landing page to load
	// WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	// WebElement createEventLink = wait.until(
	// ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.btn.btn-outline-success[href='/createEvent']")));
	// createEventLink.click();
	// // Upload Banner
	// WebElement bannerUpload = driver.findElement(By.name("banner"));
	// bannerUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png"); // Replace
	// with actual path
	// // Upload Poster
	// WebElement posterUpload = driver.findElement(By.name("poster"));
	// posterUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png"); // Replace
	// with actual path
	// // Fill in Event details
	// WebElement nameInput = driver.findElement(By.id("name"));
	// nameInput.sendKeys("Random Event Name");
	// WebElement locationInput = driver.findElement(By.id("location"));
	// locationInput.sendKeys("Random Location");
	// WebElement startDateInput = driver.findElement(By.id("start_date"));
	// startDateInput.clear();
	// startDateInput.sendKeys("07202024\t130000");
	// WebElement endDateInput = driver.findElement(By.id("end_date"));
	// endDateInput.clear();
	// endDateInput.sendKeys("07202024\t190000");
	// Select typeSelect = new Select(driver.findElement(By.id("type")));
	// typeSelect.selectByValue("music");
	// WebElement ticketSaleDateInput =
	// driver.findElement(By.id("ticket_sale_date"));
	// ticketSaleDateInput.clear();
	// ticketSaleDateInput.sendKeys("07202024150000");
	// WebElement ckEditor =
	// driver.findElement(By.className("ck-editor__editable"));
	// // Send keys to the CKEditor
	// ckEditor.sendKeys("randomKeys");
	// // Optionally, you can simulate special key actions
	// ckEditor.sendKeys(Keys.ENTER); // Example: Press Enter key
	// WebElement formElement = driver.findElement(By.cssSelector("form.form"));
	// formElement.submit();
	// // Assert the title or other elements to confirm event creation
	// String title = driver.getTitle();
	// assertTrue(title.contains("Seat Map Selection")); // Replace with expected
	// title
	// }

	// @Test
	// public void testStartDateNull() {
	// driver.get("http://localhost:8080/auth/login");
	// WebElement username = driver.findElement(By.id("username"));
	// username.sendKeys("o1");
	// WebElement password = driver.findElement(By.id("password"));
	// password.sendKeys("123");
	// WebElement loginButton =
	// driver.findElement(By.xpath("//button[text()='Login']"));
	// loginButton.click();
	// WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	// WebElement createEventLink = wait.until(
	// ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.btn.btn-outline-success[href='/createEvent']")));
	// createEventLink.click();
	// WebElement bannerUpload = driver.findElement(By.name("banner"));
	// bannerUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png");
	// WebElement posterUpload = driver.findElement(By.name("poster"));
	// posterUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png");
	// WebElement nameInput = driver.findElement(By.id("name"));
	// nameInput.sendKeys("Random Event Name");
	// WebElement locationInput = driver.findElement(By.id("location"));
	// locationInput.sendKeys("Random Location");
	// WebElement endDateInput = driver.findElement(By.id("end_date"));
	// endDateInput.clear();
	// endDateInput.sendKeys("07202024\t190000");
	// Select typeSelect = new Select(driver.findElement(By.id("type")));
	// typeSelect.selectByValue("music");
	// WebElement ticketSaleDateInput =
	// driver.findElement(By.id("ticket_sale_date"));
	// ticketSaleDateInput.clear();
	// ticketSaleDateInput.sendKeys("07202024150000");
	// WebElement ckEditor =
	// driver.findElement(By.className("ck-editor__editable"));
	// ckEditor.sendKeys("randomKeys");
	// ckEditor.sendKeys(Keys.ENTER); // Example: Press Enter key
	// WebElement createEventButton =
	// driver.findElement(By.xpath("//input[@type='submit' and @class='btn
	// btn-primary']"));
	// ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
	// createEventButton);
	// wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input:invalid")));
	// WebElement invalidElement =
	// driver.findElement(By.cssSelector("input:invalid"));
	// String validationMessage = invalidElement.getAttribute("validationMessage");
	// assertEquals(validationMessage, "Please fill out this field.");
	// }

	// @Test
	// public void testEndDateValidation() {
	// driver.get("http://localhost:8080/auth/login");
	// WebElement username = driver.findElement(By.id("username"));
	// username.sendKeys("o1");
	// WebElement password = driver.findElement(By.id("password"));
	// password.sendKeys("123");
	// WebElement loginButton =
	// driver.findElement(By.xpath("//button[text()='Login']"));
	// loginButton.click();
	// WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	// WebElement createEventLink = wait.until(
	// ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.btn.btn-outline-success[href='/createEvent']")));
	// createEventLink.click();
	// WebElement bannerUpload = driver.findElement(By.name("banner"));
	// bannerUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png");
	// WebElement posterUpload = driver.findElement(By.name("poster"));
	// posterUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png");
	// WebElement nameInput = driver.findElement(By.id("name"));
	// nameInput.sendKeys("Random Event Name");
	// WebElement locationInput = driver.findElement(By.id("location"));
	// locationInput.sendKeys("Random Location");
	// WebElement startDateInput = driver.findElement(By.id("start_date"));
	// startDateInput.clear();
	// startDateInput.sendKeys("07202024\t140000");
	// WebElement endDateInput = driver.findElement(By.id("end_date"));
	// endDateInput.clear();
	// endDateInput.sendKeys("07202024\t130000");
	// Select typeSelect = new Select(driver.findElement(By.id("type")));
	// typeSelect.selectByValue("music");
	// WebElement ticketSaleDateInput =
	// driver.findElement(By.id("ticket_sale_date"));
	// ticketSaleDateInput.clear();
	// ticketSaleDateInput.sendKeys("07202024150000");
	// WebElement ckEditor =
	// driver.findElement(By.className("ck-editor__editable"));
	// ckEditor.sendKeys("randomKeys");
	// ckEditor.sendKeys(Keys.ENTER); // Example: Press Enter key
	// WebElement createEventButton =
	// driver.findElement(By.xpath("//input[@type='submit' and @class='btn
	// btn-primary']"));
	// ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
	// createEventButton);
	// WebElement alertMessage = driver.findElement(By.id("alertMessage"));
	// String alertText = alertMessage.getText();
	// assertEquals(alertText, "End Date must be in the present or future and not
	// earlier than the Start Date.");
	// }

	// @Test
	// public void testEndDateEmpty() {
	// driver.get("http://localhost:8080/auth/login");
	// WebElement username = driver.findElement(By.id("username"));
	// username.sendKeys("o1");
	// WebElement password = driver.findElement(By.id("password"));
	// password.sendKeys("123");
	// WebElement loginButton =
	// driver.findElement(By.xpath("//button[text()='Login']"));
	// loginButton.click();
	// WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	// WebElement createEventLink = wait.until(
	// ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.btn.btn-outline-success[href='/createEvent']")));
	// createEventLink.click();
	// WebElement bannerUpload = driver.findElement(By.name("banner"));
	// bannerUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png");
	// WebElement posterUpload = driver.findElement(By.name("poster"));
	// posterUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png");
	// WebElement nameInput = driver.findElement(By.id("name"));
	// nameInput.sendKeys("Random Event Name");
	// WebElement locationInput = driver.findElement(By.id("location"));
	// locationInput.sendKeys("Random Location");
	// WebElement startDateInput = driver.findElement(By.id("start_date"));
	// startDateInput.clear();
	// startDateInput.sendKeys("07202024\t140000");
	// Select typeSelect = new Select(driver.findElement(By.id("type")));
	// typeSelect.selectByValue("music");
	// WebElement ticketSaleDateInput =
	// driver.findElement(By.id("ticket_sale_date"));
	// ticketSaleDateInput.clear();
	// ticketSaleDateInput.sendKeys("07202024150000");
	// WebElement ckEditor =
	// driver.findElement(By.className("ck-editor__editable"));
	// ckEditor.sendKeys("randomKeys");
	// ckEditor.sendKeys(Keys.ENTER); // Example: Press Enter key
	// WebElement createEventButton =
	// driver.findElement(By.xpath("//input[@type='submit' and @class='btn
	// btn-primary']"));
	// ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
	// createEventButton);
	// wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input:invalid")));
	// WebElement invalidElement =
	// driver.findElement(By.cssSelector("input:invalid"));
	// String validationMessage = invalidElement.getAttribute("validationMessage");
	// assertEquals(validationMessage, "Please fill out this field.");
	// }
	@Test
	public void testLoginWithRoleOrganizer() {
		driver.get("http://localhost:8080/auth/login");

		WebElement username = driver.findElement(By.id("username"));
		username.sendKeys("o");// o1

		WebElement password = driver.findElement(By.id("password"));
		password.sendKeys("a");// 123

		WebElement loginButton = driver.findElement(By.xpath("//button[text()='Login']"));
		loginButton.click();

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		WebElement createEventLink = wait.until(
				ExpectedConditions
						.presenceOfElementLocated(By.cssSelector("a.btn.btn-outline-success[href='/createEvent']")));

		assertTrue(createEventLink.isDisplayed());
	}

	@Test
	public void testLoginWithRoleUser() {
		driver.get("http://localhost:8080/auth/login");

		WebElement username = driver.findElement(By.id("username"));
		username.sendKeys("u"); // User credentials

		WebElement password = driver.findElement(By.id("password"));
		password.sendKeys("a");

		WebElement loginButton = driver.findElement(By.xpath("//button[text()='Login']"));
		loginButton.click();

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

		List<WebElement> createEventLink = driver
				.findElements(By.cssSelector("a.btn.btn-outline-success[href='/createEvent']"));

		assertTrue(createEventLink.isEmpty());
	}

	@Test
	public void testLoginWithRoleAdmin() {
		driver.get("http://localhost:8080/auth/login");

		WebElement username = driver.findElement(By.id("username"));
		username.sendKeys("a"); // Admin credentials

		WebElement password = driver.findElement(By.id("password"));
		password.sendKeys("a");

		WebElement loginButton = driver.findElement(By.xpath("//button[text()='Login']"));
		loginButton.click();

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		WebElement dashboardHeader = wait.until(
				ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='row mt-4']//h1[text()='Dashboard']")));

		assertTrue(dashboardHeader.isDisplayed());
	}

	// @Test
	// public void testStartDateNull() {
	// driver.get("http://localhost:8080/auth/login");
	// WebElement username = driver.findElement(By.id("username"));
	// username.sendKeys("o1");
	// WebElement password = driver.findElement(By.id("password"));
	// password.sendKeys("123");
	// WebElement loginButton =
	// driver.findElement(By.xpath("//button[text()='Login']"));
	// loginButton.click();
	// WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	// WebElement createEventLink = wait.until(
	// ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.btn.btn-outline-success[href='/createEvent']")));
	// createEventLink.click();
	// WebElement bannerUpload = driver.findElement(By.name("banner"));
	// bannerUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png");
	// WebElement posterUpload = driver.findElement(By.name("poster"));
	// posterUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png");
	// WebElement nameInput = driver.findElement(By.id("name"));
	// nameInput.sendKeys("Random Event Name");
	// WebElement locationInput = driver.findElement(By.id("location"));
	// locationInput.sendKeys("Random Location");
	// WebElement endDateInput = driver.findElement(By.id("end_date"));
	// endDateInput.clear();
	// endDateInput.sendKeys("07202024\t190000");
	// Select typeSelect = new Select(driver.findElement(By.id("type")));
	// typeSelect.selectByValue("music");
	// WebElement ticketSaleDateInput =
	// driver.findElement(By.id("ticket_sale_date"));
	// ticketSaleDateInput.clear();
	// ticketSaleDateInput.sendKeys("07202024150000");
	// WebElement ckEditor =
	// driver.findElement(By.className("ck-editor__editable"));
	// ckEditor.sendKeys("randomKeys");
	// ckEditor.sendKeys(Keys.ENTER); // Example: Press Enter key
	// WebElement createEventButton =
	// driver.findElement(By.xpath("//input[@type='submit' and @class='btn
	// btn-primary']"));
	// ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
	// createEventButton);
	// wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input:invalid")));
	// WebElement invalidElement =
	// driver.findElement(By.cssSelector("input:invalid"));
	// String validationMessage = invalidElement.getAttribute("validationMessage");
	// assertEquals(validationMessage, "Please fill out this field.");
	// }

	// @Test
	// public void testEndDateValidation() {
	// driver.get("http://localhost:8080/auth/login");
	// WebElement username = driver.findElement(By.id("username"));
	// username.sendKeys("o1");
	// WebElement password = driver.findElement(By.id("password"));
	// password.sendKeys("123");
	// WebElement loginButton =
	// driver.findElement(By.xpath("//button[text()='Login']"));
	// loginButton.click();
	// WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	// WebElement createEventLink = wait.until(
	// ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.btn.btn-outline-success[href='/createEvent']")));
	// createEventLink.click();
	// WebElement bannerUpload = driver.findElement(By.name("banner"));
	// bannerUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png");
	// WebElement posterUpload = driver.findElement(By.name("poster"));
	// posterUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png");
	// WebElement nameInput = driver.findElement(By.id("name"));
	// nameInput.sendKeys("Random Event Name");
	// WebElement locationInput = driver.findElement(By.id("location"));
	// locationInput.sendKeys("Random Location");
	// WebElement startDateInput = driver.findElement(By.id("start_date"));
	// startDateInput.clear();
	// startDateInput.sendKeys("07202024\t140000");
	// WebElement endDateInput = driver.findElement(By.id("end_date"));
	// endDateInput.clear();
	// endDateInput.sendKeys("07202024\t130000");
	// Select typeSelect = new Select(driver.findElement(By.id("type")));
	// typeSelect.selectByValue("music");
	// WebElement ticketSaleDateInput =
	// driver.findElement(By.id("ticket_sale_date"));
	// ticketSaleDateInput.clear();
	// ticketSaleDateInput.sendKeys("07202024150000");
	// WebElement ckEditor =
	// driver.findElement(By.className("ck-editor__editable"));
	// ckEditor.sendKeys("randomKeys");
	// ckEditor.sendKeys(Keys.ENTER); // Example: Press Enter key
	// WebElement createEventButton =
	// driver.findElement(By.xpath("//input[@type='submit' and @class='btn
	// btn-primary']"));
	// ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
	// createEventButton);
	// WebElement alertMessage = driver.findElement(By.id("alertMessage"));
	// String alertText = alertMessage.getText();
	// assertEquals(alertText, "End Date must be in the present or future and not
	// earlier than the Start Date.");
	// }

	// @Test
	// public void testEndDateEmpty() {
	// driver.get("http://localhost:8080/auth/login");
	// WebElement username = driver.findElement(By.id("username"));
	// username.sendKeys("o1");
	// WebElement password = driver.findElement(By.id("password"));
	// password.sendKeys("123");
	// WebElement loginButton =
	// driver.findElement(By.xpath("//button[text()='Login']"));
	// loginButton.click();
	// WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	// WebElement createEventLink = wait.until(
	// ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.btn.btn-outline-success[href='/createEvent']")));
	// createEventLink.click();
	// WebElement bannerUpload = driver.findElement(By.name("banner"));
	// bannerUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png");
	// WebElement posterUpload = driver.findElement(By.name("poster"));
	// posterUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png");
	// WebElement nameInput = driver.findElement(By.id("name"));
	// nameInput.sendKeys("Random Event Name");
	// WebElement locationInput = driver.findElement(By.id("location"));
	// locationInput.sendKeys("Random Location");
	// WebElement startDateInput = driver.findElement(By.id("start_date"));
	// startDateInput.clear();
	// startDateInput.sendKeys("07202024\t140000");
	// Select typeSelect = new Select(driver.findElement(By.id("type")));
	// typeSelect.selectByValue("music");
	// WebElement ticketSaleDateInput =
	// driver.findElement(By.id("ticket_sale_date"));
	// ticketSaleDateInput.clear();
	// ticketSaleDateInput.sendKeys("07202024150000");
	// WebElement ckEditor =
	// driver.findElement(By.className("ck-editor__editable"));
	// ckEditor.sendKeys("randomKeys");
	// ckEditor.sendKeys(Keys.ENTER); // Example: Press Enter key
	// WebElement createEventButton =
	// driver.findElement(By.xpath("//input[@type='submit' and @class='btn
	// btn-primary']"));
	// ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
	// createEventButton);
	// wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input:invalid")));
	// WebElement invalidElement =
	// driver.findElement(By.cssSelector("input:invalid"));
	// String validationMessage = invalidElement.getAttribute("validationMessage");
	// assertEquals(validationMessage, "Please fill out this field.");
	// }

	@Test
	public void testTicketSaleDateEmpty() {
		driver.get("http://localhost:8080/auth/login");
		WebElement username = driver.findElement(By.id("username"));
		username.sendKeys("o1");
		WebElement password = driver.findElement(By.id("password"));
		password.sendKeys("123");
		WebElement loginButton = driver.findElement(By.xpath("//button[text()='Login']"));
		loginButton.click();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		WebElement createEventLink = wait.until(
				ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.btn.btn-outline-success[href='/createEvent']")));
		createEventLink.click();
		WebElement bannerUpload = driver.findElement(By.name("banner"));
		bannerUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png");
		WebElement posterUpload = driver.findElement(By.name("poster"));
		posterUpload.sendKeys("C:\\Users\\ACER\\Downloads\\haizz.png");
		WebElement nameInput = driver.findElement(By.id("name"));
		nameInput.sendKeys("Random Event Name");
		WebElement locationInput = driver.findElement(By.id("location"));
		locationInput.sendKeys("Random Location");
		WebElement startDateInput = driver.findElement(By.id("start_date"));
		startDateInput.clear();
		startDateInput.sendKeys("07202024\t140000");
		WebElement endDateInput = driver.findElement(By.id("end_date"));
		endDateInput.clear();
		endDateInput.sendKeys("07202024\t190000");
		Select typeSelect = new Select(driver.findElement(By.id("type")));
		typeSelect.selectByValue("music");
		WebElement ckEditor = driver.findElement(By.className("ck-editor__editable"));
		ckEditor.sendKeys("randomKeys");
		ckEditor.sendKeys(Keys.ENTER); // Example: Press Enter key
		WebElement createEventButton = driver.findElement(By.xpath("//input[@type='submit' and @class='btn btn-primary']"));
		((JavascriptExecutor) driver).executeScript("arguments[0].click();",
				createEventButton);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input:invalid")));
		WebElement invalidElement = driver.findElement(By.cssSelector("input:invalid"));
		String validationMessage = invalidElement.getAttribute("validationMessage");
		assertEquals(validationMessage, "Please fill out this field.");
	}

}
