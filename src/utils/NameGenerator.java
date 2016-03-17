package utils;

import model.Node;

import java.util.Set;

/**
 * Created by Benedek on 3/17/2016.
 */
public class NameGenerator {
    private static final String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";

    private static final java.util.Random rand = new java.util.Random();

    public static String generateRandomName(Set<Node> nodes) {
        StringBuilder builder = new StringBuilder();
        while (builder.toString().length() == 0) {
            int length = rand.nextInt(5) + 5;
            for (int i = 0; i < length; i++) {
                builder.append(lexicon.charAt(rand.nextInt(lexicon.length())));
            }
            if (nodes.contains(builder.toString())) {
                builder = new StringBuilder();
            }
        }
        return builder.toString();
    }
}
