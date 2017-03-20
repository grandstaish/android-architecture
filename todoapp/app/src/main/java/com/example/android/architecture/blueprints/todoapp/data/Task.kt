/*
 * Copyright 2016, The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.data

import java.util.UUID

/**
 * Immutable model class for a Task.
 */
data class Task @JvmOverloads constructor(
    val title: String?,
    val description: String?,
    val completed: Boolean = false,
    val id : String = UUID.randomUUID().toString()) {

  val titleForList = if (!title.isNullOrEmpty()) title else description

  val isActive = !completed

  val isEmpty = title.isNullOrEmpty() && description.isNullOrEmpty()
}
