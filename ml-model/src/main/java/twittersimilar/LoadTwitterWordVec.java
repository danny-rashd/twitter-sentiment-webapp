package twittersimilar;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;

public class LoadTwitterWordVec {
    public static String VECTOR_FILE = "\\ms-updated.vector";
    public static String VECTOR_DIR = new File(System.getProperty("user.dir"), "src/main/resources/malay-dataset/vector").getAbsolutePath();
    public static String VECTOR_PATH = VECTOR_DIR + VECTOR_FILE;
    private static final Logger log = LoggerFactory.getLogger(LoadTwitterWordVec.class);

    public static void main(String[] args) {

        long read_time1 = System.currentTimeMillis();
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(VECTOR_PATH);
        long read_time2 = System.currentTimeMillis();

        log.info("Model loaded in {} msec", read_time2 - read_time1);

        log.info("Load model & find closest words....");
        Collection<String> lst = vec.wordsNearest("banjir", 5);
        System.out.println("5 Words closest to 'banjir': " + lst);
        System.out.println(vec.getVocab().words().size());
    }
}
