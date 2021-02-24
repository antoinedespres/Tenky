package fr.dutapp.tenky.ui.all_cities;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AllCitiesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AllCitiesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is all_cities fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}