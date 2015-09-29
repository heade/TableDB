import javax.swing.*;
import java.awt.print.PrinterException;

/**
 * Created by Daniel on 12.09.2015
 */
public class PrintTable {
    public void printtable(JTable grid){
        try {
            boolean complete = grid.print();
        } catch (PrinterException pe) {
            JOptionPane.showMessageDialog(null, "Error: " + pe);
        }
    }
}
