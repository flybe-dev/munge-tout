package munge.tout;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class ClassWithIter {
    private List<String> things = new ArrayList<>();

    public Iterator getThings() {
        return things.iterator();
    }

    public void setThings(List<String> things) {
        this.things = things;
    }
}
