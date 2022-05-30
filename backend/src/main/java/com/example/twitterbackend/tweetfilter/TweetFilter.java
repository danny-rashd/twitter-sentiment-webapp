package com.example.twitterbackend.tweetfilter;


import java.io.*;
import java.util.*;

public class TweetFilter {
    public static final String DATA_PATH=new File(System.getProperty("user.dir"), "src/main/resources").getAbsolutePath();
    public static ArrayList<String> data;

    public static String tweetFilter(String s) throws IOException {
        s = s.toLowerCase();
        s = removeURL(s);
        s = removeNumber(s);
        s = removeNumber(s);
        s = removeTag(s);
        s = removeHash(s);
        s = removeSpecialCharac(s);
        s = removeStopwordsTXT(s);
        return s;
    }
    public static String removeURL(String s){
        String urlPattern = "(((http|ftp|https):\\\\/\\\\/)?[\\\\w\\\\-_]+(\\\\.[\\\\w\\\\-_]+)+([\\\\w\\\\-\\\\.,@?^=%&amp;:/~\\\\+#]*[\\\\w\\\\-\\\\@?^=%&amp;/~\\\\+#])?)";
        return s.replaceAll(urlPattern,"").trim();
    }

    public static String removeNumber(String s) {
        s = s.replaceAll("[0-9]", "").trim();
        return s;
    }

    public static String removeTag(String s) {
        String tagPattern = "(?:\\s|\\A)[@]+([A-Za-z0-9-_]+)";
        return s.replaceAll(tagPattern,"").trim();
    }

    public static String removeHash(String s) {
        String hashPattern = "(?:\\s|\\A)[#]+([A-Za-z0-9-_]+)";
        return s.replaceAll(hashPattern,"").trim();
    }
    public static String removeSpecialCharac(String s) {
        s = s.replaceAll("[^\\p{ASCII}]", "");
        s=s.replaceAll("\\P{Print}", "");
        s = s.replace(".", " ");
        s = s.replace("/", " ");
        s = s.replace("-", " ");
        s = s.replace(";", " ");
        s = s.replace("!", " ");
        s = s.replace(":", " ");
        s = s.replace("?", " ");
        s = s.replace(",", " ");
        s = s.replace("(", " ");
        s = s.replace(")", " ");
        s = s.replace("[", " ");
        s = s.replace("]", " ");
        s = s.replace("\"", " ");
        s = s.replace("'", "");
        s = s.replace("&", " ");
        s = s.replace("=", " ");
        s = s.replace("|", " ");
        s = s.replace(">", " ");
        s = s.replace("<", " ");
        s = s.replace("*", " ");
        s = s.replace("_", " ");
        s = s.replace("%", " ");
        s = s.replace("\\", " ");
        s = s.replace("{", " ");
        s = s.replace("}", " ");
        s = s.replace("^", " ");
        s = s.replace("$", " ");
        s = s.replace("+", " ");
        s = s.replace("*", " ");
        s = s.replace("~", " ");
        s = s.replace("`", " ");

        return s.trim();
    }
    public static String removeStopwordsTXT(String s) throws IOException {
        BufferedReader bufReader = new BufferedReader(new FileReader(DATA_PATH+"/stopwords.txt"));
        ArrayList<String> listOfLines = new ArrayList<>();

        String line = bufReader.readLine();
        while (line != null) {
            listOfLines.add(line);
            line = bufReader.readLine();
        }
        bufReader.close();
        String result = "";
        String[] sSplit = s.split(" ");
        for(String sl : sSplit) {
            if(!listOfLines.contains(sl)) {
                result = result + sl +" ";
            }
        }
        return result;
    }

}