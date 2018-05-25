package chapter6;

@FunctionalInterface
public interface IntHandler {
    void handle(int i);

    boolean equals(Object o);

    default void print(){

    }
}
