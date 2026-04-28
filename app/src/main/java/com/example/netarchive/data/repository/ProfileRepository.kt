package com.example.netarchive.data.repository

import com.example.netarchive.data.mapper.toDomain
import com.example.netarchive.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import com.example.netarchive.data.local.db.dao.ProfileDao
import com.example.netarchive.domain.model.Profile

class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao
) {
    fun getProfile(): Flow<Profile?> {
        return profileDao.getProfile().map { it?.toDomain() }
    }

    suspend fun saveProfile(profile: Profile) {
        profileDao.saveProfile(profile.toEntity())
    }

    suspend fun deleteProfile(){
        profileDao.clearProfileTable()
    }
}