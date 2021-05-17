package Model;

public class Model implements ModelInterface{

    private String B1 = "B1",B2 = "B2";

    public Model(String b1, String b2) {
        B1 = b1;
        B2 = b2;
    }

    @Override
    public String getB1() {
        return B1;
    }

    @Override
    public String getB2() {
        return B2;
    }
}
