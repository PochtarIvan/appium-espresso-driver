/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
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

package io.appium.espressoserver.lib.model

import androidx.test.espresso.IdlingResource
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.ReflectionUtils
import java.lang.Exception

data class IdlingResourcesParams(
    val classNames: String
) : AppiumParams() {
    fun toIdlingResources(): List<IdlingResource> {
        return classNames
                .split(",")
                .map { it.trim() }
                .map {
                    try {
                        Class.forName(it)
                    } catch (e: ClassNotFoundException) {
                        throw IllegalArgumentException("'$it' is not a valid class name")
                    }
                }
                .map {
                    val getInstanceMethod = try {
                        ReflectionUtils.method(it, "getInstance")
                    } catch (e: Exception) {
                        AndroidLogger.logger.error(e.message!!)
                        throw IllegalArgumentException("'${it.canonicalName}' class must have getInstance() method")
                    }
                    val instance = try {
                        ReflectionUtils.invoke(getInstanceMethod, it)
                    } catch (e: Exception) {
                        AndroidLogger.logger.error(e.message!!)
                        throw IllegalArgumentException(
                                "Got an unexpected exception while calling '${it.canonicalName}.getInstance()': " +
                                        e.message)
                    }
                    if (instance !is IdlingResource) {
                        throw IllegalArgumentException(
                                "'${it.canonicalName}.getInstance()' must return an object that implements " +
                                        "androidx.test.espresso.IdlingResource interface")
                    }
                    @Suppress("USELESS_CAST")
                    instance as IdlingResource
                }
    }
}
