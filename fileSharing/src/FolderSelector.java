import javax.swing.*;
import java.io.IOException;
public class FolderSelector {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("choose the folder");
            int result = chooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                ProcessBuilder pb = new ProcessBuilder("fileSharing", path);
                pb.inheritIO();
                try {
                    pb.start();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
