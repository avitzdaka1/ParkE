package com.omeryaari.parke.logic;

import java.util.List;

public interface FirebaseParkingDownloadListener {
    void parkingDownloaded(List<Parking> parkingList, String type);
}
