package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.example.data.model.CodeFileEntity
import com.example.data.model.DeploymentRecordEntity
import com.example.data.model.GitCommitEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.SupportedLanguage
import com.example.data.model.SyncDeviceEntity
import kotlinx.coroutines.flow.Flow

class Converters {
    @TypeConverter
    fun fromLanguage(language: SupportedLanguage?): String {
        return language?.name ?: SupportedLanguage.JAVASCRIPT.name
    }

    @TypeConverter
    fun toLanguage(name: String?): SupportedLanguage {
        return name?.let {
            try {
                SupportedLanguage.valueOf(it)
            } catch (e: Exception) {
                SupportedLanguage.JAVASCRIPT
            }
        } ?: SupportedLanguage.JAVASCRIPT
    }
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    fun observeProjectById(id: Long): Flow<ProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)
}

@Dao
interface CodeFileDao {
    @Query("SELECT * FROM code_files WHERE projectId = :projectId ORDER BY isEntry DESC, name ASC")
    fun getFilesForProject(projectId: Long): Flow<List<CodeFileEntity>>

    @Query("SELECT * FROM code_files WHERE id = :fileId LIMIT 1")
    suspend fun getFileById(fileId: Long): CodeFileEntity?

    @Query("SELECT * FROM code_files WHERE projectId = :projectId AND isEntry = 1 LIMIT 1")
    suspend fun getEntryFile(projectId: Long): CodeFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: CodeFileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<CodeFileEntity>)

    @Update
    suspend fun updateFile(file: CodeFileEntity)

    @Delete
    suspend fun deleteFile(file: CodeFileEntity)

    @Query("DELETE FROM code_files WHERE id = :fileId")
    suspend fun deleteFileById(fileId: Long)
}

@Dao
interface GitDao {
    @Query("SELECT * FROM git_commits WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getCommitsForProject(projectId: Long): Flow<List<GitCommitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommit(commit: GitCommitEntity): Long
}

@Dao
interface DeploymentDao {
    @Query("SELECT * FROM deployment_records WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getDeploymentsForProject(projectId: Long): Flow<List<DeploymentRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeployment(deployment: DeploymentRecordEntity): Long

    @Update
    suspend fun updateDeployment(deployment: DeploymentRecordEntity)
}

@Dao
interface SyncDeviceDao {
    @Query("SELECT * FROM sync_devices ORDER BY isCurrentDevice DESC, lastActive DESC")
    fun getAllDevices(): Flow<List<SyncDeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: SyncDeviceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<SyncDeviceEntity>)

    @Update
    suspend fun updateDevice(device: SyncDeviceEntity)
}

@Database(
    entities = [
        ProjectEntity::class,
        CodeFileEntity::class,
        GitCommitEntity::class,
        DeploymentRecordEntity::class,
        SyncDeviceEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun codeFileDao(): CodeFileDao
    abstract fun gitDao(): GitDao
    abstract fun deploymentDao(): DeploymentDao
    abstract fun syncDeviceDao(): SyncDeviceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "codeforge_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
