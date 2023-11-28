class Test {
    String field = "hi";

    public static void main(String[] args) {
//        final String value = null;
//        System.out.println(value?.concat("other"));

        final Test test = null;
        System.out.println(test?.field);
    }
}
