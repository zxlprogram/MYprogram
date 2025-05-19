package AIlib;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ANN {
	

@SuppressWarnings("serial")
class Chart extends JPanel {
    private List<Double> losses = new ArrayList<>();

    void addLoss(double loss) {
        losses.add(loss);
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (losses.isEmpty()) return;

        List<Double> copy;
        synchronized (losses) {
            copy = new ArrayList<>(losses);
        }

        Graphics2D g2 = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();

        // 畫座標軸
        g2.setColor(Color.BLACK);
        g2.drawLine(40, height - 40, width - 10, height - 40); // x 軸
        g2.drawLine(40, 10, 40, height - 40);                  // y 軸

        double maxLoss = copy.stream().max(Double::compare).orElse(1.0);
        int xStep = 5; // 固定每點 5 像素

        for (int i = 1; i < copy.size(); i++) {
            int x1 = 40 + (i - 1) * xStep;
            int y1 = height - 40 - (int)(copy.get(i - 1) / maxLoss * (height - 50));
            int x2 = 40 + i * xStep;
            int y2 = height - 40 - (int)(copy.get(i) / maxLoss * (height - 50));

            g2.setColor(Color.RED);
            g2.drawLine(x1, y1, x2, y2);
        }
    }
    @Override
    public Dimension getPreferredSize() {
        int totalPoints = losses.size();
        int width = 50 + totalPoints * 5;
        int height = 500;
        return new Dimension(width, height);
    }
}
	
    private List<double[][]> weights;   // 每層權重 (包含 input->hidden1, hidden1->hidden2, ..., hiddenN->output)
    private List<double[]> biases;      // 每層 bias (除了輸入層)
    private double[][]inputs;
    private double[][]targets;
    private int[]hiddenLayers;
    public JFrame frame;
    private Random random = new Random();

    public ANN(double [][]inputs, int[] hiddenLayers, double [][]targets) {
        this.inputs=inputs;
        this.targets=targets;
        this.hiddenLayers=hiddenLayers;
        weights = new ArrayList<>();
        biases = new ArrayList<>();

        int prevLayerSize = inputs[0].length;
        // 初始化多層權重與 bias
        for (int layerSize : this.hiddenLayers) {
            weights.add(randomMatrix(layerSize, prevLayerSize));
            biases.add(randomArray(layerSize));
            prevLayerSize = layerSize;
        }
        // 輸出層權重與 bias
        weights.add(randomMatrix(targets[0].length, prevLayerSize));
        biases.add(randomArray(targets[0].length));
    }

    private double[][] randomMatrix(int rows, int cols) {
        double[][] m = new double[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                m[i][j] = random.nextDouble() * 2 - 1;
        return m;
    }

    private double[] randomArray(int size) {
        double[] arr = new double[size];
        for (int i = 0; i < size; i++)
            arr[i] = random.nextDouble() * 2 - 1;
        return arr;
    }
    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }
    private double dsigmoid(double y) {
        return y * (1 - y);
    }
    private double[] predict(double[] input) {
        double[] activations = input;
        for (int layer = 0; layer < weights.size(); layer++) {
            double[] nextActivations = new double[weights.get(layer).length];
            for (int i = 0; i < nextActivations.length; i++) {
                double sum = biases.get(layer)[i];
                for (int j = 0; j < activations.length; j++) {
                    sum += weights.get(layer)[i][j] * activations[j];
                }
                nextActivations[i] = sigmoid(sum);
            }
            activations = nextActivations;
        }
        return activations;
    }

    private void train(double[] inputArray, double[] targetArray,double learningRate) {
        // forward pass，紀錄每層輸出
        List<double[]> layerOutputs = new ArrayList<>();
        layerOutputs.add(inputArray);

        double[] activations = inputArray;
        for (int layer = 0; layer < weights.size(); layer++) {
            double[] nextActivations = new double[weights.get(layer).length];
            for (int i = 0; i < nextActivations.length; i++) {
                double sum = biases.get(layer)[i];
                for (int j = 0; j < activations.length; j++) {
                    sum += weights.get(layer)[i][j] * activations[j];
                }
                nextActivations[i] = sigmoid(sum);
            }
            layerOutputs.add(nextActivations);
            activations = nextActivations;
        }

        // output error
        double[] errors = new double[targetArray.length];
        double[] outputs = layerOutputs.get(layerOutputs.size() - 1);
        for (int i = 0; i < errors.length; i++) {
            errors[i] = targetArray[i] - outputs[i];
        }

        // backward pass (反向傳播)
        for (int layer = weights.size() - 1; layer >= 0; layer--) {
            double[] outputLayer = layerOutputs.get(layer + 1);
            double[] inputLayer = layerOutputs.get(layer);

            double[] gradients = new double[outputLayer.length];
            for (int i = 0; i < gradients.length; i++) {
                gradients[i] = dsigmoid(outputLayer[i]) * errors[i] * learningRate;
                biases.get(layer)[i] += gradients[i];
            }

            // 更新權重
            for (int i = 0; i < outputLayer.length; i++) {
                for (int j = 0; j < inputLayer.length; j++) {
                    weights.get(layer)[i][j] += gradients[i] * inputLayer[j];
                }
            }

            // 計算下一層誤差（傳回去）
            if (layer > 0) {
                double[] nextErrors = new double[inputLayer.length];
                for (int i = 0; i < inputLayer.length; i++) {
                    double errorSum = 0;
                    for (int j = 0; j < outputLayer.length; j++) {
                        errorSum += weights.get(layer)[j][i] * errors[j];
                    }
                    nextErrors[i] = errorSum;
                }
                errors = nextErrors;
            }
        }
    }
    public void RunMod(int epoch,double learningRate,boolean showchart) {
        Chart chart = new Chart();
    	if(showchart) {
    	frame = new JFrame("Chart");
    	JScrollPane scrollPane = new JScrollPane(chart);
    	scrollPane.setPreferredSize(new java.awt.Dimension(800, 600));
    	frame.add(scrollPane);
    	frame.pack();
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setVisible(true);
        }
        for (int iter = 1; iter <= epoch; iter++) {
            for (int i = 0; i < inputs.length; i++) {
                this.train(inputs[i], targets[i],learningRate);
            }
            if (iter% 1 == 0) {
                double loss = 0;
                for (int i = 0; i < inputs.length; i++) {
                    double[] out = this.predict(inputs[i]);
                    loss += Math.pow(out[0] - targets[i][0],2);
                }
                System.out.println("Epoch " + iter + ", Loss: " + loss);
                if(showchart)
                	chart.addLoss(loss);
            }
        }
    }
}