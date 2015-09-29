import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.JFrame;

/**
 * Created by Daniel on 20.09.2015
 */
public class Registration extends JFrame {
    private JFrame regFrame;
    private JPanel regPan;
    private JPanel buttonPanel;
    private JTextField username;
    private JTextField email;
    private JPasswordField pass;
    private JPasswordField repeatpass;
    private JButton registrationButton;
    private String usernameString;
    private String emailString;
    private String passString;
    private String repeatpassString;
    private Properties props;
    //private ArrayList<String> row;
    public void regName(){
        regFrame = new JFrame("Registration");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 350) / 2, (screenSize.height - 200) / 2, 350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        regPan = new JPanel();
        buttonPanel = new JPanel();
        username = new JTextField();
        email = new JTextField();
        pass = new JPasswordField();
        repeatpass = new JPasswordField();
        registrationButton = new JButton("Registration");
        regPan.setLayout(new GridLayout(4, 2));

        regPan.add(new JLabel("Username: ", SwingConstants.CENTER));
        regPan.add(username);
        regPan.add(new JLabel("Your e-mail: ", SwingConstants.CENTER));
        regPan.add(email);
        regPan.add(new JLabel("Password: ", SwingConstants.CENTER));
        regPan.add(pass);
        regPan.add(new JLabel("Repeat password: ", SwingConstants.CENTER));
        regPan.add(repeatpass);
        add(regPan, BorderLayout.NORTH);

        //-----------------------------------------------------------------------------------------
        //-------------------------регестрация нового пользователя---------------------------------
        registrationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                usernameString = username.getText();
                emailString = email.getText();
                passString = new String(pass.getPassword());
                repeatpassString = new String(repeatpass.getPassword());
                boolean userres = true;
                boolean res=true;
                boolean nullres=true;

                try{
                    readDatabaseProperties();
                    try(Connection conn = getConnection()){
                        Statement stat = conn.createStatement();
                        Statement usernamestat = conn.createStatement();
                        usernamestat.execute("select username from registration where username ='"+usernameString+"'");
                        stat.execute("select email from registration where email ='"+emailString+"'");
                        ResultSet userrs = usernamestat.getResultSet();
                        ResultSet rs = stat.getResultSet();

                        while(userrs.next()){
                            if(userrs.getString(1)!=null){
                                userres = false;
                            }
                        }
                        while(rs.next()){
                            if (rs.getString(1)!=null){
                                res=false;
                            }
                        }
                        if (emailString.equals("")) {
                            nullres=false;
                            JOptionPane.showMessageDialog(null,"Enter email");
                        }
                        if (res&&nullres&&userres) {
                            if (passString.equals(repeatpassString)) {
                                stat.execute("insert into registration(username,email,pass) values('" + usernameString + "', '"
                                        + emailString + "', '" + passString + "')");
                                dispose();
                                new ApplMainForm().TableDB(emailString, passString);
                            } else JOptionPane.showMessageDialog(null, "Wrong repeat password");
                            if(passString.equals("")) JOptionPane.showMessageDialog(null, "Enter password");
                        }
                        if(!userres) JOptionPane.showMessageDialog(null, "Username already registrated");
                        if(!res) JOptionPane.showMessageDialog(null, "Email already registrated");
                    }
                }catch (SQLException ex) {
                    System.out.println(ex.toString());
                } catch (Exception t) {
                    t.printStackTrace();
                }
            }
        });
        //--------------------------------------------------------------------------------------------
        buttonPanel.add(registrationButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }


    private void readDatabaseProperties() throws IOException {
        props = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get("src\\main\\resources\\database.properties.txt"))) {
            props.load(in);
        }
        String drivers = props.getProperty("jdbc.drivers");
        if (drivers != null) System.setProperty("jdbc.drivers", drivers);
    }

    /**
     * Gets a connection from the properties specified in the file database.properties.
     * @return the database connection
     */
    private Connection getConnection() throws SQLException {
        String url = props.getProperty("jdbc.url");
        String username = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");

        return DriverManager.getConnection(url, username, password);
    }
}
