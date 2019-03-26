package sample;

public class Sample {

  public Sample(int i) {
    int j = i++;
  }

  // TODO Raise an issue here!
  private String myMethod() {
    return "hello";
  }

  // TODO This method raises 3 new issues (TODO, unused, empty block)
  private void unusedAndEmpty() {

  }
}
