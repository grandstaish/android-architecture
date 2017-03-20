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

package com.example.android.architecture.blueprints.todoapp.tasks

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

import com.example.android.architecture.blueprints.todoapp.Injection.provideTasksRepository
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsActivity
import com.example.android.architecture.blueprints.todoapp.util.*
import kotlinx.android.synthetic.main.tasks_act.*

class TasksActivity : AppCompatActivity() {

  private lateinit var tasksPresenter: TasksPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.tasks_act)

    // Set up the toolbar.
    setSupportActionBar(toolbar)
    supportActionBar?.apply {
      setHomeAsUpIndicator(R.drawable.ic_menu)
      setDisplayHomeAsUpEnabled(true)
    }

    // Set up the navigation drawer.
    drawerLayout.setStatusBarBackground(R.color.colorPrimaryDark)
    navigationView?.let { setupDrawerContent(it) }

    // Get the existing fragment, or create and add a new one.
    val tasksFragment = supportFragmentManager.getOrElse(R.id.contentFrame) { id ->
      addFragment(TasksFragment.newInstance(), id)
    }

    // Create the presenter
    tasksPresenter = TasksPresenter(provideTasksRepository(applicationContext), tasksFragment)

    // Load previously saved state, if available.
    savedInstanceState?.let {
      tasksPresenter.filtering = it.getSerializable(CURRENT_FILTERING_KEY) as TasksFilterType
    }
  }

  public override fun onSaveInstanceState(outState: Bundle) {
    outState.putSerializable(CURRENT_FILTERING_KEY, tasksPresenter.filtering)
    super.onSaveInstanceState(outState)
  }

  override fun onOptionsItemSelected(item: MenuItem) =
      when (item.itemId) {
        android.R.id.home -> {
          // Open the navigation drawer when the home icon is selected from the toolbar.
          drawerLayout.openDrawer(GravityCompat.START)
          true
        }
        else -> super.onOptionsItemSelected(item)
      }

  private fun setupDrawerContent(navigationView: NavigationView) {
    navigationView.setNavigationItemSelectedListener { menuItem ->
      when (menuItem.itemId) {
        R.id.statistics_navigation_menu_item -> {
          val intent = Intent(this, StatisticsActivity::class.java)
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
          startActivity(intent)
        }
      }
      // Close the navigation drawer when an item is selected.
      menuItem.isChecked = true
      drawerLayout.closeDrawers()
      true
    }
  }

  companion object {
    private val CURRENT_FILTERING_KEY = "CURRENT_FILTERING_KEY"
  }
}
