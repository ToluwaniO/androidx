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

package androidx.ui.core.demos.gestures

import androidx.compose.Composable
import androidx.compose.remember
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.Popup
import androidx.ui.core.gesture.DragObserver
import androidx.ui.core.gesture.dragGestureFilter
import androidx.compose.foundation.Box
import androidx.compose.foundation.ContentGravity
import androidx.compose.foundation.Text
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Stack
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round

@Composable
fun PopupDragDemo() {
    // TODO fix this demo in RTL (check when draggable handles RTL)
    val offset = state {
        Offset.Zero
    }

    val observer = remember {
        object : DragObserver {
            override fun onDrag(dragDistance: Offset): Offset {
                offset.value = offset.value + dragDistance
                return dragDistance
            }
        }
    }

    Column {
        Text("That is a pop up with a dragGestureFilter on it.  You can drag it around!")
        Popup(alignment = Alignment.TopStart, offset = offset.value.round()) {
            Stack {
                Box(
                    Modifier
                        .dragGestureFilter(observer)
                        .preferredSize(70.dp),
                    shape = CircleShape,
                    backgroundColor = Color.Green,
                    gravity = ContentGravity.Center
                ) {
                    Text(
                        text = "This is a popup!",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
