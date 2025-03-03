package com.arvatosystems.t9t.zkui.services;

import jakarta.annotation.Nonnull;

public interface IGenericObjectCrudScreenProvider {

    /**
     * Return the location of the .zul file of the CRUD screen
     * @return screen URI
     */
    @Nonnull
    String getScreenURI();
}
