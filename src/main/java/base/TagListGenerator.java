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
     * @param cidLength length of category id
     * @param density number of tag per category
     * @param unknownTagNum number of unknownTags tags
     * @param missingTagNum number of missing tags
     * @return TagRepository
     */
    public static TagRepository generateTagRepository(int tagIDLength, int cidLength, int allTagNum, int density, int unknownTagNum, int missingTagNum){
        List<Tag> allTagList = TagListGenerator.tagListFactory(tagIDLength, cidLength, allTagNum,density);
        List<Tag> expectedTagList = TagListGenerator.removeTag(allTagList, unknownTagNum);
        List<Tag> actualTagList = TagListGenerator.removeTag(expectedTagList, missingTagNum);
        return new TagRepository(allTagList, expectedTagList, actualTagList);
    }

    /**
     * Generates an expected tag list with the according to the tag ID length, category id length, tag number, tag number per cid
     * @param tagIDLength Tag ID Length
     * @param cidLength Category ID length
     * @param all Number of tags
     * @param density number of tag per category
     * @return List of expected tags
     */
    public static List<Tag> tagListFactory(int tagIDLength, int cidLength, int all, int density) {
        List<Tag> allTagList = new ArrayList<>();
        for (int i = 0; i < all; i++)
            allTagList.add(new Tag());
        allocateRandom(allTagList, tagIDLength, cidLength,density);
        return allTagList;
    }



    /**
     * Compared with allocateRandom, allocateRandom2 controls thr number of tags in each category equal to density
     * @param tagList
     * @param tagIDLength
     * @param categoryIDLength
     * @param density : the number of tags in each category
     */
    private static void allocateRandom(List<Tag> tagList, int tagIDLength, int categoryIDLength, int density) {
        int num = 0;
        String cid = getRandomID(categoryIDLength);
        for (Tag tag : tagList) {
            tag.setTagID(getRandomID(tagIDLength));
            num ++;
            tag.setCategoryID(cid);
            if(num%density==0) cid=getRandomID(categoryIDLength);
        }
    }

    /**
     * Random Tag Id generator
     * @param IDLength give length of the tag ID
     * @return the returned string can be used as a tagID
     */
    private static String getRandomID(int IDLength){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < IDLength; i++)
            sb.append((Math.random()>0.5?'1':'0'));
        return sb.toString();
    }


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
