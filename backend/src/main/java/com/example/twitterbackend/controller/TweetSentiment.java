package com.example.twitterbackend.controller;

import org.apache.commons.io.FilenameUtils;
import org.deeplearning4j.iterator.CnnSentenceDataSetIterator;
import org.deeplearning4j.iterator.LabeledSentenceProvider;
import org.deeplearning4j.iterator.provider.FileLabeledSentenceProvider;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.common.primitives.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TweetSentiment {
    public static final String DATA_PATH=new File(System.getProperty("user.dir"), "src/main/resources").getAbsolutePath();
    public static final String DATA_FILE=new File(DATA_PATH,"\\dataset\\").getAbsolutePath();

    private ComputationGraph net;
    private WordVectors vec;
    private TokenizerFactory tokenizerFactory;
    private int batchSize = 64;         //Number of epochs (full passes of training data) to train on
    private int tweetWordLimit = 280;  //Truncate reviews with length (# words) greater than this
    Random rng = new Random(12345); //For shuffling repeatability

    public TweetSentiment(ComputationGraph net, WordVectors vec) {
        this.net = net;
        this.vec = vec;

        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
    }

    public Pair<String, Double> getSentiment(String postTweetText) throws IOException {

        DataSetIterator testIter = getDataSetIterator(false, vec, batchSize, tweetWordLimit, rng);
//        String postTweetText= TweetFilter.tweetFilter(preTweetText);
        INDArray data = ((CnnSentenceDataSetIterator) testIter).loadSingleSentence(postTweetText);
        INDArray prediction = net.outputSingle(data);

        List<String> labels = testIter.getLabels();
        String getlabels;
        Double getDoubles;
        if (prediction.getDouble(0) > prediction.getDouble(1)) {
            getlabels=labels.get(0);
            getDoubles=prediction.getDouble(0);
        } else if (prediction.getDouble(1) > prediction.getDouble(2)) {
            getlabels=labels.get(1);
            getDoubles=prediction.getDouble(1);
        } else {
            getlabels=labels.get(2);
            getDoubles=prediction.getDouble(2);
        }
        return new Pair<>(getlabels,getDoubles);
    }

        private static DataSetIterator getDataSetIterator(boolean isTraining, WordVectors vec, int minibatchSize, int maxSentenceLength, Random rng) {
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
                    .wordVectors(vec)
                    .minibatchSize(minibatchSize)
                    .maxSentenceLength(maxSentenceLength)
                    .useNormalizedWordVectors(false)
                    .build();
        }
}
