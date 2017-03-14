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

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

import com.example.android.architecture.blueprints.todoapp.Injection.provideTasksRepository
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.tasks.TasksActivity
import com.example.android.architecture.blueprints.todoapp.util.addFragment
import com.example.android.architecture.blueprints.todoapp.util.getOrElse
import kotlinx.android.synthetic.main.statistics_act.*

/**
 * Show statistics for tasks.
 */
class StatisticsActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.statistics_act)

    // Set up the toolbar.
    setSupportActionBar(toolbar)
    supportActionBar!!.apply {
      setTitle(R.string.statistics_title)
      setHomeAsUpIndicator(R.drawable.ic_menu)
      setDisplayHomeAsUpEnabled(true)
    }

    // Set up the navigation drawer.
    drawerLayout.setStatusBarBackground(R.color.colorPrimaryDark)
    navigationView?.let { setupDrawerContent(it) }

    val statisticsFragment: StatisticsFragment = supportFragmentManager.getOrElse(R.id.contentFrame) { id ->
      addFragment(StatisticsFragment.newInstance(), id)
    }

    StatisticsPresenter(provideTasksRepository(applicationContext), statisticsFragment)
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
        R.id.list_navigation_menu_item -> {
          val intent = Intent(this, TasksActivity::class.java)
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
}
