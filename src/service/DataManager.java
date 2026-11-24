package service;

import java.io.IOException;

public interface DataManager {
    void saveData() throws IOException;

    boolean loadData() throws IOException, ClassNotFoundException;

    boolean isDataLoadFailed();

    String getLastLoadError();
}