/*
 * Copyright 2019 The Android Open Source Project
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

package androidx.ui.core

import androidx.compose.Immutable
import androidx.ui.unit.IntPx
import androidx.ui.unit.IntPxPosition
import androidx.ui.unit.IntPxSize
import androidx.ui.unit.round
import androidx.ui.unit.toPx

/**
 * Represents a positioning of a point inside a 2D box. [Alignment] is often used to define
 * the alignment of a box inside a parent container.
 * The coordinate space of the 2D box is the continuous [-1f, 1f] range in both dimensions,
 * and (verticalBias, horizontalBias) will be points in this space. (verticalBias=0f,
 * horizontalBias=0f) represents the center of the box, (verticalBias=-1f, horizontalBias=1f)
 * will be the top right, etc.
 */
@Immutable
data class Alignment(
    val verticalBias: Float,
    val horizontalBias: Float
) {
    /**
     * Represents a positioning of a point on a 1D vertical finite line. [Alignment.Vertical] is
     * often used to define the vertical alignment of a box inside a parent container.
     * The coordinate space of the line is the continuous [-1f, 1f] range,
     * and bias will be a point in this space. bias=0f represents the center of the box,
     * bias=-1f represents the top, bias=1f represents the bottom, etc.
     */
    @Immutable
    data class Vertical(val bias: Float) {
        /**
         * Returns the position of a 2D point in a container of a given size,
         * according to this [Alignment].
         */
        fun align(
            size: IntPx
        ): IntPx {
            // Convert to Px first and only round at the end, to avoid rounding twice while
            // calculating the new positions
            val center = size.toPx() / 2f
            return (center * (1 + bias)).round()
        }
    }

    /**
     * Represents a positioning of a point on a 1D horizontal finite line. [Alignment.Horizontal]
     * is often used to define the horizontal alignment of a box inside a parent container.
     * The coordinate space of the line is the continuous [-1f, 1f] range,
     * and ltrBias will be a point in this space. ltrBias=0f represents the center of the box,
     * ltrBias=-1f represents the start, bias=1f represents the end, etc.
     */
    @Immutable
    data class Horizontal(val bias: Float) {
        /**
         * Returns the position of a 2D point in a container of a given size,
         * according to this [Alignment].
         */
        // TODO(soboleva): remove default layout direction when Rtl is supported where this function
        //  gets called
        fun align(
            size: IntPx,
            layoutDirection: LayoutDirection = LayoutDirection.Ltr
        ): IntPx {
            // Convert to Px first and only round at the end, to avoid rounding twice while
            // calculating the new positions
            val center = size.toPx() / 2f
            val resolvedBias = if (layoutDirection == LayoutDirection.Ltr) bias else -1 * bias
            return (center * (1 + resolvedBias)).round()
        }
    }

    companion object {
        // 2D Alignments.
        val TopStart = Alignment(-1f, -1f)
        val TopCenter = Alignment(-1f, 0f)
        val TopEnd = Alignment(-1f, 1f)
        val CenterStart = Alignment(0f, -1f)
        val Center = Alignment(0f, 0f)
        val CenterEnd = Alignment(0f, 1f)
        val BottomStart = Alignment(1f, -1f)
        val BottomCenter = Alignment(1f, 0f)
        val BottomEnd = Alignment(1f, 1f)

        // 1D Alignment.Verticals.
        val Top = Alignment.Vertical(-1f)
        val CenterVertically = Alignment.Vertical(0f)
        val Bottom = Alignment.Vertical(1f)

        // 1D Alignment.Horizontals.
        val Start = Alignment.Horizontal(-1f)
        val CenterHorizontally = Alignment.Horizontal(0f)
        val End = Alignment.Horizontal(1f)
    }

    /**
     * Returns the position of a 2D point in a container of a given size,
     * according to this [Alignment].
     */
    // TODO(soboleva): remove default layout direction when Rtl is supported where this function
    //  gets called
    fun align(
        size: IntPxSize,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr
    ): IntPxPosition {
        // Convert to Px first and only round at the end, to avoid rounding twice while calculating
        // the new positions
        val centerX = size.width.toPx() / 2f
        val centerY = size.height.toPx() / 2f
        val resolvedHorizontalBias = if (layoutDirection == LayoutDirection.Ltr) {
            horizontalBias
        } else {
            -1 * horizontalBias
        }

        val x = centerX * (1 + resolvedHorizontalBias)
        val y = centerY * (1 + verticalBias)
        return IntPxPosition(x.round(), y.round())
    }
}

// TODO(soboleva/popam): add AbsoluteAlignment
