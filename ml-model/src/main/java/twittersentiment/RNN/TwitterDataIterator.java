package twittersentiment.RNN;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class TwitterDataIterator implements DataSetIterator {
    private final WordVectors wordVectors;
    private final int batchSize;
    private final int vectorSize;
    private final File[] posFiles;
    private final File[] negFiles;
    private final File[] neutralFiles;
    private final TokenizerFactory tokenizerFactory;
    private int posCursor = 0;
    private int negCursor = 0;
    private int neutralCursor = 0;

    public TwitterDataIterator(String dataRootDirectory, WordVectors wordVectors, int batchSize, boolean train) {
        this.batchSize = batchSize;
        this.vectorSize = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length;

        //Training or testing dataset
        String subDirectory = (train) ? "/train/" : "/test/";

        String dataDirectory = dataRootDirectory + subDirectory;

        //Get text files in pos,neg,neutral folder
        //Pos as positive label, Neg as negative label, neutral as neutral label
        File pos = new File(dataDirectory, "pos/");
        File neg = new File(dataDirectory, "neg/");
        File neutral = new File(dataDirectory, "neutral/");

        posFiles = pos.listFiles();
        negFiles = neg.listFiles();
        neutralFiles = neutral.listFiles();

        this.wordVectors = wordVectors;

        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
    }

    @Override
    public DataSet next(int num) {
        if ((posCursor == posFiles.length) && negCursor == negFiles.length) {
            throw new NoSuchElementException();
        }
        try {
            return nextDataSet(num);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DataSet nextDataSet(int batchSize) throws IOException {
        //Load tweets to String. Unbalanced dataset
        List<String> tweetArray = new ArrayList<>(batchSize);
        int[] labelArr = new int[batchSize];
        int splitBatchSize = batchSize / 3;

        if (posCursor < posFiles.length) {
            for (int i = 0; i < splitBatchSize && posCursor < posFiles.length; ++i) {
                //load positive data
                String tweet = FileUtils.readFileToString(posFiles[posCursor], "UTF-8");
                tweetArray.add(tweet);
                labelArr[i] = 0; // 0-> positive
                ++posCursor;
            }
        }
        //negative index starts after positive index
        for (int i = splitBatchSize; i < splitBatchSize * 2 && negCursor < negFiles.length; ++i) {
            //load negative data
            String tweet = FileUtils.readFileToString(negFiles[negCursor], "UTF-8");
            tweetArray.add(tweet);
            labelArr[i] = 2; // 2-> negative
            ++negCursor;
        }
        //neutral index starts after negative index
        for (int i = splitBatchSize * 2; i < splitBatchSize * 3 && neutralCursor < neutralFiles.length; ++i) {
            //load neutral data
            String tweet = FileUtils.readFileToString(neutralFiles[neutralCursor], "UTF-8");
            tweetArray.add(tweet);
            labelArr[i] = 1; //1 -> neutral
            ++neutralCursor;
        }

        //Tokenize tweets and filter out unknown words
        List<List<String>> allTokens = new ArrayList<>(tweetArray.size());
        int maxLength = 0;
        for (String s : tweetArray) {
            List<String> tokens = tokenizerFactory.create(s).getTokens();
            List<String> tokensFiltered = new ArrayList<>();
            for (String t : tokens) {
                if (wordVectors.hasWord(t)) tokensFiltered.add(t);
            }
            allTokens.add(tokensFiltered);
            maxLength = Math.max(maxLength, tokensFiltered.size());
        }


        //Create data for training
        //Here: we have tweetArray.size() examples of varying lengths
        INDArray features = Nd4j.create(tweetArray.size(), vectorSize, maxLength);
        INDArray labels = Nd4j.create(tweetArray.size(), 3, maxLength);    //3 labels-> positive ,negative or neutral

        //Padding arrays because of tweets of different lengths and only one output at the final time step
        //Mask arrays contain 1 if data is present at that time step for that example, or 0 if data is just padding
        INDArray featuresMask = Nd4j.zeros(tweetArray.size(), maxLength);
        INDArray labelsMask = Nd4j.zeros(tweetArray.size(), maxLength);

        for (int i = 0; i < tweetArray.size(); ++i) {
            List<String> tokens = allTokens.get(i);

            // Get the truncated sequence length of document (i)
            int seqLength = Math.min(tokens.size(), maxLength);

            //Get word vectors for each word in review, and put them in the training data
            for (int j = 0; j < tokens.size() && j < maxLength; ++j) {
                String token = tokens.get(j);
                INDArray vector = wordVectors.getWordVectorMatrix(token);
                features.put(new INDArrayIndex[]{NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.point(j)}, vector);

                // Assign "1" to each position where a feature is present, that is, in the interval of [0, seqLength)
                featuresMask.get(NDArrayIndex.point(i), NDArrayIndex.interval(0, seqLength)).assign(1);
            }

            int lastIdx = Math.min(tokens.size(), maxLength);
            labels.putScalar(new int[]{i, labelArr[i], lastIdx - 1}, 1.0);   //Set label: [1, 0] for negative, [0, 1] for positive
            labelsMask.putScalar(new int[]{i, lastIdx - 1}, 1.0);   //Specify that an output exists at the final time step
        }

        return new DataSet(features, labels, featuresMask, labelsMask);
    }

    public int totalExamples() {
        return posFiles.length + negFiles.length + neutralFiles.length;
    }

    @Override
    public int inputColumns() {
        return vectorSize;
    }

    @Override
    public int totalOutcomes() {
        return 3;
    }

    @Override
    public void reset() {
        posCursor = 0;
        negCursor = 0;
        neutralCursor = 0;
    }

    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
        return true;
    }

    @Override
    public int batch() {
        return batchSize;
    }

    public int numExamples() {
        return totalExamples();
    }

    @Override
    public List<String> getLabels() {
        return Arrays.asList("Positive", "Neutral", "Negative");
    }

    @Override
    public boolean hasNext() {
        return (posCursor + negCursor + neutralCursor) < numExamples();
    }

    @Override
    public DataSet next() {
        return next(batchSize);
    }

    @Override
    public void remove() {

        throw new UnsupportedOperationException();
    }

    @Override
    public DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException();
    }

}