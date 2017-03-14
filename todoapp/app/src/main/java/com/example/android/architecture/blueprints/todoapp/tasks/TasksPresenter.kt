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

package com.example.android.architecture.blueprints.todoapp.tasks

import android.app.Activity.RESULT_OK
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity.Companion.REQUEST_ADD_TASK

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource

/**
 * Listens to user actions from the UI ([TasksFragment]), retrieves the data and updates the
 * UI as required.
 */
class TasksPresenter(
    private val tasksRepository: TasksRepository,
    private val tasksView: TasksContract.View)
  : TasksContract.Presenter {

  override var filtering = TasksFilterType.ALL_TASKS

  private var firstLoad = true

  init {
    tasksView.setPresenter(this)
  }

  override fun start() {
    loadTasks(false)
  }

  override fun result(requestCode: Int, resultCode: Int) {
    // If a task was successfully added, show snackbar
    if (REQUEST_ADD_TASK == requestCode && RESULT_OK == resultCode) {
      tasksView.showSuccessfullySavedMessage()
    }
  }

  override fun loadTasks(forceUpdate: Boolean) {
    // Simplification for sample: a network reload will be forced on first load.
    loadTasks(forceUpdate = forceUpdate || firstLoad, showLoadingUI = true)
    firstLoad = false
  }

  /**
   * @param forceUpdate   Pass in true to refresh the data in the [TasksDataSource]
   * @param showLoadingUI Pass in true to display a loading icon in the UI
   */
  private fun loadTasks(forceUpdate: Boolean = false, showLoadingUI: Boolean = false) {
    if (showLoadingUI) {
      tasksView.setLoadingIndicator(true)
    }
    if (forceUpdate) {
      tasksRepository.refreshTasks()
    }

    // The network request might be handled in a different thread so make sure Espresso knows
    // that the app is busy until the response is handled.
    EspressoIdlingResource.increment() // App is busy until further notice

    tasksRepository.getTasks(object : TasksDataSource.LoadTasksCallback {
      override fun onTasksLoaded(tasks: List<Task>) {
        // This callback may be called twice, once for the cache and once for loading
        // the data from the server API, so we check before decrementing, otherwise
        // it throws "Counter has been corrupted!" exception.
        if (!EspressoIdlingResource.idlingResource.isIdleNow) {
          EspressoIdlingResource.decrement() // Set app as idle.
        }

        // We filter the tasks based on the requestType
        val tasksToShow = tasks.filter {
          when (filtering) {
            TasksFilterType.ACTIVE_TASKS -> it.isActive
            TasksFilterType.COMPLETED_TASKS -> it.completed
            else -> true
          }
        }

        // The view may not be able to handle UI updates anymore
        if (!tasksView.isActive) return

        if (showLoadingUI) {
          tasksView.setLoadingIndicator(false)
        }

        processTasks(tasksToShow)
      }

      override fun onDataNotAvailable() {
        // The view may not be able to handle UI updates anymore
        if (!tasksView.isActive) return

        tasksView.showLoadingTasksError()
      }
    })
  }

  private fun processTasks(tasks: List<Task>) {
    if (tasks.isEmpty()) {
      // Show a message indicating there are no tasks for that filter type.
      processEmptyTasks()
    } else {
      // Show the list of tasks
      tasksView.showTasks(tasks)
      // Set the filter label's text.
      showFilterLabel()
    }
  }

  private fun showFilterLabel() {
    when (filtering) {
      TasksFilterType.ACTIVE_TASKS -> tasksView.showActiveFilterLabel()
      TasksFilterType.COMPLETED_TASKS -> tasksView.showCompletedFilterLabel()
      else -> tasksView.showAllFilterLabel()
    }
  }

  private fun processEmptyTasks() {
    when (filtering) {
      TasksFilterType.ACTIVE_TASKS -> tasksView.showNoActiveTasks()
      TasksFilterType.COMPLETED_TASKS -> tasksView.showNoCompletedTasks()
      else -> tasksView.showNoTasks()
    }
  }

  override fun addNewTask() {
    tasksView.showAddTask()
  }

  override fun openTaskDetails(requestedTask: Task) {
    tasksView.showTaskDetailsUi(requestedTask.id)
  }

  override fun completeTask(completedTask: Task) {
    tasksRepository.completeTask(completedTask)
    tasksView.showTaskMarkedComplete()
    loadTasks()
  }

  override fun activateTask(activeTask: Task) {
    tasksRepository.activateTask(activeTask)
    tasksView.showTaskMarkedActive()
    loadTasks()
  }

  override fun clearCompletedTasks() {
    tasksRepository.clearCompletedTasks()
    tasksView.showCompletedTasksCleared()
    loadTasks()
  }
}
