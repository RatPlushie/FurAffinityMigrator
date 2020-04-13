import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FurAffinityMigrator extends JFrame{
    private JPanel mainPanel;
    private JTextField oldAccountText;
    private JTextField newAccountUsernameText;
    private JTextField newAccountPasswordText;
    private JButton migrateButton;
    private JLabel taskField;
    private JLabel oldAccountUsernameLabel;
    private JLabel newAccountUsernameLabel;
    private JLabel newAccountPasswordLabel;

    // Constructor method for creating the form
    public FurAffinityMigrator(String title){
        super(title);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();

        migrateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                // Initialising and populating the user object
                FA_User mUser = new FA_User(oldAccountText.getText(), newAccountUsernameText.getText(), newAccountPasswordText.getText());

                // Displaying hint to user - DOESN'T UPDATE, SOMETHING ABOUT BEING TOO FAST
                taskField.setText("Exporting old account's watch list, Please wait...");

                // Exporting watchlist
                mUser.exportList();

                // User assisted new account login (because of CAPTCHAS)
                mUser.login(taskField);

                // Importing watchlist into new account
                mUser.importList();

                // Displaying that the activity has been completed
                taskField.setText("Account has been migrated");
                System.out.println("Account has been migrated");
            }
        });
    }


    public static void main(String[] args) {
        JFrame frame = new FurAffinityMigrator("Fur Affinity Migrator");
        frame.setVisible(true);
    }
}


class FA_User {

    String          oldUsername;
    String          newUsername;
    String          newPassword;
    List<String>    watchList;
    WebDriver       driver;

    FA_User(String oldName, String newName, String newPassword){

        // Populating the object with info from the JFrame
        this.oldUsername = oldName;
        this.newUsername = newName;
        this.newPassword = newPassword;

        this.watchList   = new ArrayList<>();
        this.driver      = new FirefoxDriver();
    }

    void exportList(){

        // Navigating to the old accounts watchlist
        String watchlistURL = "https://www.furaffinity.net/watchlist/by/" + oldUsername + "/";
        driver.get(watchlistURL);

        boolean exportFinished = false;
        int positionCount = 1;

        // Loop to export all currently displayed usernames
        while (!exportFinished){

            try{

                // Creating the xpath string to find the username at the current position
                String currentXPath = "/html/body/div/section/div[2]/div[" + positionCount + "]/a";

                // Retrieving the username and adding it to the list
                String currentUserName = driver.findElement(By.xpath(currentXPath)).getText();
                watchList.add(currentUserName);

                // Printing system log
                System.out.println("Username Added: " + currentUserName);

                // Incrementing the position count
                positionCount++;


            } catch (Exception noUserNames){

                try {

                    // Saving the current url of the webpage for comparison
                    String currentUrl = driver.getCurrentUrl();

                    // Trying to navigate to the next page
                    driver.findElement(By.xpath("/html/body/div/section/div[3]/div[2]/form/button")).click();

                    // Comparing the URLs to see if the page hasn't changed
                    if (currentUrl.equals(driver.getCurrentUrl())){

                        throw new Exception("noPagesLeft");

                    }

                    // Resetting the postion counter for the usernames
                    positionCount = 1;

                } catch (Exception noPagesLeft){

                    // Printing out the export results
                    System.out.println("Finished Exporting");
                    System.out.println("Total watchlist size: " + watchList.size());

                    // Ending the while-loop
                    exportFinished = true;
                }
            }
        }
    }

    void login(JLabel hintLabel){

        // Navigating to the login page
        driver.get("https://www.furaffinity.net/login");

        // Inputting the new account user details
        driver.findElement(By.xpath("//*[@id=\"login\"]")).sendKeys(newUsername);
        driver.findElement(By.xpath("/html/body/div[2]/div[2]/form/div/section[1]/div[2]/input[2]")).sendKeys(newPassword);

        // Displaying to the user to defeat the captcha
        hintLabel.setText("Defeat the CAPTCHA and press login");

        // While-loop to hold program till the user has logged in
        boolean loggedIn = false;
        while (!loggedIn){

            // Testing to see if the user has successfully logged in
            if (driver.getCurrentUrl().equals("https://www.furaffinity.net/")){

                loggedIn = true;

                // Waiting 2s before trying again to not overload the driver
                driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
            }
        }

        // Printing to terminal the user has successfully logged in to their new account
        System.out.println("User has successfully logged in");
    }

    void importList(){

        /*
        Test account
        Username: test11111111
        Password: pikachu94
        */

        // For every entry in the watchList, now add that artist to the new account's list
        for (String currentArtist : watchList){

            // Creating the URL to navigate to
            String currentURL = "https://www.furaffinity.net/user/" + currentArtist + "/";
            driver.get(currentURL);

            driver.findElement(By.xpath("/html/body/div[3]/div[2]/div[1]/div[1]/div[2]/div/div[3]/div/a[1]/div")).click();

            // Waiting until the request has been sent
            boolean reqSent = false;
            while (!reqSent){

                if (!driver.getCurrentUrl().equals(currentURL)){
                    reqSent = true;
                }

                // Waiting out the web page load
                driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
            }

            // Displaying in the log which artist has been imported
            System.out.println("Imported " + currentArtist);
        }
    }
}