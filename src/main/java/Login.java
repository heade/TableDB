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
 * Created by Daniel on 21.09.2015.
 */
public class Login extends JFrame{
    private JFrame logFrame;
    private JPanel logPanel;
    private JTextField user;
    private JPasswordField password;
    private JPanel buttonPanel;
    private JButton logButton;
    private JButton regButton;
    private String usernameString;
    private String emailString;
    private String passwordString;
    private Properties props;
    //private ArrayList<String> row;
    //private ArrayList<String> passrow;

    public void log(){
        logFrame=new JFrame("Log in");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 350) / 2, (screenSize.height - 150) / 2, 350, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        logButton = new JButton("Log in");
        regButton = new JButton("Registration");
        buttonPanel = new JPanel();
        logPanel = new JPanel();
        user = new JTextField();
        password = new JPasswordField();
        logPanel.setLayout(new GridLayout(2, 2));

        logPanel.add(new JLabel("Username: ", SwingConstants.CENTER));
        logPanel.add(user);
        logPanel.add(new JLabel("Password: ", SwingConstants.CENTER));
        logPanel.add(password);
        add(logPanel, BorderLayout.NORTH);

      /*  ResultSet rs;
        Statement stmt;
        row = new ArrayList<>();
        passrow = new ArrayList<>();
        //-----------------------------список аккаунтов пользователей-----------------------
        try{
            readDatabaseProperties();
            try(Connection conn = getConnection()){
                stmt = conn.createStatement();
                rs = stmt.executeQuery("select * from registration");
                while (rs.next()) {
                    row.add(rs.getString(1));
                    passrow.add(rs.getString(3));
                }
                conn.close();
            }
        }catch (SQLException ex) {
            System.out.println(ex.toString());
        } catch (Exception t) {
            t.printStackTrace();
        }*/
        //--------------------------------------------------------------------------
        //--------------------------проверка вводимых данных------------------------
        logButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                usernameString = user.getText();
                passwordString = new String(password.getPassword());

                boolean res=false;
                boolean nullres=true;
                if (usernameString.equals("")) {
                    nullres=false;
                    JOptionPane.showMessageDialog(null,"Enter username");
                }

                try{
                    readDatabaseProperties();
                    try(Connection conn = getConnection()){
                        Statement stat = conn.createStatement();
                        Statement statpass = conn.createStatement();
                        statpass.execute("select pass from registration where pass ='"+passwordString+"'");
                        stat.execute("select username from registration where username ='"+usernameString+"'");
                        ResultSet rs = stat.getResultSet();
                        ResultSet rspass = statpass.getResultSet();

                        while(rspass.next()){
                            if (rspass.getString(1)!=null){
                                nullres = true;
                            }
                        }
                        while(rs.next()){
                            if (rs.getString(1)!=null){
                                res=true;
                            }
                        }

                        if (res&&nullres) {
                            stat.execute("select email from registration where username ='"+usernameString+"'");
                            rs = stat.getResultSet();
                            while (rs.next()){
                                emailString = rs.getString(1);
                            }
                            new ApplMainForm().TableDB(emailString, passwordString);
                            dispose();
                        }
                        if(!res) JOptionPane.showMessageDialog(null, "Wrong username or password");
                    }
                }catch (SQLException ex) {
                    System.out.println(ex.toString());
                } catch (Exception t) {
                    t.printStackTrace();
                }
            }
        });
        regButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new Registration().regName();
            }
        });
        buttonPanel.add(logButton);
        buttonPanel.add(regButton);
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
