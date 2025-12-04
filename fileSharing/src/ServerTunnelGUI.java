import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
public class ServerTunnelGUI {
    private Random r = new Random();
    private Process pythonProcess;
    private Process tunnelProcess;
    private JTextArea tunnelOutput;
    public String path, originalPath,link;
    public static void main(String[] args) {
        ServerTunnelGUI gui = new ServerTunnelGUI();
        if (args.length > 0 && args[0] != null && !args[0].isEmpty())
            gui.path = args[0];
        SwingUtilities.invokeLater(gui::createAndShowGUI);
    }
    public ServerTunnelGUI() {
        path = System.getProperty("user.dir");
        try {
            originalPath = new File(
                    ServerTunnelGUI.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            ).getParent();
        } catch (Exception e) {
            e.printStackTrace();
            originalPath = "";
        }
    }
    private boolean isPortAvailable(int port) {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    private int getFreePort() {
        for (int i = 0; i < 50; i++) {
            int candidate = r.nextInt(1000) + 8000;
            if (isPortAvailable(candidate)) {
                return candidate;
            }
        }
        throw new RuntimeException("找不到可用的埠");
    }
    private void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        JFrame frame = new JFrame("Python Server + Cloudflare Tunnel Monitor");
        frame.setIconImage(new ImageIcon(ServerTunnelGUI.class.getResource("logo.png")).getImage());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new GridLayout(1, 2));
        JTextArea pythonOutput = new JTextArea();
        pythonOutput.setBackground(Color.black);
        pythonOutput.setForeground(Color.green);
        pythonOutput.setEditable(false);
        pythonOutput.setName("SERVER");
        JScrollPane pythonScroll = new JScrollPane(pythonOutput);
        tunnelOutput = new JTextArea();
        tunnelOutput.setBackground(Color.black);
        tunnelOutput.setForeground(Color.green);
        tunnelOutput.setEditable(false);
        tunnelOutput.setName("CLOUDFLARD");
        JScrollPane tunnelScroll = new JScrollPane(tunnelOutput);
        JButton copyButton = new JButton("複製臨時連結並顯示 QRCode");
        copyButton.addActionListener(e -> copyTunnelLink());
        JPanel tunnelPanel = new JPanel(new BorderLayout());
        tunnelPanel.add(copyButton, BorderLayout.NORTH);
        tunnelPanel.add(tunnelScroll, BorderLayout.CENTER);
        frame.add(pythonScroll);
        frame.add(tunnelPanel);
        frame.setVisible(true);
        int port = getFreePort();
        pythonProcess = startProcess(
                new String[]{
                        originalPath + "\\tool\\python-3.14.0-embed-amd64\\python.exe",
                        originalPath + "\\tool\\server.py",
                        Integer.toString(port)
                },
                path,
                pythonOutput
        );
        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            tunnelProcess = startProcess(
                    new String[]{
                            originalPath + "\\tool\\cloudflared.exe",
                            "tunnel",
                            "--url",
                            "http://localhost:" + port,
                            "--protocol",
                            "http2"
                    },
                    path,
                    tunnelOutput
            );
        }).start();
        tunnelOutput.append("[FILESHARING] ["+LocalDateTime.now()+"]\n");
        tunnelOutput.append("[FILESHARING] the tunnel path to folder: "+path+"\n");
        tunnelOutput.append("[FILESHARING] fileSharing is using the port "+port+"\n");
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                shutdownProcesses();
                System.exit(0);
            }
        });
    }
    public Process startProcess(String[] command, String dir, JTextArea outputArea) {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(dir));
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), "UTF-8")
                )) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String finalLine = line;
                        if(line.indexOf(".trycloudflare.com")!=-1)
                        link=line.substring(line.indexOf("https://"),line.indexOf(".trycloudflare.com")+18);
                        if(outputArea!=null)
                        SwingUtilities.invokeLater(() -> outputArea.append("["+outputArea.getName()+"] "+finalLine + "\n"));
                    }
                } catch (IOException ignored) {}
            }).start();
            return process;
        } catch (IOException e) {
        		if(outputArea!=null)
            outputArea.append("ERROR: " + e.getMessage() + "\n");
            return null;
        }
    }
    private void shutdownProcesses() {
        killIfAlive(pythonProcess);
        killIfAlive(tunnelProcess);
    }
    private void killIfAlive(Process process) {
        if (process == null) return;
        try {
            if (process.isAlive()) {
                process.destroy();
                process.waitFor(2, TimeUnit.SECONDS);
            }
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        } catch (Exception ignored) {}
    }
    private void copyTunnelLink() {
        if(link!=null) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(link), null
            );
            showQRCodeWindow(link);
            JOptionPane.showMessageDialog(null, "已複製臨時連結:\n" + link);
        		return;
        }
        JOptionPane.showMessageDialog(null, "未找到臨時連結，請確認 tunnel 是否啟動。");
    }
    private void showQRCodeWindow(String link) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(link, BarcodeFormat.QR_CODE, 300, 300);
            Image qrImage = MatrixToImageWriter.toBufferedImage(matrix);

            JLabel label = new JLabel(new ImageIcon(qrImage));
            JFrame qrFrame = new JFrame("QRCode for Link");
            qrFrame.setSize(350, 380);
            qrFrame.add(label);
            qrFrame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "QR 生成失敗: " + e.getMessage());
        }
    }
}