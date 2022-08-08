package base;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to keep the tag repository. It records the expected tags, missing tags and etc
 */
public class TagRepository {
    private List<Tag> allTagList;
    private List<Tag> expectedTagList;
    private List<Tag> actucaltagList;


    public TagRepository(List<Tag> allTagList, List<Tag> expectedTagList, List<Tag> actucaltagList){
        this.allTagList = allTagList;
        this.expectedTagList = expectedTagList;
        this.actucaltagList = actucaltagList;
    }

    public List<Tag> less (List<Tag> l1, List<Tag> l2) {
        List<Tag> l3 = new ArrayList<>();
        for(Tag tag : l1)
        {
            if(!l2.contains(tag))
                l3.add(tag);
        }
        return l3;
    }

    public List<Tag> add (List<Tag> l1, List<Tag> l2){
        for (Tag tag: l2){
            l1.add(tag);
        }
        return l1;
    }

    public List<Tag> remove (List<Tag> l1, List<Tag> l2){
        for (Tag tag: l2){
            if (l1.contains(tag))
                l1.remove(tag);
        }
        return l1;
    }

    public List<Tag> getAllTagList() {
        return allTagList;
    }

    public List<Tag> getExpectedTagList() {
        return expectedTagList;
    }

    public List<Tag> getActucaltagList() {
        return actucaltagList;
    }

}
