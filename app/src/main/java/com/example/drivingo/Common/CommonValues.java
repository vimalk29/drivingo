package com.example.drivingo.Common;

import com.example.drivingo.model.Bike;
import com.example.drivingo.model.BookedBike;
import com.google.firebase.storage.StorageTask;

import java.util.Date;
import java.util.List;

public class CommonValues {

    public static StorageTask storageTask;
    public static List<Bike> CommonBikeList;
    public static List<BookedBike> commonBookedList;
    public static Date FromDate,ToDate;
    public static int rent;
}
