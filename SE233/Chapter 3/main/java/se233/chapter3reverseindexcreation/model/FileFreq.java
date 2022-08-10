package se233.chapter3reverseindexcreation.model;

import java.util.ArrayList;
import java.util.Comparator;

public class FileFreq {
    private final String name;
    private final String path;
    private final Integer freq;

    public FileFreq(String name, String path, Integer freq) {
        this.name = name;
        this.path = path;
        this.freq = freq;
    }

    @Override
    public String toString() {
        return String.format("{%s:%d}", name, freq);
    }

    public String getPath() {
        return path;
    }

    public Integer getFreq() {
        return freq;
    }

    // Exercise 1
    // The result entries shown in the ListView on the right-hand side of the application window
    // are sorted alphabetically. Your task is to order them in terms of their total frequency, where
    // the word with the highest frequency count should come first. For the words appearing in
    // many documents, use the total sum of frequencies.
    public static class SortByFreq implements Comparator<ArrayList<FileFreq>> {
        @Override
        public int compare(ArrayList<FileFreq> o1,ArrayList<FileFreq> o2){
            return o1.get(0).getFreq().compareTo(o2.get(0).getFreq());
        }
    }
}
