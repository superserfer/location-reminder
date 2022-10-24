package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase
    private lateinit var dao: RemindersDao

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
        ).build()

        dao = database.reminderDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun cleanUpDao() = runBlockingTest {
        dao.deleteAllReminders()
    }

    @Test
    fun insertIntoDBSucceeds() = runBlockingTest {
        dao.saveReminder(reminderTestData)

        assertThat(dao.getReminders().size, `is`(1))
        assertThat(dao.getReminders().contains(reminderTestData), `is`(true))
        cleanUpDao()
    }

    @Test
    fun retrieveFromDBSucceeds() = runBlockingTest {
        dao.saveReminder(reminderTestData)

        val reminder = dao.getReminderById(reminderTestData.id)

        assertThat(reminder, notNullValue())
        assertThat(reminder?.title, `is`(reminderTestData.title))
        assertThat(reminder?.description, `is`(reminderTestData.description))
        assertThat(reminder?.location, `is`(reminderTestData.location))
        assertThat(reminder?.latitude, `is`(reminderTestData.latitude))
        assertThat(reminder?.longitude, `is`(reminderTestData.longitude))
        cleanUpDao()
    }

    @Test
    fun deleteFromDBSucceeds() = runBlockingTest {
        dao.saveReminder(reminderTestData)
        assertThat(dao.getReminders().size, `is`(1))

        dao.deleteAllReminders()
        assertThat(dao.getReminders().size, `is`(0))
    }
}