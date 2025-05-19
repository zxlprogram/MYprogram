import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ScrollTest {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Scroll Test");
        Chart chart = new Chart();

        JScrollPane scrollPane = new JScrollPane(chart);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        frame.add(scrollPane);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // 模擬加入很多資料點
        new Thread(() -> {
            for (int i = 0; i < 200; i++) {
                final int idx = i;
                SwingUtilities.invokeLater(() -> chart.addLoss(idx % 50 + 1.0));
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }
        }).start();
    }
}

@SuppressWarnings("serial")
class Chart extends JPanel {
    private final List<Double> losses = new ArrayList<>();

    void addLoss(double loss) {
        losses.add(loss);
        revalidate();  // 很重要，通知父元件尺寸變了
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(50 + losses.size() * 5, 200);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (losses.isEmpty()) return;
        Graphics2D g2 = (Graphics2D) g;
        int height = getHeight();

        g2.setColor(Color.BLACK);
        g2.drawLine(40, height - 40, getWidth() - 10, height - 40);
        g2.drawLine(40, 10, 40, height - 40);

        double maxLoss = losses.stream().max(Double::compare).orElse(1.0);
        int xStep = 5;
        for (int i = 1; i < losses.size(); i++) {
            int x1 = 40 + (i - 1) * xStep;
            int y1 = height - 40 - (int) (losses.get(i - 1) / maxLoss * (height - 50));
            int x2 = 40 + i * xStep;
            int y2 = height - 40 - (int) (losses.get(i) / maxLoss * (height - 50));
            g2.setColor(Color.RED);
            g2.drawLine(x1, y1, x2, y2);
        }
    }
}
