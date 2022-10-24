package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    private val reminderTestData = ReminderDTO(
        title = "Test Title",
        description = "Test Description",
        location = "Test Location",
        latitude = 1.2345,
        longitude = 1.2345
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun cleanUpRepository() = runBlocking {
        repository.deleteAllReminders()
    }

    @Test
    fun insertReminderSucceeds() = runBlocking {
        repository.saveReminder(reminderTestData)

        val result = repository.getReminders()
        assertThat(result, `is`(instanceOf(Result.Success::class.java)))

        result as Result.Success

        assertThat(result.data, notNullValue())
        assertThat(result.data.size, `is`(1))

        cleanUpRepository()
    }

    @Test
    fun retrieveExistingReminderSucceeds() = runBlocking {
        repository.saveReminder(reminderTestData)

        val result = repository.getReminder(reminderTestData.id)
        assertThat(result, `is`(instanceOf(Result.Success::class.java)))

        result as Result.Success

        assertThat(result, notNullValue())
        assertThat(result.data.title, `is`(reminderTestData.title))
        assertThat(result.data.description, `is`(reminderTestData.description))
        assertThat(result.data.location, `is`(reminderTestData.location))
        assertThat(result.data.latitude, `is`(reminderTestData.latitude))
        assertThat(result.data.longitude, `is`(reminderTestData.longitude))
        cleanUpRepository()
    }

    @Test
    fun deleteRemindersSucceeds() = runBlocking {
        repository.saveReminder(reminderTestData)
        repository.deleteAllReminders()

        val result = repository.getReminders()
        assertThat(result, `is`(instanceOf(Result.Success::class.java)))

        result as Result.Success
        assertThat(result.data.size, `is`(0))
        cleanUpRepository()
    }
}