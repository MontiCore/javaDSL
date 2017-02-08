package parsableButInvalidModels;

class VarargsWithTrailingArrayBrackets {
  void method(int x[], int... y[]) { // possibly allowed by grammar but not by compiler (coco check?)
  }
  public VarargsWithTrailingArrayBrackets(String...strings[]) {
  }
}
