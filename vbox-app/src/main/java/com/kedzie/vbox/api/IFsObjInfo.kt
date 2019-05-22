package com.kedzie.vbox.api

import android.os.Parcelable
import com.kedzie.vbox.api.jaxb.FsObjType
import com.kedzie.vbox.soap.Cacheable
import com.kedzie.vbox.soap.Ksoap
import com.kedzie.vbox.soap.KsoapProxy

@KsoapProxy
@Ksoap(prefix="IFsObjInfo")
interface IFsObjInfo : IManagedObjectRef, Parcelable {

    @Cacheable(value = "AccessTime")
	suspend fun getAccessTime(): Long

    @Cacheable(value = "AllocatedSize")
	suspend fun getAllocatedSize(): Long

    @Cacheable(value = "BirthTime")
	suspend fun getBirthTime(): Long

    @Cacheable(value = "ChangeTime")
	suspend fun getChangeTime(): Long

    @Cacheable(value = "DeviceNumber")
	suspend fun getDeviceNumber(): Integer

    @Cacheable(value = "FileAttributes")
	suspend fun getFileAttributes(): String

    @Cacheable(value = "GenerationId")
	suspend fun getGenerationId(): Integer

    @Cacheable(value = "GID")
	suspend fun getGID(): Integer

    @Cacheable(value = "GroupName")
	suspend fun getGroupName(): String

    @Cacheable(value = "HardLinks")
	suspend fun getHardLinks(): Integer

    @Cacheable(value = "ModificationTime")
	suspend fun getModificationTime(): Long

    @Cacheable(value = "Name")
	suspend fun getName(): String

    @Cacheable(value = "NodeId")
	suspend fun getNodeId(): Long

    @Cacheable(value = "NodeIdDevice")
	suspend fun getNodeIdDevice(): Integer

    @Cacheable(value = "ObjectSize")
	suspend fun getObjectSize(): Long

    @Cacheable(value = "Type")
	suspend fun getType(): FsObjType

    @Cacheable(value = "UID")
	suspend fun getUID(): Integer

    @Cacheable(value = "UserFlags")
	suspend fun getUserFlags(): Integer

    @Cacheable(value = "UserName")
	suspend fun getUserName(): String
}