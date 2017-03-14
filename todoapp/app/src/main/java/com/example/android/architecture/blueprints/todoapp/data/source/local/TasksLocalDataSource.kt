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

package com.example.android.architecture.blueprints.todoapp.data.source.local

import android.content.ContentValues
import android.content.Context

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract.TaskEntry

/**
 * Concrete implementation of a data source as a db.
 */
class TasksLocalDataSource private constructor(context:Context) : TasksDataSource {

  private val dbHelper : TasksDbHelper = TasksDbHelper(context)

  /**
   * Note: [LoadTasksCallback.onDataNotAvailable] is fired if the database doesn't exist
   * or the table is empty.
   */
  override fun getTasks(callback : TasksDataSource.LoadTasksCallback) {
    val tasks = mutableListOf<Task>()
    val db = dbHelper.readableDatabase

    val projection = arrayOf(TaskEntry.COLUMN_NAME_ENTRY_ID, TaskEntry.COLUMN_NAME_TITLE,
        TaskEntry.COLUMN_NAME_DESCRIPTION, TaskEntry.COLUMN_NAME_COMPLETED)

    val c = db.query(TaskEntry.TABLE_NAME, projection, null, null, null, null, null)

    if (c != null && c.count > 0) {
      while (c.moveToNext()) {
        val itemId = c.getString(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_ENTRY_ID))
        val title = c.getString(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_TITLE))
        val description = c.getString(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_DESCRIPTION))
        val completed = c.getInt(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_COMPLETED)) == 1
        val task = Task(title, description, completed, itemId)
        tasks.add(task)
      }
    }

    c?.close()
    db.close()

    if (tasks.isEmpty()) {
      // This will be called if the table is new or just empty.
      callback.onDataNotAvailable()
    } else {
      callback.onTasksLoaded(tasks)
    }
  }

  /**
   * Note: [GetTaskCallback.onDataNotAvailable] is fired if the [Task] isn't
   * found.
   */
  override fun getTask(taskId : String, callback : TasksDataSource.GetTaskCallback) {
    val db = dbHelper.readableDatabase

    val projection = arrayOf(TaskEntry.COLUMN_NAME_ENTRY_ID, TaskEntry.COLUMN_NAME_TITLE,
        TaskEntry.COLUMN_NAME_DESCRIPTION, TaskEntry.COLUMN_NAME_COMPLETED)

    val selection = TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?"
    val selectionArgs = arrayOf(taskId)

    val c = db.query(TaskEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null)

    var task : Task? = null

    if (c != null && c.count > 0) {
      c.moveToFirst()
      val itemId = c.getString(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_ENTRY_ID))
      val title = c.getString(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_TITLE))
      val description = c.getString(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_DESCRIPTION))
      val completed = c.getInt(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_COMPLETED)) == 1
      task = Task(title, description, completed, itemId)
    }

    c?.close()

    db.close()

    if (task != null) {
      callback.onTaskLoaded(task)
    } else {
      callback.onDataNotAvailable()
    }
  }

  override fun saveTask(task:Task) {
    val db = dbHelper.writableDatabase

    val values = ContentValues()
    values.put(TaskEntry.COLUMN_NAME_ENTRY_ID, task.id)
    values.put(TaskEntry.COLUMN_NAME_TITLE, task.title)
    values.put(TaskEntry.COLUMN_NAME_DESCRIPTION, task.description)
    values.put(TaskEntry.COLUMN_NAME_COMPLETED, task.completed)

    db.insert(TaskEntry.TABLE_NAME, null, values)

    db.close()
  }

  override fun completeTask(task:Task) {
    val db = dbHelper.writableDatabase

    val values = ContentValues()
    values.put(TaskEntry.COLUMN_NAME_COMPLETED, true)

    val selection = TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?"
    val selectionArgs = arrayOf(task.id)

    db.update(TaskEntry.TABLE_NAME, values, selection, selectionArgs)

    db.close()
  }

  override fun completeTask(taskId:String) {
    // Not required for the local data source because the {@link TasksRepository} handles
    // converting from a {@code taskId} to a {@link task} using its cached data.
  }

  override fun activateTask(task:Task) {
    val db = dbHelper.writableDatabase

    val values = ContentValues()
    values.put(TaskEntry.COLUMN_NAME_COMPLETED, false)

    val selection = TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?"
    val selectionArgs = arrayOf(task.id)

    db.update(TaskEntry.TABLE_NAME, values, selection, selectionArgs)

    db.close()
  }

  override fun activateTask(taskId:String) {
    // Not required for the local data source because the {@link TasksRepository} handles
    // converting from a {@code taskId} to a {@link task} using its cached data.
  }

  override fun clearCompletedTasks() {
    val db = dbHelper.writableDatabase

    val selection = TaskEntry.COLUMN_NAME_COMPLETED + " LIKE ?"
    val selectionArgs = arrayOf("1")

    db.delete(TaskEntry.TABLE_NAME, selection, selectionArgs)

    db.close()
  }

  override fun refreshTasks() {
    // Not required because the {@link TasksRepository} handles the logic of refreshing the
    // tasks from all the available data sources.
  }

  override fun deleteAllTasks() {
    val db = dbHelper.writableDatabase

    db.delete(TaskEntry.TABLE_NAME, null, null)

    db.close()
  }

  override fun deleteTask(taskId:String) {
    val db = dbHelper.writableDatabase

    val selection = TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?"
    val selectionArgs = arrayOf(taskId)

    db.delete(TaskEntry.TABLE_NAME, selection, selectionArgs)

    db.close()
  }

  companion object {
    private var INSTANCE : TasksLocalDataSource? = null

     fun getInstance(context: Context) : TasksLocalDataSource {
      return INSTANCE ?: TasksLocalDataSource(context).also { INSTANCE = it }
    }
  }
}
