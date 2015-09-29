import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;

class ApplMainForm extends JFrame{
    private Properties props;
    private JButton updateButton;
    private JPanel panelButton;
    private JFrame jf;
    private JButton okButton;
    private JPanel button;
    private JPanel cmd;
    private JPanel label;
    private JTextArea command;
    private JTextField textfield;
    private JTextField sendto;
    private JPasswordField passwordfield;
    public String nameTable;
    private String id;
    private String headersString;
    private String valuesString;
    private String table;
    private JTable grid;
    private String copyemail;
    private String copypassword;

    public void TableDB(final String emailString, final String passString){
        copyemail = emailString;
        copypassword = passString;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DefaultTableModel dtm = null;
        try{
            readDatabaseProperties();
            try (Connection conn = getConnection()){
                pstmt = conn.prepareStatement("select * from " + nameTable);
                dtm = new DefaultTableModel();
                //-------------------------------------------
                if (pstmt.execute()) {
                    rs = pstmt.getResultSet();
                    //Формируем заголовки столбцов из названия полей -->
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();

                    //считываем имена колонок
                    headersString = "";
                    table = "";
                    for (int i = 1; i <= columnCount; i++) {
                        headersString+=rsmd.getColumnLabel(i);
                        headersString+=",";
                    }
                    headersString = headersString.substring(0, headersString.length()-1);

                    for (int col = 1; col <= rsmd.getColumnCount(); col++)
                        dtm.addColumn(rsmd.getColumnName(col));
                    while (rs.next()) {

                        Vector<String> row = new Vector<String>(); //Строка таблицы
                    /*Снова с помощью метаданных узнаем количество столбцов
                     * в результате запроса и ниже в switch'е в зависимости
                     * от типа текущего столбца забираем данные из ResultSet`a и пишем в
                     * текущую строку - row.
                     * */
                        for (int col = 1; col <= rsmd.getColumnCount(); col++) {
                            int type = rsmd.getColumnType(col);
                            switch (type) {
                                case Types.INTEGER:
                                    row.add(new Integer(rs.getInt(col)).toString());
                                    break;
                                case Types.CHAR:
                                case Types.VARCHAR:
                                    row.add(rs.getString(col));
                                    break;
                                default:
                                /*В этой моей тестовой таблице всего два типа полей: целое и строка.
                                 *Соответственно если в таблице/запросе типов больше, то этот switch
                                 *нужно расширить соответствующими типами.
                                 **/
                                    throw new Exception("Неподдерживаемый тип");
                            }
                        }
                        dtm.addRow(row);
                    }
                }
                conn.close();
            }
        }
        catch (SQLException ex) {
            System.out.println(ex.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try (Connection conn = getConnection()){
                if (pstmt != null)
                    pstmt.close();
                if (conn != null)
                    conn.close();
            }
            catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        //------------------------------------------------------------------
        JMenuBar menuBar = new JMenuBar();
        JMenu filemenu = new JMenu("File");
        JMenu optionmenu = new JMenu("Option");

        optionmenu.add(new AbstractAction("Refresh") {
            @Override
            public void actionPerformed(ActionEvent e) {
                jf.dispose();
                TableDB(copyemail, copypassword);
            }
        });
        //------------------------------------------------------------------------
        optionmenu.add(new AbstractAction("Delete") {
           @Override
            public void actionPerformed(ActionEvent e) {
                final JFrame newframe = new JFrame("Delete");
                setDefaultCloseOperation(newframe.DISPOSE_ON_CLOSE);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                newframe.setBounds((screenSize.width - 240) / 2, (screenSize.height - 100) / 2, 240, 100);
                newframe.setVisible(true);

                cmd = new JPanel();
                button = new JPanel();
                command = new JTextArea(1,4);

                newframe.add(cmd, BorderLayout.NORTH);
                newframe.add(button, BorderLayout.SOUTH);
                cmd.add(new JLabel("Enter id of row: "), SwingConstants.CENTER);
                cmd.add(command);

                okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                   @Override
                    public void actionPerformed(ActionEvent e) {
                        deleteRow();
                        newframe.dispose();
                    }
                });
                button.add(okButton);
            }
        });
        //------------------------------------------------------------------------
        optionmenu.add(new AbstractAction("Add") {
           @Override
            public void actionPerformed(ActionEvent e) {
                final JFrame newframe = new JFrame("Add");
                setDefaultCloseOperation(newframe.DISPOSE_ON_CLOSE);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                newframe.setBounds((screenSize.width - 640) / 2, (screenSize.height - 100) / 2, 640, 100);
                newframe.setVisible(true);

                cmd = new JPanel();
                button = new JPanel();
                command = new JTextArea(1,30);

                newframe.add(cmd, BorderLayout.NORTH);
                newframe.add(button, BorderLayout.SOUTH);
                cmd.add(new JLabel("Enter values (" + headersString + "): ", SwingConstants.CENTER));
                cmd.add(command);

                okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        addRow();
                        newframe.dispose();
                    }
                });
                button.add(okButton);
            }
        });
        //------------------------------------------------------------------------
        optionmenu.add(new AbstractAction("Send") {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFrame newframe = new JFrame("Send");
                setDefaultCloseOperation(newframe.DISPOSE_ON_CLOSE);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                newframe.setBounds((screenSize.width - 430) / 2, (screenSize.height - 100) / 2, 430, 100);
                newframe.setVisible(true);

                textfield = new JTextField();
                passwordfield = new JPasswordField();
                sendto = new JTextField();
                button = new JPanel();
                cmd = new JPanel();
                cmd.setLayout(new GridLayout(1, 2));
                newframe.add(cmd, BorderLayout.NORTH);
                newframe.add(button, BorderLayout.SOUTH);

                cmd.add(new JLabel("Send to: ", SwingConstants.CENTER));
                cmd.add(sendto);

                okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new Sender(emailString, passString).send("Table", headersString +"\n"+ sendTable(),
                                emailString,
                                new String(sendto.getText()));
                        newframe.dispose();

                    }
                });
                button.add(okButton);
            }
        });
        //------------------------------------------------------------------------
        optionmenu.add(new AbstractAction("Print") {
           @Override
            public void actionPerformed(ActionEvent e) {
                new PrintTable().printtable(grid);
            }
        });
        //------------------------------------------------------------------------
        filemenu.add(new AbstractAction("New command") {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFrame newframe = new JFrame("New command");
                setDefaultCloseOperation(newframe.DISPOSE_ON_CLOSE);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                newframe.setBounds((screenSize.width - 480) / 2, (screenSize.height - 280) / 2, 480, 280);
                newframe.setVisible(true);

                label = new JPanel();
                cmd = new JPanel();
                button = new JPanel();
                command = new JTextArea(10,40);


                newframe.add(cmd, BorderLayout.CENTER);
                newframe.add(button, BorderLayout.SOUTH);
                newframe.add(label, BorderLayout.NORTH);
                label.add(new JLabel("Enter command: ", SwingConstants.CENTER));
                cmd.add(command);

                okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        enterComand();
                        newframe.dispose();
                    }
                });
                button.add(okButton);
            }
        });
        //------------------------------------------------------------------------
        filemenu.add(new AbstractAction("Open") {
            @Override
            public void actionPerformed(ActionEvent e) {
              final  JFrame newframe = new JFrame("Open");
                setDefaultCloseOperation(newframe.DISPOSE_ON_CLOSE);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                newframe.setBounds((screenSize.width - 440) / 2, (screenSize.height - 100) / 2, 440, 100);
                newframe.setVisible(true);

                cmd = new JPanel();
                button = new JPanel();
                command = new JTextArea(1,15);

                newframe.add(cmd, BorderLayout.NORTH);
                newframe.add(button, BorderLayout.SOUTH);
                cmd.add(new JLabel("Enter name of table: "), SwingConstants.CENTER);
                cmd.add(command);

                okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        enterNameTable();
                        newframe.dispose();
                    }
                });
                button.add(okButton);

            }
        });
        //------------------------------------------------------------------------
        filemenu.add(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        //------------------------------------------------------------------------
        menuBar.add(filemenu);
        menuBar.add(optionmenu);
        setJMenuBar(menuBar);
        //------------------------------------------------------------------------
        grid = new JTable();
        grid.setModel(dtm);
        JScrollPane jsp  = new JScrollPane(grid);
        jf = new JFrame();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 640)/2, (screenSize.height - 480)/2, 640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(jsp);
        setVisible(true);
        //-----------------------------------------------------------------
        panelButton = new JPanel();
        add(panelButton, BorderLayout.SOUTH);
        updateButton = new JButton("Refresh");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {jf.dispose();TableDB(copyemail, copypassword);}
        });
        panelButton.add(updateButton);
    }

    public void enterNameTable(){
        try {
            try (Connection conn = getConnection()) {
                Statement stat = conn.createStatement();
                nameTable = command.getText().trim();
                conn.close();
            }
        }catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e);
        }
        command.setText("");
        jf.dispose();
        TableDB(copyemail, copypassword);
    }

    public void enterComand() {
        try {
            try (Connection conn = getConnection()) {
                Statement stat = conn.createStatement();
                String line = command.getText().trim();
                try {
                    boolean isResult = stat.execute(line);
                } catch (SQLException ex) {
                    for (Throwable e : ex)
                        e.printStackTrace();
                }
                conn.close();
            }
        }catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e);
        }
        command.setText("");
        jf.dispose();
        TableDB(copyemail, copypassword);
    }

    private void deleteRow(){
        try {
            try (Connection conn = getConnection()) {
                Statement stat = conn.createStatement();
                id = command.getText().trim();
                stat.execute("delete from " + nameTable + " where id = ' "+ id+"'");
                conn.close();
            }
        }catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e);
        }
        command.setText("");
        jf.dispose();
        TableDB(copyemail,copypassword);
    }
    private void addRow(){

        try {
            try (Connection conn = getConnection()) {
                Statement stat = conn.createStatement();
                valuesString = command.getText().trim();
                stat.execute("insert into " + nameTable + "(" + headersString + ") values (" + valuesString +")");
                conn.close();
            }
        }catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e);
        }
        command.setText("");
        jf.dispose();
        TableDB(copyemail, copypassword);
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

    private String sendTable(){
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DefaultTableModel dtm = null;
        table = "";
        try{
            readDatabaseProperties();
            try (Connection conn = getConnection()){
                pstmt = conn.prepareStatement("select * from " + nameTable);
                dtm = new DefaultTableModel();
                //-------------------------------------------
                if (pstmt.execute()){
                    rs = pstmt.getResultSet();
                    //Формируем заголовки столбцов из названия полей -->
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();
                    while (rs.next()){
                        for (int i = 1; i <= columnCount; i++){
                            if (i > 1) table+="  ";
                            table += rs.getString(i);}
                        table+="\n";
                    }
                }
                conn.close();
            }
        }
        catch (SQLException ex) {
            System.out.println(ex.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return table;
    }

}

public class SwingTableDB extends SQLException{
    public static void main(String[] args) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                new Login().log();
            }
        });
    }
}
