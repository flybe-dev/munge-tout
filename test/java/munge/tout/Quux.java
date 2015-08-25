package munge.tout;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class Quux {
    private String name;
    private long foo;
    private Set<String> items;
    private SortedSet<String> sortedItems;
    private Map<Integer, String> things;
    private int integar;
    private long looong;
    private float aFloat;
    private double aDouble;
    private boolean bool;
    private BigInteger bigInt;

    public Quux(String name) {
        this.name = name;
    }

    public void setItems(Set<String> items) {
        this.items = items;
    }

    public Set<String> getItems() {
        return items;
    }

    public String getName() {
        return name;
    }

    public long getFoo() {
        return foo;
    }

    public void setFoo(long foo) {
        this.foo = foo;
    }

    public Map<Integer, String> getThings() {
        return things;
    }

    public void setThings(Map<Integer, String> things) {
        this.things = things;
    }

    public int getIntegar() {
        return integar;
    }

    public void setIntegar(int integar) {
        this.integar = integar;
    }

    public long getLooong() {
        return looong;
    }

    public void setLooong(long looong) {
        this.looong = looong;
    }


    public float getaFloat() {
        return aFloat;
    }

    public void setaFloat(float aFloat) {
        this.aFloat = aFloat;
    }

    public double getaDouble() {
        return aDouble;
    }

    public void setaDouble(double aDouble) {
        this.aDouble = aDouble;
    }

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }

    public BigInteger getBigInt() {
        return bigInt;
    }

    public void setBigInt(BigInteger bigInt) {
        this.bigInt = bigInt;
    }

    public SortedSet<String> getSortedItems() {
        return sortedItems;
    }

    public void setSortedItems(SortedSet<String> sortedItems) {
        this.sortedItems = sortedItems;
    }
}
