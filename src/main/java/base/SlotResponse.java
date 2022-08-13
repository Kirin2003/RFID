package base;

import java.util.ArrayList;
import java.util.List;

/**
 * A Slot response class to record the responses of slots
 */
public class SlotResponse {
    private int type;
    private String content;

    private Tag tag;

    public SlotResponse(int type, String content, Tag tag){
        this.setType(type);
        this.content = content;
        this.tag = tag;
    }

    public SlotResponse(int type, String content) {
        this.type = type;
        this.content = content;
    }

    //Sets and Gets
    public void setType(int type) {
        this.type = type;
    }
    public int getType() {
        return type;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void setTag(Tag tag) {
        this.tag = tag;
    }
    public Tag getTag() {
        return tag;
    }
}


