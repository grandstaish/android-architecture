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

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.util.showSnackbar
import kotlinx.android.synthetic.main.addtask_frag.*

/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
class AddEditTaskFragment : Fragment(), AddEditTaskContract.View {
  private lateinit var presenter: AddEditTaskContract.Presenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onResume() {
    super.onResume()
    presenter.start()
  }

  override fun setPresenter(presenter: AddEditTaskContract.Presenter) {
    this.presenter = presenter
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    val fab = activity.findViewById(R.id.fab_edit_task_done) as FloatingActionButton
    fab.setImageResource(R.drawable.ic_done)
    fab.setOnClickListener {
      presenter.saveTask(addTaskTitle.text.toString(), addTaskDescription.text.toString())
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.addtask_frag, container, false)
  }

  override fun showEmptyTaskError() {
    addTaskTitle.showSnackbar(R.string.empty_task_message)
  }

  override fun showTasksList() {
    activity.setResult(Activity.RESULT_OK)
    activity.finish()
  }

  override fun setTitle(title: String) {
    addTaskTitle.setText(title)
  }

  override fun setDescription(description: String) {
    addTaskDescription.setText(description)
  }

  override val isActive = isAdded

  companion object {
    val ARGUMENT_EDIT_TASK_ID = "EDIT_TASK_ID"

    fun newInstance(): AddEditTaskFragment {
      return AddEditTaskFragment()
    }
  }
}
