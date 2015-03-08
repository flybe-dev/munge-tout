package munge.tout;

import java.lang.reflect.Array;
import java.util.*;

public class ClassWithArray {
    private int[] integerArray;

    public List<Integer>[] idioticList = (List<Integer>[]) Array.newInstance(ArrayList.class, 0);
    public List<Integer[][]> reallyIdioticList = new ArrayList();

    public Map<Integer, String[]>[] idioticMap = (Map<Integer, String[]>[]) Array.newInstance(HashMap.class, 0);

    public ClassWithArray() {}

    public int[] getIntegerArray() {
        return integerArray;
    }

    public void setIntegerArray(int[] anArray) {
        integerArray = anArray;
    }

    public boolean equals(Object obj) {
        ClassWithArray other = (ClassWithArray) obj;
        return Arrays.equals(this.idioticList, other.idioticList)
                && this.reallyIdioticList.equals(other.reallyIdioticList)
                && Arrays.equals(this.integerArray, other.integerArray);
    }
}
