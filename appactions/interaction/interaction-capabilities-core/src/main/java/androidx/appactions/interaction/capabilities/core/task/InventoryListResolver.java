/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.appactions.interaction.capabilities.core.task;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

/**
 * Repeated version of {@code InventoryResolver}.
 *
 * @param <T>
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface InventoryListResolver<T> extends ValueListener<List<T>> {
    /**
     * Renders the provided entities in the app UI for dismabiguation.
     *
     * <p>The app should not modify the entity contents or their orders during the rendering.
     * Otherwise, the Assistant task will be out of sync with the app UI.
     */
    @NonNull
    ListenableFuture<Void> renderChoices(@NonNull List<String> entityIDs);
}
