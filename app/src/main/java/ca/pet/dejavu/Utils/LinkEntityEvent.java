package ca.pet.dejavu.Utils;

/**
 * Created by CAMac on 2017/11/17.
 */

public class LinkEntityEvent {

    private final int action_Id;
    private final int tag;

    public LinkEntityEvent(int action_Id, int tag) {
        this.action_Id = action_Id;
        this.tag = tag;
    }

    public int getAction_Id() {
        return action_Id;
    }

    public int getTag() {
        return tag;
    }
}
