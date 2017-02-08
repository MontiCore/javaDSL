class InnerCreator{

  final int z=10;

  class InnerInner {
    static final int x = 3;
    static int y = 4;
  }

  public static void main(String[] args) {

    Inner outer=new Inner();
    System.out.println(Inner.new InnerInner().y);
  }
}