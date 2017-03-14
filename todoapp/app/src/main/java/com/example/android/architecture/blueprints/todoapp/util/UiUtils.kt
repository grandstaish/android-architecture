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

package com.example.android.architecture.blueprints.todoapp.util

import android.content.Context
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.view.View

/**
 * The `fragment` is added to the container view with id `frameId`. The operation is
 * performed by the `fragmentManager`.
 */
fun <T: Fragment> FragmentManager.addFragment(fragment: T, frameId: Int): T {
  val transaction = this.beginTransaction()
  transaction.add(frameId, fragment)
  transaction.commit()
  return fragment
}

@Suppress("UNCHECKED_CAST") // Cleaner calling code.
fun <T: Fragment> FragmentManager.getOrElse(@IdRes id: Int, f: FragmentManager.(Int) -> (T)) : T {
  return (this.findFragmentById(id) ?: f(id)) as T
}

fun View?.compatCanScrollUp() : Boolean {
  return ViewCompat.canScrollVertically(this, -1)
}

fun Context.compatGetColor(@ColorRes id: Int) = ContextCompat.getColor(this, id)
fun Context.compatGetDrawable(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)!!

fun View?.showSnackbar(@StringRes id: Int) {
  this?.let { Snackbar.make(it, id, Snackbar.LENGTH_LONG).show() }
}
fun View?.showSnackbar(message: String) {
  this?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG).show() }
}
