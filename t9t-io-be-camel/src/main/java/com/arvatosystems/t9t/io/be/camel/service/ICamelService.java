package com.arvatosystems.t9t.io.be.camel.service;

import com.arvatosystems.t9t.io.DataSinkDTO;

public interface ICamelService {
    void addRoutes(DataSinkDTO dataSink);
    void removeRoutes(DataSinkDTO dataSink);
    void startRoute(DataSinkDTO dataSink);
}
