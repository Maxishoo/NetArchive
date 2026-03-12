package com.example.netarchive.data.mapper

import com.example.netarchive.data.local.db.entity.ContactEntity
import com.example.netarchive.domain.model.Contact

fun ContactEntity.toDomain(): Contact {
    return Contact(
        id = this.id,
        username = this.username,
        phone = this.phone,
        telegram = this.telegram,
        max = this.max,
        email = this.email,
        job = this.job,
        avatar = this.avatar
    )
}

fun Contact.toEntity(): ContactEntity {
    return ContactEntity(
        id = this.id,
        username = this.username,
        phone = this.phone,
        telegram = this.telegram,
        max = this.max,
        email = this.email,
        job = this.job,
        avatar = this.avatar
    )
}