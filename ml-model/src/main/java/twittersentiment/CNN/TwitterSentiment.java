package twittersentiment.CNN;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.deeplearning4j.iterator.CnnSentenceDataSetIterator;
import org.deeplearning4j.iterator.CnnSentenceDataSetIterator.Format;
import org.deeplearning4j.iterator.LabeledSentenceProvider;
import org.deeplearning4j.iterator.provider.FileLabeledSentenceProvider;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.ConvolutionMode;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.graph.MergeVertex;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.GlobalPoolingLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.PoolingType;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

public class TwitterSentiment {
    public static final String DATA_PATH = new File(System.getProperty("user.dir"), "src/main/resources/malay-dataset/").getAbsolutePath();
    public static final String DATA_FILE = new File(DATA_PATH, "dataset\\").getAbsolutePath();
    //CNN MODEL
    private static final Logger log = LoggerFactory.getLogger(TwitterSentiment.class);
    public static String VECTOR_FILE = "\\ms-updated.vector";
    public static String VECTOR_DIR = new File(System.getProperty("user.dir"), "src/main/resources/malay-dataset/vector").getAbsolutePath();
    public static String VECTOR_PATH = VECTOR_DIR + VECTOR_FILE;

    public static void main(String[] args) throws Exception {

        //Basic configuration
        int batchSize = 128;
        int vectorSize = 300;           //size of word vectors. 300 for the uptrained ms-wiki
        int nEpochs = 4;                 //Number of epochs (full passes of training data) to train on
        int tweetWordLimit = 280;       //Truncate tweets with length (# words) greater than this

        int cnnLayerFeatureMaps = 300;     //Number of feature maps / channels / depth for each CNN layer
        PoolingType globalPoolingType = PoolingType.MAX;
        Random rng = new Random(12345); //For shuffling repeatability

        Nd4j.getMemoryManager().setAutoGcWindow(5000);

        log.info("Building model....");
        ComputationGraphConfiguration config = new NeuralNetConfiguration.Builder()
                .weightInit(WeightInit.RELU)
                .activation(Activation.LEAKYRELU)
                .updater(new Adam(5e-3))
                .convolutionMode(ConvolutionMode.Same)
                .l2(1e-4)
                .graphBuilder()
                .addInputs("input")
                .addLayer("cnn3", new ConvolutionLayer.Builder()
                        .kernelSize(3, vectorSize)
                        .stride(1, vectorSize)
                        .nOut(cnnLayerFeatureMaps)
                        .build(), "input")
                .addLayer("cnn4", new ConvolutionLayer.Builder()
                        .kernelSize(4, vectorSize)
                        .stride(1, vectorSize)
                        .nOut(cnnLayerFeatureMaps)
                        .build(), "input")
                .addLayer("cnn5", new ConvolutionLayer.Builder()
                        .kernelSize(5, vectorSize)
                        .stride(1, vectorSize)
                        .nOut(cnnLayerFeatureMaps)
                        .build(), "input")
                //MergeVertex performs depth concatenation on activations: 3x[minibatch,100,length,300] to 1x[minibatch,300,length,300]
                .addVertex("merge", new MergeVertex(), "cnn3", "cnn4", "cnn5")
                //Global pooling: pool over x/y locations (dimensions 2 and 3): Activations [minibatch,300,length,300] to [minibatch, 300]
                .addLayer("globalPool", new GlobalPoolingLayer.Builder()
                        .poolingType(globalPoolingType)
                        .dropOut(0.5)
                        .build(), "merge")
                .addLayer("out", new OutputLayer.Builder()
                        .lossFunction(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX)
                        .nOut(3)    //2 classes: positive or negative
                        .build(), "globalPool")
                .setOutputs("out")
                .setInputTypes(InputType.convolutional(tweetWordLimit, vectorSize, 1))
                .build();

        ComputationGraph net = new ComputationGraph(config);
        net.init();

        log.info("Number of parameters by layer:");
        for (Layer l : net.getLayers()) {
            System.out.println("\t" + l.conf().getLayer().getLayerName() + "\t" + l.numParams());
        }

        log.info("Loading word vectors and creating DataSetIterators");
        WordVectors wordVectors = WordVectorSerializer.loadStaticModel(new File(VECTOR_PATH));
        DataSetIterator trainIterator = getDataSetIterator(true, wordVectors, batchSize, tweetWordLimit, rng);
        DataSetIterator testIterator = getDataSetIterator(false, wordVectors, batchSize, tweetWordLimit, rng);

        log.info("Training...");
        net.setListeners(new ScoreIterationListener(100), new EvaluativeListener(testIterator, 1, InvocationType.EPOCH_END));
        net.fit(trainIterator, nEpochs);

        Evaluation evalTrain = net.evaluate(trainIterator);
        Evaluation evalTest = net.evaluate(testIterator);
        System.out.println("=============== Train evaluation ===============");
        System.out.println(evalTrain.stats());
        System.out.println("=============== Test evaluation ===============");
        System.out.println(evalTest.stats());

        //After training: load a single sentence and generate a prediction
        String negTweetExample = FilenameUtils.concat(DATA_FILE, "test\\neg\\text0.txt");
        String negTweetContent = FileUtils.readFileToString(new File(negTweetExample), (Charset) null);
        INDArray negTweet = ((CnnSentenceDataSetIterator) testIterator).loadSingleSentence(negTweetContent);

        INDArray predictNegTweet = net.outputSingle(negTweet);
        List<String> labels = testIterator.getLabels();

        log.info("\nPredictions for first negative tweets:\n");
        log.info(negTweetExample);
        for (int i = 0; i < labels.size(); i++) {
            System.out.println("P(" + labels.get(i) + ") = " + predictNegTweet.getDouble(i));
        }
        log.info("\nSaving the trained model: ");
        ModelSerializer.writeModel(net, "cnn_sentiment.net", true);
    }

    private static DataSetIterator getDataSetIterator(boolean isTraining, WordVectors wordVectors, int minibatchSize,
                                                      int maxSentenceLength, Random rng) {
        String path = FilenameUtils.concat(DATA_FILE, (isTraining ? "train\\" : "test\\"));
        String positiveBaseDir = FilenameUtils.concat(path, "pos");
        String negativeBaseDir = FilenameUtils.concat(path, "neg");
        String neutralBaseDir = FilenameUtils.concat(path, "neutral");

        File filePositive = new File(positiveBaseDir);
        File fileNegative = new File(negativeBaseDir);
        File fileNeutral = new File(neutralBaseDir);

        Map<String, List<File>> tweetsMap = new HashMap<>();
        tweetsMap.put("Positive", Arrays.asList(Objects.requireNonNull(filePositive.listFiles())));
        tweetsMap.put("Negative", Arrays.asList(Objects.requireNonNull(fileNegative.listFiles())));
        tweetsMap.put("Neutral", Arrays.asList(Objects.requireNonNull(fileNeutral.listFiles())));
        LabeledSentenceProvider sentenceProvider = new FileLabeledSentenceProvider(tweetsMap, rng);

        return new CnnSentenceDataSetIterator.Builder(Format.CNN2D)
                .sentenceProvider(sentenceProvider)
                .wordVectors(wordVectors)
                .minibatchSize(minibatchSize)
                .maxSentenceLength(maxSentenceLength)
                .useNormalizedWordVectors(false)
                .build();
    }
}

