package com.crossover.e2e;

import com.google.common.base.Function;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;



public class GMailTest extends TestCase {
    private WebDriver driver;
    private Properties properties = new Properties();
    private WebDriverWait w;
    private int EXPLICIT_TIMEOUT = 30;
    private String userName;
    private String password;
    private ExtentReports report;
    private ExtentTest test;
    private String subjectText = "A Generic Subject";
    private String eMailText = "This is an email used for testing purposes";

    @BeforeClass
    public void setUp() throws Exception {
        System.setProperty("webdriver.chrome.driver", "chromedriver");
        driver = new ChromeDriver();
        report = new ExtentReports("./ExtentReportResults.html");
        properties.load(new FileReader(new File("test.properties")));
        test = report.startTest("testSendEmail");
        test.log(LogStatus.INFO, "Beginning Test Execution");
        w = new WebDriverWait(driver, EXPLICIT_TIMEOUT);
    }

    @AfterClass
    public void tearDown() throws Exception {
        test.log(LogStatus.INFO, "Ending Test Execution");
        report.endTest(test);
        report.flush();

        driver.quit();
    }

    @Test
    public void testSendEmail() throws Exception {

        try {
            driver.get("https://mail.google.com/");

            password = properties.getProperty("password");
            userName = properties.getProperty("username");

            /*  LOGIN TO GMAIL                                                                                          */
            userName = properties.getProperty("username") + "@gmail.com";
            WebElement userElement = w.until(ExpectedConditions.visibilityOf(driver.findElement(By.name("identifier"))));

            userElement.sendKeys(userName);
            test.log(LogStatus.PASS, "User Name was Entered as: " + userName);

            WebElement next = driver.findElement(By.cssSelector("#identifierNext > content > span"));
            w.until(ExpectedConditions.visibilityOf(next));
            next.click();
            test.log(LogStatus.PASS, "The 'Next' button was clicked.");

            WebElement passwordElement = w.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));
            passwordElement.sendKeys(properties.getProperty("password"));
            test.log(LogStatus.PASS, "The password was entered.");

            WebElement passwordNext = w.until(ExpectedConditions.visibilityOfElementLocated(By.id("passwordNext")));
            passwordNext.click();
            test.log(LogStatus.PASS, "The 'Next' button was clicked.");

            /*  Compose an email with unique subject and body */
            WebElement compose = w.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[gh*='cm']")));
            compose.click();
            test.log(LogStatus.PASS, "The 'Compose' button was clicked");

            WebElement toRecipient = w.until(ExpectedConditions.visibilityOfElementLocated(By.name("to")));
            toRecipient.clear();
            toRecipient.sendKeys(String.format("%s@gmail.com", properties.getProperty("username")));
            test.log(LogStatus.PASS, "The 'Recipients email was entered as: " + userName);

            WebElement subject = w.until(ExpectedConditions.visibilityOfElementLocated(By.name("subjectbox")));
            subject.sendKeys(subjectText);
            test.log(LogStatus.PASS, "The email 'Subject' was entered as: " + subjectText);

            WebElement message = w.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[class*='editable LW']")));
            message.sendKeys(eMailText);
            test.log(LogStatus.PASS, "The email 'Body' was entered as: " + eMailText);

            WebElement send = w.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-tooltip*='Send']")));
            send.click();
            test.log(LogStatus.PASS, "The 'Send' button was clicked.");

            waitForPageLoaded();

            /* Wait for the email to arrive in the Inbox                   */
            w.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\":2v\"]")));
            List<WebElement> gmails = driver.findElements(By.xpath("//*/span[@class='bqe'][contains(text(),'A Generic Subject')]"));
            for (WebElement gmail : gmails) {
                if (gmail.getText().contains(subjectText)) {
                    gmail.click();
                    test.log(LogStatus.PASS, "The email was successfully sent to: " + userName);
                    break;
                }
            }

            /* Mark email as starred                                       */
            WebElement star = w.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[@class='T-KT']")));
            Thread.sleep(1000);
            star.click();
            test.log(LogStatus.PASS, "The email was Starred.");

            /*  Label the email as "Social"          */
            WebElement labelIcon = w.until(ExpectedConditions.elementToBeClickable(By.xpath("(.//*[normalize-space(.)='More'])[4]/preceding::div[3]")));
            Thread.sleep(1000);
            labelIcon.click();
            test.log(LogStatus.PASS, "The 'label' dropdown was selected.");

            /* Verify email came under proper Label i.e. "Social"          */
            WebElement checkBoxSelection = w.until(ExpectedConditions.elementToBeClickable(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Social'])[3]")));
            Thread.sleep(1000);
            checkBoxSelection.click();

            test.log(LogStatus.PASS, "The email was labeled as: Social.");


            WebElement inboxLink = w.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@title='Inbox']")));
            inboxLink.click();


        }catch(Throwable ex){
            test.log(LogStatus.ERROR,ex.getCause());
        }finally {
            System.out.println("The test execution summary and detail report is located in the project root folder and is named: 'ExtentReportResults.html'");
            driver.close();
        }
    }

    public  void waitForPageLoaded() {
        test.log(LogStatus.INFO, "Waiting for page to load");
        ExpectedCondition<Boolean> expectation = driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete");
        try {
            Thread.sleep(8000);
            WebDriverWait wait = new WebDriverWait(driver, 30);
            wait.until(expectation);
        } catch (Throwable error) {
            test.log(LogStatus.FAIL,("Timeout waiting for Page Load Request to complete."));
        }
    }

}