/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.taskdetail

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment
import com.example.android.architecture.blueprints.todoapp.util.set
import com.example.android.architecture.blueprints.todoapp.util.showSnackbar
import kotlinx.android.synthetic.main.taskdetail_frag.*

/**
 * Main UI for the task detail screen.
 */
class TaskDetailFragment : Fragment(), TaskDetailContract.View {
  private lateinit var presenter: TaskDetailContract.Presenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onResume() {
    super.onResume()
    presenter.start()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.taskdetail_frag, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val fab = activity.findViewById(R.id.fab_edit_task) as FloatingActionButton
    fab.setOnClickListener { presenter.editTask() }
  }

  override fun setPresenter(presenter: TaskDetailContract.Presenter) {
    this.presenter = presenter
  }

  override fun onOptionsItemSelected(item: MenuItem) =
      when (item.itemId) {
        R.id.menu_delete -> {
          presenter.deleteTask()
          true
        }
        else -> false
      }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.taskdetail_fragment_menu, menu)
  }

  override fun setLoadingIndicator(active: Boolean) {
    if (active) {
      taskDetailTitle.text = ""
      taskDetailDescription.text = getString(R.string.loading)
    }
  }

  override fun hideDescription() {
    taskDetailDescription.visibility = View.GONE
  }

  override fun hideTitle() {
    taskDetailTitle.visibility = View.GONE
  }

  override fun showDescription(description: String) {
    taskDetailDescription.visibility = View.VISIBLE
    taskDetailDescription.text = description
  }

  override fun showCompletionStatus(complete: Boolean) {
    taskDetailComplete.isChecked = complete
    taskDetailComplete.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        presenter.completeTask()
      } else {
        presenter.activateTask()
      }
    }
  }

  override fun showEditTask(taskId: String) {
    val intent = Intent(context, AddEditTaskActivity::class.java)
    intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId)
    startActivityForResult(intent, REQUEST_EDIT_TASK)
  }

  override fun showTaskDeleted() {
    activity.finish()
  }

  override fun showTaskMarkedComplete() {
    view.showSnackbar(R.string.task_marked_complete)
  }

  override fun showTaskMarkedActive() {
    view.showSnackbar(R.string.task_marked_active)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    // If the task was edited successfully, go back to the list.
    if (requestCode == REQUEST_EDIT_TASK && resultCode == RESULT_OK) {
      activity.finish()
    }
  }

  override fun showTitle(title: String) {
    taskDetailTitle.visibility = View.VISIBLE
    taskDetailTitle.text = title
  }

  override fun showMissingTask() {
    taskDetailTitle.text = ""
    taskDetailDescription.text = getString(R.string.no_data)
  }

  override val isActive = isAdded

  companion object {
    private val ARGUMENT_TASK_ID = "TASK_ID"
    private val REQUEST_EDIT_TASK = 1

    fun newInstance(taskId: String) =
        TaskDetailFragment().apply {
          arguments = Bundle().apply {
            set(ARGUMENT_TASK_ID to taskId)
          }
        }
  }
}
