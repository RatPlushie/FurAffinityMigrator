import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class FurAffinityMigrator extends JFrame{
    private JPanel mainPanel;
    private JTextField oldAccountText;
    private JTextField newAccountUsernameText;
    private JTextField newAccountPasswordText;
    private JButton migrateButton;
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

                // Exporting watchlist
                mUser.exportList();

                // User assisted new account login (because of CAPTCHAS)
                mUser.login();

                // Importing watchlist into new account
                mUser.importList();

                // Navigating the user to the login page for the new account
                // TODO - nav user to login page
                mUser.login();

                // TODO - Change button to "i have logged in 'ok'". Wait for user to press "ok"


                // TODO - with user logged in, now follow every account in the list
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
    List<String> watchList = new ArrayList<>();

    FA_User(String oldName, String newName, String newPassword){

        // Populating the object with info from the JFrame
        oldUsername = oldName;
        newUsername = newName;
        newPassword = newPassword;
    }

    void exportList(){
        // Initialising WebDriver
        WebDriver driver = new FirefoxDriver();

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
                watchList.add(driver.findElement(By.xpath(currentXPath)).getText());

                // Printing system log
                System.out.println("Username Added: " + driver.findElement(By.xpath(currentXPath)).getText());

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

    void login(){
        // TODO - Add User login logic
    }

    void importList(){
        // TODO - Add watchlist import logic
    }
}