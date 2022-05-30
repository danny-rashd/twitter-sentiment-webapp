package twittersimilar;

import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;

public class TwitterWordVec {

    static String dataPath = "sentiment/malay-dataset/dataset/malay_dump_cleaned_min.txt";
    static String vectorPath = "sentiment/malay-dataset/vectors/mswiki.vector";
    static int minWordFrequency = 5;
    static int epoch = 1;
    static int layerSize = 300;
    static int seed = 284;
    static int windowSize = 5;
    private static final Logger log = LoggerFactory.getLogger(TwitterWordVec.class);

    public static void main(String[] args) throws Exception {

        String filePath = new ClassPathResource(dataPath).getFile().getAbsolutePath();

        SentenceIterator iter = new BasicLineIterator(filePath);
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());

        log.info("Building model....");
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(minWordFrequency)
                .epochs(epoch)
                .layerSize(layerSize)
                .seed(seed)
                .windowSize(windowSize)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        long fit_time1 = System.currentTimeMillis();
        log.info("Fitting Word2Vec model....");
        vec.fit();
        long fit_time2 = System.currentTimeMillis();
        log.info("Fitting time in {} msec", fit_time2 - fit_time1);

        Collection<String> lst = vec.wordsNearest("banjir", 10);
        log.info("Closest words to 'banjir' on 1st run: " + lst);


        long write_time1 = System.currentTimeMillis();
        WordVectorSerializer.writeWordVectors(vec.lookupTable(), new File("31_3_ms2.vector")); //save model
        long write_time2 = System.currentTimeMillis();
        log.info("Model loaded in {} msec", write_time2 - write_time1);

        long read_time1 = System.currentTimeMillis();

        // Update weights
        Word2Vec word2Vec = WordVectorSerializer.readWord2VecModel(new ClassPathResource(vectorPath).getFile().getAbsolutePath(), false);
        long read_time2 = System.currentTimeMillis();

        log.info("Model loaded in {} msec", read_time2 - read_time1);
        log.info("Load & Vectorize Sentences....");

        SentenceIterator iterator = new BasicLineIterator(filePath);
        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        word2Vec.setTokenizerFactory(tokenizerFactory);
        word2Vec.setSentenceIterator(iterator);

        log.info("Word2vec uptraining...");

        word2Vec.fit();

        lst = word2Vec.wordsNearest("banjir", 10);
        log.info("Closest words to 'banjir' on 2nd run: " + lst);

        final double cosSimilarity = word2Vec.similarity("banjir", "hujan");
        System.out.println(cosSimilarity);
        log.info("Saving updated model...");
        WordVectorSerializer.writeWordVectors(word2Vec.lookupTable(), new File("ms-updated.vector"));
    }
}