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

package com.example.android.architecture.blueprints.todoapp.data.source

import android.support.annotation.VisibleForTesting
import com.example.android.architecture.blueprints.todoapp.data.Task

import java.util.ArrayList

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 *
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
class TasksRepository private constructor(
    private val tasksRemoteDataSource: TasksDataSource,
    private val tasksLocalDataSource: TasksDataSource)
  : TasksDataSource {

  @VisibleForTesting
  var cachedTasks : MutableMap<String, Task>? = null
    private set

  /**
   * Marks the cache as invalid, to force an update the next time data is requested. This variable
   * has package local visibility so it can be accessed from tests.
   */
  private var cacheIsDirty: Boolean = false

  /**
   * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
   * available first.
   *
   * Note: [LoadTasksCallback.onDataNotAvailable] is fired if all data sources fail to
   * get the data.
   */
  override fun getTasks(callback: TasksDataSource.LoadTasksCallback) {
    // Respond immediately with cache if available and not dirty
    val cachedTasks = cachedTasks
    if (cachedTasks != null && !cacheIsDirty) {
      callback.onTasksLoaded(ArrayList(cachedTasks.values))
      return
    }

    if (cacheIsDirty) {
      // If the cache is dirty we need to fetch new data from the network.
      getTasksFromRemoteDataSource(callback)
    } else {
      // Query the local storage if available. If not, query the network.
      tasksRemoteDataSource.getTasks(object : TasksDataSource.LoadTasksCallback {
        override fun onTasksLoaded(tasks: List<Task>) {
          refreshCache(tasks)
          callback.onTasksLoaded(ArrayList(cachedTasks!!.values))
        }

        override fun onDataNotAvailable() {
          getTasksFromRemoteDataSource(callback)
        }
      })
    }
  }

  override fun saveTask(task: Task) {
    tasksRemoteDataSource.saveTask(task)
    tasksLocalDataSource.saveTask(task)

    // Do in memory cache update to keep the app UI up to date
    val cachedTasks = cachedTasks ?: mutableMapOf<String, Task>().also { cachedTasks = it }
    cachedTasks.put(task.id, task)
  }

  override fun completeTask(task: Task) {
    tasksRemoteDataSource.completeTask(task)
    tasksLocalDataSource.completeTask(task)

    val completedTask = Task(task.title, task.description, true, task.id)

    // Do in memory cache update to keep the app UI up to date
    val cachedTasks = cachedTasks ?: mutableMapOf<String, Task>().also { cachedTasks = it }
    cachedTasks.put(task.id, completedTask)
  }

  override fun completeTask(taskId: String) {
    completeTask(getTaskWithId(taskId)!!)
  }

  override fun activateTask(task: Task) {
    tasksRemoteDataSource.activateTask(task)
    tasksLocalDataSource.activateTask(task)

    val activeTask = Task(task.title, task.description, id = task.id)

    // Do in memory cache update to keep the app UI up to date
    val cachedTasks = cachedTasks ?: mutableMapOf<String, Task>().also { cachedTasks = it }
    cachedTasks.put(task.id, activeTask)
  }

  override fun activateTask(taskId: String) {
    activateTask(getTaskWithId(taskId)!!)
  }

  override fun clearCompletedTasks() {
    tasksRemoteDataSource.clearCompletedTasks()
    tasksLocalDataSource.clearCompletedTasks()

    val oldCachedTasks = cachedTasks ?: mutableMapOf<String, Task>().also { cachedTasks = it }
    cachedTasks = oldCachedTasks.filter { !it.value.completed }.toMutableMap()
  }

  /**
   * Gets tasks from local data source (sqlite) unless the table is new or empty. In that case it
   * uses the network data source. This is done to simplify the sample.
   *
   * Note: [GetTaskCallback.onDataNotAvailable] is fired if both data sources fail to
   * get the data.
   */
  override fun getTask(taskId: String, callback: TasksDataSource.GetTaskCallback) {
    val cachedTask = getTaskWithId(taskId)

    // Respond immediately with cache if available
    if (cachedTask != null) {
      callback.onTaskLoaded(cachedTask)
      return
    }

    // Is the task in the local data source? If not, query the network.
    tasksLocalDataSource.getTask(taskId, object : TasksDataSource.GetTaskCallback {
      override fun onTaskLoaded(task: Task) {
        // Do in memory cache update to keep the app UI up to date
        val cachedTasks = cachedTasks ?: mutableMapOf<String, Task>().also { cachedTasks = it }
        cachedTasks.put(task.id, task)
        callback.onTaskLoaded(task)
      }

      override fun onDataNotAvailable() {
        tasksRemoteDataSource.getTask(taskId, object : TasksDataSource.GetTaskCallback {
          override fun onTaskLoaded(task: Task) {
            // Do in memory cache update to keep the app UI up to date
            val cachedTasks = cachedTasks ?: mutableMapOf<String, Task>().also { cachedTasks = it }
            cachedTasks.put(task.id, task)
            callback.onTaskLoaded(task)
          }

          override fun onDataNotAvailable() {
            callback.onDataNotAvailable()
          }
        })
      }
    })
  }

  override fun refreshTasks() {
    cacheIsDirty = true
  }

  override fun deleteAllTasks() {
    tasksRemoteDataSource.deleteAllTasks()
    tasksLocalDataSource.deleteAllTasks()

    val cachedTasks = cachedTasks ?: mutableMapOf<String, Task>().also { cachedTasks = it }
    cachedTasks.clear()
  }

  override fun deleteTask(taskId: String) {
    tasksRemoteDataSource.deleteTask(taskId)
    tasksLocalDataSource.deleteTask(taskId)

    cachedTasks!!.remove(taskId)
  }

  private fun getTasksFromRemoteDataSource(callback: TasksDataSource.LoadTasksCallback) {
    tasksRemoteDataSource.getTasks(object : TasksDataSource.LoadTasksCallback {
      override fun onTasksLoaded(tasks: List<Task>) {
        refreshCache(tasks)
        refreshLocalDataSource(tasks)
        callback.onTasksLoaded(ArrayList(cachedTasks!!.values))
      }

      override fun onDataNotAvailable() {
        callback.onDataNotAvailable()
      }
    })
  }

  private fun refreshCache(tasks: List<Task>) {
    val cachedTasks = cachedTasks ?: LinkedHashMap<String, Task>().also { cachedTasks = it }
    cachedTasks.clear()
    for (task in tasks) {
      cachedTasks.put(task.id, task)
    }
    cacheIsDirty = false
  }

  private fun refreshLocalDataSource(tasks: List<Task>) {
    tasksLocalDataSource.deleteAllTasks()
    for (task in tasks) {
      tasksLocalDataSource.saveTask(task)
    }
  }

  private fun getTaskWithId(id: String) : Task? {
    val cachedTasks = cachedTasks
    return if (cachedTasks == null || cachedTasks.isEmpty()) null else cachedTasks[id]
  }

  companion object {
    var INSTANCE: TasksRepository? = null

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param tasksRemoteDataSource the backend data source
     * @param tasksLocalDataSource  the device storage data source
     * @return the [TasksRepository] instance
     */
    fun getInstance(tasksRemoteDataSource: TasksDataSource, tasksLocalDataSource: TasksDataSource)
        : TasksRepository {
      return INSTANCE
          ?: TasksRepository(tasksRemoteDataSource, tasksLocalDataSource).also { INSTANCE = it }
    }

    /**
     * Used to force [.getInstance] to create a new instance
     * next time it's called.
     */
    fun destroyInstance() {
      INSTANCE = null
    }
  }
}
