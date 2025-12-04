import javax.swing.*;

import java.io.File;
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
                String path = chooser.getSelectedFile().getAbsolutePath(),originalPath;
                try {
                    originalPath = new File(
                            ServerTunnelGUI.class.getProtectionDomain().getCodeSource().getLocation().toURI()
                    ).getParent();
                } catch (Exception e) {
                    e.printStackTrace();
                    originalPath = "";
                }
                ProcessBuilder pb = new ProcessBuilder(originalPath+"\\tool\\jdk-25\\bin\\java.exe","-jar","fileSharing.jar", path);
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
