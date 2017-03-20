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

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.PopupMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity.Companion.REQUEST_ADD_TASK
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity
import com.example.android.architecture.blueprints.todoapp.util.compatGetColor
import com.example.android.architecture.blueprints.todoapp.util.compatGetDrawable
import com.example.android.architecture.blueprints.todoapp.util.showSnackbar
import kotlinx.android.synthetic.main.tasks_frag.*

import java.util.ArrayList

/**
 * Display a grid of [Task]s. User can choose to view all, active or completed tasks.
 */
class TasksFragment : Fragment(), TasksContract.View {
  private lateinit var presenter: TasksContract.Presenter
  private lateinit var listAdapter: TasksAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
    listAdapter = TasksAdapter(ArrayList<Task>(0), itemListener)
  }

  override fun onResume() {
    super.onResume()
    presenter.start()
  }

  override fun setPresenter(presenter: TasksContract.Presenter) {
    this.presenter = presenter
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    presenter.result(requestCode, resultCode)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.tasks_frag, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    noTasksAdd.setOnClickListener { showAddTask() }

    tasksList.adapter = listAdapter

    // Set up floating action button
    val fab = activity.findViewById(R.id.fab_add_task) as FloatingActionButton
    fab.setImageResource(R.drawable.ic_add)
    fab.setOnClickListener { presenter.addNewTask() }

    // Set up progress indicator
    refreshLayout.setColorSchemeColors(
        compatGetColor(R.color.colorPrimary),
        compatGetColor(R.color.colorAccent),
        compatGetColor(R.color.colorPrimaryDark))

    // Set the scrolling view in the custom SwipeRefreshLayout.
    refreshLayout.scrollUpChild = tasksLL

    refreshLayout.setOnRefreshListener { presenter.loadTasks(false) }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.menu_clear -> presenter.clearCompletedTasks()
      R.id.menu_filter -> showFilteringPopUpMenu()
      R.id.menu_refresh -> presenter.loadTasks(true)
    }
    return true
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.tasks_fragment_menu, menu)
  }

  override fun showFilteringPopUpMenu() {
    val popup = PopupMenu(context, activity.findViewById(R.id.menu_filter))
    popup.menuInflater.inflate(R.menu.filter_tasks, popup.menu)

    popup.setOnMenuItemClickListener { item ->
      when (item.itemId) {
        R.id.active -> presenter.filtering = TasksFilterType.ACTIVE_TASKS
        R.id.completed -> presenter.filtering = TasksFilterType.COMPLETED_TASKS
        else -> presenter.filtering = TasksFilterType.ALL_TASKS
      }
      presenter.loadTasks(false)
      true
    }

    popup.show()
  }

  override fun setLoadingIndicator(active: Boolean) {
    // Make sure setRefreshing() is called after the layout is done with everything else.
    refreshLayout.post { refreshLayout.isRefreshing = active }
  }

  override fun showTasks(tasks: List<Task>) {
    listAdapter.replaceData(tasks)
    tasksLL.visibility = View.VISIBLE
    noTasks.visibility = View.GONE
  }

  override fun showNoActiveTasks() {
    showNoTasksViews(getString(R.string.no_tasks_active), R.drawable.ic_check_circle_24dp)
  }

  override fun showNoTasks() {
    showNoTasksViews(getString(R.string.no_tasks_all), R.drawable.ic_assignment_turned_in_24dp)
  }

  override fun showNoCompletedTasks() {
    showNoTasksViews(getString(R.string.no_tasks_completed), R.drawable.ic_verified_user_24dp)
  }

  override fun showSuccessfullySavedMessage() {
    view.showSnackbar(getString(R.string.successfully_saved_task_message))
  }

  private fun showNoTasksViews(mainText: String, iconRes: Int, showAddView: Boolean = false) {
    tasksLL.visibility = View.GONE
    noTasks.visibility = View.VISIBLE
    noTasksMain.text = mainText
    noTasksIcon.setImageDrawable(activity.compatGetDrawable(iconRes))
    noTasksAdd.visibility = if (showAddView) View.VISIBLE else View.GONE
  }

  override fun showActiveFilterLabel() {
    filteringLabel.text = getString(R.string.label_active)
  }

  override fun showCompletedFilterLabel() {
    filteringLabel.text = getString(R.string.label_completed)
  }

  override fun showAllFilterLabel() {
    filteringLabel.text = getString(R.string.label_all)
  }

  override fun showAddTask() {
    val intent = Intent(context, AddEditTaskActivity::class.java)
    startActivityForResult(intent, REQUEST_ADD_TASK)
  }

  override fun showTaskDetailsUi(taskId: String) {
    // In it's own Activity, since it makes more sense that way and it gives us the flexibility
    // to show some Intent stubbing.
    val intent = Intent(context, TaskDetailActivity::class.java)
    intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId)
    startActivity(intent)
  }

  override fun showTaskMarkedComplete() {
    view.showSnackbar(getString(R.string.task_marked_complete))
  }

  override fun showTaskMarkedActive() {
    view.showSnackbar(getString(R.string.task_marked_active))
  }

  override fun showCompletedTasksCleared() {
    view.showSnackbar(getString(R.string.completed_tasks_cleared))
  }

  override fun showLoadingTasksError() {
    view.showSnackbar(getString(R.string.loading_tasks_error))
  }

  override val isActive = isAdded

  private val itemListener: TaskItemListener = object : TaskItemListener {
    override fun onTaskClick(clickedTask: Task) {
      presenter.openTaskDetails(clickedTask)
    }

    override fun onCompleteTaskClick(completedTask: Task) {
      presenter.completeTask(completedTask)
    }

    override fun onActivateTaskClick(activatedTask: Task) {
      presenter.activateTask(activatedTask)
    }
  }

  private class TasksAdapter(var tasks: List<Task>, val itemListener: TaskItemListener)
    : BaseAdapter() {

    fun replaceData(tasks: List<Task>) {
      this.tasks = tasks
      notifyDataSetChanged()
    }

    override fun getCount() = tasks.size

    override fun getItem(i: Int) = tasks[i]

    override fun getItemId(i: Int) = i.toLong()

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
      val rowView = if (view != null) view else {
        val inflater = LayoutInflater.from(viewGroup.context)
        inflater.inflate(R.layout.task_item, viewGroup, false)
      }

      val task = getItem(i)

      val titleTV = rowView.findViewById(R.id.title) as TextView
      titleTV.text = task.titleForList

      val completeCB = rowView.findViewById(R.id.complete) as CheckBox

      // Active/completed task UI
      completeCB.isChecked = task.completed
      rowView.background = if (task.completed) {
        viewGroup.context.compatGetDrawable(R.drawable.list_completed_touch_feedback)
      } else {
        viewGroup.context.compatGetDrawable(R.drawable.touch_feedback)
      }

      completeCB.setOnClickListener {
        if (!task.completed) {
          itemListener.onCompleteTaskClick(task)
        } else {
          itemListener.onActivateTaskClick(task)
        }
      }

      rowView.setOnClickListener { itemListener.onTaskClick(task) }

      return rowView
    }
  }

  interface TaskItemListener {
    fun onTaskClick(clickedTask: Task)
    fun onCompleteTaskClick(completedTask: Task)
    fun onActivateTaskClick(activatedTask: Task)
  }

  companion object {
    fun newInstance(): TasksFragment {
      return TasksFragment()
    }
  }
}
