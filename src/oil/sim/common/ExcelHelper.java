package oil.sim.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.JTable;
import javax.swing.table.TableModel;

public class ExcelHelper {

    public static void exportTable(JTable table, File file) throws IOException {
        TableModel model = table.getModel();// 得到Jtable的Model
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "GB2312"));

        for (int i = 0; i < model.getColumnCount(); i++) {
            String tmp = model.getColumnName(i);
            out.write(tmp + "");
            if (i < model.getColumnCount() - 1) {
                out.write(",");
            }
        }
        out.write("\n");
        for (int i = 0; i < model.getRowCount(); i++) {
            for (int j = 0; j < model.getColumnCount(); j++) {
                if (model.getValueAt(i, j).toString() != null && !model.getValueAt(i, j).toString().equals("")) {
                    out.write(model.getValueAt(i, j).toString() + ",");
                } else {
                    out.write("null" + ",");
                }
            }
            out.write("\n");
        }
        out.close();
    }
}
