/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arvatosystems.t9t.auth.jpa;

import com.arvatosystems.t9t.auth.jpa.entities.PasswordEntity;
import com.arvatosystems.t9t.auth.services.PasswordSyncStatus;

/**
 * Result carrier for {@link IPasswordSettingService} operations.
 * Combines the newly created {@link PasswordEntity} with the outcome of the
 * optional password synchronization to an external secrets store.
 */
public record PasswordSettingResult(PasswordEntity passwordEntity, PasswordSyncStatus syncStatus) {
}
