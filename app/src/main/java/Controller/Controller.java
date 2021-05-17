package Controller;
import Model.Model;
import Model.ModelInterface;
import View.ViewInterface;


public class Controller implements ControllerInterface{

   private ViewInterface viewInterface;

    public Controller(ViewInterface view)
    {
        this.viewInterface = view;
    }

    @Override
    public void onPassThrough(String b1,String b2) {

        Model model = new Model(b1, b2);
        viewInterface.onUserAction(model.getB1(),model.getB2());
    }
}
