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

package com.example.android.architecture.blueprints.todoapp.statistics

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource

/**
 * Listens to user actions from the UI ([StatisticsFragment]), retrieves the data and updates
 * the UI as required.
 */
class StatisticsPresenter(
    private val tasksRepository: TasksRepository,
    private val statisticsView: StatisticsContract.View)
  : StatisticsContract.Presenter {

  init {
    statisticsView.setPresenter(this)
  }

  override fun start() {
    loadStatistics()
  }

  private fun loadStatistics() {
    statisticsView.setProgressIndicator(true)

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

        // The view may not be able to handle UI updates anymore
        if (statisticsView.isActive) {

          // We calculate number of active and completed tasks
          val completedTasks = tasks.filter { it.completed }.count()
          val activeTasks = tasks.filter { !it.completed }.count()

          statisticsView.setProgressIndicator(false)
          statisticsView.showStatistics(activeTasks, completedTasks)
        }
      }

      override fun onDataNotAvailable() {
        // The view may not be able to handle UI updates anymore
        if (statisticsView.isActive) {
          statisticsView.showLoadingStatisticsError()
        }
      }
    })
  }
}
