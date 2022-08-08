package base;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to generate a tags list
 */
public class TagListGenerator {

    /**
     *
     * @param tagIDLength length of tag id
     * @param allTagNum length of the original tag list
     * @param unknownTagNum number of unknownTags tags
     * @param missingTagNum number of missing tags
     * @return TagRepository
     */
    public static TagRepository generateTagRepository(int tagIDLength, int cidLength, int allTagNum, int unknownTagNum, int missingTagNum){
        List<Tag> allTagList = TagListGenerator.tagListFactory(tagIDLength, cidLength, allTagNum);
        List<Tag> expectedTagList = TagListGenerator.removeTag(allTagList, unknownTagNum);
        List<Tag> actualTagList = TagListGenerator.removeTag(expectedTagList, missingTagNum);
        return new TagRepository(allTagList, expectedTagList, actualTagList);
    }

    /**
     * Generates an expected tag list with the according to the tag ID length and tag number
     * @param tagIDLength Tag ID Length
     * @param all Number of tags
     * @return List of expected tags
     */
    public static List<Tag> tagListFactory(int tagIDLength, int cidLength, int all) {
        List<Tag> allTagList = new ArrayList<>();
        for (int i = 0; i < all; i++)
            allTagList.add(new Tag());
        allocateRandom(allTagList, tagIDLength, cidLength);
        return allTagList;
    }

    /**
     * Sets a random tagID to each tag in the list according to the ID length
     * @param tagList list of Tags
     * @param tagIDLength Length of the tag ID
     */
    public static void allocateRandom(List<Tag> tagList, int tagIDLength, int cidLength){
        for (Tag tag: tagList) {
            tag.setTagID(getRandomTagID(tagIDLength));
        }
    }

    /**
     * Random Tag Id generator
     * @param tagIDLength give length of the tag ID
     * @return the returned string can be used as a tagID
     */
    private static String getRandomTagID(int tagIDLength){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tagIDLength; i++)
            sb.append((int)(Math.random()>0.5?'1':'0'));
        return sb.toString();
    }

    //TODO Check this function more, when it is used?
    public static List<Tag> removeTag(List<Tag> tagList, int unknown){
        List<Tag> tagList0 = new ArrayList<>(tagList);
        for (int i = 0; i < unknown; i++){
            int index = (int) (Math.random() * tagList0.size());
            if (index < tagList.size()) {
                tagList0.remove(index);
            }
        }
        return tagList0;
    }
}
