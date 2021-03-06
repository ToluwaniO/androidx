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
package androidx.ui.core.test

import android.widget.FrameLayout
import androidx.compose.Composable
import androidx.compose.Composition
import androidx.compose.Providers
import androidx.compose.Recomposer
import androidx.compose.ambientOf
import androidx.compose.compositionReference
import androidx.compose.invalidate
import androidx.compose.onActive
import androidx.compose.onCommit
import androidx.compose.onDispose
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.test.filters.MediumTest
import androidx.ui.core.setContent
import androidx.ui.framework.test.TestActivity
import androidx.ui.viewinterop.AndroidView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Composable private fun Recompose(body: @Composable (recompose: () -> Unit) -> Unit) =
    body(invalidate)

@MediumTest
@RunWith(JUnit4::class)
class WrapperTest {
    @Suppress("DEPRECATION")
    @get:Rule
    val activityTestRule = androidx.test.rule.ActivityTestRule<TestActivity>(
        TestActivity::class.java
    )
    private lateinit var activity: TestActivity

    @Before
    fun setup() {
        activity = activityTestRule.activity
    }

    @Test
    fun ensureComposeWrapperDoesntPropagateInvalidations() {
        val commitLatch = CountDownLatch(2)
        var composeWrapperCount = 0
        var innerCount = 0

        activityTestRule.runOnUiThread {
            activity.setContent {
                onCommit { composeWrapperCount++ }
                Recompose { recompose ->
                    onCommit {
                        innerCount++
                        commitLatch.countDown()
                    }
                    onActive { recompose() }
                }
            }
        }
        assertTrue(commitLatch.await(1, TimeUnit.SECONDS))
        assertEquals(1, composeWrapperCount)
        assertEquals(2, innerCount)
    }

    @Test
    fun disposedWhenActivityDestroyed() {
        val composedLatch = CountDownLatch(1)
        val disposeLatch = CountDownLatch(1)

        val owner = RegistryOwner()

        activityTestRule.runOnUiThread {
            val view = FrameLayout(activity)
            activity.setContentView(view)
            ViewTreeLifecycleOwner.set(view, owner)
            view.setContent(Recomposer.current()) {
                onDispose {
                    disposeLatch.countDown()
                }
                composedLatch.countDown()
            }
        }

        assertTrue(composedLatch.await(1, TimeUnit.SECONDS))

        activityTestRule.runOnUiThread {
            assertEquals(1, disposeLatch.count)
            owner.registry.currentState = Lifecycle.State.DESTROYED
        }

        assertTrue(disposeLatch.await(1, TimeUnit.SECONDS))
    }

    @Test
    fun detachedFromLifecycleWhenDisposed() {
        val owner = RegistryOwner()
        var composition: Composition? = null
        val composedLatch = CountDownLatch(1)

        activityTestRule.runOnUiThread {
            val view = FrameLayout(activity)
            activity.setContentView(view)
            ViewTreeLifecycleOwner.set(view, owner)
            composition = view.setContent(Recomposer.current()) {
                composedLatch.countDown()
            }
        }

        assertTrue(composedLatch.await(1, TimeUnit.SECONDS))

        activityTestRule.runOnUiThread {
            assertEquals(1, owner.registry.observerCount)
            composition!!.dispose()
            assertEquals(0, owner.registry.observerCount)
        }
    }

    @Test
    @Ignore("b/159106722")
    fun compositionLinked_whenParentProvided() {
        val composedLatch = CountDownLatch(1)
        var value = 0f

        activityTestRule.runOnUiThread {
            val frameLayout = FrameLayout(activity)
            activity.setContent {
                val ambient = ambientOf<Float>()
                Providers(ambient provides 1f) {
                    val recomposer = Recomposer.current()
                    val composition = compositionReference()

                    AndroidView({ frameLayout })
                    onCommit {
                        frameLayout.setContent(recomposer, composition) {
                            value = ambient.current
                            composedLatch.countDown()
                        }
                    }
                }
            }
        }
        assertTrue(composedLatch.await(1, TimeUnit.SECONDS))
        assertEquals(1f, value)
    }

    private class RegistryOwner : LifecycleOwner {
        var registry = LifecycleRegistry(this).also {
            it.currentState = Lifecycle.State.RESUMED
        }
        override fun getLifecycle() = registry
    }
}
