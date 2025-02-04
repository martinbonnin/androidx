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

package androidx.compose.ui.focus

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester.Companion.Default
import androidx.compose.ui.focus.FocusStateImpl.Active
import androidx.compose.ui.focus.FocusStateImpl.ActiveParent
import androidx.compose.ui.focus.FocusStateImpl.Captured
import androidx.compose.ui.focus.FocusStateImpl.Inactive
import androidx.compose.ui.layout.BeyondBoundsLayout
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.modifier.ModifierLocalNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.Nodes
import androidx.compose.ui.node.ObserverNode
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.node.requireOwner
import androidx.compose.ui.node.visitAncestors
import androidx.compose.ui.platform.InspectorInfo

/**
 * This modifier node can be used to create a modifier that makes a component focusable.
 * Use a different instance of [FocusTargetModifierNode] for each focusable component.
 */
class FocusTargetModifierNode : ObserverNode, ModifierLocalNode, Modifier.Node() {
    /**
     * The [FocusState] associated with this [FocusTargetModifierNode].
     */
    val focusState: FocusState
        get() = focusStateImpl

    private var isProcessingCustomExit = false
    private var isProcessingCustomEnter = false

    internal var focusStateImpl = Inactive
    internal val beyondBoundsLayoutParent: BeyondBoundsLayout?
        get() = ModifierLocalBeyondBoundsLayout.current

    override fun onObservedReadsChanged() {
        val previousFocusState = focusState
        invalidateFocus()
        if (previousFocusState != focusState) refreshFocusEventNodes()
    }

    /**
     * Clears focus if this focus target has it.
     */
    override fun onReset() {
        when (focusState) {
            // Clear focus from the current FocusTarget.
            // This currently clears focus from the entire hierarchy, but we can change the
            // implementation so that focus is sent to the immediate focus parent.
            Active, Captured -> requireOwner().focusOwner.clearFocus(force = true)
            ActiveParent -> {
                scheduleInvalidationForFocusEvents()
                // This node might be reused, so reset the state to Inactive.
                focusStateImpl = Inactive
            }
            Inactive -> scheduleInvalidationForFocusEvents()
        }
    }

    /**
     * Visits parent [FocusPropertiesModifierNode]s and runs
     * [FocusPropertiesModifierNode.modifyFocusProperties] on each parent.
     * This effectively collects an aggregated focus state.
     */
    internal fun fetchFocusProperties(): FocusProperties {
        val properties = FocusPropertiesImpl()
        visitAncestors(Nodes.FocusProperties or Nodes.FocusTarget) {
            // If we reach the previous default focus properties node, we have gone too far, as
            //  this is applies to the parent focus modifier.
            if (it.isKind(Nodes.FocusTarget)) return properties

            // Parent can override any values set by this
            check(it is FocusPropertiesModifierNode)
            it.modifyFocusProperties(properties)
        }
        return properties
    }

    /**
     * Fetch custom enter destination associated with this [focusTarget].
     *
     * Custom focus enter properties are specified as a lambda. If the user runs code in this
     * lambda that triggers a focus search, or some other focus change that causes focus to leave
     * the sub-hierarchy associated with this node, we could end up in a loop as that operation
     * will trigger another invocation of the lambda associated with the focus exit property.
     * This function prevents that re-entrant scenario by ensuring there is only one concurrent
     * invocation of this lambda.
     */
    @OptIn(ExperimentalComposeUiApi::class)
    internal inline fun fetchCustomEnter(
        focusDirection: FocusDirection,
        block: (FocusRequester) -> Unit
    ) {
        if (!isProcessingCustomEnter) {
            isProcessingCustomEnter = true
            fetchFocusProperties().enter(focusDirection).also {
                if (it !== Default) block(it)
            }
            isProcessingCustomEnter = false
        }
    }

    /**
     * Fetch custom exit destination associated with this [focusTarget].
     *
     * Custom focus exit properties are specified as a lambda. If the user runs code in this
     * lambda that triggers a focus search, or some other focus change that causes focus to leave
     * the sub-hierarchy associated with this node, we could end up in a loop as that operation
     * will trigger another invocation of the lambda associated with the focus exit property.
     * This function prevents that re-entrant scenario by ensuring there is only one concurrent
     * invocation of this lambda.
     */
    @OptIn(ExperimentalComposeUiApi::class)
    internal inline fun fetchCustomExit(
        focusDirection: FocusDirection,
        block: (FocusRequester) -> Unit
    ) {
        if (!isProcessingCustomExit) {
            isProcessingCustomExit = true
            fetchFocusProperties().exit(focusDirection).also {
                if (it !== Default) block(it)
            }
            isProcessingCustomExit = false
        }
    }

    internal fun invalidateFocus() {
        when (focusState) {
            // Clear focus from the current FocusTarget.
            // This currently clears focus from the entire hierarchy, but we can change the
            // implementation so that focus is sent to the immediate focus parent.
            Active, Captured -> {
                lateinit var focusProperties: FocusProperties
                observeReads {
                    focusProperties = fetchFocusProperties()
                }
                if (!focusProperties.canFocus) {
                    requireOwner().focusOwner.clearFocus(force = true)
                }
            }

            ActiveParent, Inactive -> {}
        }
    }

    internal fun scheduleInvalidationForFocusEvents() {
        visitAncestors(Nodes.FocusEvent or Nodes.FocusTarget) {
            if (it.isKind(Nodes.FocusTarget)) return@visitAncestors

            check(it is FocusEventModifierNode)
            requireOwner().focusOwner.scheduleInvalidation(it)
        }
    }

    internal object FocusTargetModifierElement : ModifierNodeElement<FocusTargetModifierNode>() {
        override fun create() = FocusTargetModifierNode()

        override fun update(node: FocusTargetModifierNode) = node

        override fun InspectorInfo.inspectableProperties() {
            name = "focusTarget"
        }

        override fun hashCode() = "focusTarget".hashCode()
        override fun equals(other: Any?) = other === this
    }
}
