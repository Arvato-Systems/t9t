package com.arvatosystems.t9t.genconf.services;

import com.arvatosystems.t9t.genconf.ConfigDTO;

import jakarta.annotation.Nonnull;

public interface IConfigUpdater {

    void updateConfig(@Nonnull ConfigDTO config);

    void deleteConfig(@Nonnull Long configRef);
}
