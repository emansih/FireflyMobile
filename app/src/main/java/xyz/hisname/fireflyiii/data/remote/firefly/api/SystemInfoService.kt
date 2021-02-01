/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.Response
import retrofit2.http.GET
import xyz.hisname.fireflyiii.Constants.Companion.SYSTEM_INFO_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.userinfo.system.SystemInfoModel
import xyz.hisname.fireflyiii.repository.models.userinfo.user.UserDataModel


interface SystemInfoService {

    @GET(SYSTEM_INFO_ENDPOINT)
    suspend fun getSystemInfo(): Response<SystemInfoModel>

    @GET("$SYSTEM_INFO_ENDPOINT/user")
    suspend fun getCurrentUserInfo(): Response<UserDataModel>

}