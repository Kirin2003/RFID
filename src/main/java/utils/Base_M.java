/**
 * General methods to get information of readers, tags and coverage ranges
 */
package utils;

import base.Tag;

import java.util.List;

public class Base_M {

    /**
     * Output all tag IDs of a tag list such as: < ID1 , ID2 , .... >
     * @param tagList		input tag list
     * @return				output tag ID string
     */
    public static String getListIDInfo(List<Tag> tagList) {
        StringBuilder sb = new StringBuilder();
        for (Tag tag : tagList) {
            sb.append(tag.getTagID()).append(",");
        }
        return sb.toString();
    }

    /**
     * Output all IDs of the reader/writer list in the format: < ID1 , ID2 , ...>
     * @param readerList		input reader list
     * @return					output ID string
     */
    public static String getListIDInof(List<Reader_M> readerList) {
        StringBuilder sb = new StringBuilder();
        for (Reader_M reader : readerList) {
            sb.append(" ").append(reader.getID()).append(" ,");
        }
        return sb.toString();
    }

    /**
     * To judge whether a tag is within the recognition range of a reader, if it is, returns true
     * @param reader			input reader
     * @param tag				input tag
     * @return					Returns whether the tag is in the reader area
     */
    public static boolean inCircle(Reader_M reader, Tag tag) {
        boolean result = false;

        double x = reader.getLocation().getX() - tag.getLocation().getX();
        double y = reader.getLocation().getY() - tag.getLocation().getY();
        if (x * x + y * y < reader.getReadingRadius() * reader.getReadingRadius()) {
            result = true;
        }
        return result;
    }

    /**
     * Helper function for outputting all values of an IntegerList
     *
     * @param list list
     * @return string builder
     */
    public static String getIntegerListString(List<Integer> list) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                sb.append("< ");
            }
            sb.append(list.get(i));
            if (i != list.size() - 1) {
                sb.append(",");
            } else {
                sb.append(" >");
            }
        }
        return sb.toString();
    }
}
