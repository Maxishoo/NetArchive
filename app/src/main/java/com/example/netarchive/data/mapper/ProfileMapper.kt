package com.example.netarchive.data.mapper

import com.example.netarchive.data.local.db.entity.ProfileEntity
import com.example.netarchive.domain.model.Profile

fun ProfileEntity.toDomain(): Profile {
    return Profile(
        id = this.id,
        username = this.username,
        phone = this.phone,
        telegram = this.telegram,
        max = this.max,
        email = this.email,
        job = this.job,
        avatar = this.avatar,
        birthday = this.birthday,
    )
}

fun Profile.toEntity(): ProfileEntity {
    return ProfileEntity(
        id = 1,
        username = this.username,
        phone = this.phone,
        telegram = this.telegram,
        max = this.max,
        email = this.email,
        job = this.job,
        avatar = this.avatar,
        birthday = this.birthday,
    )
}