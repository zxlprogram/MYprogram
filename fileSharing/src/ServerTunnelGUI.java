// 完整重寫版本：ServerTunnelGUI.java
// 特點：
// 1. 確保 python 與 cloudflared 子進程被正確殺掉
// 2. copyTunnelLink() 能正確抓第二個 https:// 開頭的 Cloudflare 臨時通道
// 3. 按下複製時會自動生成 QR Code 並開新視窗
// 4. 使用 ZXing 產生 QRCode

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.concurrent.TimeUnit;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class ServerTunnelGUI {
    private Process pythonProcess;
    private Process tunnelProcess;
    private JTextArea tunnelOutput;
    public String path = System.getProperty("user.dir");

    public static void main(String[] args) {
        ServerTunnelGUI gui = new ServerTunnelGUI();

        if (args.length > 0 && args[0] != null && !args[0].isEmpty()) {
            gui.path = args[0];
        }

        SwingUtilities.invokeLater(gui::createAndShowGUI);
    }

    private void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JFrame frame = new JFrame("Python Server + Cloudflare Tunnel Monitor");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new GridLayout(1, 2));

        // Python output
        JTextArea pythonOutput = new JTextArea();
        pythonOutput.setBackground(Color.black);
        pythonOutput.setForeground(Color.green);
        pythonOutput.setEditable(false);
        JScrollPane pythonScroll = new JScrollPane(pythonOutput);

        // Tunnel output
        tunnelOutput = new JTextArea();
        tunnelOutput.setBackground(Color.black);
        tunnelOutput.setForeground(Color.green);
        tunnelOutput.setEditable(false);
        JScrollPane tunnelScroll = new JScrollPane(tunnelOutput);

        JButton copyButton = new JButton("複製臨時連結並顯示 QRCode");
        copyButton.addActionListener(e -> copyTunnelLink());

        JPanel tunnelPanel = new JPanel(new BorderLayout());
        tunnelPanel.add(copyButton, BorderLayout.NORTH);
        tunnelPanel.add(tunnelScroll, BorderLayout.CENTER);

        frame.add(pythonScroll);
        frame.add(tunnelPanel);
        frame.setVisible(true);

        // 啟動 Python HTTP server
        pythonProcess = startProcess(new String[]{"python", "-m", "server", "8000"}, path, pythonOutput);

        // 延遲啟動 Cloudflare tunnel
        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            tunnelProcess = startProcess(new String[]{".\\cloudflared.exe", "tunnel", "--url", "http://localhost:8000"}, path, tunnelOutput);
        }).start();

        // 關閉事件：確保所有子進程被完全終止
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                shutdownProcesses();
                System.exit(0);
            }
        });
    }

    private Process startProcess(String[] command, String dir, JTextArea outputArea) {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(dir));
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String finalLine = line;
                        SwingUtilities.invokeLater(() -> outputArea.append(finalLine + "\n"));
                    }
                } catch (IOException ignored) {}
            }).start();

            return process;
        } catch (IOException e) {
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

    // ============================
    // 重新寫好的 copyTunnelLink()
    // ============================
    private void copyTunnelLink() {
        String[] lines = tunnelOutput.getText().split("\\n");
        int httpsCount = 0;

        for (String line : lines) {
            int pos = line.indexOf("https://");
            if (pos != -1) {
                httpsCount++;

                if (httpsCount == 2) {  // 第二個 https:// 才是臨時通道
                    int end = line.indexOf(".trycloudflare.com");
                    if (end == -1) continue;
                    end += ".trycloudflare.com".length();

                    String link = line.substring(pos, end);

                    Toolkit.getDefaultToolkit().getSystemClipboard()
                            .setContents(new StringSelection(link), null);

                    showQRCodeWindow(link);

                    JOptionPane.showMessageDialog(null, "已複製臨時連結:\n" + link);
                    return;
                }
            }
        }

        JOptionPane.showMessageDialog(null, "未找到臨時連結，請確認 tunnel 是否啟動。");
    }

    // ============================
    // QR Code 視窗生成
    // ============================
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