package chapter5;

public class PCData {
    private final int intData;

    public PCData(String d){
        intData = Integer.valueOf(d);
    }

    public PCData(int intData) {
        this.intData = intData;
    }

    public int getIntData() {
        return intData;
    }

    @Override
    public String toString() {
        return "PCData{" +
                "intData=" + intData +
                '}';
    }
}
