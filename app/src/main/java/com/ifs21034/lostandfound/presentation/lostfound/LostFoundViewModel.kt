package com.ifs21034.lostandfound.presentation.lostfound

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ifs21034.lostandfound.data.local.entity.DelcomLostFoundEntity
import com.ifs21034.lostandfound.data.remote.MyResult
import com.ifs21034.lostandfound.data.remote.response.DataAddLostFoundResponse
import com.ifs21034.lostandfound.data.remote.response.DelcomLostFoundResponse
import com.ifs21034.lostandfound.data.remote.response.DelcomResponse
import com.ifs21034.lostandfound.data.repository.LocalLostFoundRepository
import com.ifs21034.lostandfound.data.repository.LostFoundRepository
import com.ifs21034.lostandfound.presentation.ViewModelFactory
import okhttp3.MultipartBody

class LostFoundViewModel(
    private val lostFoundRepository: LostFoundRepository,
    private val localLostFoundRepository: LocalLostFoundRepository
) : ViewModel() {

    fun getLostFound(lostFoundId: Int): LiveData<MyResult<DelcomLostFoundResponse>>{
        return lostFoundRepository.getLostFound(lostFoundId).asLiveData()
    }

    fun postLostFound(
        title: String,
        description: String,
        status: String
    ): LiveData<MyResult<DataAddLostFoundResponse>>{
        return lostFoundRepository.postLostFound(
            title,
            description,
            status
        ).asLiveData()
    }

    fun putLostFound(
        lostFoundId: Int,
        title: String,
        description: String,
        status: String,
        isCompleted: Boolean,
    ): LiveData<MyResult<DelcomResponse>> {
        return lostFoundRepository.putLostFound(
            lostFoundId,
            title,
            description,
            status,
            isCompleted,
        ).asLiveData()
    }

    fun deleteLostFound(lostFoundId: Int): LiveData<MyResult<DelcomResponse>> {
        return lostFoundRepository.deleteLostFound(lostFoundId).asLiveData()
    }

    fun getLocalLostFounds(): LiveData<List<DelcomLostFoundEntity>?> {
        return localLostFoundRepository.getAllLostFounds()
    }

    fun getLocalLostFound(lostfoundId: Int): LiveData<DelcomLostFoundEntity?> {
        return localLostFoundRepository.get(lostfoundId)
    }
    fun insertLocalLostFound(lostfound: DelcomLostFoundEntity) {
        localLostFoundRepository.insert(lostfound)
    }
    fun deleteLocalLostFound(lostfound: DelcomLostFoundEntity) {
        localLostFoundRepository.delete(lostfound)
    }

    fun addCoverLostFound(
        lostFoundId: Int,
        cover: MultipartBody.Part,
    ): LiveData<MyResult<DelcomResponse>> {
        return lostFoundRepository.addCoverLostFound(lostFoundId, cover).asLiveData()
    }

    companion object {
        @Volatile
        private var INSTANCE: LostFoundViewModel? = null
        fun getInstance(
            lostFoundRepository: LostFoundRepository,
            localLostFoundRepository: LocalLostFoundRepository,
        ): LostFoundViewModel {
            synchronized(ViewModelFactory::class.java) {
                INSTANCE = LostFoundViewModel(
                    lostFoundRepository,
                    localLostFoundRepository
                )
            }
            return INSTANCE as LostFoundViewModel
        }
    }
}
