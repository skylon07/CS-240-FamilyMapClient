package Examples;

import androidx.lifecycle.ViewModel;

public class LifecyclesViewModel extends ViewModel {
    private String stateVal;

    public String getStateVal() {
        return this.stateVal;
    }

    public void setStateVal(String stateVal) {
        this.stateVal = stateVal;
    }
}
