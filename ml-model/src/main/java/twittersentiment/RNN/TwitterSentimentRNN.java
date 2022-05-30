package twittersentiment.RNN;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class TwitterSentimentRNN {
    public static final String DATA_PATH = new File(System.getProperty("user.dir"), "src/main/resources/malay-dataset/").getAbsolutePath();
    public static final String DATA_FILE = new File(DATA_PATH, "dataset\\").getAbsolutePath();
    public static final int batchSize = 64;     //Number of examples in each minibatch
    public static final int vectorSize = 300;   //Size of the word vectors. 300 in the Google News model
    public static final int nEpochs = 3;        //Number of epochs (full passes of training data) to train on
    public static final int seed = 123;     //Seed for reproducibility
    public static final int output = 3;
    private static final Logger log = LoggerFactory.getLogger(TwitterSentimentRNN.class);
    public static String VECTOR_FILE = "\\ms-updated.vector";
    public static String VECTOR_DIR = new File(System.getProperty("user.dir"), "src/main/resources/malay-dataset/vector").getAbsolutePath();
    public static String VECTOR_PATH = VECTOR_DIR + VECTOR_FILE;

    public static void main(String[] args) throws Exception {

        WordVectors wordVectors = WordVectorSerializer.readWord2VecModel(new File(VECTOR_PATH));
        TwitterDataIterator trainIterator = new TwitterDataIterator(DATA_FILE, wordVectors, batchSize, true);
        TwitterDataIterator testIterator = new TwitterDataIterator(DATA_FILE, wordVectors, batchSize, false);

        Nd4j.getMemoryManager().setAutoGcWindow(10000);

        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .updater(new Adam(1e-3))
                .l2(1e-5)
                .weightInit(WeightInit.XAVIER)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
                .list()
                .layer(new LSTM.Builder()
                        .nIn(vectorSize)
                        .nOut(vectorSize)
                        .activation(Activation.TANH)
                        .build())
                .layer(new LSTM.Builder()
                        .nIn(vectorSize)
                        .nOut(150)
                        .activation(Activation.TANH)
                        .build())
                .layer(new LSTM.Builder()
                        .nIn(vectorSize)
                        .nOut(75)
                        .activation(Activation.TANH)
                        .build())
                .layer(new RnnOutputLayer.Builder()
                        .nOut(output)
                        .activation(Activation.SOFTMAX)
                        .lossFunction(LossFunctions.LossFunction.MCXENT)
                        .build())
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(config);
        net.init();

        log.info("Model Training: ");
        net.setListeners(new ScoreIterationListener(100), new EvaluativeListener(testIterator, 1, InvocationType.EPOCH_END));
        net.fit(trainIterator, nEpochs);

        log.info("Model Evaluation:");
        Evaluation evalTrain = net.evaluate(trainIterator);
        Evaluation evalTest = net.evaluate(testIterator);
        System.out.println("Train evaluation: " + evalTrain.stats());
        System.out.println("Test evaluation: " + evalTest.stats());

        log.info("Saving model: ");
        ModelSerializer.writeModel(net, DATA_PATH + "rnn_sentiment.zip", true);
        log.info("Model saved at {}", DATA_PATH);
    }
}

