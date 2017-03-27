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

package com.example.android.architecture.blueprints.todoapp.addedittask

import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.test.espresso.IdlingResource
import android.support.v7.app.AppCompatActivity

import com.example.android.architecture.blueprints.todoapp.Injection.provideTasksRepository
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment.Companion.ARGUMENT_EDIT_TASK_ID
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.addFragmentToActivity
import com.example.android.architecture.blueprints.todoapp.util.getOrInit
import com.example.android.architecture.blueprints.todoapp.util.set
import kotlinx.android.synthetic.main.addtask_act.*

/**
 * Displays an add or edit task screen.
 */
class AddEditTaskActivity : AppCompatActivity() {

  private lateinit var addEditTaskPresenter: AddEditTaskPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.addtask_act)

    // Set up the toolbar.
    setSupportActionBar(toolbar)
    supportActionBar?.apply {
      setDisplayHomeAsUpEnabled(true)
      setDisplayShowHomeEnabled(true)
    }

    val taskId : String? = intent.getStringExtra(ARGUMENT_EDIT_TASK_ID)

    val addEditTaskFragment = supportFragmentManager.getOrInit(R.id.contentFrame) { id ->
      val result = AddEditTaskFragment.newInstance()
      val title = if (taskId == null) R.string.edit_task else R.string.add_task
      actionBar.setTitle(title)
      taskId?.let { taskId ->
        result.arguments = Bundle().apply {
          set(ARGUMENT_EDIT_TASK_ID to taskId)
//          set("mykey" to 3)
//          set("myotherkey" to 100)
        }
      }
      addFragmentToActivity(result, id)
    }

    val shouldLoadDataFromRepo = savedInstanceState?.getBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY) ?: true

    // Create the presenter
    addEditTaskPresenter = AddEditTaskPresenter(
        taskId,
        provideTasksRepository(applicationContext),
        addEditTaskFragment,
        shouldLoadDataFromRepo)

    addEditTaskFragment.setPresenter(addEditTaskPresenter)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    // Save the state so that next time we know if we need to refresh data.
    outState.putBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY, addEditTaskPresenter.isDataMissing)
    super.onSaveInstanceState(outState)
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }

  @VisibleForTesting
  val countingIdlingResource: IdlingResource
    get() = EspressoIdlingResource.idlingResource

  companion object {
    val REQUEST_ADD_TASK = 1

    val SHOULD_LOAD_DATA_FROM_REPO_KEY = "SHOULD_LOAD_DATA_FROM_REPO_KEY"
  }
}
