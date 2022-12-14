package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResource(): Unit = IdlingRegistry.getInstance().run {
        register(EspressoIdlingResource.countingIdlingResource)
        register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource(): Unit = IdlingRegistry.getInstance().run {
        unregister(EspressoIdlingResource.countingIdlingResource)
        unregister(dataBindingIdlingResource)
    }

    @Test
    fun openRemindersActivityCreateActivity() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        activityScenario.onActivity {
            dataBindingIdlingResource.activity = it
            var activity = it
        }
        val title = "Test Title"
        val description = "Test Description"

        // Open Fragment
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Write the Title and Description
        onView(withId(R.id.reminderTitle)).perform(typeText(title))
        onView(withId(R.id.reminderDescription)).perform(typeText(description))
        closeSoftKeyboard()

        // Select the Location
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(click())
        onView(withId(R.id.confirmFAB)).perform(click())

        // Save Reminder
        onView(withId(R.id.saveReminder)).perform(click())

        // Check Result
        onView(withText(title)).check(matches(isDisplayed()))
        onView(withText(description)).check(matches(isDisplayed()))

        // This would be the test for the Toast, but i there is a Bug on my version so it wont work: Issue on Github: https://github.com/android/android-test/issues/803
        //activityScenario.onActivity {
        //    onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(`is`(it.window.decorView)))).check(matches(isDisplayed()))
        //}

        activityScenario.close()
    }

    @Test
    fun openRemindersActivityCreateErrorActivity() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        activityScenario.onActivity { dataBindingIdlingResource.activity = it }
        val title = "Test Title"
        val description = "Test Description"

        // Open Fragment
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Write the Description
        //onView(withId(R.id.reminderTitle)).perform(typeText(title))
        onView(withId(R.id.reminderDescription)).perform(typeText(description))
        closeSoftKeyboard()

        // Select the Location
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(click())
        onView(withId(R.id.confirmFAB)).perform(click())

        // Save Reminder
        onView(withId(R.id.saveReminder)).perform(click())

        // Check Result
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_enter_title)))

        activityScenario.close()
    }

    @Test
    fun openRemindersActivityWithActiveReminder() {
        val reminderTestDTO = ReminderDTO(
            title = "Test Title",
            description = "Test Description",
            location = "Test Location",
            longitude = 1.2345,
            latitude = 1.2345
        )
        runBlocking {
            repository.saveReminder(reminderTestDTO)
        }
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        activityScenario.onActivity { dataBindingIdlingResource.activity = it }
        // Check if reminder is displayed
        onView(withText(reminderTestDTO.title)).check(matches(isDisplayed()))
        onView(withText(reminderTestDTO.location)).check(matches(isDisplayed()))
        onView(withText(reminderTestDTO.description)).check(matches(isDisplayed()))

        activityScenario.close()
    }

}
