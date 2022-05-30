package twittersentiment.CNN;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.deeplearning4j.iterator.CnnSentenceDataSetIterator;
import org.deeplearning4j.iterator.LabeledSentenceProvider;
import org.deeplearning4j.iterator.provider.FileLabeledSentenceProvider;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class LoadTwitterSentiment {

    public static final String DATA_PATH = new File(System.getProperty("user.dir"), "src/main/resources").getAbsolutePath();
    public static final String DATA_FILE = new File(DATA_PATH, "\\malay-dataset\\dataset\\").getAbsolutePath();
    private static final Logger log = LoggerFactory.getLogger(LoadTwitterSentiment.class);
    public static String VECTOR_FILE = "\\ms-updated.vector";
    public static String VECTOR_DIR = new File(System.getProperty("user.dir"), "src/main/resources/malay-dataset/vector").getAbsolutePath();
    public static String VECTOR_PATH = VECTOR_DIR + VECTOR_FILE;
    public static String MODEL_FILE = "\\cnn_sentiment.net";
    public static String MODEL_PATH = DATA_PATH + MODEL_FILE;

    public static void main(String[] args) throws IOException {

        int batchSize = 128;         //Number of epochs (full passes of training data) to train on
        int truncateReviewsToLength = 256;  //Truncate reviews with length (# words) greater than this
        Random rng = new Random(12345); //For shuffling repeatability

        WordVectors wordVectors = WordVectorSerializer.loadStaticModel(new File(VECTOR_PATH));
        DataSetIterator testIterator = getDataSetIterator(false, wordVectors, batchSize, truncateReviewsToLength, rng);
        String negTweetExample = FilenameUtils.concat(DATA_FILE, "test\\neg\\text0.txt");
        String negTweetContent = FileUtils.readFileToString(new File(negTweetExample), (Charset) null);
        INDArray featuresFirstNegative = ((CnnSentenceDataSetIterator) testIterator).loadSingleSentence(negTweetContent);

        ComputationGraph net = ComputationGraph.load(new File(MODEL_PATH), true);

        INDArray predictionsFirstNegative = net.outputSingle(featuresFirstNegative);
        List<String> labels = testIterator.getLabels();

        log.info("\n\nPredictions sentiment :");
        log.info(negTweetContent);
        for (int i = 0; i < labels.size(); i++) {
            System.out.println("P(" + labels.get(i) + ") = " + predictionsFirstNegative.getDouble(i));
        }

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

        return new CnnSentenceDataSetIterator.Builder(CnnSentenceDataSetIterator.Format.CNN2D)
                .sentenceProvider(sentenceProvider)
                .wordVectors(wordVectors)
                .minibatchSize(minibatchSize)
                .maxSentenceLength(maxSentenceLength)
                .useNormalizedWordVectors(false)
                .build();
    }
}

