package com.quiniela.backend.service

import com.quiniela.backend.dto.*

interface AdminService {
    fun getDashboard(): AdminDashboardDTO
    fun getSystemStatus(): AdminSystemDTO
    fun getActivity(): AdminActivityDTO
    fun getQuinielas(search: String? = null, sort: String? = null, order: String? = null): List<AdminQuinielaListDTO>
    fun getUserDetail(id: Long): AdminUserDetailDTO?
    fun getUsers(search: String? = null, verificado: Boolean? = null): List<AdminUserListDTO>
}
