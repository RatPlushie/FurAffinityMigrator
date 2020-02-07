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
    private JProgressBar logProgressBar;


    // Constructor method for creating the form
    public FurAffinityMigrator(String title){
        super(title);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();

        migrateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                // Enabling the progress bar on button click
                logProgressBar.setEnabled(true);

                // Initialising and populating the user object
                FA_User mUser = new FA_User(oldAccountText.getText(), newAccountUsernameText.getText(), newAccountPasswordText.getText());

                // Exporting watchlist
                mUser.exportList(logProgressBar);

                // User assisted new account login (because of CAPTCHAS)
                mUser.login();

                // Importing watchlist into new account
                mUser.importList();
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
    int             numberFollowers;
    int             followerPageCount;
    int             followerCountProgress;
    List<String> watchList = new ArrayList<>();

    FA_User(String oldName, String newName, String newPassword){

        // Populating the object with info from the JFrame
        oldUsername = oldName;
        newUsername = newName;
        newPassword = newPassword;
    }

    void exportList(JProgressBar progressBar){
        // Initialising WebDriver
        WebDriver driver = new FirefoxDriver();

        // Navigating to old account page to retrieve info
        String userPageURL = "http://www.furaffinity.net/user/" + oldUsername + "/";
        driver.get(userPageURL);

        // TODO - Add invalid username error handling

        // Maximising the internet window for better visibility
        driver.manage().window().maximize();

        // Finding out how many people the user is following
        numberFollowers = Integer.parseInt(driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[3]/div/div[1]/div/section[4]/div/div[2]/span")).getText());

        // Calculating how many pages of followers there are
        followerPageCount = numberFollowers / 200;

        // Export progress counter
        followerCountProgress = 0;

        // Integrating through the page(s) and recording all present artists
        for (int i = 1; i <= followerPageCount + 1; i++){

            // Navigating to user's watchlist
            String userWatchListURL = "http://www.furaffinity.net/watchlist/by/" + oldUsername + "/" + i + "/";
            driver.get(userWatchListURL);

            // Iterating through the current page of watched accounts
            for (int y = 1; y <= 200; y++){
                if (followerCountProgress == numberFollowers){ // Once all watched accounts have been listed - End ForLoop
                    break;

                } else {
                    // Finding the next artist and adding them to the watchlist
                    String xpathString = "/html/body/div/section/div[2]/div[" + y + "]/a";
                    if (driver.findElement(By.xpath(xpathString)).isDisplayed()){
                        watchList.add(driver.findElement(By.xpath(xpathString)).getText());

                        /* TODO - Get progress bar to work

                        // Progressbar
                        int progressPercentage = (followerCountProgress / numberFollowers ) * 100;
                        String progressString = ("Exporting [" + followerCountProgress + "/" + numberFollowers + "] - " + driver.findElement(By.xpath(xpathString)).getText());

                        progressBar.setValue(progressPercentage);
                        progressBar.setString(progressString);
                        */

                    }
                }
            }
        }

        // Wont need later, remember to remove
        driver.quit();
    }

    void login(){
        // TODO - Add User login logic
    }

    void importList(){
        // TODO - Add watchlist import logic
    }
}