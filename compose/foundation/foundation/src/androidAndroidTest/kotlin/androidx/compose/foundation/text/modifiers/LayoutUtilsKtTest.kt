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

package androidx.compose.foundation.text.modifiers

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class LayoutUtilsKtTest {

    @Test
    fun finalConstraints_returnsTightWidth() {
        val subject = finalConstraints(
            Constraints(500, 500, 0, 50),
            true,
            TextOverflow.Ellipsis,
            42f
        )
        assertThat(subject.maxWidth).isEqualTo(500)
    }

    @Test
    fun finalConstraints_returnsMaxIntrinsicWhenUnbound() {
        val subject = finalConstraints(
            Constraints(500, 500, 0, 50),
            false,
            TextOverflow.Clip,
            1234.1f
        )
        assertThat(subject.maxWidth).isEqualTo(1235)
    }

    @Test
    fun finalMaxWidth_returnsTightWidth() {
        val subject = finalMaxWidth(
            Constraints(500, 500, 0, 50),
            true,
            TextOverflow.Ellipsis,
            42f
        )
        assertThat(subject).isEqualTo(500)
    }

    @Test
    fun finalMaxWidth_returnsMaxIntrinsicWhenUnbound() {
        val subject = finalMaxWidth(
            Constraints(500, 500, 0, 50),
            false,
            TextOverflow.Clip,
            1234.1f
        )
        assertThat(subject).isEqualTo(1235)
    }

    @Test
    fun finalMaxLines_negative() {
        val subject = finalMaxLines(true, TextOverflow.Clip, -1)
        assertThat(subject).isEqualTo(1)
    }

    @Test
    fun finalMaxLines_positive_noOverride() {
        val subject = finalMaxLines(true, TextOverflow.Clip, 4)
        assertThat(subject).isEqualTo(4)
    }

    @Test
    fun finalMaxLines_overrideOn_TextOverflowEllipsis_andSoftwrapFalse() {
        val subject = finalMaxLines(false, TextOverflow.Ellipsis, 4)
        assertThat(subject).isEqualTo(1)
    }

    @Test
    fun canChangeBreak_canWrap_false() {
        val subject = canChangeBreaks(
            canWrap = false,
            newConstraints = Constraints(0),
            oldConstraints = Constraints(0),
            maxIntrinsicWidth = 42f,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )
        assertThat(subject).isFalse()
    }

    @Test
    fun canChangeBreak_sameWidth() {
        val subject = canChangeBreaks(
            canWrap = true,
            newConstraints = Constraints.fixedWidth(50),
            oldConstraints = Constraints.fixedWidth(50),
            maxIntrinsicWidth = 1234f,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )
        assertThat(subject).isFalse()
    }

    @Test
    fun canChangeBreak_textSmallerThanConstraints() {
        val subject = canChangeBreaks(
            canWrap = true,
            newConstraints = Constraints.fixedWidth(50),
            oldConstraints = Constraints.fixedWidth(40),
            maxIntrinsicWidth = 12f,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )
        assertThat(subject).isFalse()
    }

    @Test
    fun canChangeBreak_textBiggerThanConstraints() {
        val subject = canChangeBreaks(
            canWrap = true,
            newConstraints = Constraints.fixedWidth(100),
            oldConstraints = Constraints.fixedWidth(200),
            maxIntrinsicWidth = 300f,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )
        assertThat(subject).isTrue()
    }

    @Test
    fun canChangeBreak_shrinking_textSmallerThanNewConstraints() {
        val subject = canChangeBreaks(
            canWrap = true,
            newConstraints = Constraints.fixedWidth(50),
            oldConstraints = Constraints.fixedWidth(60),
            maxIntrinsicWidth = 45f,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )
        assertThat(subject).isFalse()
    }

    @Test
    fun canChangeBreak_shrinking_textBiggerThanNewConstraints() {
        val subject = canChangeBreaks(
            canWrap = true,
            newConstraints = Constraints.fixedWidth(50),
            oldConstraints = Constraints.fixedWidth(60),
            maxIntrinsicWidth = 59f,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )
        assertThat(subject).isTrue()
    }

    @Test
    fun canChangeBreak_growing_textSmallerThanNewConstraints() {
        val subject = canChangeBreaks(
            canWrap = true,
            newConstraints = Constraints.fixedWidth(60),
            oldConstraints = Constraints.fixedWidth(50),
            maxIntrinsicWidth = 45f,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )
        assertThat(subject).isFalse()
    }

    @Test
    fun canChangeBreak_growing_textBiggerThanNewConstraints() {
        val subject = canChangeBreaks(
            canWrap = true,
            newConstraints = Constraints.fixedWidth(60),
            oldConstraints = Constraints.fixedWidth(50),
            maxIntrinsicWidth = 59f,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )
        assertThat(subject).isTrue()
    }
}